import { useEffect, useState } from 'react';
import { m } from 'motion/react';
import { AGENDAR_HREF, avaliacaoGoogle, contato, horarios, navegacao } from '../content/site';
import { Button } from './Button';
import { CountUp } from './CountUp';
import { Glass } from './Glass';
import { Stars, WhatsAppIcon } from './Icons';
import { Magnetic, Reveal, SplitHeading, inView } from './Motion';
import { ReviewRail } from './ReviewRail';

export function Reviews() {
  return (
    <section className="reviews" id="avaliacoes" aria-labelledby="avaliacoes-titulo">
      <div className="wrap reviews__head">
        <Reveal as="p" className="label label--dark">
          Avaliações
        </Reveal>
        <SplitHeading id="avaliacoes-titulo" text="Quem passou por aqui" />
        <Reveal i={2} className="reviews__bar">
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
          <Magnetic>
            <Button href={AGENDAR_HREF} size="xl" arrow hoverText="Escolher meu horário">
              Agendar minha experiência
            </Button>
          </Magnetic>
        </Reveal>
      </div>
      <m.div {...inView(3)}>
        <ReviewRail />
      </m.div>
    </section>
  );
}

export function Visit() {
  return (
    <section className="visit" id="visite" aria-labelledby="visite-titulo">
      <div className="wrap visit__grid">
        <div className="visit__text">
          <Reveal as="p" className="label label--dark">
            Agende sua visita
          </Reveal>
          <SplitHeading id="visite-titulo" text="Um espaço criado para cuidar de você por inteiro." />
          <Reveal as="p" i={1}>
            Escolha o tratamento e o horário na agenda online. Se preferir, fale com a gente pelo WhatsApp.
          </Reveal>
          <Reveal i={2} className="actions">
            <Magnetic>
              <Button href={AGENDAR_HREF} size="xl" arrow hoverText="Ver horários livres">
                Agendar horário
              </Button>
            </Magnetic>
            <Button href={contato.whatsappAgendar} variant="clear" external burst hoverText={<><WhatsAppIcon />Abrir conversa</>}>
              <WhatsAppIcon />
              Falar no WhatsApp
            </Button>
          </Reveal>
        </div>

        <Glass as={m.div} className="info m" {...inView(2)} data-reveal-group>
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
