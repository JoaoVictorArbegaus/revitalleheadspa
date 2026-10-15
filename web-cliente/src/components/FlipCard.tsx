import {
  useCallback,
  useEffect,
  useId,
  useRef,
  useState,
  type KeyboardEvent,
  type PointerEvent,
  type ReactNode,
  type Ref,
} from 'react';

/*
 * Cartão que gira com arrasto, clique ou teclado, com mola e espessura (adaptado de Card Flip, kokonutui, MIT).
 * Diferenças: faces empilhadas no grid (a altura segue o conteúdo), vidro simulado nas faces
 * (backdrop-filter não funciona dentro de preserve-3d) e textos em português.
 */

const DEG_PER_PX = 0.6;
const STIFFNESS = 170;
const DAMPING = 20;
const PROJECTION = 0.22;
const FLICK_SPEED = 450;
const MAX_HALF_TURNS = 6;
const DRAG_SLOP = 4;
const LIFT = 36;
const TILT = 6;
const THICKNESS = 6;
const SLICES = 7;
const SLICE_DEPTHS = Array.from({ length: SLICES }, (_, i) => -THICKNESS / 2 + (THICKNESS * (i + 0.5)) / SLICES);
const SUBSTEP = 1 / 240;
const VELOCITY_WINDOW = 80;
const INTERACTIVE = 'button, a, input, select, textarea, [data-no-flip]';

const mod2 = (n: number) => ((n % 2) + 2) % 2;
const sideOf = (angle: number) => mod2(Math.round(angle / 180)) === 1;
const nearestFace = (angle: number) => Math.round(angle / 180) * 180;
const cardTransform = (angle: number, lift: number) => `translateZ(${lift.toFixed(2)}px) rotateY(${angle.toFixed(2)}deg)`;

type Sim = {
  angle: number;
  velocity: number;
  target: number;
  raf: number;
  last: number;
  reduceMotion: boolean;
  pointerId: number | null;
  startX: number;
  startY: number;
  startAngle: number;
  dragging: boolean;
  samples: { t: number; a: number }[];
};

function releaseVelocity(samples: Sim['samples'], now: number) {
  const last = samples.at(-1);
  if (!last || now - last.t > VELOCITY_WINDOW) return 0;
  const first = samples.find((p) => last.t - p.t <= VELOCITY_WINDOW);
  if (!first || last.t <= first.t) return 0;
  return ((last.a - first.a) / (last.t - first.t)) * 1000;
}

function LightBand({ bandRef, shadeRef }: { bandRef: Ref<HTMLDivElement>; shadeRef: Ref<HTMLDivElement> }) {
  return (
    <div className="flip__light" aria-hidden="true">
      <div className="flip__shade" ref={shadeRef} />
      <div className="flip__band" ref={bandRef} />
    </div>
  );
}

type Props = { front: ReactNode; back: ReactNode; label: string; className?: string };

export function FlipCard({ front, back, label, className }: Props) {
  const [flipped, setFlipped] = useState(false);
  const [backVisible, setBackVisible] = useState(false);
  const hintId = useId();

  const rootRef = useRef<HTMLDivElement>(null);
  const tiltRef = useRef<HTMLDivElement>(null);
  const cardRef = useRef<HTMLDivElement>(null);
  const shadowRef = useRef<HTMLDivElement>(null);
  const frontBand = useRef<HTMLDivElement>(null);
  const backBand = useRef<HTMLDivElement>(null);
  const frontShade = useRef<HTMLDivElement>(null);
  const backShade = useRef<HTMLDivElement>(null);
  const backVisibleRef = useRef(false);
  const flippedRef = useRef(false);
  const hovering = useRef(false);
  const restTimer = useRef<ReturnType<typeof setTimeout>>(undefined);

  const sim = useRef<Sim>({
    angle: 0, velocity: 0, target: 0, raf: 0, last: 0, reduceMotion: false,
    pointerId: null, startX: 0, startY: 0, startAngle: 0, dragging: false, samples: [],
  });

  // Perspectiva só enquanto o cartão está em uso: em repouso, a face 3px à frente ficaria ampliada
  // pela perspectiva e o texto, rasterizado numa escala intermediária, ficaria borrado.
  const activate = useCallback(() => {
    clearTimeout(restTimer.current);
    rootRef.current?.classList.add('is-active');
  }, []);
  const maybeRest = useCallback(() => {
    clearTimeout(restTimer.current);
    restTimer.current = setTimeout(() => {
      const s = sim.current;
      if (!s.raf && s.pointerId === null && !hovering.current) rootRef.current?.classList.remove('is-active');
    }, 600);
  }, []);

  const render = useCallback(() => {
    const s = sim.current;
    const rad = (s.angle * Math.PI) / 180;
    const sin = Math.sin(rad);
    const turn = Math.abs(sin);
    const facing = Math.abs(Math.cos(rad));
    if (cardRef.current) cardRef.current.style.transform = cardTransform(s.angle, turn * LIFT);
    if (shadowRef.current) {
      shadowRef.current.style.transform = `scale(${(0.16 + 0.84 * facing).toFixed(3)}, ${(1 - turn * 0.12).toFixed(3)})`;
      shadowRef.current.style.opacity = (1 - turn * 0.45).toFixed(3);
    }
    // faixa de luz atravessa a face visível e a face escurece ao virar
    const shift = `translateX(${(sin * 140).toFixed(1)}%) rotate(18deg)`;
    for (const b of [frontBand.current, backBand.current]) if (b) {
      b.style.opacity = (turn * 0.14).toFixed(3);
      b.style.transform = shift;
    }
    for (const sh of [frontShade.current, backShade.current]) if (sh) sh.style.opacity = (turn * 0.16).toFixed(3);
    const showsBack = sideOf(s.angle);
    if (showsBack !== backVisibleRef.current) {
      backVisibleRef.current = showsBack;
      setBackVisible(showsBack);
    }
  }, []);

  const step = useCallback(
    (now: number) => {
      const s = sim.current;
      const dt = Math.min(0.05, (now - s.last) / 1000);
      s.last = now;
      const n = Math.max(1, Math.ceil(dt / SUBSTEP));
      const h = dt / n;
      for (let i = 0; i < n; i++) {
        s.velocity += (-STIFFNESS * (s.angle - s.target) - DAMPING * s.velocity) * h;
        s.angle += s.velocity * h;
      }
      if (Math.abs(s.angle - s.target) < 0.05 && Math.abs(s.velocity) < 0.5) {
        s.target = sideOf(s.target) ? 180 : 0;
        s.angle = s.target;
        s.velocity = 0;
        s.raf = 0;
        render();
        maybeRest();
        return;
      }
      render();
      s.raf = requestAnimationFrame(step);
    },
    [render, maybeRest],
  );

  const settle = useCallback(() => {
    const s = sim.current;
    if (s.reduceMotion) {
      cancelAnimationFrame(s.raf);
      s.raf = 0;
      s.target = sideOf(s.target) ? 180 : 0;
      s.angle = s.target;
      s.velocity = 0;
      render();
      return;
    }
    if (!s.raf) {
      activate();
      s.last = performance.now();
      s.raf = requestAnimationFrame(step);
    }
  }, [render, step, activate]);

  const aim = useCallback(
    (target: number) => {
      sim.current.target = target;
      const next = sideOf(target);
      if (next !== flippedRef.current) {
        flippedRef.current = next;
        setFlipped(next);
      }
      settle();
    },
    [settle],
  );

  const flipBy = useCallback((dir: number) => aim(nearestFace(sim.current.target) + 180 * dir), [aim]);

  useEffect(() => {
    const s = sim.current;
    const q = window.matchMedia('(prefers-reduced-motion: reduce)');
    s.reduceMotion = q.matches;
    const onChange = () => (s.reduceMotion = q.matches);
    q.addEventListener('change', onChange);
    return () => {
      q.removeEventListener('change', onChange);
      clearTimeout(restTimer.current);
      cancelAnimationFrame(s.raf);
      s.raf = 0;
    };
  }, []);

  const setTilt = (x: number, y: number) => {
    if (tiltRef.current) tiltRef.current.style.transform = `rotateX(${(-y * TILT).toFixed(2)}deg) rotateY(${(x * TILT).toFixed(2)}deg)`;
  };

  const onPointerDown = (e: PointerEvent<HTMLDivElement>) => {
    const s = sim.current;
    if (s.pointerId !== null || e.button !== 0 || (e.target as Element).closest(INTERACTIVE)) return;
    activate();
    s.pointerId = e.pointerId;
    s.startX = e.clientX;
    s.startY = e.clientY;
    s.startAngle = s.angle;
    s.dragging = false;
    s.samples = [{ t: e.timeStamp, a: s.angle }];
    e.currentTarget.setPointerCapture(e.pointerId);
  };

  const onPointerMove = (e: PointerEvent<HTMLDivElement>) => {
    const s = sim.current;
    if (s.pointerId !== e.pointerId) {
      if (e.pointerType === 'mouse' && rootRef.current) {
        const r = rootRef.current.getBoundingClientRect();
        setTilt(((e.clientX - r.left) / r.width) * 2 - 1, ((e.clientY - r.top) / r.height) * 2 - 1);
      }
      return;
    }
    const dx = e.clientX - s.startX;
    const dy = e.clientY - s.startY;
    if (!s.dragging) {
      if (Math.hypot(dx, dy) < DRAG_SLOP) return;
      s.dragging = true;
      cancelAnimationFrame(s.raf);
      s.raf = 0;
      s.velocity = 0;
      setTilt(0, 0);
    }
    s.angle = s.startAngle + dx * DEG_PER_PX;
    s.samples.push({ t: e.timeStamp, a: s.angle });
    if (s.samples.length > 8) s.samples.shift();
    render();
  };

  const release = (e: PointerEvent<HTMLDivElement>, cancelled: boolean) => {
    const s = sim.current;
    if (s.pointerId !== e.pointerId) return;
    s.pointerId = null;
    maybeRest();
    if (!s.dragging) {
      if (!cancelled) {
        // o clique empurra para longe a metade pressionada
        const r = e.currentTarget.getBoundingClientRect();
        flipBy(e.clientX > r.left + r.width / 2 ? 1 : -1);
      }
      return;
    }
    s.dragging = false;
    const velocity = cancelled ? 0 : releaseVelocity(s.samples, e.timeStamp);
    const from = nearestFace(s.startAngle);
    let target = nearestFace(s.angle + velocity * PROJECTION);
    if (Math.abs(velocity) > FLICK_SPEED && target === from) target = from + 180 * Math.sign(velocity);
    const limit = MAX_HALF_TURNS * 180;
    target = Math.max(from - limit, Math.min(from + limit, target));
    s.velocity = velocity;
    aim(target);
  };

  const onKeyDown = (e: KeyboardEvent<HTMLDivElement>) => {
    if (e.target !== e.currentTarget) return;
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      flipBy(1);
    }
  };

  return (
    <div
      ref={rootRef}
      className={className ? `flip ${className}` : 'flip'}
      role="group"
      aria-label={label}
      aria-roledescription="cartão giratório"
      aria-describedby={hintId}
      tabIndex={0}
      onKeyDown={onKeyDown}
      onPointerDown={onPointerDown}
      onPointerMove={onPointerMove}
      onPointerUp={(e) => release(e, false)}
      onPointerCancel={(e) => release(e, true)}
      onLostPointerCapture={(e) => release(e, true)}
      onPointerEnter={(e) => {
        if (e.pointerType !== 'mouse') return;
        hovering.current = true;
        activate();
      }}
      onPointerLeave={() => {
        hovering.current = false;
        if (sim.current.pointerId === null) setTilt(0, 0);
        maybeRest();
      }}
    >
      <div className="flip__floor" aria-hidden="true" ref={shadowRef} />
      <div className="flip__tilt" ref={tiltRef}>
        <div className="flip__card" ref={cardRef}>
          {SLICE_DEPTHS.map((z) => (
            <div key={z} className="flip__slice" aria-hidden="true" style={{ transform: `translateZ(${z.toFixed(3)}px)` }} />
          ))}
          <div className="flip__face flip__face--front" aria-hidden={backVisible || undefined} inert={backVisible} style={{ transform: `translateZ(${THICKNESS / 2}px)` }}>
            {front}
            <LightBand bandRef={frontBand} shadeRef={frontShade} />
          </div>
          <div
            className="flip__face flip__face--back"
            aria-hidden={!backVisible || undefined}
            inert={!backVisible}
            style={{ transform: `rotateY(180deg) translateZ(${THICKNESS / 2}px)` }}
          >
            {back}
            <LightBand bandRef={backBand} shadeRef={backShade} />
          </div>
        </div>
      </div>
      <span className="sr-only" id={hintId}>
        Mostrando {flipped ? 'as opções de agendamento' : 'a descrição'}. Pressione Enter ou Espaço, ou arraste, para virar.
      </span>
    </div>
  );
}
