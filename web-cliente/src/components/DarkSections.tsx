import { useEffect, useRef, useState } from 'react';
import { AGENDAR_HREF, avaliacaoGoogle, contato, depoimentos, horarios, navegacao } from '../content/site';
import { prefersReducedMotion } from '../hooks/useLandingEffects';
import { Button } from './Button';
import { CountUp } from './CountUp';
import { Glass } from './Glass';
import { Stars, WhatsAppIcon } from './Icons';

function ReviewCard({ nome, texto, copia = false }: { nome: string; texto: string; copia?: boolean }) {
  return (
    <Glass as="li" className="review" aria-hidden={copia || undefined}>
      <div className="review__top">
        <Stars label="5 de 5 estrelas" />
        <span className="review__src">Google</span>
      </div>
      <blockquote>
        <p>{texto}</p>
      </blockquote>
      <p className="review__who">
        <span className="review__avatar" aria-hidden="true">
          {nome[0]}
        </span>
        <span>
          {nome}
          <small>Avaliação no Google</small>
        </span>
      </p>
    </Glass>
  );
}

/** Faixa contínua: a segunda cópia (oculta de leitores de tela) fecha o loop sem emenda; pausa no hover e no foco. */
function Marquee() {
  const ref = useRef<HTMLDivElement>(null);
  const [rodando, setRodando] = useState(false);

  useEffect(() => {
    const el = ref.current;
    if (!el || prefersReducedMotion()) return;
    const track = el.querySelector<HTMLElement>('.marquee__track')!;
    // medido antes da cópia: uma volta completa a ~40px/s
    el.style.setProperty('--marquee-dur', `${Math.round(track.scrollWidth / 40)}s`);
    setRodando(true);
  }, []);

  return (
    <div ref={ref} className={rodando ? 'marquee is-running' : 'marquee'} data-reveal>
      <ul className="marquee__track" aria-label="Depoimentos de clientes">
        {depoimentos.map((d) => (
          <ReviewCard key={d.nome} {...d} />
        ))}
        {rodando && depoimentos.map((d) => <ReviewCard key={`${d.nome}-copia`} {...d} copia />)}
      </ul>
    </div>
  );
}

export function Reviews() {
  return (
    <section className="reviews" id="avaliacoes" aria-labelledby="avaliacoes-titulo">
      <div className="wrap reviews__head" data-reveal-group>
        <p className="label label--dark" data-reveal>
          Avaliações
        </p>
        <h2 id="avaliacoes-titulo" data-reveal>
          Quem passou por aqui
        </h2>
        <div className="reviews__bar" data-reveal>
          <Glass className="score">
            <p className="score__n">
              <CountUp to={avaliacaoGoogle.nota} decimals={1} />
            </p>
            <div>
              <Stars large />
              <p>
                no Google, com <CountUp to={avaliacaoGoogle.total} /> avaliações
              </p>
            </div>
          </Glass>
          <Button href={AGENDAR_HREF} size="xl" arrow>
            Agendar minha experiência
          </Button>
        </div>
      </div>
      <Marquee />
    </section>
  );
}

export function Visit() {
  return (
    <section className="visit" id="visite" aria-labelledby="visite-titulo">
      <div className="wrap visit__grid">
        <div className="visit__text" data-reveal-group>
          <p className="label label--dark" data-reveal>
            Agende sua visita
          </p>
          <h2 id="visite-titulo" data-reveal>
            Um espaço criado para cuidar de você por inteiro.
          </h2>
          <p data-reveal>Escolha o tratamento e o horário na agenda online. Se preferir, fale com a gente pelo WhatsApp.</p>
          <div className="actions" data-reveal>
            <Button href={AGENDAR_HREF} size="xl" arrow>
              Agendar horário
            </Button>
            <Button href={contato.whatsappAgendar} variant="clear" external>
              <WhatsAppIcon />
              Falar no WhatsApp
            </Button>
          </div>
        </div>

        <Glass className="info" data-reveal-group>
          <div className="info__block" data-reveal>
            <h3>Endereço</h3>
            <address>
              {contato.endereco}
              <br />
              {contato.bairroCidade}
            </address>
            <a className="link" href={contato.comoChegarHref} target="_blank" rel="noopener">
              Como chegar <span aria-hidden="true">→</span>
            </a>
          </div>
          <div className="info__block" data-reveal>
            <h3>Horários</h3>
            <table className="hours">
              <caption className="sr-only">Horário de funcionamento</caption>
              <tbody>
                {horarios.map((h) => (
                  <tr key={h.dias}>
                    <th scope="row">{h.dias}</th>
                    <td>{h.horario}</td>
                  </tr>
                ))}
              </tbody>
            </table>
            <p className="note">Atendimento com hora marcada.</p>
          </div>
          <div className="info__block" data-reveal>
            <h3>Contato</h3>
            <ul className="contact">
              <li>
                <a className="link" href={contato.telefoneHref}>
                  {contato.telefone}
                </a>
              </li>
              <li>
                <a className="link" href={contato.whatsappInfo} target="_blank" rel="noopener">
                  WhatsApp
                </a>
              </li>
              <li>
                <a className="link" href={contato.instagramHref} target="_blank" rel="noopener">
                  {contato.instagram}
                </a>
              </li>
            </ul>
          </div>
        </Glass>
      </div>
    </section>
  );
}

export function Footer() {
  return (
    <footer className="site-footer">
      <div className="wrap footer__grid">
        <a className="footer__brand" href="#topo">
          <img src="/img/logo-v-dourado.webp" width={212} height={168} alt="Revitalle Head Spa" loading="lazy" />
        </a>
        <p className="footer__claim">O 1º Head Spa de Lages</p>
        <nav className="footer__nav" aria-label="Rodapé">
          {navegacao.slice(0, 4).map((item) => (
            <a key={item.href} href={item.href}>
              {item.rotulo}
            </a>
          ))}
          <a href={AGENDAR_HREF}>Agendar</a>
        </nav>
        <p className="footer__legal">© 2026 Revitalle Head Spa · R. Euclídes Cardoso, 141, Beatriz, Lages - SC</p>
      </div>
    </footer>
  );
}

/** Some quando o bloco final, que já tem WhatsApp, está visível. */
export function WhatsAppFloat() {
  const [oculto, setOculto] = useState(false);

  useEffect(() => {
    const visit = document.getElementById('visite');
    if (!visit || !('IntersectionObserver' in window)) return;
    const io = new IntersectionObserver(([entry]) => setOculto(entry.isIntersecting), { threshold: 0.3 });
    io.observe(visit);
    return () => io.disconnect();
  }, []);

  return (
    <Glass
      as="a"
      tone="wa"
      className={oculto ? 'wa-float is-hidden' : 'wa-float'}
      href={contato.whatsappInfo}
      target="_blank"
      rel="noopener"
      aria-label="Conversar com a Revitalle Head Spa no WhatsApp"
    >
      <svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
        <use href="#i-wa" />
      </svg>
    </Glass>
  );
}
