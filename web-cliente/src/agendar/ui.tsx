import { useEffect, useRef, type ComponentPropsWithoutRef, type ReactNode } from 'react';

// Peças pequenas do fluxo de agendamento: ícones de traço, botão de ação e título de etapa com foco.

const traco = { fill: 'none', stroke: 'currentColor', strokeWidth: 1.7, strokeLinecap: 'round', strokeLinejoin: 'round' } as const;

const caminhos = {
  voltar: <path d="M19 12H5M11 6l-6 6 6 6" />,
  anterior: <path d="M15 6l-6 6 6 6" />,
  proximo: <path d="M9 6l6 6-6 6" />,
  ok: <path d="M5 12.5l4.2 4.2L19 7" />,
  relogio: (
    <>
      <circle cx="12" cy="12" r="8.5" />
      <path d="M12 7.5V12l3 2" />
    </>
  ),
  calendario: (
    <>
      <rect x="3.5" y="5" width="17" height="15.5" rx="3" />
      <path d="M3.5 10h17M8 3v4M16 3v4" />
    </>
  ),
  pessoa: (
    <>
      <circle cx="12" cy="8.5" r="3.8" />
      <path d="M4.5 20c1.2-3.6 4-5.5 7.5-5.5s6.3 1.9 7.5 5.5" />
    </>
  ),
  local: (
    <>
      <path d="M12 21s-6.5-5.6-6.5-11a6.5 6.5 0 0 1 13 0c0 5.4-6.5 11-6.5 11z" />
      <circle cx="12" cy="10" r="2.3" />
    </>
  ),
  alerta: (
    <>
      <circle cx="12" cy="12" r="8.5" />
      <path d="M12 7.8v5M12 16.2h.01" />
    </>
  ),
  baixar: <path d="M12 4v11M7 10.5l5 5 5-5M5 19.5h14" />,
} as const;

export function Icone({ nome, className = 'ico' }: { nome: keyof typeof caminhos; className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" aria-hidden="true" focusable="false" {...traco}>
      {caminhos[nome]}
    </svg>
  );
}

type BotaoProps = ComponentPropsWithoutRef<'button'> & {
  variante?: 'dark' | 'gold' | 'clear';
  tamanho?: 'sm' | 'md' | 'xl';
  /** Texto que sobe no hover, como nos botões da landing. */
  textoHover?: ReactNode;
  ocupado?: boolean;
};

/** Mesmo vidro dos botões da landing, como <button> (ações que não trocam de página). */
export function Botao({ variante = 'dark', tamanho = 'md', textoHover, ocupado = false, className, children, disabled, ...rest }: BotaoProps) {
  const classes = ['btn', `btn--${variante}`, tamanho !== 'md' && `btn--${tamanho}`, ocupado && 'is-busy', className].filter(Boolean).join(' ');
  return (
    <button type="button" className={classes} disabled={disabled || ocupado} aria-busy={ocupado || undefined} {...rest}>
      <span className="btn__label">
        <span className="btn__text">{children}</span>
        <span className="btn__text btn__text--hover" aria-hidden="true">
          {textoHover ?? children}
        </span>
      </span>
      {ocupado && <span className="btn__spinner" aria-hidden="true" />}
    </button>
  );
}

/** Cabeçalho da etapa. Ao trocar de etapa o foco vai para o título, para leitores de tela anunciarem a mudança. */
export function TituloEtapa({ titulo, descricao, focar }: { titulo: string; descricao?: ReactNode; focar: boolean }) {
  const ref = useRef<HTMLHeadingElement>(null);
  useEffect(() => {
    if (focar) ref.current?.focus({ preventScroll: true });
  }, [focar]);
  return (
    <header className="ag-step__head">
      <h2 ref={ref} tabIndex={-1}>
        {titulo}
      </h2>
      {descricao && <p>{descricao}</p>}
    </header>
  );
}

/** Mensagem de erro ou aviso dentro da etapa, anunciada a leitores de tela. */
export function Aviso({ tom = 'erro', children, acao }: { tom?: 'erro' | 'info'; children: ReactNode; acao?: ReactNode }) {
  return (
    <div className={`ag-alert ag-alert--${tom}`} role={tom === 'erro' ? 'alert' : 'status'}>
      <Icone nome="alerta" />
      <div>
        <p>{children}</p>
        {acao && <div className="ag-alert__acao">{acao}</div>}
      </div>
    </div>
  );
}
