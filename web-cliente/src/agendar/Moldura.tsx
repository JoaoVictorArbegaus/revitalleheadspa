import { contato } from '../content/site';
import { Button } from '../components/Button';
import { Glass } from '../components/Glass';
import { WhatsAppIcon } from '../components/Icons';
import type { Horario, Servico } from './api';
import { diaCurto, diaPorExtenso, dinheiro, duracao, hora } from './formato';
import { Icone } from './ui';

export type Etapa = 'servico' | 'horario' | 'dados' | 'revisao' | 'concluido';

export function Cabecalho() {
  return (
    <header className="site-header">
      <Glass className="nav">
        <div className="nav__bar">
          <a className="nav__brand" href="/">
            <img src="/img/logo-h-dourado.webp" width={473} height={160} alt="Revitalle Head Spa: voltar ao site" />
          </a>
          <a className="ag-home" href="/">
            <Icone nome="voltar" />
            <span>Voltar ao site</span>
          </a>
          <Button href={contato.whatsappAgendar} variant="clear" size="sm" external hoverText="Falar agora" aria-label="Tirar dúvidas no WhatsApp">
            <WhatsAppIcon /> <span className="ag-hide-xs">Dúvidas?</span>
          </Button>
        </div>
      </Glass>
    </header>
  );
}

const ETAPAS: { id: Etapa; rotulo: string }[] = [
  { id: 'servico', rotulo: 'Tratamento' },
  { id: 'horario', rotulo: 'Dia e horário' },
  { id: 'dados', rotulo: 'Seus dados' },
  { id: 'revisao', rotulo: 'Confirmação' },
];

/** Trilha das etapas. Etapas já cumpridas viram atalhos para voltar. */
export function Trilha({ etapa, alcancada, onIr }: { etapa: Etapa; alcancada: number; onIr: (e: Etapa) => void }) {
  const atual = etapa === 'concluido' ? ETAPAS.length : ETAPAS.findIndex((e) => e.id === etapa);
  return (
    <nav aria-label="Etapas do agendamento">
      <ol className="ag-steps">
        {ETAPAS.map((e, i) => {
          const feito = i < atual;
          const clicavel = i !== atual && i <= alcancada && etapa !== 'concluido';
          const conteudo = (
            <>
              <span className="ag-steps__n">{feito ? <Icone nome="ok" /> : i + 1}</span>
              <span className="ag-steps__rotulo">{e.rotulo}</span>
            </>
          );
          return (
            <li key={e.id} className={[feito && 'is-done', i === atual && 'is-current'].filter(Boolean).join(' ')}>
              {clicavel ? (
                <button type="button" onClick={() => onIr(e.id)}>
                  {conteudo}
                  <span className="sr-only">{feito ? ' (concluída, voltar)' : ''}</span>
                </button>
              ) : (
                <span aria-current={i === atual ? 'step' : undefined}>{conteudo}</span>
              )}
            </li>
          );
        })}
      </ol>
    </nav>
  );
}

/** Resumo que se completa a cada escolha: lateral no desktop, faixa compacta no celular. */
export function Resumo({ servico, dia, horario, nome }: { servico: Servico | null; dia: string | null; horario: Horario | null; nome: string }) {
  const quando = horario ? `${diaPorExtenso(horario.inicioEm.slice(0, 10))}, ${hora(horario.inicioEm)}` : dia ? diaPorExtenso(dia) : null;
  return (
    <Glass as="aside" tone="light" className="ag-summary" aria-label="Resumo do agendamento">
        <p className="label">Seu horário</p>
        <dl>
          <div className={servico ? '' : 'is-empty'}>
            <dt>Tratamento</dt>
            <dd>{servico ? servico.nome : 'A escolher'}</dd>
          </div>
          <div className={quando ? '' : 'is-empty'}>
            <dt>Quando</dt>
            <dd>{quando ?? 'A escolher'}</dd>
          </div>
          {servico && (
            <div>
              <dt>Duração</dt>
              <dd>
                {duracao(servico.duracaoMinutos)}
                {horario && `, até ${hora(horario.fimServicoEm)}`}
              </dd>
            </div>
          )}
          {servico && (
            <div>
              <dt>Com</dt>
              <dd>{servico.profissionalNome}</dd>
            </div>
          )}
          {nome.trim() && (
            <div>
              <dt>Em nome de</dt>
              <dd>{nome.trim()}</dd>
            </div>
          )}
        </dl>
        {servico && (
          <p className="ag-summary__total">
            <span>Valor</span>
            <strong>{dinheiro(servico.preco, servico.moeda)}</strong>
          </p>
        )}
        <p className="ag-summary__where">
          <Icone nome="local" />
          <span>
            {contato.endereco}
            <br />
            {contato.bairroCidade.split(' · ')[0]}
          </span>
        </p>
    </Glass>
  );
}

/** Faixa compacta do resumo no topo do painel (só no celular, onde a lateral não aparece). */
export function ResumoMini({ servico, horario }: { servico: Servico | null; horario: Horario | null }) {
  if (!servico) return null;
  return (
    <p className="ag-mini" aria-hidden="true">
      <strong>{servico.nome}</strong>
      <span>{horario ? `${diaCurto(horario.inicioEm.slice(0, 10))} · ${hora(horario.inicioEm)}` : duracao(servico.duracaoMinutos)}</span>
      <span>{dinheiro(servico.preco, servico.moeda)}</span>
    </p>
  );
}

export function Rodape() {
  return (
    <footer className="ag-footer">
      <div className="wrap">
        <p>
          © 2026 Revitalle Head Spa · {contato.endereco}, {contato.bairroCidade.split(' · ')[0]}
        </p>
        <p>
          <a href={contato.telefoneHref}>{contato.telefone}</a> ·{' '}
          <a href={contato.instagramHref} target="_blank" rel="noopener">
            {contato.instagram}
          </a>
        </p>
      </div>
    </footer>
  );
}
