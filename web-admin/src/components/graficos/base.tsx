import { useState, type ReactNode } from 'react';

const compacto = new Intl.NumberFormat('pt-BR', { notation: 'compact', maximumFractionDigits: 1 });
const moeda0 = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL', maximumFractionDigits: 0 });

/** Rótulo de eixo em reais, compacto a partir de mil: R$ 0 · R$ 500 · R$ 1,2 mil. */
export const eixoMoeda = (v: number) => (v >= 1000 ? `R$ ${compacto.format(v)}` : moeda0.format(v));

/** Passo "bonito" para o eixo: 1, 2 ou 5 × 10^n, com ~4 marcas. */
export function escala(max: number, marcas = 4) {
  if (max <= 0) return { topo: 1, passos: [0, 1] };
  const bruto = max / marcas;
  const pot = 10 ** Math.floor(Math.log10(bruto));
  const passo = [1, 2, 5, 10].map((m) => m * pot).find((p) => p >= bruto)!;
  const topo = Math.ceil(max / passo) * passo;
  return { topo, passos: Array.from({ length: Math.round(topo / passo) + 1 }, (_, i) => i * passo) };
}

export type Dica = { x: number; y: number; conteudo: ReactNode } | null;

/** Tooltip único do gráfico: valor em destaque, rótulo secundário, chave em traço da cor da série. */
export function useDica() {
  const [dica, setDica] = useState<Dica>(null);
  const el = dica && (
    <div
      role="status"
      className="pointer-events-none absolute z-10 min-w-36 -translate-x-1/2 -translate-y-[calc(100%+12px)] rounded-lg border bg-popover px-3 py-2 text-xs shadow-md"
      style={{ left: dica.x, top: dica.y }}
    >
      {dica.conteudo}
    </div>
  );
  return { dica: el, mostrar: setDica, esconder: () => setDica(null) };
}

export function LinhaDica({ cor, rotulo, valor }: { cor: string; rotulo: string; valor: string }) {
  return (
    <div className="flex items-center gap-2 py-0.5">
      <span className="h-0.5 w-3 rounded-full" style={{ background: cor }} aria-hidden="true" />
      <span className="font-semibold text-[var(--viz-ink)]">{valor}</span>
      <span className="text-[var(--viz-ink-2)]">{rotulo}</span>
    </div>
  );
}

export function Legenda({ itens }: { itens: { cor: string; rotulo: string; forma?: 'barra' | 'linha' }[] }) {
  return (
    <ul className="flex flex-wrap gap-x-4 gap-y-1 text-xs text-[var(--viz-ink-2)]">
      {itens.map((i) => (
        <li key={i.rotulo} className="flex items-center gap-1.5">
          <span
            aria-hidden="true"
            className={i.forma === 'linha' ? 'h-0.5 w-3.5 rounded-full' : 'size-2.5 rounded-[3px]'}
            style={{ background: i.cor }}
          />
          {i.rotulo}
        </li>
      ))}
    </ul>
  );
}

/** Os mesmos dados em tabela: alternativa acessível ao gráfico, sem depender de hover. */
export function TabelaDados({ colunas, linhas }: { colunas: string[]; linhas: (string | number)[][] }) {
  return (
    <details className="mt-3 text-xs text-[var(--viz-ink-2)]">
      <summary className="cursor-pointer select-none hover:text-[var(--viz-ink)]">Ver dados em tabela</summary>
      <div className="mt-2 max-h-64 overflow-auto rounded-md border">
        <table className="w-full text-left tabular-nums">
          <thead className="sticky top-0 bg-muted">
            <tr>
              {colunas.map((c) => (
                <th key={c} className="px-2 py-1.5 font-semibold">
                  {c}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {linhas.map((l, i) => (
              <tr key={i} className="border-t">
                {l.map((v, j) => (
                  <td key={j} className="px-2 py-1">
                    {v}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </details>
  );
}
