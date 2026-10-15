import { useEffect, useMemo, useRef, useState, type KeyboardEvent, type ReactNode } from 'react';
import { contato } from '../content/site';
import { consultarDisponibilidade, ErroRede, type DiaDisponivel, type Horario, type Servico } from './api';
import { diaPorExtenso, duracao, hoje, hora, limitesDoMes, nomeDoMes, semanasDoMes, somarMeses } from './formato';
import { Aviso, Botao, Icone, TituloEtapa } from './ui';

/** O horizonte público da API (90 dias) cabe no mês atual e nos 3 seguintes. */
const MESES_A_FRENTE = 3;
const SEMANA = ['D', 'S', 'T', 'Q', 'Q', 'S', 'S'];

type Mes = { estado: 'carregando' } | { estado: 'ok'; dias: Map<string, DiaDisponivel> } | { estado: 'erro'; mensagem: string };

const periodos: [string, (h: number) => boolean][] = [
  ['Manhã', (h) => h < 12],
  ['Tarde', (h) => h >= 12 && h < 18],
  ['Noite', (h) => h >= 18],
];

export function PassoHorario({
  servico,
  dia,
  horario,
  versao,
  alerta,
  focar,
  onDia,
  onHorario,
  onVoltar,
}: {
  servico: Servico;
  dia: string | null;
  horario: Horario | null;
  /** Muda para descartar a disponibilidade já carregada (ex.: depois de um conflito). */
  versao: number;
  alerta?: ReactNode;
  focar: boolean;
  onDia: (d: string) => void;
  onHorario: (h: Horario) => void;
  onVoltar: () => void;
}) {
  const hojeLocal = useMemo(hoje, []);
  const primeiroMes = hojeLocal.slice(0, 7);
  const ultimoMes = somarMeses(primeiroMes, MESES_A_FRENTE);
  const [mes, setMes] = useState(dia?.slice(0, 7) ?? primeiroMes);
  const [meses, setMeses] = useState<Record<string, Mes>>({});
  const [tentativa, setTentativa] = useState(0);
  const pedidos = useRef(new Set<string>());
  // enquanto ninguém escolheu um dia, pula sozinho os meses sem horário (ex.: fim de mês lotado)
  const automatico = useRef(dia === null);

  const chave = (m: string) => `${servico.id}|${versao}|${m}`;
  const atual = meses[chave(mes)];

  useEffect(() => {
    const k = `${servico.id}|${versao}|${mes}`;
    if (pedidos.current.has(k)) return;
    pedidos.current.add(k);
    setMeses((m) => ({ ...m, [k]: { estado: 'carregando' } }));
    const { inicio, fim } = limitesDoMes(mes);
    consultarDisponibilidade(servico.id, inicio < hojeLocal ? hojeLocal : inicio, fim)
      .then((r) => setMeses((m) => ({ ...m, [k]: { estado: 'ok', dias: new Map(r.dias.map((d) => [d.data, d])) } })))
      .catch((e: Error) => {
        pedidos.current.delete(k); // permite "Tentar de novo"
        const mensagem =
          e instanceof ErroRede ? 'Sem conexão com a agenda. Confira sua internet e tente de novo.' : 'Não conseguimos carregar os horários agora.';
        setMeses((m) => ({ ...m, [k]: { estado: 'erro', mensagem } }));
      });
  }, [mes, servico.id, versao, tentativa, hojeLocal]);

  // escolhe o primeiro dia livre do mês, ou avança para o próximo se este não tiver nenhum
  useEffect(() => {
    if (atual?.estado !== 'ok' || !automatico.current) return;
    const primeiro = [...atual.dias.keys()].sort()[0];
    if (primeiro) {
      automatico.current = false;
      onDia(primeiro);
    } else if (mes < ultimoMes) setMes(somarMeses(mes, 1));
    else automatico.current = false;
  }, [atual, mes, ultimoMes, onDia]);

  const mesDoDia = dia ? meses[chave(dia.slice(0, 7))] : undefined;
  const horarios = mesDoDia?.estado === 'ok' && dia ? (mesDoDia.dias.get(dia)?.horarios ?? []) : [];
  const semVagas = atual?.estado === 'ok' && atual.dias.size === 0 && mes === ultimoMes && !dia;

  const trocarMes = (n: number) => {
    automatico.current = false;
    setMes((m) => somarMeses(m, n));
  };

  // setas movem o foco entre os dias disponíveis do calendário
  const grade = useRef<HTMLDivElement>(null);
  const navegar = (e: KeyboardEvent<HTMLDivElement>) => {
    const passo = ({ ArrowLeft: -1, ArrowRight: 1, ArrowUp: -7, ArrowDown: 7 } as Record<string, number>)[e.key];
    if (!passo) return;
    const botoes = [...(grade.current?.querySelectorAll<HTMLButtonElement>('button[data-dia]:not(:disabled)') ?? [])];
    const i = botoes.indexOf(document.activeElement as HTMLButtonElement);
    if (i < 0) return;
    e.preventDefault();
    const alvo = Number((document.activeElement as HTMLButtonElement).dataset.n) + passo;
    // procura o dia disponível mais próximo na direção da seta
    const ordenados = passo > 0 ? botoes : [...botoes].reverse();
    (ordenados.find((b) => (passo > 0 ? Number(b.dataset.n) >= alvo : Number(b.dataset.n) <= alvo)) ?? botoes[i]).focus();
  };

  return (
    <>
      <TituloEtapa
        titulo="Escolha o dia e o horário"
        descricao={`${servico.nome} com ${servico.profissionalNome}, ${duracao(servico.duracaoMinutos)}. Aparecem só os horários livres.`}
        focar={focar}
      />
      {alerta && <Aviso tom="info">{alerta}</Aviso>}

      <div className="ag-when">
        <div className="ag-cal">
          <div className="ag-cal__head">
            <button type="button" className="ag-cal__nav" onClick={() => trocarMes(-1)} disabled={mes <= primeiroMes} aria-label="Mês anterior">
              <Icone nome="anterior" />
            </button>
            <p className="ag-cal__mes" aria-live="polite">
              {nomeDoMes(mes)}
            </p>
            <button type="button" className="ag-cal__nav" onClick={() => trocarMes(1)} disabled={mes >= ultimoMes} aria-label="Próximo mês">
              <Icone nome="proximo" />
            </button>
          </div>

          <div className="ag-cal__semana" aria-hidden="true">
            {SEMANA.map((d, i) => (
              <span key={i}>{d}</span>
            ))}
          </div>
          <div ref={grade} className="ag-cal__dias" aria-busy={atual?.estado === 'carregando'} onKeyDown={navegar}>
            {semanasDoMes(mes).map((d, i) => {
              if (!d) return <span key={`v${i}`} />;
              const livres = atual?.estado === 'ok' ? (atual.dias.get(d)?.horarios.length ?? 0) : 0;
              const classes = [
                'ag-day',
                livres > 0 && 'has-slots',
                d === dia && 'is-selected',
                d === hojeLocal && 'is-today',
                atual?.estado === 'carregando' && d >= hojeLocal && 'is-loading',
              ];
              return (
                <button
                  key={d}
                  type="button"
                  data-dia={d}
                  data-n={i}
                  className={classes.filter(Boolean).join(' ')}
                  disabled={!livres}
                  aria-pressed={d === dia}
                  aria-label={`${diaPorExtenso(d)}${d === hojeLocal ? ' (hoje)' : ''}: ${
                    livres ? `${livres} ${livres === 1 ? 'horário livre' : 'horários livres'}` : 'sem horários'
                  }`}
                  onClick={() => {
                    automatico.current = false;
                    onDia(d);
                  }}
                >
                  {Number(d.slice(8))}
                </button>
              );
            })}
          </div>
          {atual?.estado === 'erro' && (
            <Aviso
              acao={
                <Botao variante="dark" tamanho="sm" onClick={() => setTentativa((t) => t + 1)}>
                  Tentar de novo
                </Botao>
              }
            >
              {atual.mensagem}
            </Aviso>
          )}
          {atual?.estado === 'ok' && atual.dias.size === 0 && !semVagas && <p className="ag-cal__vazio">Sem horários livres neste mês.</p>}
        </div>

        <div className="ag-slots" aria-live="polite">
          {semVagas ? (
            <Aviso tom="info">
              A agenda online está sem horários livres para este tratamento nos próximos meses.{' '}
              <a className="ag-link" href={contato.whatsappAgendar} target="_blank" rel="noopener">
                Fale com a gente pelo WhatsApp
              </a>{' '}
              para encontrarmos um encaixe.
            </Aviso>
          ) : !dia ? (
            <div className="ag-slots__empty">
              <Icone nome="calendario" />
              <p>{atual?.estado === 'ok' ? 'Escolha um dia com horário livre.' : 'Procurando horários livres…'}</p>
            </div>
          ) : (
            <>
              <p className="ag-slots__dia">{diaPorExtenso(dia)}</p>
              {mesDoDia?.estado !== 'ok' ? (
                <div className="ag-slots__grid" aria-hidden="true">
                  {Array.from({ length: 8 }, (_, i) => (
                    <span key={i} className="ag-slot ag-slot--skeleton" />
                  ))}
                </div>
              ) : horarios.length === 0 ? (
                <p className="ag-cal__vazio">Este dia não tem mais horários livres. Escolha outro.</p>
              ) : (
                periodos.map(([rotulo, filtro]) => {
                  const lista = horarios.filter((h) => filtro(Number(hora(h.inicioEm).slice(0, 2))));
                  if (!lista.length) return null;
                  return (
                    <fieldset key={rotulo} className="ag-slots__grupo">
                      <legend>{rotulo}</legend>
                      <div className="ag-slots__grid">
                        {lista.map((h) => (
                          <button
                            key={h.inicioEm}
                            type="button"
                            className={horario?.inicioEm === h.inicioEm ? 'ag-slot is-selected' : 'ag-slot'}
                            aria-pressed={horario?.inicioEm === h.inicioEm}
                            aria-label={`${hora(h.inicioEm)}, até ${hora(h.fimServicoEm)}`}
                            onClick={() => onHorario(h)}
                          >
                            {hora(h.inicioEm)}
                          </button>
                        ))}
                      </div>
                    </fieldset>
                  );
                })
              )}
            </>
          )}
        </div>
      </div>

      <div className="ag-actions">
        <button type="button" className="ag-back" onClick={onVoltar}>
          <Icone nome="voltar" /> Trocar tratamento
        </button>
      </div>
    </>
  );
}
