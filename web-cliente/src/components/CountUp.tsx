import { useEffect, useRef } from 'react';
import { prefersReducedMotion } from '../hooks/useLandingEffects';

const format = (v: number, decimals: number) => v.toFixed(decimals).replace('.', ',');

/** Número que conta até o valor final ao entrar na tela. O HTML pré-renderizado já traz o valor final. */
export function CountUp({ to, decimals = 0 }: { to: number; decimals?: number }) {
  const ref = useRef<HTMLSpanElement>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el || prefersReducedMotion() || !('IntersectionObserver' in window)) return;
    let frame = 0;
    const io = new IntersectionObserver(([entry]) => {
      if (!entry.isIntersecting) return;
      io.disconnect();
      const t0 = performance.now();
      const step = (now: number) => {
        const p = Math.min(1, (now - t0) / 1400);
        el.textContent = format(to * (1 - Math.pow(1 - p, 3)), decimals);
        if (p < 1) frame = requestAnimationFrame(step);
      };
      frame = requestAnimationFrame(step);
    }, { threshold: 0.4 });
    io.observe(el);
    return () => {
      io.disconnect();
      cancelAnimationFrame(frame);
    };
  }, [to, decimals]);

  return <span ref={ref}>{format(to, decimals)}</span>;
}
