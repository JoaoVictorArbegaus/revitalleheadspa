import { useCallback, useEffect, useMemo, useRef, useState, type KeyboardEvent, type PointerEvent, type ReactNode } from 'react';
import { ArrowDownRight, ArrowUpRight } from 'lucide-react';
import { cn } from '@/lib/utils';

/*
 * Cartão de indicador com valor que "rola", variação e linha do período contra o anterior
 * (adaptado de Metric Card, kokonutui, MIT). Sem seletor próprio: o período vem do filtro da página.
 */

const W = 400;
const H = 110;
const PAD = 10;
const DIGITOS = ['0', '1', '2', '3', '4', '5', '6', '7', '8', '9'];

function caminho(alturas: number[]) {
  if (alturas.length === 1) alturas = [alturas[0], alturas[0]];
  const pts = alturas.map((h, i) => [(i / (alturas.length - 1)) * W, PAD + (1 - h) * (H - PAD * 2)]);
  let d = `M${pts[0][0].toFixed(1)} ${pts[0][1].toFixed(1)}`;
  for (let i = 0; i < pts.length - 1; i++) {
    const p0 = pts[i - 1] ?? pts[i];
    const p1 = pts[i];
    const p2 = pts[i + 1];
    const p3 = pts[i + 2] ?? p2;
    // pontos de controle presos à faixa do gráfico: a curva nunca passa abaixo do zero
    const lim = (y: number) => Math.min(H - PAD, Math.max(PAD, y));
    d += `C${(p1[0] + (p2[0] - p0[0]) / 6).toFixed(1)} ${lim(p1[1] + (p2[1] - p0[1]) / 6).toFixed(1)} ${(p2[0] - (p3[0] - p1[0]) / 6).toFixed(1)} ${lim(p2[1] - (p3[1] - p1[1]) / 6).toFixed(1)} ${p2[0].toFixed(1)} ${p2[1].toFixed(1)}`;
  }
  return d;
}

/** Um dígito numa roda: nasce no 0 e gira até o valor, então dígitos novos também animam. */
function Digito({ d }: { d: number }) {
  const [atual, setAtual] = useState(0);
  useEffect(() => {
    const id = requestAnimationFrame(() => setAtual(d));
    return () => cancelAnimationFrame(id);
  }, [d]);
  return (
    <span className="relative inline-block h-[1em] overflow-hidden leading-none">
      <span className="invisible">0</span>
      <span
        className="absolute inset-x-0 top-0 flex flex-col transition-transform duration-[520ms] ease-[cubic-bezier(0.23,1,0.32,1)] motion-reduce:transition-none"
        style={{ transform: `translateY(-${atual * 10}%)` }}
      >
        {DIGITOS.map((x) => (
          <span key={x} className="block h-[1em] leading-none">
            {x}
          </span>
        ))}
      </span>
    </span>
  );
}

const SAIDA_MS = 520;

/** Coluna que abre ao entrar e fecha ao sair, para o número não pular de largura. */
function Coluna({ fechando, children }: { fechando: boolean; children: ReactNode }) {
  const [aberta, setAberta] = useState(false);
  useEffect(() => {
    const id = requestAnimationFrame(() => setAberta(!fechando));
    return () => cancelAnimationFrame(id);
  }, [fechando]);
  return (
    <span
      className="inline-grid transition-[grid-template-columns,opacity] duration-[520ms] ease-[cubic-bezier(0.23,1,0.32,1)] motion-reduce:transition-none"
      style={{ gridTemplateColumns: aberta ? '1fr' : '0fr', opacity: aberta ? 1 : 0 }}
    >
      <span className="min-w-0 overflow-hidden">{children}</span>
    </span>
  );
}

/**
 * Cada dígito gira na própria roda; prefixo ("R$ ") fica parado. Posições contadas da direita:
 * quando o número encolhe, os dígitos que sobram à esquerda giram até 0 e recolhem antes de sair.
 */
function NumeroRolando({ valor }: { valor: string }) {
  const [, prefixo = '', corpo = ''] = /^(\D*)(.*)$/.exec(valor) ?? [];
  const [estado, setEstado] = useState({ corpo, saindo: '' });
  if (estado.corpo !== corpo) {
    const antes = estado.saindo + estado.corpo;
    setEstado({ corpo, saindo: antes.length > corpo.length ? antes.slice(0, antes.length - corpo.length) : '' });
  }
  useEffect(() => {
    if (!estado.saindo) return;
    const t = setTimeout(() => setEstado((e) => ({ ...e, saindo: '' })), SAIDA_MS);
    return () => clearTimeout(t);
  }, [estado.saindo]);

  const cs = (estado.saindo + corpo).split('');
  const nSaindo = estado.saindo.length;
  return (
    <span aria-hidden="true" className="inline-flex">
      {prefixo && <span className="whitespace-pre">{prefixo}</span>}
      {cs.map((c, i) => {
        const sai = i < nSaindo;
        const digito = /\d/.test(c);
        return (
          <Coluna key={`${digito ? 'd' : 's'}${cs.length - i}`} fechando={sai}>
            {digito ? <Digito d={sai ? 0 : Number(c)} /> : <span>{c}</span>}
          </Coluna>
        );
      })}
    </span>
  );
}

type Props = {
  rotulo: string;
  /** Valores diários do período atual e do anterior (mesmo tamanho). */
  atual: { data: string; valor: number }[];
  anterior: number[];
  formatar: (v: number) => string;
  rotuloDia: (data: string) => string;
  comparacao: string;
  className?: string;
};

export function CartaoMetrica({ rotulo, atual, anterior, formatar, rotuloDia, comparacao, className }: Props) {
  const [cursor, setCursor] = useState<number | null>(null);
  const ref = useRef<HTMLDivElement>(null);
  const total = atual.reduce((s, p) => s + p.valor, 0);
  const totalAnt = anterior.reduce((s, v) => s + v, 0);

  const { pathAtual, pathAnt, norm } = useMemo(() => {
    const todos = [...atual.map((p) => p.valor), ...anterior];
    const hi = Math.max(1, ...todos) * 1.08;
    const n = (v: number) => v / hi;
    return { pathAtual: caminho(atual.map((p) => n(p.valor))), pathAnt: caminho(anterior.map(n)), norm: n };
  }, [atual, anterior]);

  const indice = useCallback(
    (x: number) => {
      const r = ref.current?.getBoundingClientRect();
      if (!r) return 0;
      return Math.round(Math.min(1, Math.max(0, (x - r.left) / r.width)) * (atual.length - 1));
    },
    [atual.length],
  );

  const ativo = cursor !== null && cursor < atual.length;
  const ponto = ativo ? atual[cursor] : null;
  const valor = ponto ? ponto.valor : total;
  const base = ativo ? (anterior[cursor] ?? 0) : totalAnt;
  const delta = base ? (valor - base) / base : null;
  const x = ativo && atual.length > 1 ? (cursor / (atual.length - 1)) * 100 : 100;
  const y = ponto ? PAD + (1 - norm(ponto.valor)) * (H - PAD * 2) : 0;
  const texto = formatar(valor);

  const teclas = (e: KeyboardEvent<HTMLDivElement>) => {
    const ult = atual.length - 1;
    const mov: Record<string, number> = { ArrowLeft: -1, ArrowRight: 1, Home: -ult, End: ult };
    if (e.key === 'Escape') return setCursor(null);
    if (mov[e.key] === undefined) return;
    e.preventDefault();
    setCursor((c) => Math.min(ult, Math.max(0, (c ?? ult) + mov[e.key])));
  };

  return (
    <div className={cn('flex flex-col rounded-xl border bg-card p-5', className)}>
      <p className="text-sm font-medium text-[var(--viz-ink-2)]">{rotulo}</p>
      <p className="mt-2 text-4xl font-semibold tracking-tight text-[var(--viz-ink)]">
        <span className="sr-only">{texto}</span>
        <NumeroRolando valor={texto} />
      </p>
      <div className="mt-1.5 flex flex-wrap items-center gap-x-2 text-sm">
        {delta !== null ? (
          <span className={cn('inline-flex items-center gap-0.5 font-semibold', delta >= 0 ? 'text-[var(--viz-up)]' : 'text-[var(--viz-down)]')}>
            {delta >= 0 ? <ArrowUpRight className="size-4" aria-hidden="true" /> : <ArrowDownRight className="size-4" aria-hidden="true" />}
            {delta >= 0 ? '+' : '−'}
            {Math.abs(delta * 100).toFixed(0)}%
          </span>
        ) : (
          <span className="text-[var(--viz-muted)]">sem base de comparação</span>
        )}
        {(delta !== null || ponto) && (
          <span aria-live="polite" className="text-[var(--viz-ink-2)]">
            {ponto ? `${rotuloDia(ponto.data)} vs. mesmo dia do período anterior` : comparacao}
          </span>
        )}
      </div>

      <div
        ref={ref}
        role="slider"
        tabIndex={0}
        aria-label={`${rotulo} por dia. Use as setas para ler cada dia.`}
        aria-valuemin={1}
        aria-valuemax={atual.length}
        aria-valuenow={ativo ? cursor + 1 : atual.length}
        aria-valuetext={ponto ? `${rotuloDia(ponto.data)}: ${formatar(ponto.valor)}` : `${formatar(total)} no período`}
        className="relative mt-auto h-28 cursor-crosshair touch-pan-y rounded-md pt-4 outline-none focus-visible:ring-2 focus-visible:ring-ring"
        onPointerMove={(e: PointerEvent<HTMLDivElement>) => setCursor(indice(e.clientX))}
        onPointerLeave={() => setCursor(null)}
        onBlur={() => setCursor(null)}
        onKeyDown={teclas}
      >
        <div className="absolute inset-x-0 top-4 bottom-0">
          <svg aria-hidden="true" className="absolute inset-0 size-full overflow-visible" preserveAspectRatio="none" viewBox={`0 0 ${W} ${H}`}>
            <line x1={0} x2={W} y1={H - 0.5} y2={H - 0.5} stroke="var(--viz-axis)" vectorEffect="non-scaling-stroke" />
            <path d={pathAnt} fill="none" stroke="var(--viz-cinza)" strokeWidth={1.5} vectorEffect="non-scaling-stroke" strokeLinecap="round" />
            <path d={`${pathAtual}L${W} ${H}L0 ${H}Z`} fill="var(--viz-realizado)" opacity={0.08} />
            <path d={pathAtual} fill="none" stroke="var(--viz-realizado)" strokeWidth={2} vectorEffect="non-scaling-stroke" strokeLinecap="round" strokeLinejoin="round" />
          </svg>
          {ativo && (
            <>
              <span className="pointer-events-none absolute inset-y-0 w-px bg-[var(--viz-axis)]" style={{ left: `${x}%` }} />
              <span
                className="pointer-events-none absolute size-2.5 -translate-x-1/2 -translate-y-1/2 rounded-full bg-[var(--viz-realizado)] ring-2 ring-[var(--viz-surface)]"
                style={{ left: `${x}%`, top: `${(y / H) * 100}%` }}
              />
            </>
          )}
        </div>
      </div>
      <div className="mt-2 flex items-center gap-4 text-xs text-[var(--viz-ink-2)]">
        <span className="flex items-center gap-1.5">
          <span className="h-0.5 w-3.5 rounded-full bg-[var(--viz-realizado)]" aria-hidden="true" /> Período atual
        </span>
        <span className="flex items-center gap-1.5">
          <span className="h-0.5 w-3.5 rounded-full bg-[var(--viz-cinza)]" aria-hidden="true" /> Período anterior
        </span>
      </div>
    </div>
  );
}
