import type { ReactNode } from 'react';
import { Loader2 } from 'lucide-react';
import { Skeleton } from '@/components/ui/skeleton';
import { cn } from '@/lib/utils';

export function Carregando({ tela = false }: { tela?: boolean }) {
  return (
    <div className={cn('grid place-items-center text-muted-foreground', tela ? 'min-h-svh' : 'py-16')} role="status">
      <Loader2 className="size-6 animate-spin" aria-hidden="true" />
      <span className="sr-only">Carregando</span>
    </div>
  );
}

export function LinhasCarregando({ linhas = 4 }: { linhas?: number }) {
  return (
    <div className="grid gap-3" role="status" aria-label="Carregando">
      {Array.from({ length: linhas }, (_, i) => (
        <Skeleton key={i} className="h-14 w-full" />
      ))}
    </div>
  );
}

export function Vazio({ titulo, children, acao }: { titulo: string; children?: ReactNode; acao?: ReactNode }) {
  return (
    <div className="grid justify-items-center gap-3 rounded-xl border border-dashed bg-card/60 px-6 py-14 text-center">
      <h3 className="text-xl">{titulo}</h3>
      {children && <p className="max-w-md text-sm text-muted-foreground">{children}</p>}
      {acao}
    </div>
  );
}

export function Cabecalho({ titulo, descricao, acao }: { titulo: string; descricao?: ReactNode; acao?: ReactNode }) {
  return (
    <header className="mb-6 flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 className="text-3xl leading-tight">{titulo}</h1>
        {descricao && <p className="mt-1 text-sm text-muted-foreground">{descricao}</p>}
      </div>
      {acao}
    </header>
  );
}
