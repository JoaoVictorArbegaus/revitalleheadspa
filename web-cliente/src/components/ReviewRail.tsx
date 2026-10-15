import { useCallback, useEffect, useRef, useState, type RefObject } from 'react';
import { depoimentos } from '../content/site';
import { BorderGlowLayers, borderGlowMove } from './BorderGlow';
import { Glass } from './Glass';
import { Stars } from './Icons';

/*
 * Trilho de depoimentos arrastável (adaptado de Carousel Cards / Momentum Rail, kokonutui, MIT):
 * arrasto com inércia, mola até o cartão mais próximo, elástico nas pontas, setas e teclado.
 * A profundidade (coverflow) é CSS ligado à rolagem (animation-timeline), fora da thread principal.
 */

const STIFFNESS = 170;
const DAMPING = 26;
const DECELERATION = 0.997;
const DRAG_THRESHOLD = 4;

const rubber = (over: number, size: number) => Math.sign(over) * (1 - 1 / ((Math.abs(over) * 0.55) / size + 1)) * size;
const clamp = (v: number, min: number, max: number) => Math.min(max, Math.max(min, v));
const maxScroll = (el: HTMLElement) => el.scrollWidth - el.clientWidth;

const snapPoints = (scroller: HTMLElement, track: HTMLUListElement) => {
  const max = maxScroll(scroller);
  return Array.from(track.children, (child) => {
    const card = child as HTMLElement;
    return clamp(track.offsetLeft + card.offsetLeft + card.offsetWidth / 2 - scroller.clientWidth / 2, 0, max);
  });
};

const nearestIndex = (points: number[], pos: number) => {
  let best = 0;
  for (let i = 1; i < points.length; i++) if (Math.abs(points[i] - pos) < Math.abs(points[best] - pos)) best = i;
  return best;
};

type RailState = {
  mode: 'idle' | 'pressing' | 'dragging' | 'spring';
  pointerId: number;
  startX: number;
  startPos: number;
  pos: number;
  vel: number;
  target: number;
  samples: { t: number; x: number }[];
  raf: number;
  last: number;
  suppressClick: boolean;
};

const releaseVelocity = (samples: RailState['samples'], now: number) => {
  const first = samples[0];
  const last = samples.at(-1) ?? first;
  const dt = last.t - first.t;
  if (dt <= 0 || now - last.t > 60) return 0;
  return -(last.x - first.x) / dt;
};

function useMomentumRail(scrollerRef: RefObject<HTMLElement | null>, trackRef: RefObject<HTMLUListElement | null>, onIndex: (i: number) => void) {
  const state = useRef<RailState>({
    mode: 'idle', pointerId: -1, startX: 0, startPos: 0, pos: 0, vel: 0, target: 0, samples: [], raf: 0, last: 0, suppressClick: false,
  });
  const onIndexRef = useRef(onIndex);
  onIndexRef.current = onIndex;

  // a parte dentro do limite rola de verdade; o excesso vira translate amortecido (elástico)
  const apply = useCallback(
    (pos: number) => {
      const scroller = scrollerRef.current;
      const track = trackRef.current;
      if (!scroller || !track) return;
      const inRange = clamp(pos, 0, maxScroll(scroller));
      scroller.scrollLeft = inRange;
      const over = pos - inRange;
      track.style.transform = Math.abs(over) > 0.1 ? `translate3d(${(-rubber(over, scroller.clientWidth)).toFixed(2)}px,0,0)` : '';
    },
    [scrollerRef, trackRef],
  );

  const setFree = useCallback(
    (free: boolean) => {
      if (scrollerRef.current) scrollerRef.current.dataset.free = String(free);
    },
    [scrollerRef],
  );

  const step = useCallback(
    (now: number) => {
      const s = state.current;
      const dt = Math.min((now - s.last) / 1000, 1 / 30);
      s.last = now;
      const h = dt / 4;
      for (let i = 0; i < 4; i++) {
        s.vel += (-STIFFNESS * (s.pos - s.target) - DAMPING * s.vel) * h;
        s.pos += s.vel * h;
      }
      if (Math.abs(s.pos - s.target) < 0.5 && Math.abs(s.vel) < 8) {
        s.pos = s.target;
        s.vel = 0;
        s.mode = 'idle';
        apply(s.target);
        setFree(false);
        return;
      }
      apply(s.pos);
      s.raf = requestAnimationFrame(step);
    },
    [apply, setFree],
  );

  const springTo = useCallback(
    (target: number, velocity: number) => {
      const s = state.current;
      const scroller = scrollerRef.current;
      if (!scroller) return;
      cancelAnimationFrame(s.raf);
      if (s.mode !== 'spring' && s.mode !== 'dragging') s.pos = scroller.scrollLeft;
      s.target = target;
      s.vel = velocity;
      if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
        s.mode = 'idle';
        s.pos = target;
        apply(target);
        setFree(false);
        return;
      }
      s.mode = 'spring';
      setFree(true);
      s.last = performance.now();
      s.raf = requestAnimationFrame(step);
    },
    [apply, scrollerRef, setFree, step],
  );

  const goTo = useCallback(
    (index: number) => {
      const scroller = scrollerRef.current;
      const track = trackRef.current;
      if (!scroller || !track) return;
      const points = snapPoints(scroller, track);
      springTo(points[clamp(index, 0, points.length - 1)], state.current.mode === 'spring' ? state.current.vel : 0);
    },
    [scrollerRef, springTo, trackRef],
  );

  const currentIndex = useCallback(() => {
    const scroller = scrollerRef.current;
    const track = trackRef.current;
    if (!scroller || !track) return 0;
    const s = state.current;
    return nearestIndex(snapPoints(scroller, track), s.mode === 'spring' ? s.target : scroller.scrollLeft);
  }, [scrollerRef, trackRef]);

  useEffect(() => {
    const scroller = scrollerRef.current;
    const track = trackRef.current;
    if (!scroller || !track) return;
    const s = state.current;
    let scrollRaf = 0;

    const onScroll = () => {
      cancelAnimationFrame(scrollRaf);
      scrollRaf = requestAnimationFrame(() => onIndexRef.current(nearestIndex(snapPoints(scroller, track), scroller.scrollLeft)));
    };
    const onPointerDown = (e: PointerEvent) => {
      if (e.pointerType !== 'mouse' || e.button !== 0) return;
      cancelAnimationFrame(s.raf);
      s.pos = s.mode === 'spring' ? s.pos : scroller.scrollLeft;
      s.mode = 'pressing';
      s.pointerId = e.pointerId;
      s.startX = e.clientX;
      s.startPos = s.pos;
      s.samples = [{ t: e.timeStamp, x: e.clientX }];
    };
    const onPointerMove = (e: PointerEvent) => {
      if (e.pointerId !== s.pointerId) return;
      const dx = e.clientX - s.startX;
      if (s.mode === 'pressing' && Math.abs(dx) > DRAG_THRESHOLD) {
        s.mode = 'dragging';
        scroller.setPointerCapture(e.pointerId);
        scroller.dataset.dragging = 'true';
        setFree(true);
      }
      if (s.mode !== 'dragging') return;
      s.pos = s.startPos - dx;
      apply(s.pos);
      s.samples.push({ t: e.timeStamp, x: e.clientX });
      while (s.samples.length > 2 && e.timeStamp - s.samples[0].t > 100) s.samples.shift();
    };
    const onRelease = (e: PointerEvent) => {
      if (e.pointerId !== s.pointerId) return;
      s.pointerId = -1;
      if (s.mode !== 'dragging') {
        s.mode = 'idle';
        setFree(false);
        return;
      }
      scroller.dataset.dragging = 'false';
      s.suppressClick = true;
      setTimeout(() => (s.suppressClick = false), 0);
      const v = releaseVelocity(s.samples, e.timeStamp);
      const projected = s.pos + (v * DECELERATION) / (1 - DECELERATION);
      const points = snapPoints(scroller, track);
      springTo(points[nearestIndex(points, projected)], v * 1000);
    };
    const onClickCapture = (e: MouseEvent) => {
      if (!s.suppressClick) return;
      e.preventDefault();
      e.stopPropagation();
      s.suppressClick = false;
    };
    const onNativeInput = () => {
      if (s.mode !== 'spring') return;
      cancelAnimationFrame(s.raf);
      s.mode = 'idle';
      apply(clamp(s.pos, 0, maxScroll(scroller)));
      setFree(false);
    };
    const onKeyDown = (e: KeyboardEvent) => {
      if (e.target !== scroller) return;
      const last = track.children.length - 1;
      const index = nearestIndex(snapPoints(scroller, track), s.mode === 'spring' ? s.target : scroller.scrollLeft);
      const next = { ArrowLeft: index - 1, ArrowRight: index + 1, Home: 0, End: last }[e.key];
      if (next === undefined) return;
      e.preventDefault();
      goTo(next);
    };

    scroller.addEventListener('keydown', onKeyDown);
    scroller.addEventListener('scroll', onScroll, { passive: true });
    scroller.addEventListener('pointerdown', onPointerDown);
    scroller.addEventListener('pointermove', onPointerMove);
    scroller.addEventListener('pointerup', onRelease);
    scroller.addEventListener('pointercancel', onRelease);
    scroller.addEventListener('lostpointercapture', onRelease);
    scroller.addEventListener('click', onClickCapture, true);
    scroller.addEventListener('wheel', onNativeInput, { passive: true });
    scroller.addEventListener('touchstart', onNativeInput, { passive: true });
    return () => {
      cancelAnimationFrame(scrollRaf);
      cancelAnimationFrame(s.raf);
      scroller.removeEventListener('keydown', onKeyDown);
      scroller.removeEventListener('scroll', onScroll);
      scroller.removeEventListener('pointerdown', onPointerDown);
      scroller.removeEventListener('pointermove', onPointerMove);
      scroller.removeEventListener('pointerup', onRelease);
      scroller.removeEventListener('pointercancel', onRelease);
      scroller.removeEventListener('lostpointercapture', onRelease);
      scroller.removeEventListener('click', onClickCapture, true);
      scroller.removeEventListener('wheel', onNativeInput);
      scroller.removeEventListener('touchstart', onNativeInput);
    };
  }, [apply, goTo, scrollerRef, setFree, springTo, trackRef]);

  return { goTo, currentIndex };
}

const Chevron = ({ dir }: { dir: 'left' | 'right' }) => (
  <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
    <path d={dir === 'left' ? 'M15 5l-7 7 7 7' : 'M9 5l7 7-7 7'} fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round" />
  </svg>
);

export function ReviewRail() {
  const scrollerRef = useRef<HTMLDivElement>(null);
  const trackRef = useRef<HTMLUListElement>(null);
  const [active, setActive] = useState(0);
  const { goTo, currentIndex } = useMomentumRail(scrollerRef, trackRef, setActive);
  const total = depoimentos.length;

  // começa no segundo depoimento, com vizinhos visíveis dos dois lados
  useEffect(() => {
    const scroller = scrollerRef.current;
    const track = trackRef.current;
    if (!scroller || !track) return;
    const points = snapPoints(scroller, track);
    scroller.scrollLeft = points[1] ?? 0;
  }, []);

  return (
    <div className="rail" aria-roledescription="carrossel" aria-label="Depoimentos de clientes">
      <div className="rail__controls wrap">
        <span className="rail__count" aria-live="polite">
          <span className="sr-only">Depoimento </span>
          {String(active + 1).padStart(2, '0')}
          <span aria-hidden="true"> / </span>
          <span className="sr-only"> de </span>
          {String(total).padStart(2, '0')}
        </span>
        <Glass as="button" type="button" className="rail__arrow" aria-label="Depoimento anterior" aria-disabled={active === 0} onClick={() => goTo(currentIndex() - 1)}>
          <Chevron dir="left" />
        </Glass>
        <Glass as="button" type="button" className="rail__arrow" aria-label="Próximo depoimento" aria-disabled={active === total - 1} onClick={() => goTo(currentIndex() + 1)}>
          <Chevron dir="right" />
        </Glass>
      </div>

      <div ref={scrollerRef} className="rail__scroller" tabIndex={0} aria-label="Depoimentos, use as setas do teclado para navegar">
        <ul ref={trackRef} className="rail__track">
          {depoimentos.map((d) => (
            <li key={d.nome} className="rail__item">
              <div className="rail__tilt">
                <Glass as="figure" className="review bglow" onPointerMove={borderGlowMove}>
                  <BorderGlowLayers />
                  <div className="review__top">
                    <Stars label="5 de 5 estrelas" />
                    <span className="review__src">Google</span>
                  </div>
                  <blockquote>
                    <p>{d.texto}</p>
                  </blockquote>
                  <figcaption className="review__who">
                    <span className="review__avatar" aria-hidden="true">
                      {d.nome[0]}
                    </span>
                    <span>
                      {d.nome}
                      <small>Avaliação no Google</small>
                    </span>
                  </figcaption>
                </Glass>
              </div>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
