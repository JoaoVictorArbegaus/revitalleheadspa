import type { ComponentPropsWithoutRef } from 'react';
import { ArrowIcon } from './Icons';

type Props = ComponentPropsWithoutRef<'a'> & {
  href: string;
  variant?: 'gold' | 'clear' | 'dark';
  size?: 'sm' | 'md' | 'xl';
  /** Seta que desliza no hover: reservada ao CTA de agendamento. */
  arrow?: boolean;
  external?: boolean;
};

export function Button({ href, variant = 'gold', size = 'md', arrow = false, external = false, className, children, ...rest }: Props) {
  const classes = ['btn', `btn--${variant}`, size !== 'md' && `btn--${size}`, className].filter(Boolean).join(' ');
  return (
    <a className={classes} href={href} {...(external ? { target: '_blank', rel: 'noopener' } : {})} {...rest}>
      {children}
      {arrow && <ArrowIcon />}
    </a>
  );
}
