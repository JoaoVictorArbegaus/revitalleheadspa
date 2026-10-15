import { m } from 'motion/react';
import { contato, whatsapp } from '../content/site';
import { WhatsAppIcon } from '../components/Icons';
import { EASE } from '../components/Motion';
import type { Confirmacao } from './api';
import { diaPorExtenso, dinheiro, duracao, hora } from './formato';
import { Botao, Icone, TituloEtapa } from './ui';

/** Arquivo .ics para a cliente guardar o horário na agenda do celular. */
function baixarIcs(c: Confirmacao) {
  const utc = (iso: string) => new Date(iso).toISOString().replace(/[-:]/g, '').replace(/\.\d{3}/, '');
  const esc = (s: string) => s.replace(/([,;\\])/g, '\\$1');
  const linhas = [
    'BEGIN:VCALENDAR',
    'VERSION:2.0',
    'PRODID:-//Revitalle Head Spa//Agendamento//PT',
    'BEGIN:VEVENT',
    `UID:${utc(c.inicioEm)}-${c.servico.id}@revitalle`,
    `DTSTAMP:${utc(new Date().toISOString())}`,
    `DTSTART:${utc(c.inicioEm)}`,
    `DTEND:${utc(c.fimServicoEm)}`,
    `SUMMARY:${esc(`${c.servico.nome} - Revitalle Head Spa`)}`,
    `LOCATION:${esc(`${contato.endereco} - ${contato.bairroCidade}`)}`,
    `DESCRIPTION:${esc(`Atendimento com ${c.profissional.nome}. Para remarcar ou cancelar: WhatsApp ${contato.telefone}.`)}`,
    'END:VEVENT',
    'END:VCALENDAR',
  ];
  const url = URL.createObjectURL(new Blob([linhas.join('\r\n')], { type: 'text/calendar;charset=utf-8' }));
  const a = Object.assign(document.createElement('a'), { href: url, download: 'revitalle-agendamento.ics' });
  a.click();
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

export function Confirmado({ c, nome, onNovo }: { c: Confirmacao; nome: string; onNovo: () => void }) {
  const quando = `${diaPorExtenso(c.inicioEm.slice(0, 10))}, às ${hora(c.inicioEm)}`;
  const primeiroNome = nome.trim().split(/\s+/)[0];
  const msg = whatsapp(`Olá! Agendei pelo site: ${c.servico.nome} em ${quando.toLowerCase()}, em nome de ${nome.trim()}.`);

  return (
    <div className="ag-done">
      <m.div
        className="ag-done__seal"
        initial={{ scale: 0.6, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        transition={{ type: 'spring', stiffness: 180, damping: 14 }}
        aria-hidden="true"
      >
        <svg viewBox="0 0 24 24">
          <m.path
            d="M5 12.5l4.2 4.2L19 7"
            fill="none"
            stroke="currentColor"
            strokeWidth={2}
            strokeLinecap="round"
            strokeLinejoin="round"
            initial={{ pathLength: 0 }}
            animate={{ pathLength: 1 }}
            transition={{ duration: 0.6, ease: EASE, delay: 0.25 }}
          />
        </svg>
      </m.div>
      <TituloEtapa titulo={`Tudo certo, ${primeiroNome}!`} descricao="Seu horário está confirmado. Estamos te esperando." focar />

      <dl className="ag-ticket">
        <div>
          <dt>Tratamento</dt>
          <dd>{c.servico.nome}</dd>
        </div>
        <div>
          <dt>Quando</dt>
          <dd>{quando}</dd>
        </div>
        <div>
          <dt>Duração</dt>
          <dd>
            {duracao(c.servico.duracaoMinutos)}, até {hora(c.fimServicoEm)}
          </dd>
        </div>
        <div>
          <dt>Com</dt>
          <dd>{c.profissional.nome}</dd>
        </div>
        <div>
          <dt>Valor</dt>
          <dd>{dinheiro(c.precoContratado, c.moedaContratada)}</dd>
        </div>
        <div>
          <dt>Onde</dt>
          <dd>
            {contato.endereco}, {contato.bairroCidade.split(' · ')[0]}
            <a className="ag-link" href={contato.comoChegarHref} target="_blank" rel="noopener">
              Como chegar
            </a>
          </dd>
        </div>
      </dl>

      <p className="ag-note">Precisa remarcar ou cancelar? Fale com a gente pelo WhatsApp, de preferência com um dia de antecedência.</p>

      <div className="ag-done__actions">
        <Botao variante="dark" onClick={() => baixarIcs(c)} textoHover="Arquivo .ics">
          <Icone nome="baixar" /> Salvar na agenda
        </Botao>
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
