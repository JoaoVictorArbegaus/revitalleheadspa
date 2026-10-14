import { useEffect } from 'react';

export const prefersReducedMotion = () => window.matchMedia('(prefers-reduced-motion: reduce)').matches;

/** Efeitos globais da landing. O conteúdo pré-renderizado é visível sem JS; estes efeitos só acrescentam movimento. */
export function useLandingEffects() {
  useEffect(() => {
    const doc = document.documentElement;
    const reduce = prefersReducedMotion();

    // Refração do vidro: só o Chromium aplica filtro SVG em backdrop-filter.
    const brands = (navigator as Navigator & { userAgentData?: { brands: { brand: string }[] } }).userAgentData?.brands;
    if (brands?.some((b) => /Chromium/.test(b.brand))) doc.classList.add('has-refraction');

    // Cabeçalho muda ao rolar + parallax da foto do hero (um único rAF por quadro).
    const heroImg = document.querySelector<HTMLElement>('[data-parallax]');
    const hero = document.getElementById('topo');
    let ticking = false;
    const onScroll = () => {
      if (ticking) return;
      ticking = true;
      requestAnimationFrame(() => {
        const y = window.scrollY;
        doc.classList.toggle('is-scrolled', y > 40);
        if (heroImg && hero && !reduce && y < hero.offsetHeight) heroImg.style.setProperty('--py', `${(y * 0.28).toFixed(1)}px`);
        ticking = false;
      });
    };
    window.addEventListener('scroll', onScroll, { passive: true });
    onScroll();

    // Entradas no scroll, encadeadas por grupo.
    document.querySelectorAll('[data-reveal-group]').forEach((g) =>
      g.querySelectorAll<HTMLElement>('[data-reveal]').forEach((el, i) => el.style.setProperty('--i', String(i))),
    );
    const items = document.querySelectorAll('[data-reveal]');
    let io: IntersectionObserver | undefined;
    if ('IntersectionObserver' in window && !reduce) {
      io = new IntersectionObserver(
        (entries) =>
          entries.forEach((en) => {
            if (!en.isIntersecting) return;
            en.target.classList.add('is-in');
            io!.unobserve(en.target);
          }),
        { rootMargin: '0px 0px -10% 0px', threshold: 0.12 },
      );
      items.forEach((el) => io!.observe(el));
    } else {
      items.forEach((el) => el.classList.add('is-in'));
    }

    // Reflexo do vidro acompanhando o ponteiro.
    let last: HTMLElement | null = null;
    const onPointer = (e: PointerEvent) => {
      const g = (e.target as Element).closest?.<HTMLElement>('.glass, .btn') ?? null;
      if (last && last !== g) {
        last.style.removeProperty('--mx');
        last.style.removeProperty('--my');
      }
      last = g;
      if (!g) return;
      const r = g.getBoundingClientRect();
      g.style.setProperty('--mx', `${(((e.clientX - r.left) / r.width) * 100).toFixed(1)}%`);
      g.style.setProperty('--my', `${(((e.clientY - r.top) / r.height) * 100).toFixed(1)}%`);
    };
    const finePointer = window.matchMedia('(hover: hover) and (pointer: fine)').matches;
    if (finePointer && !reduce) document.addEventListener('pointermove', onPointer, { passive: true });

    return () => {
      window.removeEventListener('scroll', onScroll);
      document.removeEventListener('pointermove', onPointer);
      io?.disconnect();
    };
  }, []);
}
