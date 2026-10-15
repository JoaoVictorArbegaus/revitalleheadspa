import { useRef, type PointerEvent, type ReactNode } from 'react';
import { m, useReducedMotion, useScroll, useSpring, useTransform } from 'motion/react';
import { AGENDAR_HREF, agendarServico, etapas, pilares, servicos, whatsapp } from '../content/site';
import { BorderGlowLayers, borderGlowMove } from './BorderGlow';
import { FlipCard } from './FlipCard';
import { FlowLines } from './FlowLines';
import { Button } from './Button';
import { Glass } from './Glass';
import { PillarIcon, WhatsAppIcon } from './Icons';
import { EASE, Reveal, ScrollText, SplitHeading, flipUp, inView, useTilt } from './Motion';

const numero = (i: number) => String(i).padStart(2, '0');

function PillarCard({ i, icone, titulo, texto }: { i: number; icone: (typeof pilares)[number]['icone']; titulo: string; texto: string }) {
  const tilt = useTilt(9);
  return (
    <Glass
      as={m.li}
      tone="light"
      className="pillar m bglow"
      {...inView(i, flipUp)}
      {...tilt}
      onPointerMove={(e: PointerEvent<HTMLElement>) => {
        tilt.onPointerMove(e);
        borderGlowMove(e);
      }}
    >
      <BorderGlowLayers />
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

const FlipHint = () => (
  <span className="flip__hint" aria-hidden="true">
    <svg viewBox="0 0 24 24" focusable="false">
      <path d="M4 12a8 8 0 0 1 13.66-5.66L20 8.7M20 4v4.7h-4.7M20 12a8 8 0 0 1-13.66 5.66L4 15.3M4 20v-4.7h4.7" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
    Virar para agendar
  </span>
);

function ServiceFlip({ i, nome, descricao }: { i: number; nome: string; descricao: string }) {
  return (
    <m.li className="service" {...inView(i % 3)}>
      <FlipCard
        label={nome}
        front={
          <div className="service__front">
            <span className="plate__n" aria-hidden="true">
              {numero(i + 2)}
            </span>
            <h3>{nome}</h3>
            <p>{descricao}</p>
            <FlipHint />
          </div>
        }
        back={
          <div className="service__back">
            <p className="plate__tag">Agende online</p>
            <h3>{nome}</h3>
            <p>Escolha o dia e o horário na agenda online. Na primeira visita, o atendimento começa pela consulta.</p>
            <div className="service__actions">
              <Button href={agendarServico(nome)} arrow hoverText="Ver horários livres">
                Agendar
              </Button>
              <Button
                href={whatsapp(`Olá! Vi o site da Revitalle e gostaria de saber mais sobre ${nome}.`)}
                variant="clear"
                size="sm"
                external
                burst
                hoverText={
                  <>
                    <WhatsAppIcon />
                    Abrir conversa
                  </>
                }
              >
                <WhatsAppIcon />
                Tirar dúvidas
              </Button>
            </div>
          </div>
        }
      />
    </m.li>
  );
}

export function Services() {
  return (
    <section className="services" id="servicos" aria-labelledby="servicos-titulo">
      <FlowLines className="flow--light" count={30} angle={-6} />
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

        <Glass as={m.div} tone="light" className="plate plate--feature m" {...inView(0)}>
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
            <Button href={agendarServico('Consulta')} arrow hoverText="Ver horários livres">
              Agendar consulta
            </Button>
        </Glass>

        <ol className="services__grid">
          {servicos.map((s, i) => (
            <ServiceFlip key={s.nome} i={i} {...s} />
          ))}
        </ol>

        <div className="services__foot">
          <Reveal as="p">Valores, duração e horários livres aparecem na agenda online. Os tratamentos não substituem o acompanhamento médico.</Reveal>
          <Reveal i={1}>
            <Button href={AGENDAR_HREF} variant="dark" arrow hoverText="Abrir a agenda">
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
  const reduce = useReducedMotion();
  const mediaRef = useRef<HTMLDivElement>(null);
  const { scrollYProgress: mediaP } = useScroll({ target: mediaRef, offset: ['start end', 'end start'] });
  const segundaY = useTransform(mediaP, [0, 1], [90, -90]);

  return (
    <section className="ritual" id="como-funciona" aria-labelledby="ritual-titulo">
      <div className="wrap ritual__grid">
        <div ref={mediaRef} className="ritual__media">
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
          {/* segunda foto sobreposta, em moldura de vidro, com parallax próprio */}
          <m.figure className="photo ritual__photo2" style={reduce ? undefined : { y: segundaY }}>
            <Glass as={m.div} tone="light" className="ritual__glassframe m" {...inView(2)}>
              <PhotoReveal>
                <img
                  src="/img/hero-sala.webp"
                  width={876}
                  height={871}
                  loading="lazy"
                  decoding="async"
                  alt="Sala de atendimento: maca com lavatório iluminado, prateleiras com plantas e velas acesas"
                />
              </PhotoReveal>
            </Glass>
            <Glass as={m.figcaption} tone="light" className="chip photo__cap m" {...inView(5)}>
              Sala de atendimento
            </Glass>
          </m.figure>
        </div>

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
          {/* PLACEHOLDER: recorte provisório de design/originais/servicos/0.png (arte do Instagram com texto; recorte quadrado sem os textos). Pedir a foto original sem texto. */}
          <PhotoReveal>
            <img
              src="/img/marineuza-q.webp"
              srcSet="/img/marineuza-q-320.webp 320w, /img/marineuza-q.webp 458w"
              sizes="(min-width: 960px) 480px, 92vw"
              width={458}
              height={458}
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
