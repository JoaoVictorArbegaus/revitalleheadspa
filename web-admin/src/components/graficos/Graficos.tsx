import { useRef, useState, type PointerEvent } from 'react';
import { CheckCircle2, CircleSlash, UserX, CalendarClock } from 'lucide-react';
import { formatarMoeda } from '@/lib/formato';
import type { LinhaServico, PontoDia } from '@/lib/metricas';
import { escala, eixoMoeda, Legenda, LinhaDica, TabelaDados, useDica } from './base';
import { useLargura } from './largura';

// ---------------------------------------------------------------------------
// Faturamento por dia: colunas empilhadas realizado (escuro) + previsto (claro), uma rampa só.
// ---------------------------------------------------------------------------
export function FaturamentoPorDia({ pontos, rotuloDia, rotuloCurto }: { pontos: PontoDia[]; rotuloDia: (d: string) => string; rotuloCurto: (d: string) => string }) {
  // desenhado na largura real do cartão: texto sempre 11px, colunas e rótulos se ajustam ao espaço
  const { ref: medida, largura: W } = useLargura<HTMLDivElement>();
  const H = W < 480 ? 180 : 240;
  const M = { t: 12, r: 8, b: 26, l: W < 480 ? 50 : 58 };
  const [foco, setFoco] = useState<string | null>(null);
  const iw = W - M.l - M.r;
  const ih = H - M.t - M.b;
  const { topo, passos } = escala(Math.max(...pontos.map((p) => p.realizado + p.previsto), 0));
  const banda = iw / Math.max(1, pontos.length);
  const larg = Math.min(24, Math.max(3, banda - 2)); // ≤ 24px e 2px de respiro entre colunas
  const yv = (v: number) => (v / topo) * ih;
  const cada = Math.max(1, Math.ceil(pontos.length / Math.max(2, Math.floor(iw / 56)))); // ~56px por rótulo, sem colisão
  const caixa = useRef<HTMLDivElement>(null);
  const { dica, mostrar, esconder } = useDica();

  const sobre = (e: PointerEvent<SVGRectElement>, p: PontoDia) => {
    setFoco(p.data);
    const r = caixa.current!.getBoundingClientRect();
    const alvo = (e.target as SVGRectElement).getBoundingClientRect();
    mostrar({
      x: alvo.left - r.left + alvo.width / 2,
      y: Math.max(8, e.clientY - r.top),
      conteudo: (
        <>
          <p className="mb-1 font-medium text-[var(--viz-ink-2)]">{rotuloDia(p.data)}</p>
          <LinhaDica cor="var(--viz-realizado)" rotulo="realizado" valor={formatarMoeda(p.realizado)} />
          <LinhaDica cor="var(--viz-previsto)" rotulo="previsto" valor={formatarMoeda(p.previsto)} />
        </>
      ),
    });
  };

  return (
    <div>
      <Legenda itens={[{ cor: 'var(--viz-realizado)', rotulo: 'Realizado (concluídos)' }, { cor: 'var(--viz-previsto)', rotulo: 'Previsto (confirmados)' }]} />
      <div ref={medida} className="mt-3 w-full">
      <div ref={caixa} className="relative" onPointerLeave={() => (esconder(), setFoco(null))}>
        <svg width={W} height={H} viewBox={`0 0 ${W} ${H}`} className="block" role="img" aria-label="Faturamento por dia, realizado e previsto">
          <g transform={`translate(${M.l},${M.t})`}>
            {passos.map((v) => (
              <g key={v} transform={`translate(0,${ih - yv(v)})`}>
                <line x1={0} x2={iw} stroke={v === 0 ? 'var(--viz-axis)' : 'var(--viz-grid)'} strokeWidth={1} />
                <text x={-8} dy="0.32em" textAnchor="end" className="fill-[var(--viz-muted)] text-[11px] tabular-nums">
                  {eixoMoeda(v)}
                </text>
              </g>
            ))}
            {pontos.map((p, i) => {
              const x = i * banda + (banda - larg) / 2;
              const hr = yv(p.realizado);
              const hp = yv(p.previsto);
              const gap = hr > 0 && hp > 0 ? 2 : 0;
              return (
                <g key={p.data} className="transition-opacity duration-150" opacity={foco && foco !== p.data ? 0.45 : 1}>
                  {hr > 0 && <path d={coluna(x, ih - hr, larg, hr, hp === 0)} fill="var(--viz-realizado)" />}
                  {hp > 0 && <path d={coluna(x, ih - hr - gap - hp, larg, hp, true)} fill="var(--viz-previsto)" />}
                  {/* alvo do hover: a banda inteira, maior que a coluna */}
                  <rect x={i * banda} y={0} width={banda} height={ih} fill="transparent" onPointerMove={(e) => sobre(e, p)} />
                </g>
              );
            })}
            {/* rótulos fora do grupo esmaecido: o eixo continua legível durante o hover */}
            {pontos.map((p, i) =>
              i % cada === 0 ? (
                <text key={p.data} x={i * banda + banda / 2} y={ih + 17} textAnchor="middle" className="pointer-events-none fill-[var(--viz-muted)] text-[11px] tabular-nums">
                  {rotuloCurto(p.data)}
                </text>
              ) : null,
            )}
          </g>
        </svg>
        {dica}
      </div>
      </div>
      <TabelaDados colunas={['Dia', 'Realizado', 'Previsto']} linhas={pontos.map((p) => [rotuloDia(p.data), formatarMoeda(p.realizado), formatarMoeda(p.previsto)])} />
    </div>
  );
}

/** Coluna com ponta arredondada de 4px no topo e base reta. */
function coluna(x: number, y: number, w: number, h: number, arredondar: boolean) {
  const r = arredondar ? Math.min(4, w / 2, h) : 0;
  return `M${x} ${y + h}V${y + r}Q${x} ${y} ${x + r} ${y}H${x + w - r}Q${x + w} ${y} ${x + w} ${y + r}V${y + h}Z`;
}

// ---------------------------------------------------------------------------
// Serviços: barras horizontais de uma cor (uma série), valor na ponta.
// ---------------------------------------------------------------------------
export function RankingServicos({ linhas }: { linhas: LinhaServico[] }) {
  const max = Math.max(1, ...linhas.map((l) => l.faturamento));
  return (
    <div>
      <ul className="grid gap-3">
        {linhas.map((l) => (
          <li key={l.nome} className="grid gap-1">
            <div className="flex items-baseline justify-between gap-3 text-sm">
              <span className="truncate font-medium text-[var(--viz-ink)]">{l.nome}</span>
              <span className="shrink-0 text-[var(--viz-ink-2)] tabular-nums">
                <span className="font-semibold text-[var(--viz-ink)]">{formatarMoeda(l.faturamento)}</span> · {l.atendimentos}{' '}
                {l.atendimentos === 1 ? 'atendimento' : 'atendimentos'}
              </span>
            </div>
            <div className="h-2.5 w-full rounded-full bg-[var(--seq-0)]" aria-hidden="true">
              <div className="h-full rounded-full bg-[var(--viz-realizado)]" style={{ width: `${(l.faturamento / max) * 100}%` }} />
            </div>
          </li>
        ))}
      </ul>
      <TabelaDados colunas={['Serviço', 'Faturamento', 'Atendimentos']} linhas={linhas.map((l) => [l.nome, formatarMoeda(l.faturamento), l.atendimentos])} />
    </div>
  );
}

// ---------------------------------------------------------------------------
// Estados das reservas: barra 100% com segmentos separados por 2px; cada estado com ícone + rótulo.
// ---------------------------------------------------------------------------
const ESTADOS = [
  { chave: 'concluidos', rotulo: 'Concluídos', cor: 'var(--viz-ok)', Icone: CheckCircle2 },
  { chave: 'confirmados', rotulo: 'Confirmados', cor: 'var(--viz-realizado)', Icone: CalendarClock },
  { chave: 'ausencias', rotulo: 'Não compareceu', cor: 'var(--viz-ausencia)', Icone: UserX },
  { chave: 'cancelados', rotulo: 'Cancelados', cor: 'var(--viz-cancelado)', Icone: CircleSlash },
] as const;

export function DistribuicaoEstados({ valores }: { valores: Record<(typeof ESTADOS)[number]['chave'], number> }) {
  const total = ESTADOS.reduce((s, e) => s + valores[e.chave], 0);
  const caixa = useRef<HTMLDivElement>(null);
  const { dica, mostrar, esconder } = useDica();
  const pct = (n: number) => (total ? Math.round((n / total) * 100) : 0);

  return (
    <div>
      <div ref={caixa} className="relative" onPointerLeave={esconder}>
        <div className="flex h-4 w-full gap-0.5 overflow-hidden rounded-md" role="img" aria-label="Distribuição das reservas por estado">
          {total === 0 && <div className="h-full w-full rounded-md bg-[var(--seq-0)]" />}
          {ESTADOS.filter((e) => valores[e.chave] > 0).map((e) => (
            <div
              key={e.chave}
              className="h-full transition-opacity first:rounded-l-md last:rounded-r-md hover:opacity-85"
              style={{ width: `${(valores[e.chave] / total) * 100}%`, background: e.cor }}
              onPointerMove={(ev) => {
                const r = caixa.current!.getBoundingClientRect();
                mostrar({
                  x: ev.clientX - r.left,
                  y: 0,
                  conteudo: <LinhaDica cor={e.cor} rotulo={e.rotulo.toLowerCase()} valor={`${valores[e.chave]} (${pct(valores[e.chave])}%)`} />,
                });
              }}
            />
          ))}
        </div>
        {dica}
      </div>
      <ul className="mt-4 grid grid-cols-2 gap-3">
        {ESTADOS.map(({ chave, rotulo, cor, Icone }) => (
          <li key={chave} className="flex items-start gap-2">
            <Icone className="mt-0.5 size-4 shrink-0" style={{ color: cor }} aria-hidden="true" />
            <div>
              <p className="text-xl leading-none font-semibold text-[var(--viz-ink)]">{valores[chave]}</p>
              <p className="mt-1 text-xs text-[var(--viz-ink-2)]">
                {rotulo} · {pct(valores[chave])}%
              </p>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}

// ---------------------------------------------------------------------------
// Procura por dia da semana × hora: mapa de calor sequencial (uma rampa, claro → escuro).
// ---------------------------------------------------------------------------
const RAMPA = ['var(--seq-1)', 'var(--seq-2)', 'var(--seq-3)', 'var(--seq-4)', 'var(--seq-5)', 'var(--seq-6)'];

export function MapaDeCalor({ dias, horas, valor, max }: { dias: { valor: string; rotulo: string }[]; horas: number[]; valor: (d: string, h: number) => number; max: number }) {
  const caixa = useRef<HTMLDivElement>(null);
  const { dica, mostrar, esconder } = useDica();
  const cor = (v: number) => (v === 0 ? 'var(--seq-0)' : RAMPA[Math.min(RAMPA.length - 1, Math.floor(((v - 1) / Math.max(1, max)) * RAMPA.length))]);
  if (!dias.length) return <p className="py-8 text-center text-sm text-[var(--viz-ink-2)]">Sem reservas no período.</p>;

  return (
    <div>
      <div ref={caixa} className="relative overflow-x-auto" onPointerLeave={esconder}>
        <div className="grid min-w-max gap-0.5" style={{ gridTemplateColumns: `4.5rem repeat(${horas.length}, minmax(1.75rem, 1fr))` }} role="img" aria-label="Reservas por dia da semana e hora de início">
          <span />
          {horas.map((h) => (
            <span key={h} className="pb-1 text-center text-[11px] text-[var(--viz-muted)] tabular-nums">
              {h}h
            </span>
          ))}
          {dias.map((d) => (
            <div key={d.valor} className="contents">
              <span className="self-center pr-2 text-xs text-[var(--viz-ink-2)]">{d.rotulo}</span>
              {horas.map((h) => {
                const v = valor(d.valor, h);
                return (
                  <span
                    key={h}
                    className="h-7 rounded-[4px] transition-[filter] hover:brightness-95"
                    style={{ background: cor(v) }}
                    onPointerMove={(e) => {
                      const r = caixa.current!.getBoundingClientRect();
                      const a = (e.target as HTMLElement).getBoundingClientRect();
                      mostrar({
                        x: a.left - r.left + a.width / 2 + caixa.current!.scrollLeft,
                        y: a.top - r.top,
                        conteudo: <LinhaDica cor={v ? cor(v) : 'var(--viz-axis)'} rotulo={`${d.rotulo}, ${h}h`} valor={`${v} ${v === 1 ? 'reserva' : 'reservas'}`} />,
                      });
                    }}
                  />
                );
              })}
            </div>
          ))}
        </div>
        {dica}
      </div>
      <div className="mt-3 flex items-center gap-2 text-[11px] text-[var(--viz-muted)]">
        <span>Menos</span>
        {['var(--seq-0)', ...RAMPA].map((c) => (
          <span key={c} className="size-3 rounded-[3px]" style={{ background: c }} aria-hidden="true" />
        ))}
        <span>Mais (máx. {max})</span>
      </div>
      <TabelaDados
        colunas={['Dia', ...horas.map((h) => `${h}h`)]}
        linhas={dias.map((d) => [d.rotulo, ...horas.map((h) => valor(d.valor, h))])}
      />
    </div>
  );
}
