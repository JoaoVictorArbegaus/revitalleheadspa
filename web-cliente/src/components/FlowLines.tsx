import { useEffect, useRef } from 'react';

/*
 * Fita de linhas que flui e se afasta do cursor (adaptado de Background Paths, kokonutui, MIT).
 * Camada decorativa absoluta: a cor vem do `color` do CSS, o ponteiro é lido na seção-mãe e o
 * loop dorme fora da tela, com a aba oculta e com movimento reduzido (desenho estático).
 */

type Props = {
  className?: string;
  /** Quantidade de linhas, dividida entre as três camadas de profundidade. */
  count?: number;
  /** Abertura da fita, como fração da altura. */
  spread?: number;
  /** Inclinação da fita, em graus. */
  angle?: number;
  /** Velocidade do fluxo. */
  speed?: number;
};

const SAMPLES = 64;
const OVERSCAN = 0.06;
const DRAW_MS = 1600;
const DRAW_STAGGER_MS = 40;
const PART_RADIUS = 160;
const PART_PUSH = 60;

type Layer = { width: number; opacity: number; phase: number; pace: number; part: number };
const LAYERS: Layer[] = [
  { width: 1, opacity: 0.16, phase: 0.6, pace: 0.7, part: 0.45 },
  { width: 1.25, opacity: 0.26, phase: 0.3, pace: 0.85, part: 0.75 },
  { width: 1.5, opacity: 0.4, phase: 0, pace: 1, part: 1 },
];

type Frame = {
  width: number;
  height: number;
  time: number;
  drawn: number[];
  pointer: { x: number; y: number; amount: number; on: boolean };
};
type Shape = { count: number; spread: number; slope: number; interactive: boolean };

function lineY(s: number, u: number, layer: Layer, f: Frame, shape: Shape, band: number) {
  const t = f.time * layer.pace;
  const x = u * f.width;
  const center =
    f.height * 0.5 + shape.slope * (x - f.width / 2) + band * 0.1 * Math.sin(Math.PI * 2 * (0.75 * u + 0.1) + layer.phase + t * 0.35);
  const turn = 0.22 + 0.09 * Math.sin(t * 0.21 + layer.phase);
  const breathe = 0.3 * Math.sin(t * 0.5 + s * 4 + layer.phase);
  const twist = Math.cos(Math.PI * (u - turn) + s * (0.6 + breathe) + layer.phase);
  return center + s * shape.spread * band * twist;
}

function part(x: number, y: number, f: Frame, strength: number) {
  const p = f.pointer;
  if (p.amount < 0.001) return y;
  const dx = x - p.x;
  const dy = y - p.y;
  const falloff = Math.exp(-(dx * dx + dy * dy) / (PART_RADIUS * PART_RADIUS));
  const side = dy / (Math.abs(dy) + 24);
  return y + side * falloff * PART_PUSH * strength * p.amount;
}

function drawLines(ctx: CanvasRenderingContext2D, f: Frame, shape: Shape, color: string) {
  ctx.clearRect(0, 0, f.width, f.height);
  ctx.strokeStyle = color;
  ctx.lineCap = 'round';
  ctx.lineJoin = 'round';
  const band = Math.min(f.height, f.width * 1.1);
  const perLayer = Math.max(1, Math.round(shape.count / LAYERS.length));
  const xs = new Float32Array(SAMPLES + 1);
  const ys = new Float32Array(SAMPLES + 1);

  LAYERS.forEach((layer, li) => {
    ctx.lineWidth = layer.width;
    for (let i = 0; i < perLayer; i++) {
      const drawn = f.drawn[li * perLayer + i] ?? 1;
      if (drawn <= 0) continue;
      const s = perLayer === 1 ? 0 : i / (perLayer - 1) - 0.5;
      const last = Math.max(1, Math.ceil(SAMPLES * drawn));
      for (let k = 0; k <= last; k++) {
        const u = -OVERSCAN + (k / SAMPLES) * (1 + OVERSCAN * 2);
        const x = u * f.width;
        const y = lineY(s, u, layer, f, shape, band);
        xs[k] = x;
        ys[k] = shape.interactive ? part(x, y, f, layer.part) : y;
      }
      ctx.globalAlpha = layer.opacity * (0.45 + 0.55 * (1 - Math.abs(s) * 2));
      ctx.beginPath();
      ctx.moveTo(xs[0], ys[0]);
      for (let k = 1; k < last; k++) ctx.quadraticCurveTo(xs[k], ys[k], (xs[k] + xs[k + 1]) / 2, (ys[k] + ys[k + 1]) / 2);
      ctx.lineTo(xs[last], ys[last]);
      ctx.stroke();
    }
  });
  ctx.globalAlpha = 1;
}

const easeOut = (t: number) => 1 - (1 - t) ** 3;

export function FlowLines({ className, count = 36, spread = 1, angle = -8, speed = 0.8 }: Props) {
  const layerRef = useRef<HTMLDivElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);

  useEffect(() => {
    const layer = layerRef.current;
    const canvas = canvasRef.current;
    const host = layer?.parentElement;
    const ctx = canvas?.getContext('2d');
    if (!layer || !canvas || !host || !ctx) return;

    const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const shape: Shape = { count, spread, slope: Math.tan((angle * Math.PI) / 180), interactive: !reduce };
    const perLayer = Math.max(1, Math.round(count / LAYERS.length));
    const f: Frame = {
      width: 0,
      height: 0,
      time: 40,
      drawn: new Array(perLayer * LAYERS.length).fill(reduce ? 1 : 0),
      pointer: { x: 0, y: 0, amount: 0, on: false },
    };
    const color = getComputedStyle(canvas).color;
    let start = 0;
    let last = 0;
    let raf = 0;
    let visible = false;

    const paint = () => drawLines(ctx, f, shape, color);
    const resize = () => {
      const r = canvas.getBoundingClientRect();
      const dpr = Math.min(window.devicePixelRatio || 1, 2);
      f.width = r.width;
      f.height = r.height;
      canvas.width = Math.round(r.width * dpr);
      canvas.height = Math.round(r.height * dpr);
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
      paint();
    };
    const loop = (now: number) => {
      raf = 0;
      if (!start) start = last = now;
      const dt = Math.min((now - last) / 1000, 0.05);
      last = now;
      f.time += dt * speed;
      const elapsed = now - start;
      for (let i = 0; i < f.drawn.length; i++) {
        if (f.drawn[i] < 1) f.drawn[i] = easeOut(Math.min(Math.max((elapsed - (i % perLayer) * DRAW_STAGGER_MS) / DRAW_MS, 0), 1));
      }
      f.pointer.amount += ((f.pointer.on ? 1 : 0) - f.pointer.amount) * 0.08;
      paint();
      if (visible) raf = requestAnimationFrame(loop);
    };
    const play = () => {
      if (!raf && visible && !reduce) {
        last = performance.now();
        raf = requestAnimationFrame(loop);
      }
    };

    resize();
    canvas.style.opacity = '1';
    const ro = new ResizeObserver(resize);
    ro.observe(canvas);
    const io = new IntersectionObserver(([entry]) => {
      visible = entry.isIntersecting && !document.hidden;
      play();
    });
    io.observe(layer);
    const onVisibility = () => {
      visible = !document.hidden;
      play();
    };
    document.addEventListener('visibilitychange', onVisibility);
    const onMove = (e: PointerEvent) => {
      if (e.pointerType !== 'mouse' || !shape.interactive) return;
      const r = canvas.getBoundingClientRect();
      f.pointer.x = e.clientX - r.left;
      f.pointer.y = e.clientY - r.top;
      f.pointer.on = true;
    };
    const onLeave = () => {
      f.pointer.on = false;
    };
    host.addEventListener('pointermove', onMove);
    host.addEventListener('pointerleave', onLeave);

    return () => {
      cancelAnimationFrame(raf);
      ro.disconnect();
      io.disconnect();
      document.removeEventListener('visibilitychange', onVisibility);
      host.removeEventListener('pointermove', onMove);
      host.removeEventListener('pointerleave', onLeave);
    };
  }, [count, spread, angle, speed]);

  return (
    <div ref={layerRef} className={className ? `flow ${className}` : 'flow'} aria-hidden="true">
      <canvas ref={canvasRef} className="flow__canvas" />
    </div>
  );
}
