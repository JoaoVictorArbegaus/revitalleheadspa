import { About, Pillars, Ritual, Services } from './components/LightSections';
import { Footer, Reviews, Visit, WhatsAppFloat } from './components/DarkSections';
import { Header } from './components/Header';
import { Hero } from './components/Hero';
import { SvgDefs } from './components/Icons';
import { useLandingEffects } from './hooks/useLandingEffects';
import './styles/landing.css';

export function App() {
  useLandingEffects();

  return (
    <>
      <a className="skip" href="#conteudo">
        Pular para o conteúdo
      </a>
      <SvgDefs />
      <Header />

      <main id="conteudo">
        <Hero />

        {/* Seções claras: fundo contínuo com manchas de luz, sem emendas */}
        <div className="light">
          <div className="orbs orbs--light" aria-hidden="true">
            {Array.from({ length: 7 }, (_, i) => (
              <i key={i} />
            ))}
          </div>
          <Pillars />
          <Services />
          <Ritual />
          <About />
        </div>

        {/* Capítulo escuro contínuo: avaliações, visite e rodapé */}
        <div className="dark">
          <div className="orbs orbs--dark" aria-hidden="true">
            {Array.from({ length: 4 }, (_, i) => (
              <i key={i} />
            ))}
          </div>
          <div className="grain grain--soft" aria-hidden="true" />
          <Reviews />
          <Visit />
          <Footer />
        </div>
      </main>

      <WhatsAppFloat />
    </>
  );
}
