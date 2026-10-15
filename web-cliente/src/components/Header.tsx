import { useEffect, useRef, useState, type MouseEvent } from 'react';
import { AnimatePresence, m } from 'motion/react';
import { AGENDAR_HREF, contato, navegacao } from '../content/site';
import { Button } from './Button';
import { EASE } from './Motion';
import { Glass } from './Glass';

// Menu do celular em cartões (adaptado de CardNav, React Bits, MIT; animado com Motion em vez de GSAP).
const grupos = [
  {
    rotulo: 'Tratamentos',
    tom: 'noz',
    links: [
      { href: '#servicos', rotulo: 'Serviços' },
      { href: '#como-funciona', rotulo: 'Como funciona' },
    ],
  },
  {
    rotulo: 'A Revitalle',
    tom: 'cobre',
    links: [
      { href: '#sobre', rotulo: 'Sobre' },
      { href: '#avaliacoes', rotulo: 'Avaliações' },
    ],
  },
  {
    rotulo: 'Visite',
    tom: 'ouro',
    links: [
      { href: '#visite', rotulo: 'Endereço e horários' },
      { href: contato.whatsappInfo, rotulo: 'WhatsApp', externo: true },
      { href: contato.instagramHref, rotulo: 'Instagram', externo: true },
    ],
  },
];

const LinkArrow = () => (
  <svg className="nav-card__ico" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
    <path d="M7 17L17 7M9 7h8v8" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
);

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

  const fecharAoNavegar = (e: MouseEvent) => (e.target as Element).closest('a') && setOpen(false);

  return (
    <header className="site-header">
      <Glass className={open ? 'nav is-open' : 'nav'}>
        <div className="nav__bar">
          <a className="nav__brand" href="#topo">
            <img src="/img/logo-h-dourado.webp" width={473} height={160} alt="Revitalle Head Spa" />
          </a>
          <nav className="nav__menu" aria-label="Seções da página">
            <ul>
              {navegacao.map((item) => (
                <li key={item.href}>
                  <a href={item.href}>{item.rotulo}</a>
                </li>
              ))}
            </ul>
          </nav>
          <Button href={AGENDAR_HREF} size="sm" className="nav__cta" hoverText="Horários">
            Agendar
          </Button>
          <button
            ref={toggleRef}
            className={open ? 'nav__toggle is-open' : 'nav__toggle'}
            type="button"
            aria-expanded={open}
            aria-controls="menu-cartoes"
            aria-label={open ? 'Fechar menu' : 'Abrir menu'}
            onClick={() => setOpen((v) => !v)}
          >
            <span className="nav__line" />
            <span className="nav__line" />
          </button>
        </div>

        <AnimatePresence initial={false}>
          {open && (
            <m.nav
              id="menu-cartoes"
              className="nav__cards"
              aria-label="Menu"
              initial={{ height: 0 }}
              animate={{ height: 'auto', transition: { duration: 0.45, ease: EASE } }}
              exit={{ height: 0, transition: { duration: 0.35, ease: EASE, delay: 0.1 } }}
              onClick={fecharAoNavegar}
            >
              <div className="nav__cards-inner">
                {grupos.map((g, i) => (
                  <m.div
                    key={g.rotulo}
                    className={`nav-card nav-card--${g.tom}`}
                    initial={{ y: 40, opacity: 0 }}
                    animate={{ y: 0, opacity: 1, transition: { duration: 0.45, ease: EASE, delay: 0.12 + i * 0.08 } }}
                    exit={{ y: 20, opacity: 0, transition: { duration: 0.2, delay: (grupos.length - 1 - i) * 0.04 } }}
                  >
                    <p className="nav-card__label">{g.rotulo}</p>
                    <ul className="nav-card__links">
                      {g.links.map((l) => (
                        <li key={l.rotulo}>
                          <a className="nav-card__link" href={l.href} {...('externo' in l ? { target: '_blank', rel: 'noopener' } : {})}>
                            <LinkArrow />
                            {l.rotulo}
                          </a>
                        </li>
                      ))}
                    </ul>
                  </m.div>
                ))}
              </div>
            </m.nav>
          )}
        </AnimatePresence>
      </Glass>
    </header>
  );
}
