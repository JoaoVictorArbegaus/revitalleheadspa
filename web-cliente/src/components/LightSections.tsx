import { AGENDAR_HREF, etapas, pilares, servicos } from '../content/site';
import { Button } from './Button';
import { Glass } from './Glass';
import { PillarIcon } from './Icons';

const numero = (i: number) => String(i).padStart(2, '0');

export function Pillars() {
  return (
    <section className="pillars" aria-label="O que define a Revitalle">
      <ul className="wrap pillars__grid" data-reveal-group>
        {pilares.map((p) => (
          <Glass as="li" tone="light" lift className="pillar" data-reveal key={p.titulo}>
            <PillarIcon name={p.icone} />
            <h2>{p.titulo}</h2>
            <p>{p.texto}</p>
          </Glass>
        ))}
      </ul>
    </section>
  );
}

export function Services() {
  return (
    <section className="services" id="servicos" aria-labelledby="servicos-titulo">
      <div className="wrap">
        <header className="section-head" data-reveal-group>
          <p className="label" data-reveal>
            Serviços
          </p>
          <h2 id="servicos-titulo" data-reveal>
            Tratamentos pensados para você
          </h2>
          <p className="section-head__lead" data-reveal>
            Todo atendimento começa pela consulta, que indica o cuidado certo para o seu caso.
          </p>
        </header>

        <ol className="plates" data-reveal-group>
          <Glass as="li" tone="light" lift className="plate plate--feature" data-reveal>
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
          </Glass>
          {servicos.map((s, i) => (
            <Glass as="li" tone="light" lift className={i === servicos.length - 1 ? 'plate plate--wide' : 'plate'} data-reveal key={s.nome}>
              <span className="plate__n" aria-hidden="true">
                {numero(i + 2)}
              </span>
              <h3>{s.nome}</h3>
              <p>{s.descricao}</p>
            </Glass>
          ))}
        </ol>

        <div className="services__foot" data-reveal-group>
          <p data-reveal>Valores, duração e horários livres aparecem na agenda online. Os tratamentos não substituem o acompanhamento médico.</p>
          <Button href={AGENDAR_HREF} variant="dark" data-reveal>
            Ver horários e agendar
          </Button>
        </div>
      </div>
    </section>
  );
}

export function Ritual() {
  return (
    <section className="ritual" id="como-funciona" aria-labelledby="ritual-titulo">
      <div className="wrap ritual__grid">
        <figure className="photo ritual__photo" data-reveal-group>
          <div className="photo__frame" data-reveal>
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
          </div>
          <Glass as="figcaption" tone="light" className="chip photo__cap" data-reveal>
            Recepção · Bairro Beatriz, Lages
          </Glass>
        </figure>

        <div className="ritual__text" data-reveal-group>
          <p className="label" data-reveal>
            Como funciona
          </p>
          <h2 id="ritual-titulo" data-reveal>
            Do acolhimento à escova, cada etapa explicada
          </h2>
          <ol className="timeline">
            {etapas.map((e, i) => (
              <li data-reveal key={e.titulo}>
                <span className="timeline__n" aria-hidden="true">
                  {numero(i + 1)}
                </span>
                <div>
                  <h3>{e.titulo}</h3>
                  <p>{e.texto}</p>
                </div>
              </li>
            ))}
          </ol>
        </div>
      </div>
    </section>
  );
}

export function About() {
  return (
    <section className="about" id="sobre" aria-labelledby="sobre-titulo">
      <div className="wrap about__grid">
        <div className="about__text" data-reveal-group>
          <p className="label" data-reveal>
            Sobre
          </p>
          <h2 id="sobre-titulo" data-reveal>
            Por trás de cada experiência, existe uma história.
          </h2>
          <p data-reveal>
            O Revitalle nasceu da vontade de criar um lugar onde as pessoas pudessem, por algumas horas, desacelerar, respirar fundo e
            lembrar que também merecem ser cuidadas.
          </p>
          <p data-reveal>
            Cada detalhe foi pensado com carinho. O ambiente, os aromas, a música, os cuidados, os protocolos e, principalmente, a forma
            como cada cliente é recebido.
          </p>
          <blockquote className="about__quote" data-reveal>
            <p>
              Porque, para mim, cuidar não é apenas realizar um procedimento. É olhar para cada pessoa, entender o que ela precisa e
              proporcionar uma experiência que vá além do que ela esperava.
            </p>
          </blockquote>
          <p className="signature" data-reveal>
            <strong>Marineuza Branco</strong>
            <span>Fundadora da Revitalle Head Spa e responsável pelos atendimentos</span>
          </p>
        </div>
        <figure className="photo about__photo" data-reveal-group>
          {/* PLACEHOLDER: recorte provisório de design/originais/servicos/0.png (arte do Instagram com texto). Pedir a foto original sem texto. */}
          <div className="photo__frame" data-reveal>
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
          </div>
          <Glass as="figcaption" tone="light" className="chip photo__cap" data-reveal>
            Marineuza Branco · fundadora
          </Glass>
        </figure>
      </div>
    </section>
  );
}
