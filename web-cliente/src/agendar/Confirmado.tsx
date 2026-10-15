import { whatsapp } from '../content/site';
import { WhatsAppIcon } from '../components/Icons';
import type { Confirmacao } from './api';
import { diaPorExtenso, hora } from './formato';
import { Recibo } from './Recibo';
import { TituloEtapa } from './ui';

export function Confirmado({ c, nome, onNovo }: { c: Confirmacao; nome: string; onNovo: () => void }) {
  const quando = `${diaPorExtenso(c.inicioEm.slice(0, 10))}, às ${hora(c.inicioEm)}`;
  const primeiroNome = nome.trim().split(/\s+/)[0];
  const msg = whatsapp(`Olá! Agendei pelo site: ${c.servico.nome} em ${quando.toLowerCase()}, em nome de ${nome.trim()}.`);

  return (
    <div className="ag-done">
      <TituloEtapa titulo={`Tudo certo, ${primeiroNome}!`} descricao="Seu horário está confirmado. Estamos te esperando." focar />

      <Recibo c={c} nome={nome} />

      <p className="ag-note">Precisa remarcar ou cancelar? Fale com a gente pelo WhatsApp, de preferência com um dia de antecedência.</p>

      <div className="ag-done__actions">
        <a className="btn btn--wa" href={msg} target="_blank" rel="noopener">
          <span className="btn__label">
            <span className="btn__text">
              <WhatsAppIcon /> Avisar no WhatsApp
            </span>
            <span className="btn__text btn__text--hover" aria-hidden="true">
              <WhatsAppIcon /> Mensagem pronta
            </span>
          </span>
        </a>
      </div>
      <p className="ag-done__more">
        <button type="button" className="ag-link" onClick={onNovo}>
          Agendar outro horário
        </button>
        <span aria-hidden="true">·</span>
        <a className="ag-link" href="/">
          Voltar ao site
        </a>
      </p>
    </div>
  );
}
