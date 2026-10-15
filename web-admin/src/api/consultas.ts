import { useMutation, useQuery, useQueryClient, type QueryKey } from '@tanstack/react-query';
import { api, qs, type S } from './client';

const ADM = '/administracao';

// ---------- chaves ----------
export const chaves = {
  eu: ['eu'] as const,
  profissionais: ['profissionais'] as const,
  recursos: ['recursos'] as const,
  servicos: ['servicos'] as const,
  expediente: (p?: string) => ['expediente', p] as const,
  bloqueios: (p: string | undefined, ini: string, fim: string) => ['bloqueios', p, ini, fim] as const,
  agendamentos: (f: FiltroAgendamentos) => ['agendamentos', f] as const,
  aviso: ['aviso-privacidade'] as const,
};

// ---------- sessão ----------
export const useEu = () =>
  useQuery({ queryKey: chaves.eu, queryFn: () => api<S['DadosUsuarioAdministrativo']>(`${ADM}/autenticacao/eu`), retry: false, staleTime: 60_000 });

export const entrar = (c: S['Credenciais']) => api<void>(`${ADM}/autenticacao/entrar`, { method: 'POST', body: c });
export const sair = () => api<void>(`${ADM}/autenticacao/sair`, { method: 'POST' });
export const alterarSenha = (s: S['AlteracaoSenha']) => api<void>(`${ADM}/autenticacao/senha`, { method: 'PUT', body: s });

// ---------- catálogo ----------
export const useProfissionais = (habilitado = true) =>
  useQuery({ queryKey: chaves.profissionais, queryFn: () => api<S['DadosProfissional'][]>(`${ADM}/profissionais`), enabled: habilitado });

export const useRecursos = () => useQuery({ queryKey: chaves.recursos, queryFn: () => api<S['DadosRecursoOperacional'][]>(`${ADM}/recursos`) });

export const useServicos = () => useQuery({ queryKey: chaves.servicos, queryFn: () => api<S['DadosServico'][]>(`${ADM}/servicos`) });

export const salvarProfissional = (id: string | undefined, d: S['AlteracaoProfissional']) =>
  api<S['DadosProfissional']>(id ? `${ADM}/profissionais/${id}` : `${ADM}/profissionais`, { method: id ? 'PUT' : 'POST', body: d });

export const salvarRecurso = (id: string | undefined, d: S['AlteracaoRecurso']) =>
  api<S['DadosRecursoOperacional']>(id ? `${ADM}/recursos/${id}` : `${ADM}/recursos`, { method: id ? 'PUT' : 'POST', body: d });

export const salvarServico = (id: string | undefined, d: S['AlteracaoServico']) =>
  api<S['DadosServico']>(id ? `${ADM}/servicos/${id}` : `${ADM}/servicos`, { method: id ? 'PUT' : 'POST', body: d });

// ---------- agenda ----------
export const useExpediente = (profissionalId?: string) =>
  useQuery({
    queryKey: chaves.expediente(profissionalId),
    queryFn: () => api<S['DadosExpedienteSemanal']>(`${ADM}/agenda/semanal${qs({ profissionalId })}`),
    enabled: !!profissionalId,
  });

export const salvarExpediente = (d: S['AlteracaoExpediente']) =>
  api<S['DadosExpedienteSemanal']>(`${ADM}/agenda/semanal`, { method: 'PUT', body: d });

export const useBloqueios = (profissionalId: string | undefined, dataInicial: string, dataFinal: string) =>
  useQuery({
    queryKey: chaves.bloqueios(profissionalId, dataInicial, dataFinal),
    queryFn: () => api<S['DadosBloqueioAgenda'][]>(`${ADM}/agenda/bloqueios${qs({ profissionalId, dataInicial, dataFinal })}`),
    enabled: !!profissionalId,
  });

export const criarBloqueio = (d: S['CriacaoBloqueio']) => api<S['DadosBloqueioAgenda']>(`${ADM}/agenda/bloqueios`, { method: 'POST', body: d });
export const excluirBloqueio = (id: string) => api<void>(`${ADM}/agenda/bloqueios/${id}`, { method: 'DELETE' });

// ---------- agendamentos ----------
export type EstadoAgendamento = NonNullable<S['DadosAgendamentoAdministrativo']['estado']>;
export type FiltroAgendamentos = { dataInicial: string; dataFinal: string; profissionalId?: string; estado?: EstadoAgendamento };

export const useAgendamentos = (f: FiltroAgendamentos, habilitado = true) =>
  useQuery({
    queryKey: chaves.agendamentos(f),
    queryFn: () => api<S['DadosAgendamentoAdministrativo'][]>(`${ADM}/agendamentos${qs(f)}`),
    enabled: habilitado,
  });

export const criarAgendamento = (d: S['CriacaoAgendamento'], chave: string) =>
  api<S['DadosAgendamentoAdministrativo']>(`${ADM}/agendamentos`, { method: 'POST', body: d, headers: { 'Chave-Idempotencia': chave } });

export const transicionar = (id: string, acao: 'cancelar' | 'concluir' | 'nao-compareceu', motivo?: string) =>
  api<S['DadosAgendamentoAdministrativo']>(`${ADM}/agendamentos/${id}/${acao}`, {
    method: 'POST',
    body: acao === 'cancelar' ? { motivo } : undefined,
  });

// aviso vigente (rota pública): a criação administrativa confirma a ciência do cliente nessa versão
export const useAvisoPrivacidade = (habilitado: boolean) =>
  useQuery({ queryKey: chaves.aviso, queryFn: () => api<S['DadosAvisoPrivacidade']>('/publico/aviso-privacidade'), enabled: habilitado });

/** Mutação que invalida as chaves informadas ao concluir. */
export function useAcao<A, R>(fn: (a: A) => Promise<R>, invalidar: QueryKey[]) {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: fn,
    onSuccess: () => Promise.all(invalidar.map((k) => qc.invalidateQueries({ queryKey: k }))),
  });
}
