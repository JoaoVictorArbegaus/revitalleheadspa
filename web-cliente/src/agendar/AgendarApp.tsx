import { useCallback, useEffect, useRef, useState, type ReactNode } from 'react';
import { AnimatePresence, LazyMotion, MotionConfig, domAnimation, m } from 'motion/react';
import { SvgDefs } from '../components/Icons';
import { EASE } from '../components/Motion';
import { contato } from '../content/site';
import { useGlassEffects } from '../hooks/useLandingEffects';
import {
  criarAgendamento,
  ErroApi,
  ErroRede,
  listarServicos,
  novaChave,
  obterAvisoPrivacidade,
  type AvisoPrivacidade,
  type Confirmacao,
  type Horario,
  type NovoAgendamento,
  type Servico,
} from './api';
import { Confirmado } from './Confirmado';
import { normalizar } from './formato';
import { Cabecalho, Resumo, ResumoMini, Rodape, Trilha, type Etapa } from './Moldura';
import { PassoHorario } from './PassoHorario';
import { PassoDados, PassoRevisao, PassoServico, validarDados, type Carga, type ErrosDados } from './Passos';
import '../styles/landing.css';
import '../styles/agendar.css';

const ORDEM: Etapa[] = ['servico', 'horario', 'dados', 'revisao', 'concluido'];
const esperar = (ms: number) => new Promise((r) => setTimeout(r, ms));

const mensagemDeCarga = (e: unknown) =>
  e instanceof ErroRede ? 'Sem conexão com a agenda. Confira sua internet e tente de novo.' : 'Não conseguimos abrir a agenda agora. Tente de novo em instantes.';

const suporte = (id?: string) => (id ? ` Se continuar, fale com a gente pelo WhatsApp e informe o código ${id.slice(0, 8)}.` : '');

export function AgendarApp() {
  useGlassEffects();

  const [etapa, setEtapa] = useState<Etapa>('servico');
  const [alcancada, setAlcancada] = useState(0);
  const [focar, setFocar] = useState(false);
  const [servicos, setServicos] = useState<Carga<Servico[]>>({ estado: 'carregando' });
  const [servico, setServico] = useState<Servico | null>(null);
  const [dia, setDia] = useState<string | null>(null);
  const [horario, setHorario] = useState<Horario | null>(null);
  const [versaoAgenda, setVersaoAgenda] = useState(0);
  const [nome, setNome] = useState('');
  const [celular, setCelular] = useState('');
  const [errosDados, setErrosDados] = useState<ErrosDados>({});
  const [aviso, setAviso] = useState<Carga<AvisoPrivacidade>>({ estado: 'carregando' });
  const [ciente, setCiente] = useState(false);
  const [enviando, setEnviando] = useState(false);
  const [erroEnvio, setErroEnvio] = useState<ReactNode>(null);
  const [aguardarAte, setAguardarAte] = useState(0);
  const [agora, setAgora] = useState(0);
  const [alerta, setAlerta] = useState<{ etapa: Etapa; texto: ReactNode } | null>(null);
  const [confirmacao, setConfirmacao] = useState<Confirmacao | null>(null);
  const painel = useRef<HTMLDivElement>(null);
  // a mesma chave acompanha as repetições de um mesmo pedido; pedido diferente, chave nova
  const chave = useRef<{ valor: string; corpo: string } | null>(null);
  const estado = useRef({ servico, horario, confirmacao, nome, celular });
  estado.current = { servico, horario, confirmacao, nome, celular };

  /** Não deixa abrir uma etapa sem os dados das anteriores (atalhos da trilha e botão Voltar). */
  const limitar = useCallback((pedida: Etapa): Etapa => {
    const { servico: s, horario: h, nome: n, celular: c } = estado.current;
    if (pedida === 'servico' || !s) return 'servico';
    if (pedida === 'horario' || !h) return 'horario';
    if (pedida === 'dados' || Object.keys(validarDados(n, c)).length) return 'dados';
    return 'revisao';
  }, []);

  /* ---------- navegação entre etapas (com o botão Voltar do navegador) ---------- */

  const irPara = useCallback((proxima: Etapa, modo: 'push' | 'replace' | 'none' = 'push') => {
    setEtapa(proxima);
    setFocar(true);
    setAlcancada((a) => Math.max(a, ORDEM.indexOf(proxima)));
    if (modo !== 'none') history[modo === 'push' ? 'pushState' : 'replaceState']({ etapa: proxima }, '');
    // traz o topo do painel para a vista no celular, sem pular quando ele já está visível
    const topo = painel.current?.getBoundingClientRect().top ?? 0;
    if (topo < 0) window.scrollBy({ top: topo - 96, behavior: 'smooth' });
  }, []);

  useEffect(() => {
    history.replaceState({ etapa: 'servico' }, '');
    const onPop = (e: PopStateEvent) => {
      const pedida: Etapa = (e.state as { etapa?: Etapa } | null)?.etapa ?? 'servico';
      if (estado.current.confirmacao) return novoAgendamento(); // depois de confirmado, voltar começa um novo agendamento
      irPara(limitar(pedida), 'none');
    };
    window.addEventListener('popstate', onPop);
    return () => window.removeEventListener('popstate', onPop);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [irPara, limitar]);

  /* ---------- tratamentos ---------- */

  const carregarServicos = useCallback(() => {
    setServicos({ estado: 'carregando' });
    listarServicos()
      .then((dados) => {
        setServicos({ estado: 'ok', dados });
        // vindo da landing com ?servico=hair-spa, já abre a agenda daquele tratamento
        const pedido = new URLSearchParams(location.search).get('servico');
        const achado = pedido ? dados.find((s) => normalizar(s.nome) === normalizar(pedido)) : undefined;
        if (achado && !estado.current.servico) {
          setServico(achado);
          irPara('horario');
        }
      })
      .catch((e) => setServicos({ estado: 'erro', mensagem: mensagemDeCarga(e) }));
  }, [irPara]);

  useEffect(carregarServicos, [carregarServicos]);

  const escolherServico = (s: Servico) => {
    if (s.id !== servico?.id) {
      setDia(null);
      setHorario(null);
    }
    setServico(s);
    setAlerta(null);
    irPara('horario');
  };

  /* ---------- horário e dados ---------- */

  const dadosOk = () => Object.keys(validarDados(nome, celular)).length === 0;

  const escolherHorario = (h: Horario) => {
    setHorario(h);
    setAlerta(null);
    // depois de um conflito, nome e celular continuam preenchidos: segue direto para a confirmação
    irPara(dadosOk() && alcancada >= ORDEM.indexOf('revisao') ? 'revisao' : 'dados');
  };

  const continuarDados = () => {
    const erros = validarDados(nome, celular);
    setErrosDados(erros);
    if (Object.keys(erros).length === 0) irPara('revisao');
  };

  /* ---------- aviso de privacidade ---------- */

  const carregarAviso = useCallback(() => {
    setAviso({ estado: 'carregando' });
    obterAvisoPrivacidade()
      .then((dados) => setAviso({ estado: 'ok', dados }))
      .catch((e) => setAviso({ estado: 'erro', mensagem: mensagemDeCarga(e) }));
  }, []);

  useEffect(() => {
    if (etapa === 'revisao' && aviso.estado !== 'ok') carregarAviso();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [etapa]);

  /* ---------- confirmação ---------- */

  // contagem regressiva do limite de requisições (429 com Retry-After)
  useEffect(() => {
    if (!aguardarAte) return;
    setAgora(Date.now());
    const t = setInterval(() => {
      setAgora(Date.now());
      if (Date.now() >= aguardarAte) {
        setAguardarAte(0);
        clearInterval(t);
      }
    }, 500);
    return () => clearInterval(t);
  }, [aguardarAte]);
  const aguardando = aguardarAte ? Math.max(0, Math.ceil((aguardarAte - agora) / 1000)) : 0;

  const confirmar = async () => {
    if (!servico || !horario || aviso.estado !== 'ok' || !ciente || enviando) return;
    const corpo: NovoAgendamento = {
      servicoId: servico.id,
      inicioEm: horario.inicioEm,
      cliente: { nome: nome.trim(), celular },
      versaoAvisoPrivacidade: aviso.dados.versao,
      cienteAvisoPrivacidade: true,
    };
    const texto = JSON.stringify(corpo);
    if (chave.current?.corpo !== texto) chave.current = { valor: novaChave(), corpo: texto };
    const { valor } = chave.current;
    setEnviando(true);
    setErroEnvio(null);

    // sem resposta ou erro 5xx: repete com a mesma chave (a API devolve o 201 original, sem duplicar)
    for (let tentativa = 0; ; tentativa++) {
      try {
        const r = await criarAgendamento(corpo, valor);
        chave.current = null;
        setConfirmacao(r);
        setEnviando(false);
        irPara('concluido', 'replace');
        return;
      } catch (e) {
        const repetir = e instanceof ErroRede || (e instanceof ErroApi && e.inesperado);
        if (repetir && tentativa < 2) {
          await esperar(900 * (tentativa + 1));
          continue;
        }
        setEnviando(false);
        tratarFalha(e);
        return;
      }
    }
  };

  const tratarFalha = (e: unknown) => {
    if (!(e instanceof ErroApi) || e.inesperado) {
      // efeito incerto: a chave é mantida para a próxima tentativa não duplicar a reserva
      setErroEnvio(
        <>
          Não recebemos a confirmação da agenda. Toque em confirmar de novo: se a reserva já tiver sido registrada, ela não será duplicada.
          {e instanceof ErroApi && suporte(e.idRastreio)}
        </>,
      );
      return;
    }
    switch (e.codigo) {
      case 'CONFLITO_AGENDAMENTO':
        chave.current = null;
        setHorario(null);
        setVersaoAgenda((v) => v + 1);
        setAlerta({ etapa: 'horario', texto: 'Esse horário acabou de ser reservado. Escolha outro: seus dados continuam preenchidos.' });
        irPara('horario');
        return;
      case 'AVISO_PRIVACIDADE_DESATUALIZADO':
        setCiente(false);
        carregarAviso();
        setErroEnvio('O aviso de privacidade foi atualizado. Leia a nova versão e marque a ciência de novo.');
        return;
      case 'CONFLITO_IDEMPOTENCIA':
        chave.current = null;
        setErroEnvio('Os dados mudaram desde a última tentativa. Toque em confirmar de novo.');
        return;
      case 'SERVICO_NAO_ENCONTRADO':
        setServico(null);
        setHorario(null);
        setDia(null);
        carregarServicos();
        setAlerta({ etapa: 'servico', texto: 'Esse tratamento não está mais disponível para agendamento online. Escolha outro.' });
        irPara('servico');
        return;
      case 'LIMITE_REQUISICOES_PUBLICAS':
        setAguardarAte(Date.now() + (e.aguardar ?? 30) * 1000);
        setErroEnvio('Muitas tentativas em pouco tempo. Aguarde alguns segundos e confirme de novo.');
        return;
      case 'FALHA_VALIDACAO': {
        const erros: ErrosDados = {};
        for (const c of e.errosCampos) {
          if (c.campo.startsWith('cliente.nome')) erros.nome = 'Confira o nome informado.';
          if (c.campo.startsWith('cliente.celular')) erros.celular = 'Confira o celular: DDD e número.';
        }
        if (erros.nome || erros.celular) {
          setErrosDados(erros);
          irPara('dados');
          return;
        }
        break;
      }
    }
    setErroEnvio(<>Não foi possível confirmar o agendamento.{suporte(e.idRastreio)}</>);
  };

  const novoAgendamento = () => {
    setConfirmacao(null);
    setServico(null);
    setDia(null);
    setHorario(null);
    setCiente(false);
    setErroEnvio(null);
    setAlcancada(0);
    setVersaoAgenda((v) => v + 1);
    irPara('servico', 'replace');
  };

  const alertaDa = (e: Etapa) => (alerta?.etapa === e ? alerta.texto : undefined);

  /* ---------- página ---------- */

  let passo: ReactNode = null;
  if (etapa === 'servico' || !servico) {
    passo = (
      <PassoServico
        carga={servicos}
        selecionado={servico?.id}
        focar={focar}
        alerta={alertaDa('servico')}
        onEscolher={escolherServico}
        onRecarregar={carregarServicos}
      />
    );
  } else if (etapa === 'horario' || !horario) {
    passo = (
      <PassoHorario
        servico={servico}
        dia={dia}
        horario={horario}
        versao={versaoAgenda}
        alerta={alertaDa('horario')}
        focar={focar}
        onDia={setDia}
        onHorario={escolherHorario}
        onVoltar={() => irPara('servico')}
      />
    );
  } else if (etapa === 'dados') {
    passo = (
      <PassoDados
        nome={nome}
        celular={celular}
        erros={errosDados}
        focar={focar}
        onNome={(v) => {
          setNome(v);
          if (errosDados.nome) setErrosDados((x) => ({ ...x, nome: undefined }));
        }}
        onCelular={(v) => {
          setCelular(v);
          if (errosDados.celular) setErrosDados((x) => ({ ...x, celular: undefined }));
        }}
        onContinuar={continuarDados}
        onVoltar={() => irPara('horario')}
      />
    );
  } else if (etapa === 'revisao') {
    passo = (
      <PassoRevisao
        servico={servico}
        horario={horario}
        nome={nome}
        celular={celular}
        aviso={aviso}
        ciente={ciente}
        enviando={enviando}
        erro={erroEnvio}
        aguardando={aguardando}
        focar={focar}
        onCiente={setCiente}
        onConfirmar={confirmar}
        onRecarregarAviso={carregarAviso}
        onEditar={(e) => irPara(e)}
      />
    );
  } else if (confirmacao) {
    passo = <Confirmado c={confirmacao} nome={nome} onNovo={novoAgendamento} />;
  }

  const concluido = etapa === 'concluido';

  return (
    <LazyMotion features={domAnimation} strict>
      <MotionConfig reducedMotion="user">
        <a className="skip" href="#agendamento">
          Pular para o agendamento
        </a>
        <SvgDefs />
        <Cabecalho />

        <main className="ag">
          <section className="ag-top" aria-labelledby="ag-titulo">
            <div className="orbs orbs--dark" aria-hidden="true">
              {Array.from({ length: 3 }, (_, i) => (
                <i key={i} />
              ))}
            </div>
            <div className="grain" aria-hidden="true" />
            <div className="wrap ag-top__inner">
              <p className="label label--dark">Agenda online</p>
              <h1 id="ag-titulo">{concluido ? 'Horário confirmado' : 'Agende seu horário'}</h1>
              <p className="ag-top__lead">
                {concluido ? 'Obrigada por escolher a Revitalle.' : 'Escolha o tratamento, o dia e um horário livre. Leva menos de um minuto.'}
              </p>
              <Trilha etapa={etapa} alcancada={alcancada} onIr={(e) => irPara(limitar(e))} />
            </div>
          </section>

          <div className="ag-body">
            <div className="orbs orbs--light" aria-hidden="true">
              {Array.from({ length: 4 }, (_, i) => (
                <i key={i} />
              ))}
            </div>
            <div className={concluido ? 'wrap ag-grid is-done' : 'wrap ag-grid'}>
              <div ref={painel} id="agendamento" className="ag-panel glass glass--light">
                {!concluido && <ResumoMini servico={servico} horario={horario} />}
                <AnimatePresence mode="wait" initial={false}>
                  <m.div
                    key={etapa}
                    className="ag-step"
                    initial={{ opacity: 0, y: 18 }}
                    animate={{ opacity: 1, y: 0, transition: { duration: 0.45, ease: EASE } }}
                    exit={{ opacity: 0, y: -10, transition: { duration: 0.18 } }}
                  >
                    {passo}
                  </m.div>
                </AnimatePresence>
              </div>
              {!concluido && <Resumo servico={servico} dia={dia} horario={horario} nome={etapa === 'revisao' || etapa === 'dados' ? nome : ''} />}
            </div>
            <p className="ag-help">
              Prefere conversar?{' '}
              <a className="ag-link" href={contato.whatsappAgendar} target="_blank" rel="noopener">
                Agende pelo WhatsApp
              </a>{' '}
              ou ligue para <a className="ag-link" href={contato.telefoneHref}>{contato.telefone}</a>.
            </p>
          </div>
        </main>
        <Rodape />
      </MotionConfig>
    </LazyMotion>
  );
}
