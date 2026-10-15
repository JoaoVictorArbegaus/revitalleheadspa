import { LazyMotion, MotionConfig, domAnimation, useReducedMotion } from 'motion/react';
import { ReactLenis } from 'lenis/react';
import { About, Pillars, Ritual, Services } from './components/LightSections';
import { Footer, Reviews, Visit, WhatsAppFloat } from './components/DarkSections';
import { FlowLines } from './components/FlowLines';
import { Header } from './components/Header';
import { Waves } from './components/Waves';
import { Hero } from './components/Hero';
import { SvgDefs } from './components/Icons';
import { useLandingEffects } from './hooks/useLandingEffects';
import './styles/landing.css';

export function App() {
  useLandingEffects();
  const reduce = useReducedMotion();

  return (
    // LazyMotion + m.*: carrega só os recursos de animação de DOM usados na página (bundle menor)
    <LazyMotion features={domAnimation} strict>
      <MotionConfig reducedMotion="user">
        {/* rolagem suave com inércia; âncoras descontam o cabeçalho fixo */}
        {!reduce && <ReactLenis root options={{ lerp: 0.085, anchors: { offset: -96 } }} />}
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
            {/* "Como funciona" e "Sobre" dividem o mesmo fundo de ondas suaves */}
            <div className="calm">
              <Waves />
              <Ritual />
              <About />
            </div>
          </div>

          {/* Capítulo escuro contínuo: avaliações, visite e rodapé */}
          <div className="dark">
            <div className="orbs orbs--dark" aria-hidden="true">
              {Array.from({ length: 4 }, (_, i) => (
                <i key={i} />
              ))}
            </div>
            <div className="grain grain--soft" aria-hidden="true" />
            <FlowLines className="flow--dark" count={42} />
            <Reviews />
            <Visit />
            <Footer />
          </div>
        </main>

        <WhatsAppFloat />
      </MotionConfig>
    </LazyMotion>
  );
}
