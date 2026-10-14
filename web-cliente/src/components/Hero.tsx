import { Fragment, type CSSProperties } from 'react';
import { AGENDAR_HREF, avaliacaoGoogle, contato } from '../content/site';
import { Button } from './Button';
import { Glass } from './Glass';
import { Stars, WhatsAppIcon } from './Icons';

const TITULO = 'Seu momento de se reconectar com você';
const ordem = (i: number) => ({ '--i': i }) as CSSProperties;

export function Hero() {
  return (
    <section className="hero" id="topo" aria-labelledby="hero-titulo">
      <div className="hero__media" aria-hidden="true">
        <img className="hero__img" src="/img/hero-sala.webp" width={876} height={871} alt="" fetchPriority="high" data-parallax />
      </div>
      <div className="hero__shade" aria-hidden="true" />
      <div className="grain" aria-hidden="true" />

      <div className="hero__content wrap">
        <Glass as="p" className="chip hero__in" style={ordem(0)}>
          O 1º Head Spa de Lages
        </Glass>
        {/* revelação palavra por palavra; o h1 continua um texto único para leitores de tela e buscadores */}
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
          <Button href={AGENDAR_HREF} size="xl" arrow>
            Agendar horário
          </Button>
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
      </div>
    </section>
  );
}
