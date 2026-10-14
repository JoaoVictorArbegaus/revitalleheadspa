import type { ComponentPropsWithoutRef, ElementType } from 'react';

type Props<T extends ElementType> = {
  as?: T;
  tone?: 'light' | 'dark' | 'wa';
  /** Sobe levemente no hover. */
  lift?: boolean;
} & Omit<ComponentPropsWithoutRef<T>, 'as'>;

/** Superfície de liquid glass: desfoque, borda especular, reflexo que segue o ponteiro e refração no Chromium. */
export function Glass<T extends ElementType = 'div'>({ as, tone = 'dark', lift = false, className, ...rest }: Props<T>) {
  const Tag = (as ?? 'div') as ElementType;
  const classes = ['glass', `glass--${tone}`, 'glass--refract', lift && 'glass--lift', className].filter(Boolean).join(' ');
  return <Tag className={classes} {...rest} />;
}
