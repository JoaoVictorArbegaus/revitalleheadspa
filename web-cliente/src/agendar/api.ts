// Rotas públicas da API (api/docs/guia-frontend.md, "Fluxo público de reserva"). Mesma origem, sem sessão.

const BASE = '/api/v1/publico';

export type Servico = {
  id: string;
  nome: string;
  descricao?: string;
  duracaoMinutos: number;
  preco: number;
  moeda: string;
  profissionalId: string;
  profissionalNome: string;
};

export type Horario = { inicioEm: string; fimServicoEm: string };
export type DiaDisponivel = { data: string; horarios: Horario[] };
export type Disponibilidade = { servicoId: string; fusoHorario: string; dataInicial: string; dataFinal: string; dias: DiaDisponivel[] };
export type AvisoPrivacidade = { versao: string; texto: string };

export type NovoAgendamento = {
  servicoId: string;
  inicioEm: string;
  cliente: { nome: string; celular: string };
  versaoAvisoPrivacidade: string;
  cienteAvisoPrivacidade: true;
};

export type Confirmacao = {
  estado: 'CONFIRMADO';
  servico: { id: string; nome: string; duracaoMinutos: number };
  profissional: { id: string; nome: string };
  inicioEm: string;
  fimServicoEm: string;
  fusoHorario: string;
  precoContratado: number;
  moedaContratada: string;
};

export type ErroCampo = { campo: string; codigo: string; mensagem: string };

/** Falha com resposta da API (RFC 9457). Decida sempre pelo `codigo`, nunca pelo texto. */
export class ErroApi extends Error {
  constructor(
    readonly status: number,
    readonly codigo: string,
    readonly idRastreio?: string,
    readonly errosCampos: ErroCampo[] = [],
    /** Segundos pedidos em `Retry-After` (429). */
    readonly aguardar?: number,
  ) {
    super(codigo);
  }
  /** Erro do lado do servidor: o efeito é incerto, pode repetir. */
  get inesperado() {
    return this.status >= 500;
  }
}

/** Sem resposta (queda de rede, timeout, API fora do ar). */
export class ErroRede extends Error {
  constructor() {
    super('ERRO_REDE');
  }
}

async function chamar<T>(caminho: string, init: RequestInit = {}): Promise<T> {
  let r: Response;
  try {
    r = await fetch(BASE + caminho, { ...init, headers: { Accept: 'application/json', ...init.headers } });
  } catch (e) {
    if ((e as Error).name === 'AbortError') throw e;
    throw new ErroRede();
  }
  if (r.ok) return (await r.json()) as T;
  let p: { codigo?: string; idRastreio?: string; errosCampos?: ErroCampo[] } = {};
  try {
    p = await r.json();
  } catch {
    // proxy sem a API (502/504) responde HTML
  }
  const aguardar = Number(r.headers.get('Retry-After')) || undefined;
  throw new ErroApi(r.status, p.codigo ?? (r.status >= 500 ? 'ERRO_INTERNO' : 'SOLICITACAO_INVALIDA'), p.idRastreio, p.errosCampos ?? [], aguardar);
}

export const listarServicos = (signal?: AbortSignal) => chamar<Servico[]>('/servicos', { signal });

export const consultarDisponibilidade = (servicoId: string, dataInicial: string, dataFinal: string, signal?: AbortSignal) =>
  chamar<Disponibilidade>(
    `/servicos/${encodeURIComponent(servicoId)}/disponibilidade?dataInicial=${dataInicial}&dataFinal=${dataFinal}`,
    { signal },
  );

export const obterAvisoPrivacidade = (signal?: AbortSignal) => chamar<AvisoPrivacidade>('/aviso-privacidade', { signal });

export const criarAgendamento = (corpo: NovoAgendamento, chave: string) =>
  chamar<Confirmacao>('/agendamentos', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Chave-Idempotencia': chave },
    body: JSON.stringify(corpo),
  });

/** Chave de idempotência nova para cada intenção de reserva (16 a 128 caracteres de [A-Za-z0-9._~-]). */
export function novaChave() {
  if (typeof crypto.randomUUID === 'function') return crypto.randomUUID();
  // randomUUID só existe em contexto seguro (HTTPS ou localhost)
  return Array.from(crypto.getRandomValues(new Uint8Array(16)), (b) => b.toString(16).padStart(2, '0')).join('');
}
