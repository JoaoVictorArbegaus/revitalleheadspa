import { useCallback, useEffect, useRef, useState, type PointerEvent } from 'react';
import { contato } from '../content/site';
import type { Confirmacao } from './api';
import { dataDoInstante, diaPorExtenso, dinheiro, duracao, hora } from './formato';

/*
 * Comprovante que sai de uma impressora térmica, linha a linha, e fica pendurado balançando
 * (adaptado de Receipt Printer, kokonutui, MIT). Diferente do original, não rasga nem cai:
 * é o comprovante da cliente e precisa continuar na tela. Arrastar para o lado só balança.
 */

// Onde o papel sai, a partir do topo da impressora.
const FENDA_Y = 58;
// A impressora avança uma "linha" por vez, com uma pausa curta entre elas (~1 s no total).
const LINHA = 30;
const AVANCO = 34;
const PAUSA = 0.04;
const VARIACAO = 0.03;
// Simulação em px e segundos, em passo fixo.
const PASSO = 1 / 120;
const GRAVIDADE = 2600;
const AMORTECIMENTO = 0.994;
const PUXAO_MAX = 18;
const REPOUSO = 0.02;
const FOLGA = 28;

type Fase = 'parado' | 'imprimindo' | 'pendurado';

// Larguras das barras do código de barras (decorativo), fixas para servidor e navegador concordarem.
const BARRAS = [2, 1, 1, 3, 1, 2, 2, 1, 3, 1, 1, 2, 1, 1, 2, 3, 1, 2, 1, 1, 3, 2, 1, 1, 2, 1, 3, 1, 1, 2, 2, 1, 1, 3, 1, 2];

/** Ponto (px, py) da folha girada por `angulo` em torno do canto superior esquerdo. */
function girar(px: number, py: number, angulo: number) {
  const cos = Math.cos(angulo);
  const sin = Math.sin(angulo);
  return [px * cos - py * sin, px * sin + py * cos];
}

const amortecer = (d: number, max: number) => max * (1 - Math.exp(-d / max));

// Borda serrilhada embaixo: o papel foi destacado do rolo.
const DENTE = 8;
const MASCARA = `conic-gradient(from -45deg at 50% 100%, #0000, #000 1deg 89deg, #0000 90deg) 50% 100% / ${DENTE}px ${DENTE / 2}px repeat-x, linear-gradient(#000 0 0) 0 0 / 100% calc(100% - ${DENTE / 2}px) no-repeat`;

function Papel({ c, nome }: { c: Confirmacao; nome: string }) {
  const data = dataDoInstante(c.inicioEm);
  const linhas: [string, string][] = [
    ['Tratamento', c.servico.nome],
    ['Data', diaPorExtenso(data)],
    ['Horário', `${hora(c.inicioEm)} às ${hora(c.fimServicoEm)}`],
    ['Duração', duracao(c.servico.duracaoMinutos)],
    ['Profissional', c.profissional.nome],
    ['Cliente', nome.trim()],
  ];
  return (
    <>
      <p className="rp__marca">Revitalle</p>
      <p className="rp__sub">Head Spa · Lages - SC</p>
      <p className="rp__tipo">Comprovante de agendamento</p>
      <hr className="rp__corte" />
      <dl className="rp__linhas">
        {linhas.map(([rotulo, valor]) => (
          <div key={rotulo}>
            <dt>{rotulo}</dt>
            <dd>{valor}</dd>
          </div>
        ))}
      </dl>
      <hr className="rp__corte" />
      <p className="rp__total">
        <span>Total</span>
        <strong>{dinheiro(c.precoContratado, c.moedaContratada)}</strong>
      </p>
      <p className="rp__obs">Pagamento no local</p>
      <div className="rp__pe" aria-hidden="true">
        <span className="rp__carimbo">Confirmado</span>
        <div className="rp__barras">
          {BARRAS.map((w, i) => (
            <span key={i} style={{ width: w, background: i % 2 ? 'transparent' : 'currentColor' }} />
          ))}
        </div>
      </div>
      <p className="rp__rodape">
        {contato.endereco}
        <br />
        WhatsApp {contato.telefone}
        <br />
        Até breve!
      </p>
    </>
  );
}

export function Recibo({ c, nome }: { c: Confirmacao; nome: string }) {
  const [fase, setFase] = useState<Fase>('parado');
  const maquina = useRef<HTMLDivElement>(null);
  const papel = useRef<HTMLDivElement>(null);
  const carcaca = useRef<HTMLDivElement>(null);

  const sim = useRef({
    fase: 'parado' as Fase,
    largura: 0,
    altura: 0,
    fendaX: 0,
    // centro de massa agora e um passo atrás (Verlet), e o ângulo da folha
    cx: 0,
    cy: 0,
    pcx: 0,
    pcy: 0,
    angulo: 0,
    // dobradiça: ponto do mundo de onde a folha pende, e onde ele fica na folha
    hx: 0,
    hy: 0,
    dobraX: 0,
    dobraY: 0,
    // impressão: quanto papel ainda está dentro da impressora
    escondido: 0,
    alvo: 0,
    pausa: 0,
    ponteiro: -1,
    pegaX: 0,
    pegaY: 0,
    inicioY: 0,
    raf: 0,
    ultimo: 0,
    sobra: 0,
  });

  const mudarFase = useCallback((f: Fase) => {
    sim.current.fase = f;
    setFase(f);
  }, []);

  const desenhar = useCallback(() => {
    const s = sim.current;
    if (!papel.current) return;
    const [ox, oy] = girar(s.largura / 2, s.altura / 2, s.angulo);
    const tx = s.cx - ox - (s.fendaX - s.largura / 2);
    const ty = s.cy - oy - FENDA_Y;
    papel.current.style.transform = `translate3d(${tx.toFixed(2)}px,${ty.toFixed(2)}px,0) rotate(${s.angulo.toFixed(4)}rad)`;
  }, []);

  const penderEm = useCallback((angulo: number) => {
    const s = sim.current;
    const [dx, dy] = girar(s.largura / 2 - s.dobraX, s.altura / 2 - s.dobraY, angulo);
    return [s.hx + dx, s.hy + dy];
  }, []);

  const naFenda = useCallback(
    (puxao: number) => {
      const s = sim.current;
      s.dobraX = s.largura / 2;
      s.dobraY = 0;
      s.hx = s.fendaX;
      s.hy = FENDA_Y + puxao;
      const [cx, cy] = penderEm(0);
      s.cx = s.pcx = cx;
      s.cy = s.pcy = cy;
      s.angulo = 0;
    },
    [penderEm],
  );

  // impressora térmica: avança uma linha, para, avança
  const passoImpressao = useCallback(
    (dt: number) => {
      const s = sim.current;
      if (s.pausa > 0) s.pausa -= dt;
      else if (s.alvo > 0) {
        s.alvo = Math.max(0, s.alvo - LINHA);
        s.pausa = PAUSA + Math.random() * VARIACAO;
      }
      s.escondido += (s.alvo - s.escondido) * Math.min(1, AVANCO * dt);
      if (Math.abs(s.escondido - s.alvo) < 0.05) s.escondido = s.alvo;
      naFenda(-s.escondido);
      // a carcaça vibra enquanto o motor gira
      const zumbido = s.pausa > 0 ? 0 : Math.sin(performance.now() / 9) * 0.4;
      if (carcaca.current) carcaca.current.style.transform = `translate3d(${zumbido.toFixed(2)}px,0,0)`;
      if (s.escondido === 0 && s.alvo === 0) {
        if (carcaca.current) carcaca.current.style.transform = '';
        // um empurrãozinho para o papel balançar ao sair
        s.pcx = s.cx - 2.2;
        mudarFase('pendurado');
        return 1;
      }
      return 1;
    },
    [naFenda, mudarFase],
  );

  // pendurado: o centro de massa oscila numa haste de tamanho fixo e a folha gira junto
  const passoPendulo = useCallback(() => {
    const s = sim.current;
    const vx = (s.cx - s.pcx) * AMORTECIMENTO;
    const vy = (s.cy - s.pcy) * AMORTECIMENTO;
    s.pcx = s.cx;
    s.pcy = s.cy;
    s.cx += vx;
    s.cy += vy + GRAVIDADE * PASSO * PASSO;
    const haste = Math.hypot(s.largura / 2 - s.dobraX, s.altura / 2 - s.dobraY);
    const dx = s.cx - s.hx;
    const dy = s.cy - s.hy;
    const dist = Math.hypot(dx, dy) || 1;
    s.cx = s.hx + (dx / dist) * haste;
    s.cy = s.hy + (dy / dist) * haste;
    const repouso = Math.atan2(s.altura / 2 - s.dobraY, s.largura / 2 - s.dobraX);
    s.angulo = Math.atan2(s.cy - s.hy, s.cx - s.hx) - repouso;
    return Math.abs(vx) + Math.abs(vy);
  }, []);

  const quadro = useCallback(
    (agora: number) => {
      const s = sim.current;
      s.raf = 0;
      const dt = s.ultimo ? Math.min((agora - s.ultimo) / 1000, 0.05) : PASSO;
      s.ultimo = agora;
      let ativo = 0;
      if (s.fase === 'imprimindo') ativo = passoImpressao(dt);
      else {
        s.sobra += dt;
        while (s.sobra >= PASSO) {
          s.sobra -= PASSO;
          ativo = passoPendulo();
        }
      }
      desenhar();
      if (s.fase === 'imprimindo' || s.ponteiro >= 0 || ativo > REPOUSO) s.raf = requestAnimationFrame(quadro);
      else s.ultimo = 0;
    },
    [desenhar, passoImpressao, passoPendulo],
  );

  const acordar = useCallback(() => {
    const s = sim.current;
    if (!s.raf) s.raf = requestAnimationFrame(quadro);
  }, [quadro]);

  const medir = useCallback(() => {
    const s = sim.current;
    if (!maquina.current || !papel.current) return;
    s.largura = papel.current.offsetWidth;
    s.altura = papel.current.offsetHeight;
    s.fendaX = maquina.current.clientWidth / 2;
    // a máquina cresce com o papel: nada fica cortado embaixo
    maquina.current.style.height = `${FENDA_Y + s.altura + FOLGA}px`;
  }, []);

  useEffect(() => {
    const s = sim.current;
    medir();
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      naFenda(0);
      desenhar();
      mudarFase('pendurado');
    } else {
      s.escondido = s.alvo = s.altura;
      s.pausa = 0.35;
      naFenda(-s.escondido);
      desenhar();
      mudarFase('imprimindo');
      acordar();
    }
    // largura mudou (girar o celular): recentraliza a folha
    const ro = new ResizeObserver(() => {
      if (s.fase !== 'pendurado') return;
      medir();
      naFenda(0);
      desenhar();
    });
    if (maquina.current) ro.observe(maquina.current);
    return () => {
      ro.disconnect();
      cancelAnimationFrame(s.raf);
      s.raf = 0;
      s.ultimo = 0;
    };
  }, [acordar, desenhar, medir, mudarFase, naFenda]);

  const pontoLocal = (e: PointerEvent) => {
    const r = maquina.current!.getBoundingClientRect();
    return { x: e.clientX - r.left, y: e.clientY - r.top };
  };

  const aoPressionar = (e: PointerEvent<HTMLDivElement>) => {
    const s = sim.current;
    if (s.fase !== 'pendurado' || (e.pointerType === 'mouse' && e.button !== 0)) return;
    const { x, y } = pontoLocal(e);
    e.currentTarget.setPointerCapture(e.pointerId);
    s.ponteiro = e.pointerId;
    s.inicioY = y;
    const [ox, oy] = girar(s.largura / 2, s.altura / 2, s.angulo);
    const [gx, gy] = girar(x - (s.cx - ox), y - (s.cy - oy), -s.angulo);
    s.pegaX = gx;
    s.pegaY = gy;
    acordar();
  };

  // preso na fenda: a folha gira para o ponto pego seguir a mão e estica um pouco para baixo
  const aoMover = (e: PointerEvent<HTMLDivElement>) => {
    const s = sim.current;
    if (s.ponteiro !== e.pointerId) return;
    const { x, y } = pontoLocal(e);
    s.dobraX = s.largura / 2;
    s.dobraY = 0;
    s.hx = s.fendaX;
    s.hy = FENDA_Y + amortecer(Math.max(0, y - s.inicioY), PUXAO_MAX);
    const alcance = Math.max(s.pegaY, 24);
    const angulo = -Math.atan2(x - s.fendaX - (s.pegaX - s.largura / 2), alcance) * 0.7;
    const [cx, cy] = penderEm(angulo);
    s.pcx = s.cx;
    s.pcy = s.cy;
    s.cx = cx;
    s.cy = cy;
    s.angulo = angulo;
    acordar();
  };

  // ao soltar, volta para a fenda e o balanço se desfaz sozinho
  const aoSoltar = (e: PointerEvent<HTMLDivElement>) => {
    const s = sim.current;
    if (s.ponteiro !== e.pointerId) return;
    s.ponteiro = -1;
    s.hy = FENDA_Y;
    acordar();
  };

  return (
    <div ref={maquina} className={`rp is-${fase}`}>
      <div className="rp__saida">
        <div
          ref={papel}
          className="rp__papel"
          role="document"
          aria-label="Comprovante do agendamento"
          style={{ mask: MASCARA, WebkitMask: MASCARA }}
          onPointerDown={aoPressionar}
          onPointerMove={aoMover}
          onPointerUp={aoSoltar}
          onPointerCancel={aoSoltar}
          onLostPointerCapture={aoSoltar}
        >
          <Papel c={c} nome={nome} />
        </div>
      </div>
      <div ref={carcaca} className="rp__carcaca" aria-hidden="true">
        <span className="rp__nome">Revitalle Head Spa</span>
        <span className="rp__status">
          <i />
          {fase === 'imprimindo' ? 'Imprimindo…' : 'Confirmado'}
        </span>
        <span className="rp__fenda" />
      </div>
    </div>
  );
}
