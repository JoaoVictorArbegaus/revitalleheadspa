import { useRef, useState, type ComponentPropsWithoutRef, type MouseEvent, type ReactNode } from 'react';
import { m, useReducedMotion } from 'motion/react';
import { ArrowIcon } from './Icons';

type Props = ComponentPropsWithoutRef<'a'> & {
  href: string;
  variant?: 'gold' | 'clear' | 'dark';
  size?: 'sm' | 'md' | 'xl';
  /** Seta que desliza no hover: reservada ao CTA de agendamento. */
  arrow?: boolean;
  external?: boolean;
  /** Texto que sobe no hover (slide text). Sem ele, o próprio rótulo se repete. */
  hoverText?: ReactNode;
  /** Estouro de partículas no clique: só para ações que não trocam de página (ex.: WhatsApp em nova aba). */
  burst?: boolean;
};

const PARTICLES = 8;
const EASE_OUT = [0.23, 1, 0.32, 1] as const;

function makeParticles() {
  return Array.from({ length: PARTICLES }, (_, i) => ({
    x: (i % 2 ? 1 : -1) * (Math.random() * 46 + 14),
    y: -(Math.random() * 40 + 18),
  }));
}

export function Button({
  href,
  variant = 'gold',
  size = 'md',
  arrow = false,
  external = false,
  hoverText,
  burst = false,
  className,
  children,
  onClick,
  ...rest
}: Props) {
  const reduce = useReducedMotion();
  const [rajada, setRajada] = useState<{ id: number; pts: { x: number; y: number }[] } | null>(null);
  const timer = useRef<ReturnType<typeof setTimeout>>(undefined);

  const classes = ['btn', `btn--${variant}`, size !== 'md' && `btn--${size}`, className].filter(Boolean).join(' ');

  const handleClick = (e: MouseEvent<HTMLAnchorElement>) => {
    onClick?.(e);
    if (!burst || reduce || e.defaultPrevented) return;
    clearTimeout(timer.current);
    // um id novo remonta as partículas: cliques seguidos reiniciam o estouro
    setRajada((r) => ({ id: (r?.id ?? 0) + 1, pts: makeParticles() }));
    timer.current = setTimeout(() => setRajada(null), 700);
  };

  const link = (
    <a className={classes} href={href} {...(external ? { target: '_blank', rel: 'noopener' } : {})} onClick={handleClick} {...rest}>
      <span className="btn__label">
        <span className="btn__text">{children}</span>
        <span className="btn__text btn__text--hover" aria-hidden="true">
          {hoverText ?? children}
        </span>
      </span>
      {arrow && <ArrowIcon />}
    </a>
  );

  if (!burst) return link;

  // as partículas ficam fora do .btn (que corta o brilho com overflow hidden)
  return (
    <span className="burst">
      {link}
      {rajada && (
        <span key={rajada.id} className="burst__layer" aria-hidden="true">
          {rajada.pts.map((p, i) => (
            <m.span
              key={i}
              className="burst__dot"
              initial={{ opacity: 0, scale: 0.4, x: 0, y: 0 }}
              animate={{ opacity: [1, 1, 0], scale: [0.4, 1, 0.5], x: p.x, y: p.y }}
              transition={{ duration: 0.55, delay: i * 0.02, ease: EASE_OUT }}
            />
          ))}
        </span>
      )}
    </span>
  );
}
