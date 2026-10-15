import { useId, type FormEvent, type ReactNode } from 'react';
import { contato } from '../content/site';
import type { AvisoPrivacidade, Horario, Servico } from './api';
import { celularValido, diaPorExtenso, dinheiro, duracao, hora, mascararCelular } from './formato';
import { Aviso, Botao, Icone, TituloEtapa } from './ui';

export type Carga<T> = { estado: 'carregando' } | { estado: 'ok'; dados: T } | { estado: 'erro'; mensagem: string };

const linkWhatsApp = (
  <a className="ag-link" href={contato.whatsappAgendar} target="_blank" rel="noopener">
    agende pelo WhatsApp
  </a>
);

/* ---------- 1. Tratamento ---------- */

export function PassoServico({
  carga,
  selecionado,
  focar,
  alerta,
  onEscolher,
  onRecarregar,
}: {
  carga: Carga<Servico[]>;
  selecionado?: string;
  focar: boolean;
  alerta?: ReactNode;
  onEscolher: (s: Servico) => void;
  onRecarregar: () => void;
}) {
  return (
    <>
      <TituloEtapa
        titulo="Qual tratamento você quer agendar?"
        descricao="Na dúvida, comece pela consulta: nela a Marineuza indica o cuidado certo para você."
        focar={focar}
      />
      {alerta && <Aviso tom="info">{alerta}</Aviso>}

      {carga.estado === 'carregando' && (
        <ul className="ag-services" aria-busy="true" aria-label="Carregando tratamentos">
          {Array.from({ length: 4 }, (_, i) => (
            <li key={i} className="ag-service ag-service--skeleton" aria-hidden="true">
              <span />
              <span />
              <span />
            </li>
          ))}
        </ul>
      )}

      {carga.estado === 'erro' && (
        <Aviso
          acao={
            <Botao variante="dark" tamanho="sm" onClick={onRecarregar}>
              Tentar de novo
            </Botao>
          }
        >
          {carga.mensagem}
        </Aviso>
      )}

      {carga.estado === 'ok' && carga.dados.length === 0 && (
        <Aviso tom="info">A agenda online ainda não tem tratamentos disponíveis. Enquanto isso, {linkWhatsApp}.</Aviso>
      )}

      {carga.estado === 'ok' && carga.dados.length > 0 && (
        <ul className="ag-services">
          {carga.dados.map((s) => (
            <li key={s.id}>
              <button type="button" className="ag-service" aria-pressed={s.id === selecionado} onClick={() => onEscolher(s)}>
                <span className="ag-service__top">
                  <span className="ag-service__nome">{s.nome}</span>
                  <span className="ag-service__preco">{dinheiro(s.preco, s.moeda)}</span>
                </span>
                {s.descricao && <span className="ag-service__desc">{s.descricao}</span>}
                <span className="ag-service__meta">
                  <span>
                    <Icone nome="relogio" />
                    {duracao(s.duracaoMinutos)}
                  </span>
                  <span>
                    <Icone nome="pessoa" />
                    {s.profissionalNome}
                  </span>
                  <span className="ag-service__go" aria-hidden="true">
                    Ver horários <Icone nome="proximo" />
                  </span>
                </span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </>
  );
}

/* ---------- 3. Seus dados ---------- */

export type ErrosDados = { nome?: string; celular?: string };

export function validarDados(nome: string, celular: string): ErrosDados {
  const erros: ErrosDados = {};
  if (!nome.trim()) erros.nome = 'Informe seu nome.';
  else if (nome.trim().length > 120) erros.nome = 'Use no máximo 120 caracteres.';
  if (!celular.trim()) erros.celular = 'Informe seu celular com DDD.';
  else if (!celularValido(celular)) erros.celular = 'Confira o número: DDD e celular, como (49) 99999-0000.';
  return erros;
}

export function PassoDados({
  nome,
  celular,
  erros,
  focar,
  onNome,
  onCelular,
  onContinuar,
  onVoltar,
}: {
  nome: string;
  celular: string;
  erros: ErrosDados;
  focar: boolean;
  onNome: (v: string) => void;
  onCelular: (v: string) => void;
  onContinuar: () => void;
  onVoltar: () => void;
}) {
  const id = useId();
  const enviar = (e: FormEvent) => {
    e.preventDefault();
    onContinuar();
  };
  return (
    <form noValidate onSubmit={enviar}>
      <TituloEtapa titulo="Como podemos te chamar?" descricao="Só o nome e o celular, para confirmar e falar sobre o seu horário." focar={focar} />
      <div className="ag-fields">
        <div className={erros.nome ? 'ag-field has-error' : 'ag-field'}>
          <label htmlFor={`${id}-nome`}>Nome</label>
          <input
            id={`${id}-nome`}
            name="nome"
            autoComplete="name"
            autoCapitalize="words"
            maxLength={120}
            value={nome}
            aria-invalid={!!erros.nome}
            aria-describedby={erros.nome ? `${id}-nome-erro` : undefined}
            onChange={(e) => onNome(e.target.value)}
          />
          {erros.nome && (
            <p className="ag-field__erro" id={`${id}-nome-erro`}>
              {erros.nome}
            </p>
          )}
        </div>
        <div className={erros.celular ? 'ag-field has-error' : 'ag-field'}>
          <label htmlFor={`${id}-celular`}>Celular com DDD</label>
          <input
            id={`${id}-celular`}
            name="celular"
            type="tel"
            inputMode="tel"
            autoComplete="tel-national"
            placeholder="(49) 99999-0000"
            value={celular}
            aria-invalid={!!erros.celular}
            aria-describedby={erros.celular ? `${id}-celular-erro` : `${id}-celular-ajuda`}
            onChange={(e) => onCelular(mascararCelular(e.target.value))}
          />
          {erros.celular ? (
            <p className="ag-field__erro" id={`${id}-celular-erro`}>
              {erros.celular}
            </p>
          ) : (
            <p className="ag-field__ajuda" id={`${id}-celular-ajuda`}>
              Se precisar, falamos com você por aqui ou pelo WhatsApp.
            </p>
          )}
        </div>
      </div>
      <div className="ag-actions">
        <button type="button" className="ag-back" onClick={onVoltar}>
          <Icone nome="voltar" /> Trocar horário
        </button>
        <Botao type="submit" variante="dark" textoHover="Revisar e confirmar">
          Continuar
        </Botao>
      </div>
    </form>
  );
}

/* ---------- 4. Confirmação ---------- */

export function PassoRevisao({
  servico,
  horario,
  nome,
  celular,
  aviso,
  ciente,
  enviando,
  erro,
  aguardando,
  focar,
  onCiente,
  onConfirmar,
  onRecarregarAviso,
  onEditar,
}: {
  servico: Servico;
  horario: Horario;
  nome: string;
  celular: string;
  aviso: Carga<AvisoPrivacidade>;
  ciente: boolean;
  enviando: boolean;
  erro?: ReactNode;
  /** Segundos até poder tentar de novo (limite de requisições). */
  aguardando: number;
  focar: boolean;
  onCiente: (v: boolean) => void;
  onConfirmar: () => void;
  onRecarregarAviso: () => void;
  onEditar: (etapa: 'servico' | 'horario' | 'dados') => void;
}) {
  const id = useId();
  const pronto = aviso.estado === 'ok' && ciente && !enviando && aguardando === 0;
  const linhas: [string, ReactNode, 'servico' | 'horario' | 'dados'][] = [
    ['Tratamento', `${servico.nome} · ${duracao(servico.duracaoMinutos)}`, 'servico'],
    ['Quando', `${diaPorExtenso(horario.inicioEm.slice(0, 10))}, às ${hora(horario.inicioEm)}`, 'horario'],
    ['Em nome de', `${nome.trim()} · ${celular}`, 'dados'],
  ];

  return (
    <>
      <TituloEtapa titulo="Confira e confirme" descricao={`Atendimento com ${servico.profissionalNome}. O horário só fica reservado depois de confirmar.`} focar={focar} />

      <dl className="ag-review">
        {linhas.map(([rotulo, valor, etapa]) => (
          <div key={rotulo} className="ag-review__row">
            <dt>{rotulo}</dt>
            <dd>{valor}</dd>
            <button type="button" className="ag-link ag-review__edit" onClick={() => onEditar(etapa)} disabled={enviando}>
              Alterar<span className="sr-only"> {rotulo.toLowerCase()}</span>
            </button>
          </div>
        ))}
        <div className="ag-review__row ag-review__row--total">
          <dt>Valor</dt>
          <dd>{dinheiro(servico.preco, servico.moeda)}</dd>
        </div>
      </dl>
      <p className="ag-note">Pagamento no local. Para remarcar ou cancelar depois, é só falar com a gente pelo WhatsApp.</p>

      <section className="ag-privacy" aria-labelledby={`${id}-aviso`}>
        <h3 id={`${id}-aviso`}>Aviso de privacidade</h3>
        {aviso.estado === 'carregando' && <p className="ag-privacy__text ag-privacy__text--loading">Carregando o aviso…</p>}
        {aviso.estado === 'erro' && (
          <Aviso
            acao={
              <Botao variante="dark" tamanho="sm" onClick={onRecarregarAviso}>
                Tentar de novo
              </Botao>
            }
          >
            {aviso.mensagem}
          </Aviso>
        )}
        {aviso.estado === 'ok' && (
          <>
            <div className="ag-privacy__text" tabIndex={0} data-lenis-prevent>
              {aviso.dados.texto}
            </div>
            <label className="ag-check">
              <input type="checkbox" checked={ciente} disabled={enviando} onChange={(e) => onCiente(e.target.checked)} />
              <span className="ag-check__box" aria-hidden="true">
                <Icone nome="ok" />
              </span>
              <span>Li e estou ciente do aviso de privacidade.</span>
            </label>
          </>
        )}
      </section>

      {erro && <Aviso>{erro}</Aviso>}

      <div className="ag-actions">
        <button type="button" className="ag-back" onClick={() => onEditar('dados')} disabled={enviando}>
          <Icone nome="voltar" /> Voltar
        </button>
        <Botao variante="dark" tamanho="xl" disabled={!pronto} ocupado={enviando} onClick={onConfirmar} textoHover={aguardando ? undefined : 'Reservar meu horário'}>
          {enviando ? 'Confirmando…' : aguardando ? `Aguarde ${aguardando}s` : 'Confirmar agendamento'}
        </Botao>
      </div>
      {!ciente && aviso.estado === 'ok' && !enviando && <p className="ag-hint">Marque a ciência do aviso para confirmar.</p>}
    </>
  );
}
