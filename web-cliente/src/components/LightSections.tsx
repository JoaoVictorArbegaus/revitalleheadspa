import { useRef, type ReactNode } from 'react';
import { m, useScroll, useSpring } from 'motion/react';
import { AGENDAR_HREF, etapas, pilares, servicos } from '../content/site';
import { Button } from './Button';
import { Glass } from './Glass';
import { PillarIcon } from './Icons';
import { EASE, Reveal, ScrollText, SplitHeading, flipUp, inView, useTilt } from './Motion';

const numero = (i: number) => String(i).padStart(2, '0');

function PillarCard({ i, icone, titulo, texto }: { i: number; icone: (typeof pilares)[number]['icone']; titulo: string; texto: string }) {
  const tilt = useTilt(9);
  return (
    <Glass as={m.li} tone="light" className="pillar m" {...inView(i, flipUp)} {...tilt}>
      <PillarIcon name={icone} />
      <h2>{titulo}</h2>
      <p>{texto}</p>
    </Glass>
  );
}

export function Pillars() {
  return (
    <section className="pillars" aria-label="O que define a Revitalle">
      <ul className="wrap pillars__grid">
        {pilares.map((p, i) => (
          <PillarCard key={p.titulo} i={i} {...p} />
        ))}
      </ul>
    </section>
  );
}

function Plate({ i, className = 'plate', children }: { i: number; className?: string; children: ReactNode }) {
  const tilt = useTilt(5);
  return (
    <Glass as={m.li} tone="light" className={`${className} m`} {...inView(i % 4)} {...tilt}>
      {children}
    </Glass>
  );
}

export function Services() {
  return (
    <section className="services" id="servicos" aria-labelledby="servicos-titulo">
      <div className="wrap">
        <header className="section-head">
          <Reveal as="p" className="label">
            Serviços
          </Reveal>
          <SplitHeading id="servicos-titulo" text="Tratamentos pensados para você" />
          <Reveal as="p" i={2} className="section-head__lead">
            Todo atendimento começa pela consulta, que indica o cuidado certo para o seu caso.
          </Reveal>
        </header>

        <ol className="plates">
          <Plate i={0} className="plate plate--feature">
            <span className="plate__n" aria-hidden="true">
              01
            </span>
            <div>
              <p className="plate__tag">O primeiro passo</p>
              <h3>Consulta</h3>
              <p>
                Avaliação individual e personalizada, com análise do couro cabeludo e dos fios, para indicar o tratamento ideal. É o
                primeiro passo antes de qualquer tratamento.
              </p>
            </div>
            <Button href={AGENDAR_HREF}>Agendar consulta</Button>
          </Plate>
          {servicos.map((s, i) => (
            <Plate key={s.nome} i={i + 1} className={i === servicos.length - 1 ? 'plate plate--wide' : 'plate'}>
              <span className="plate__n" aria-hidden="true">
                {numero(i + 2)}
              </span>
              <h3>{s.nome}</h3>
              <p>{s.descricao}</p>
            </Plate>
          ))}
        </ol>

        <div className="services__foot">
          <Reveal as="p">Valores, duração e horários livres aparecem na agenda online. Os tratamentos não substituem o acompanhamento médico.</Reveal>
          <Reveal i={1}>
            <Button href={AGENDAR_HREF} variant="dark">
              Ver horários e agendar
            </Button>
          </Reveal>
        </div>
      </div>
    </section>
  );
}

/** Foto que se abre de dentro para fora (máscara) enquanto a imagem desacelera o zoom. */
function PhotoReveal({ children }: { children: ReactNode }) {
  return (
    <m.div
      className="photo__frame"
      initial={{ clipPath: 'inset(16% 16% 16% 16% round 28px)' }}
      whileInView={{ clipPath: 'inset(0% 0% 0% 0% round 20px)' }}
      viewport={{ once: true, amount: 0.35 }}
      transition={{ duration: 1.4, ease: EASE }}
    >
      <m.div
        initial={{ scale: 1.35 }}
        whileInView={{ scale: 1 }}
        viewport={{ once: true, amount: 0.35 }}
        transition={{ duration: 1.8, ease: EASE }}
      >
        {children}
      </m.div>
    </m.div>
  );
}

export function Ritual() {
  const listRef = useRef<HTMLDivElement>(null);
  // a linha da linha do tempo se desenha conforme a rolagem
  const { scrollYProgress } = useScroll({ target: listRef, offset: ['start 0.8', 'end 0.55'] });
  const progress = useSpring(scrollYProgress, { stiffness: 90, damping: 24 });

  return (
    <section className="ritual" id="como-funciona" aria-labelledby="ritual-titulo">
      <div className="wrap ritual__grid">
        <figure className="photo ritual__photo">
          <PhotoReveal>
            <picture>
              <source media="(max-width: 599px)" srcSet="/img/recepcao-480.webp" width={480} height={640} />
              <img
                src="/img/recepcao.webp"
                width={683}
                height={910}
                loading="lazy"
                decoding="async"
                alt="Recepção da Revitalle Head Spa: painel de madeira ripada com o logo dourado, paredes verdes, mesa branca com pés dourados e poltrona creme"
              />
            </picture>
          </PhotoReveal>
          <Glass as={m.figcaption} tone="light" className="chip photo__cap m" {...inView(4)}>
            Recepção · Bairro Beatriz, Lages
          </Glass>
        </figure>

        <div className="ritual__text">
          <Reveal as="p" className="label">
            Como funciona
          </Reveal>
          <SplitHeading id="ritual-titulo" text="Do acolhimento à escova, cada etapa explicada" />
          <div ref={listRef} className="timeline">
            <m.span className="timeline__progress" aria-hidden="true" style={{ scaleY: progress }} />
            <ol>
            {etapas.map((e, i) => (
              <m.li key={e.titulo} {...inView(i)}>
                <span className="timeline__n" aria-hidden="true">
                  {numero(i + 1)}
                </span>
                <div>
                  <h3>{e.titulo}</h3>
                  <p>{e.texto}</p>
                </div>
              </m.li>
            ))}
            </ol>
          </div>
        </div>
      </div>
    </section>
  );
}

export function About() {
  return (
    <section className="about" id="sobre" aria-labelledby="sobre-titulo">
      <div className="wrap about__grid">
        <div className="about__text">
          <Reveal as="p" className="label">
            Sobre
          </Reveal>
          <SplitHeading id="sobre-titulo" text="Por trás de cada experiência, existe uma história." />
          <Reveal as="p" i={1}>
            O Revitalle nasceu da vontade de criar um lugar onde as pessoas pudessem, por algumas horas, desacelerar, respirar fundo e
            lembrar que também merecem ser cuidadas.
          </Reveal>
          <Reveal as="p" i={2}>
            Cada detalhe foi pensado com carinho. O ambiente, os aromas, a música, os cuidados, os protocolos e, principalmente, a forma
            como cada cliente é recebido.
          </Reveal>
          <blockquote className="about__quote">
            <ScrollText text="Porque, para mim, cuidar não é apenas realizar um procedimento. É olhar para cada pessoa, entender o que ela precisa e proporcionar uma experiência que vá além do que ela esperava." />
          </blockquote>
          <Reveal as="p" className="signature">
            <strong>Marineuza Branco</strong>
            <span>Fundadora da Revitalle Head Spa e responsável pelos atendimentos</span>
          </Reveal>
        </div>
        <figure className="photo about__photo">
          {/* PLACEHOLDER: recorte provisório de design/originais/servicos/0.png (arte do Instagram com texto). Pedir a foto original sem texto. */}
          <PhotoReveal>
            <img
              src="/img/marineuza.webp"
              srcSet="/img/marineuza-440.webp 440w, /img/marineuza.webp 658w"
              sizes="(min-width: 960px) 540px, 92vw"
              width={658}
              height={456}
              loading="lazy"
              decoding="async"
              alt="Marineuza Branco, de jaleco branco com o logo da Revitalle, preparando um produto para o tratamento"
            />
          </PhotoReveal>
          <Glass as={m.figcaption} tone="light" className="chip photo__cap m" {...inView(4)}>
            Marineuza Branco · fundadora
          </Glass>
        </figure>
      </div>
    </section>
  );
}
