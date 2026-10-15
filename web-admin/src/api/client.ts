import type { components } from './schema';

/** Esquemas do OpenAPI da API (gerados com `npm run api:tipos`). */
export type S = components['schemas'];
export type Problema = S['Problema'];

const BASE = '/api/v1';

export class ApiError extends Error {
  readonly status: number;
  readonly problema?: Problema;
  /** Segundos informados em `Retry-After` (429). */
  readonly retryAfter?: number;

  constructor(status: number, problema?: Problema, retryAfter?: string | null) {
    super(problema?.detail ?? `Falha HTTP ${status}`);
    this.status = status;
    this.problema = problema;
    this.retryAfter = retryAfter ? Number(retryAfter) : undefined;
  }

  get codigo() {
    return this.problema?.codigo;
  }
}

// Token CSRF: obtido uma vez e reaproveitado; renovado só ao receber FALHA_VALIDACAO_CSRF.
let csrf: string | null = null;
async function tokenCsrf(renovar = false) {
  if (csrf && !renovar) return csrf;
  const r = await fetch(`${BASE}/administracao/autenticacao/csrf`, { credentials: 'same-origin' });
  if (!r.ok) throw new ApiError(r.status);
  csrf = ((await r.json()) as S['TokenCsrf']).token ?? null;
  return csrf ?? '';
}

let aoExpirarSessao: (() => void) | undefined;
/** Chamado quando uma rota administrativa responde 401 (sessão ausente ou expirada). */
export function quandoSessaoExpirar(fn: () => void) {
  aoExpirarSessao = fn;
}

type Opcoes = { method?: 'GET' | 'POST' | 'PUT' | 'DELETE'; body?: unknown; headers?: Record<string, string> };

export async function api<T>(caminho: string, { method = 'GET', body, headers }: Opcoes = {}): Promise<T> {
  const executar = async (tentarDeNovo: boolean): Promise<T> => {
    const h: Record<string, string> = { Accept: 'application/json', ...headers };
    if (body !== undefined) h['Content-Type'] = 'application/json';
    if (method !== 'GET') h['X-CSRF-TOKEN'] = await tokenCsrf();

    const r = await fetch(BASE + caminho, {
      method,
      headers: h,
      body: body !== undefined ? JSON.stringify(body) : undefined,
      credentials: 'same-origin',
    });
    if (r.status === 204) return undefined as T;
    const json = (r.headers.get('content-type') ?? '').includes('json') ? await r.json() : undefined;
    if (r.ok) return json as T;

    const problema = json as Problema | undefined;
    if (r.status === 403 && problema?.codigo === 'FALHA_VALIDACAO_CSRF' && tentarDeNovo) {
      await tokenCsrf(true);
      return executar(false);
    }
    if (r.status === 401 && !caminho.endsWith('/autenticacao/entrar')) aoExpirarSessao?.();
    throw new ApiError(r.status, problema, r.headers.get('Retry-After'));
  };
  return executar(true);
}

/** Monta query string ignorando valores vazios. */
export function qs(params: Record<string, string | undefined | null>) {
  const p = new URLSearchParams();
  for (const [k, v] of Object.entries(params)) if (v) p.set(k, v);
  const s = p.toString();
  return s ? `?${s}` : '';
}
