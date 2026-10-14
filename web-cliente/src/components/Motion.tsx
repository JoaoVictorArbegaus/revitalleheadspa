import { Fragment, useRef, type ElementType, type PointerEvent, type ReactNode } from 'react';
import {
  m,
  useMotionValue,
  useReducedMotion,
  useScroll,
  useSpring,
  useTransform,
  type MotionValue,
  type Variants,
} from 'motion/react';

export const EASE = [0.16, 1, 0.3, 1] as const;
const VIEWPORT = { once: true, amount: 0.25 } as const;

/** Entrada ao rolar: sobe, perde o desfoque e ganha opacidade. `i` encadeia irmãos. */
export const rise: Variants = {
  hidden: { opacity: 0, y: 56, filter: 'blur(12px)' },
  // transitionEnd remove o filter: um filter residual isolaria o backdrop-filter do vidro dentro do elemento
  show: (i = 0) => ({
    opacity: 1,
    y: 0,
    filter: 'blur(0px)',
    transition: { duration: 1.1, ease: EASE, delay: i * 0.09 },
    transitionEnd: { filter: 'none' },
  }),
};

/** Entrada com dobra 3D: o elemento "se levanta" da mesa. */
export const flipUp: Variants = {
  hidden: { opacity: 0, y: 80, rotateX: -32, scale: 0.94 },
  show: (i = 0) => ({
    opacity: 1,
    y: 0,
    rotateX: 0,
    scale: 1,
    transition: { type: 'spring', stiffness: 70, damping: 16, mass: 1, delay: i * 0.12 },
  }),
};

/** Props prontas para um motion.* entrar ao ficar visível. */
export const inView = (i = 0, variants: Variants = rise) =>
  ({ variants, initial: 'hidden', whileInView: 'show', viewport: VIEWPORT, custom: i }) as const;

export function Reveal({ as = 'div', i = 0, className, children }: { as?: 'div' | 'p' | 'li' | 'header'; i?: number; className?: string; children: ReactNode }) {
  const Tag = m[as] as ElementType;
  return (
    <Tag className={className} {...inView(i)}>
      {children}
    </Tag>
  );
}

/** Título que sobe palavra por palavra de dentro de uma máscara. */
export function SplitHeading({ as = 'h2', id, text, className }: { as?: 'h1' | 'h2'; id?: string; text: string; className?: string }) {
  const Tag = m[as] as ElementType;
  const words = text.split(' ');
  return (
    <Tag
      id={id}
      className={className}
      initial="hidden"
      whileInView="show"
      viewport={{ once: true, amount: 0.6 }}
      transition={{ staggerChildren: 0.06 }}
    >
      {words.map((w, i) => (
        // o espaço fica fora da máscara: dentro de um inline-block ele seria descartado no fim da linha
        <Fragment key={i}>
          <span className="split-w">
            <m.span
              className="split-w__in"
              variants={{ hidden: { y: '110%', rotate: 4 }, show: { y: '0%', rotate: 0, transition: { duration: 1, ease: EASE } } }}
            >
              {w}
            </m.span>
          </span>
          {i < words.length - 1 && ' '}
        </Fragment>
      ))}
    </Tag>
  );
}

/** Inclinação 3D que segue o ponteiro, com mola. Retorna handlers e estilo para um motion.*. */
export function useTilt(max = 8) {
  const reduce = useReducedMotion();
  const px = useMotionValue(0.5);
  const py = useMotionValue(0.5);
  const spring = { stiffness: 160, damping: 18, mass: 0.6 };
  const rotateX = useSpring(useTransform(py, [0, 1], [max, -max]), spring);
  const rotateY = useSpring(useTransform(px, [0, 1], [-max, max]), spring);

  const onPointerMove = (e: PointerEvent<HTMLElement>) => {
    if (reduce || e.pointerType !== 'mouse') return;
    const r = e.currentTarget.getBoundingClientRect();
    px.set((e.clientX - r.left) / r.width);
    py.set((e.clientY - r.top) / r.height);
  };
  const onPointerLeave = () => {
    px.set(0.5);
    py.set(0.5);
  };
  return { onPointerMove, onPointerLeave, style: { rotateX, rotateY, transformPerspective: 900 } };
}

/** CTA magnético: o botão é atraído pelo cursor e volta com mola. */
export function Magnetic({ children, strength = 0.35 }: { children: ReactNode; strength?: number }) {
  const reduce = useReducedMotion();
  const x = useSpring(0, { stiffness: 220, damping: 15, mass: 0.4 });
  const y = useSpring(0, { stiffness: 220, damping: 15, mass: 0.4 });
  return (
    <m.span
      className="magnetic"
      style={{ x, y }}
      onPointerMove={(e) => {
        if (reduce || e.pointerType !== 'mouse') return;
        const r = e.currentTarget.getBoundingClientRect();
        x.set((e.clientX - (r.left + r.width / 2)) * strength);
        y.set((e.clientY - (r.top + r.height / 2)) * strength);
      }}
      onPointerLeave={() => {
        x.set(0);
        y.set(0);
      }}
    >
      {children}
    </m.span>
  );
}

function ScrollWord({ word, progress, range }: { word: string; progress: MotionValue<number>; range: [number, number] }) {
  const opacity = useTransform(progress, range, [0.16, 1]);
  const y = useTransform(progress, range, [6, 0]);
  return <m.span style={{ opacity, y, display: 'inline-block' }}>{word}</m.span>;
}

/** Texto que "acende" palavra por palavra conforme a rolagem. */
export function ScrollText({ text, className }: { text: string; className?: string }) {
  const ref = useRef<HTMLParagraphElement>(null);
  const { scrollYProgress } = useScroll({ target: ref, offset: ['start 0.85', 'end 0.45'] });
  const words = text.split(' ');
  return (
    <p ref={ref} className={className}>
      {words.map((w, i) => (
        <span key={i}>
          <ScrollWord word={w} progress={scrollYProgress} range={[i / words.length, (i + 1) / words.length]} />
          {i < words.length - 1 && ' '}
        </span>
      ))}
    </p>
  );
}
