import { useEffect, useState } from 'react';

/*
 * Anéis de progresso (adaptado de Apple Activity Card, kokonutui, MIT).
 * Cada anel é um medidor de uma proporção (0–100%), identificado pelo rótulo ao lado, não pela cor:
 * todos usam o mesmo tom, com trilho mais claro da mesma rampa (validado com a skill dataviz).
 */

export type Anel = { rotulo: string; descricao: string; valor: number | null };

const TRACO = 13;
const ESPACO = 4;

export function Aneis({ aneis }: { aneis: Anel[] }) {
  const [montado, setMontado] = useState(false);
  useEffect(() => {
    const id = requestAnimationFrame(() => setMontado(true));
    return () => cancelAnimationFrame(id);
  }, []);
  const tamanho = 168;
  const c = tamanho / 2;

  return (
    <div className="flex flex-wrap items-center gap-6">
      <svg width={tamanho} height={tamanho} viewBox={`0 0 ${tamanho} ${tamanho}`} role="img" aria-label={aneis.map((a) => `${a.rotulo}: ${a.valor === null ? 'sem dados' : `${Math.round(a.valor * 100)}%`}`).join('; ')} className="shrink-0 -rotate-90">
        {aneis.map((a, i) => {
          const r = c - TRACO / 2 - i * (TRACO + ESPACO);
          const circ = 2 * Math.PI * r;
          const v = Math.max(0, Math.min(1, a.valor ?? 0));
          return (
            <g key={a.rotulo}>
              <circle cx={c} cy={c} r={r} fill="none" stroke="var(--seq-1)" strokeWidth={TRACO} opacity={0.6} />
              <circle
                cx={c}
                cy={c}
                r={r}
                fill="none"
                stroke={['var(--seq-5)', 'var(--seq-4)', 'var(--seq-3)'][i] ?? 'var(--seq-5)'}
                strokeWidth={TRACO}
                strokeLinecap="round"
                strokeDasharray={circ}
                strokeDashoffset={montado ? circ * (1 - v) : circ}
                className="transition-[stroke-dashoffset] duration-[1200ms] ease-[cubic-bezier(0.23,1,0.32,1)] motion-reduce:transition-none"
                style={{ transitionDelay: `${i * 150}ms` }}
              />
            </g>
          );
        })}
      </svg>
      <ul className="grid min-w-44 flex-1 gap-3">
        {aneis.map((a, i) => (
          <li key={a.rotulo} className="flex items-start gap-2.5">
            <span className="mt-1.5 h-0.5 w-4 shrink-0 rounded-full" style={{ background: ['var(--seq-5)', 'var(--seq-4)', 'var(--seq-3)'][i] }} aria-hidden="true" />
            <div>
              <p className="text-xl leading-none font-semibold text-[var(--viz-ink)]">{a.valor === null ? '—' : `${Math.round(a.valor * 100)}%`}</p>
              <p className="mt-1 text-sm font-medium text-[var(--viz-ink)]">{a.rotulo}</p>
              <p className="text-xs text-[var(--viz-ink-2)]">{a.descricao}</p>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}
