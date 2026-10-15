// Datas, horas, dinheiro e celular no padrão brasileiro. Datas locais (AAAA-MM-DD) são tratadas ao meio-dia UTC
// para nunca mudarem de dia por causa do fuso do navegador.

export const FUSO = 'America/Sao_Paulo';

const meioDia = (data: string) => new Date(`${data}T12:00:00Z`);
const iso = (d: Date) => d.toISOString().slice(0, 10);
const inicialMaiuscula = (s: string) => s.charAt(0).toUpperCase() + s.slice(1);

/** Hoje no fuso do negócio, não no do navegador. */
export const hoje = () => new Intl.DateTimeFormat('en-CA', { timeZone: FUSO }).format(new Date());

export function somarDias(data: string, dias: number) {
  const d = meioDia(data);
  d.setUTCDate(d.getUTCDate() + dias);
  return iso(d);
}

/** Primeiro e último dia do mês de `data` (AAAA-MM). */
export function limitesDoMes(mes: string) {
  const [a, m] = mes.split('-').map(Number);
  const fim = new Date(Date.UTC(a, m, 0, 12));
  return { inicio: `${mes}-01`, fim: iso(fim) };
}

export function somarMeses(mes: string, n: number) {
  const [a, m] = mes.split('-').map(Number);
  const d = new Date(Date.UTC(a, m - 1 + n, 1, 12));
  return iso(d).slice(0, 7);
}

/** Células do calendário (semana começando no domingo); `null` preenche antes do dia 1. */
export function semanasDoMes(mes: string) {
  const { inicio, fim } = limitesDoMes(mes);
  const vazios = meioDia(inicio).getUTCDay();
  const dias: (string | null)[] = Array.from({ length: vazios }, () => null);
  for (let d = inicio; d <= fim; d = somarDias(d, 1)) dias.push(d);
  while (dias.length % 7) dias.push(null);
  return dias;
}

const fmt = (opts: Intl.DateTimeFormatOptions) => new Intl.DateTimeFormat('pt-BR', { timeZone: 'UTC', ...opts });
const fmtMes = fmt({ month: 'long', year: 'numeric' });
const fmtExtenso = fmt({ weekday: 'long', day: 'numeric', month: 'long' });
const fmtCurto = fmt({ weekday: 'short', day: '2-digit', month: '2-digit' });
const fmtDiaSemana = fmt({ weekday: 'long' });

export const nomeDoMes = (mes: string) => inicialMaiuscula(fmtMes.format(meioDia(`${mes}-01`)));
/** "Segunda-feira, 19 de outubro" */
export const diaPorExtenso = (data: string) => inicialMaiuscula(fmtExtenso.format(meioDia(data)));
/** "seg., 19/10" */
export const diaCurto = (data: string) => fmtCurto.format(meioDia(data));
export const diaDaSemana = (data: string) => fmtDiaSemana.format(meioDia(data));

/** A API devolve instantes com o offset do negócio: a hora local já está no texto. */
export const hora = (instante: string) => instante.slice(11, 16);
export const dataDoInstante = (instante: string) => instante.slice(0, 10);

export function duracao(minutos: number) {
  if (minutos < 60) return `${minutos} min`;
  const h = Math.floor(minutos / 60);
  const m = minutos % 60;
  return m ? `${h}h${String(m).padStart(2, '0')}` : `${h}h`;
}

export const dinheiro = (valor: number, moeda = 'BRL') =>
  new Intl.NumberFormat('pt-BR', { style: 'currency', currency: moeda }).format(valor);

/** Dígitos do celular sem o +55, se houver: até 11 (DDD + número). */
export function digitosCelular(texto: string) {
  let d = texto.replace(/\D/g, '');
  if (d.length > 11 && d.startsWith('55')) d = d.slice(2);
  return d.slice(0, 11);
}

/** Máscara progressiva: (49) 99965-7420 ou (49) 3222-1234. */
export function mascararCelular(texto: string) {
  const d = digitosCelular(texto);
  if (d.length <= 2) return d.length ? `(${d}` : '';
  const ddd = d.slice(0, 2);
  const n = d.slice(2);
  const corte = n.length > 8 ? 5 : 4;
  return n.length > corte ? `(${ddd}) ${n.slice(0, corte)}-${n.slice(corte)}` : `(${ddd}) ${n}`;
}

export const celularValido = (texto: string) => [10, 11].includes(digitosCelular(texto).length);

/** Remove acentos e caixa para comparar nomes (ex.: ?servico=hair-spa). */
export const normalizar = (s: string) =>
  s
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-|-$/g, '');
