import { useEffect, useRef } from 'react';

/**
 * Ondas suaves de fundo em canvas: três senoides preenchidas que deslizam e respiram devagar.
 * (Uma versão em SVG de 200% de largura rasterizava em blocos no compositor; o canvas desenha exato.)
 * O loop dorme fora da tela e com a aba oculta; com movimento reduzido, desenha uma vez e para.
 */
type Onda = { y: number; amp: number; comprimento: number; velocidade: number; fase: number; cor: string };

const ONDAS: Onda[] = [
  { y: 0.42, amp: 0.1, comprimento: 1.1, velocidade: 0.035, fase: 0, cor: 'rgba(226, 198, 147, .22)' },
  { y: 0.56, amp: 0.08, comprimento: 0.85, velocidade: -0.05, fase: 2.1, cor: 'rgba(200, 160, 98, .14)' },
  { y: 0.7, amp: 0.06, comprimento: 0.7, velocidade: 0.07, fase: 4.2, cor: 'rgba(255, 252, 246, .5)' },
];

export function Waves({ className }: { className?: string }) {
  const ref = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const canvas = ref.current;
    const ctx = canvas?.getContext('2d');
    if (!canvas || !ctx) return;
    const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    let w = 0;
    let h = 0;
    let raf = 0;
    let visible = false;
    let t = 0;
    let last = 0;

    const draw = () => {
      ctx.clearRect(0, 0, w, h);
      for (const o of ONDAS) {
        const k = (Math.PI * 2) / (o.comprimento * w);
        const desloc = t * o.velocidade * Math.PI * 2 + o.fase;
        // respiração: a amplitude oscila devagar
        const amp = o.amp * h * (0.85 + 0.15 * Math.sin(t * 0.25 + o.fase));
        ctx.beginPath();
        ctx.moveTo(0, h);
        for (let x = 0; x <= w + 8; x += 8) ctx.lineTo(x, o.y * h + Math.sin(x * k + desloc) * amp);
        ctx.lineTo(w, h);
        ctx.closePath();
        ctx.fillStyle = o.cor;
        ctx.fill();
      }
    };
    const resize = () => {
      const r = canvas.getBoundingClientRect();
      const dpr = Math.min(window.devicePixelRatio || 1, 2);
      w = r.width;
      h = r.height;
      canvas.width = Math.round(w * dpr);
      canvas.height = Math.round(h * dpr);
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
      draw();
    };
    const loop = (now: number) => {
      raf = 0;
      t += Math.min((now - last) / 1000, 0.05);
      last = now;
      draw();
      if (visible) raf = requestAnimationFrame(loop);
    };
    const play = () => {
      if (raf || !visible || reduce) return;
      last = performance.now();
      raf = requestAnimationFrame(loop);
    };

    resize();
    const ro = new ResizeObserver(resize);
    ro.observe(canvas);
    const io = new IntersectionObserver(([entry]) => {
      visible = entry.isIntersecting && !document.hidden;
      play();
    });
    io.observe(canvas);
    const onVisibility = () => {
      visible = !document.hidden;
      play();
    };
    document.addEventListener('visibilitychange', onVisibility);
    return () => {
      cancelAnimationFrame(raf);
      ro.disconnect();
      io.disconnect();
      document.removeEventListener('visibilitychange', onVisibility);
    };
  }, []);

  return (
    <div className={className ? `waves ${className}` : 'waves'} aria-hidden="true">
      <canvas ref={ref} className="waves__canvas" />
    </div>
  );
}
