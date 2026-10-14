import { Fragment, useRef, type CSSProperties } from 'react';
import { m, useReducedMotion, useScroll, useTransform } from 'motion/react';
import { AGENDAR_HREF, avaliacaoGoogle, contato } from '../content/site';
import { Button } from './Button';
import { Glass } from './Glass';
import { Stars, WhatsAppIcon } from './Icons';
import { Magnetic } from './Motion';

const TITULO = 'Seu momento de se reconectar com você';
const ordem = (i: number) => ({ '--i': i }) as CSSProperties;

export function Hero() {
  const ref = useRef<HTMLElement>(null);
  const reduce = useReducedMotion();
  // Ao rolar para fora do hero: a foto aproxima e desce devagar; o conteúdo sobe, encolhe e esmaece.
  const { scrollYProgress: p } = useScroll({ target: ref, offset: ['start start', 'end start'] });
  const imgY = useTransform(p, [0, 1], ['0%', '22%']);
  const imgScale = useTransform(p, [0, 1], [1, 1.18]);
  const contentY = useTransform(p, [0, 1], [0, -160]);
  const contentScale = useTransform(p, [0, 1], [1, 0.9]);
  const contentOpacity = useTransform(p, [0, 0.65], [1, 0]);
  // filter só quando há desfoque: qualquer filter num ancestral isola o backdrop-filter do vidro dos botões e selos
  const contentBlur = useTransform(p, (v) => (v < 0.02 ? 'none' : `blur(${Math.min(10, v * 14).toFixed(1)}px)`));

  return (
    <section ref={ref} className="hero" id="topo" aria-labelledby="hero-titulo">
      <div className="hero__media" aria-hidden="true">
        <m.img
          className="hero__img"
          src="/img/hero-sala.webp"
          width={876}
          height={871}
          alt=""
          fetchPriority="high"
          style={reduce ? undefined : { y: imgY, scale: imgScale }}
        />
      </div>
      <div className="hero__shade" aria-hidden="true" />
      <div className="grain" aria-hidden="true" />

      <m.div
        className="hero__content wrap"
        style={reduce ? undefined : { y: contentY, scale: contentScale, opacity: contentOpacity, filter: contentBlur }}
      >
        <Glass as="p" className="chip hero__in" style={ordem(0)}>
          O 1º Head Spa de Lages
        </Glass>
        {/* revelação palavra por palavra (CSS, roda antes da hidratação); o h1 segue um texto único para leitores e buscadores */}
        <h1 id="hero-titulo" className="hero__title">
          {TITULO.split(' ').map((palavra, i) => (
            <Fragment key={i}>
              {i > 0 && ' '}
              <span className="w" style={ordem(i)}>
                {palavra}
              </span>
            </Fragment>
          ))}
        </h1>
        <p className="hero__lead hero__in" style={ordem(5)}>
          Cada tratamento é pensado para cuidar de você de forma individual, unindo tecnologia, relaxamento e saúde capilar.
        </p>
        <div className="actions actions--center hero__in" style={ordem(6)}>
          <Magnetic>
            <Button href={AGENDAR_HREF} size="xl" arrow>
              Agendar horário
            </Button>
          </Magnetic>
          <Button href={contato.whatsappDuvida} variant="clear" external>
            <WhatsAppIcon />
            Tirar dúvidas no WhatsApp
          </Button>
        </div>
        <ul className="hero__chips hero__in" style={ordem(7)}>
          <Glass as="li" className="chip">
            <Stars />
            {avaliacaoGoogle.nota.toFixed(1).replace('.', ',')} no Google · {avaliacaoGoogle.total} avaliações
          </Glass>
          <Glass as="li" className="chip">
            Atendimento individual com hora marcada
          </Glass>
        </ul>
      </m.div>
    </section>
  );
}
