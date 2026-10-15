// Datas, horários e dinheiro no fuso e na moeda da instalação (padrões da API: America/Sao_Paulo e BRL).
export const FUSO = 'America/Sao_Paulo';
export const GRADE_MINUTOS = 15;

const dinheiro = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });
export const formatarMoeda = (v?: number) => (v === undefined || v === null ? '—' : dinheiro.format(v));

const fmtHora = new Intl.DateTimeFormat('pt-BR', { timeZone: FUSO, hour: '2-digit', minute: '2-digit' });
const fmtDia = new Intl.DateTimeFormat('pt-BR', { timeZone: FUSO, weekday: 'long', day: 'numeric', month: 'long' });
const fmtDiaCurto = new Intl.DateTimeFormat('pt-BR', { timeZone: FUSO, weekday: 'short', day: '2-digit', month: '2-digit' });
const fmtDataHora = new Intl.DateTimeFormat('pt-BR', { timeZone: FUSO, dateStyle: 'short', timeStyle: 'short' });
const fmtIsoData = new Intl.DateTimeFormat('en-CA', { timeZone: FUSO, year: 'numeric', month: '2-digit', day: '2-digit' });

export const hora = (iso?: string) => (iso ? fmtHora.format(new Date(iso)) : '');
export const dataHora = (iso?: string) => (iso ? fmtDataHora.format(new Date(iso)) : '');
/** Só a primeira letra maiúscula (o `capitalize` do CSS erraria "De Outubro"). */
export const inicialMaiuscula = (s: string) => s.charAt(0).toUpperCase() + s.slice(1);

/** "Segunda-feira, 19 de outubro" a partir de uma data local `YYYY-MM-DD`. */
export const diaPorExtenso = (data: string) => inicialMaiuscula(fmtDia.format(meioDia(data)));
export const diaCurto = (data: string) => fmtDiaCurto.format(meioDia(data));

/** Data local (`YYYY-MM-DD`) de um instante, no fuso da instalação. */
export const dataLocal = (d: Date | string) => fmtIsoData.format(typeof d === 'string' ? new Date(d) : d);
export const hoje = () => dataLocal(new Date());

// meio-dia UTC evita que a data "pule" ao formatar em outro fuso
const meioDia = (data: string) => new Date(`${data}T12:00:00Z`);

export function somarDias(data: string, dias: number) {
  const d = meioDia(data);
  d.setUTCDate(d.getUTCDate() + dias);
  return d.toISOString().slice(0, 10);
}

/** Segunda-feira da semana de uma data. */
export function inicioDaSemana(data: string) {
  const dow = meioDia(data).getUTCDay(); // 0 = domingo
  return somarDias(data, dow === 0 ? -6 : 1 - dow);
}

/** Offset do fuso naquela data, ex.: "-03:00". */
function offset(data: string) {
  const nome = new Intl.DateTimeFormat('en-US', { timeZone: FUSO, timeZoneName: 'longOffset' })
    .formatToParts(meioDia(data))
    .find((p) => p.type === 'timeZoneName')?.value;
  const m = nome?.match(/GMT([+-]\d{2}:\d{2})/);
  return m ? m[1] : '+00:00';
}

/** Instante ISO 8601 com offset a partir de data e hora locais, ex.: 2026-10-19T09:00:00-03:00. */
export const instante = (data: string, horaLocal: string) => `${data}T${horaLocal}:00${offset(data)}`;

/** Hora local `HH:mm` de um instante. */
export const horaLocal = (iso: string) => hora(iso);

export const DIAS_SEMANA = [
  { valor: 'SEGUNDA_FEIRA', rotulo: 'Segunda' },
  { valor: 'TERCA_FEIRA', rotulo: 'Terça' },
  { valor: 'QUARTA_FEIRA', rotulo: 'Quarta' },
  { valor: 'QUINTA_FEIRA', rotulo: 'Quinta' },
  { valor: 'SEXTA_FEIRA', rotulo: 'Sexta' },
  { valor: 'SABADO', rotulo: 'Sábado' },
  { valor: 'DOMINGO', rotulo: 'Domingo' },
] as const;
export type DiaSemana = (typeof DIAS_SEMANA)[number]['valor'];

/** Dia da semana (enum da API) de uma data local `YYYY-MM-DD`. */
export function diaDaSemana(data: string): DiaSemana {
  const dow = meioDia(data).getUTCDay(); // 0 = domingo
  return DIAS_SEMANA[(dow + 6) % 7].valor;
}

export const paraMinutos = (hhmm: string) => Number(hhmm.slice(0, 2)) * 60 + Number(hhmm.slice(3, 5));
export const paraHhmm = (min: number) => `${String(Math.floor(min / 60)).padStart(2, '0')}:${String(min % 60).padStart(2, '0')}`;

export const duracaoTexto = (min?: number) => {
  if (!min) return '—';
  const h = Math.floor(min / 60);
  const m = min % 60;
  return h ? `${h}h${m ? ` ${m}min` : ''}` : `${m}min`;
};

/** Celular para exibição: +5549999990010 → (49) 99999-0010. */
export function formatarCelular(celular?: string) {
  const d = (celular ?? '').replace(/\D/g, '').replace(/^55(?=\d{10,11}$)/, '');
  if (d.length === 11) return `(${d.slice(0, 2)}) ${d.slice(2, 7)}-${d.slice(7)}`;
  if (d.length === 10) return `(${d.slice(0, 2)}) ${d.slice(2, 6)}-${d.slice(6)}`;
  return celular ?? '';
}

/** Link de WhatsApp para o celular do cliente (só dígitos, com 55). */
export function whatsappDoCliente(celular?: string) {
  const d = (celular ?? '').replace(/\D/g, '');
  if (!d) return undefined;
  return `https://wa.me/${d.startsWith('55') ? d : `55${d}`}`;
}
