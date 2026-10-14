import { useEffect, useRef, useState } from 'react';
import { AGENDAR_HREF, navegacao } from '../content/site';
import { Button } from './Button';
import { Glass } from './Glass';

export function Header() {
  const [open, setOpen] = useState(false);
  const toggleRef = useRef<HTMLButtonElement>(null);

  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key !== 'Escape') return;
      setOpen(false);
      toggleRef.current?.focus();
    };
    document.addEventListener('keydown', onKey);
    return () => document.removeEventListener('keydown', onKey);
  }, [open]);

  return (
    <header className="site-header">
      <Glass className={open ? 'nav is-open' : 'nav'}>
        <a className="nav__brand" href="#topo">
          <img src="/img/logo-h-dourado.webp" width={473} height={160} alt="Revitalle Head Spa" />
        </a>
        <nav className="nav__menu" id="menu" aria-label="Seções da página" onClick={(e) => (e.target as Element).closest('a') && setOpen(false)}>
          <ul>
            {navegacao.map((item) => (
              <li key={item.href}>
                <a href={item.href}>{item.rotulo}</a>
              </li>
            ))}
          </ul>
        </nav>
        <Button href={AGENDAR_HREF} size="sm" className="nav__cta">
          Agendar
        </Button>
        <button
          ref={toggleRef}
          className="nav__toggle"
          type="button"
          aria-expanded={open}
          aria-controls="menu"
          aria-label={open ? 'Fechar menu' : 'Abrir menu'}
          onClick={() => setOpen((v) => !v)}
        >
          <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
            <path d={open ? 'M6 6l12 12M18 6L6 18' : 'M4 8h16M4 16h16'} />
          </svg>
        </button>
      </Glass>
    </header>
  );
}
