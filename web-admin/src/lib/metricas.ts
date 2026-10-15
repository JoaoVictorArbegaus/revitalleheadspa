import type { S } from '@/api/client';
import { dataLocal, paraMinutos, somarDias, diaDaSemana, DIAS_SEMANA, FUSO } from '@/lib/formato';

type Agendamento = S['DadosAgendamentoAdministrativo'];

export type Periodo = { inicio: string; fim: string };

/** Período de mesmo tamanho imediatamente anterior (para comparar). */
export function periodoAnterior({ inicio, fim }: Periodo): Periodo {
  const dias = diasEntre(inicio, fim);
  return { inicio: somarDias(inicio, -dias), fim: somarDias(inicio, -1) };
}

export const diasEntre = (inicio: string, fim: string) =>
  Math.round((Date.parse(`${fim}T12:00:00Z`) - Date.parse(`${inicio}T12:00:00Z`)) / 86_400_000) + 1;

export function listaDeDias({ inicio, fim }: Periodo) {
  const d: string[] = [];
  for (let x = inicio; x <= fim; x = somarDias(x, 1)) d.push(x);
  return d;
}

const preco = (a: Agendamento) => a.precoContratado ?? 0;
const no = ({ inicio, fim }: Periodo) => (a: Agendamento) => {
  const d = dataLocal(a.inicioEm!);
  return d >= inicio && d <= fim;
};

export type Resumo = {
  realizado: number; // soma dos concluídos
  previsto: number; // soma dos confirmados (ainda vão acontecer ou aguardam registro)
  perdido: number; // ausências + cancelados
  concluidos: number;
  ausencias: number;
  cancelados: number;
  confirmados: number;
  comparecimento: number | null; // concluídos / (concluídos + ausências)
  ticketMedio: number | null;
  clientes: number;
  clientesNovas: number;
  clientesRecorrentes: number;
  pelosite: number; // reservas não canceladas vindas do site
  reservas: number; // reservas não canceladas
};

/**
 * Indicadores do período. "Nova" é a cliente cujo primeiro atendimento conhecido cai no período
 * (considerando também o período anterior carregado).
 */
export function resumir(todos: Agendamento[], periodo: Periodo, primeiraVisita?: Map<string, string>): Resumo {
  const ag = todos.filter(no(periodo));
  const por = (e: string) => ag.filter((a) => a.estado === e);
  const concl = por('CONCLUIDO');
  const aus = por('NAO_COMPARECEU');
  const canc = por('CANCELADO');
  const conf = por('CONFIRMADO');
  const soma = (l: Agendamento[]) => l.reduce((s, a) => s + preco(a), 0);
  const atendidas = new Set(concl.map((a) => a.cliente?.id).filter(Boolean) as string[]);
  let novas = 0;
  for (const id of atendidas) if (!primeiraVisita || (primeiraVisita.get(id) ?? '') >= periodo.inicio) novas++;
  const validas = ag.filter((a) => a.estado !== 'CANCELADO');
  return {
    realizado: soma(concl),
    previsto: soma(conf),
    perdido: soma(aus) + soma(canc),
    concluidos: concl.length,
    ausencias: aus.length,
    cancelados: canc.length,
    confirmados: conf.length,
    comparecimento: concl.length + aus.length ? concl.length / (concl.length + aus.length) : null,
    ticketMedio: concl.length ? soma(concl) / concl.length : null,
    clientes: atendidas.size,
    clientesNovas: novas,
    clientesRecorrentes: atendidas.size - novas,
    pelosite: validas.filter((a) => a.origem === 'PUBLICO').length,
    reservas: validas.length,
  };
}

/** Primeira data de atendimento concluído de cada cliente nos dados carregados. */
export function primeirasVisitas(todos: Agendamento[]) {
  const m = new Map<string, string>();
  for (const a of todos) {
    if (a.estado !== 'CONCLUIDO' || !a.cliente?.id) continue;
    const d = dataLocal(a.inicioEm!);
    if (!m.has(a.cliente.id) || d < m.get(a.cliente.id)!) m.set(a.cliente.id, d);
  }
  return m;
}

export type PontoDia = { data: string; realizado: number; previsto: number };

/** Faturamento por dia: realizado (concluídos) e previsto (confirmados). */
export function faturamentoDiario(todos: Agendamento[], periodo: Periodo): PontoDia[] {
  const m = new Map(listaDeDias(periodo).map((d) => [d, { data: d, realizado: 0, previsto: 0 }]));
  for (const a of todos) {
    const p = m.get(dataLocal(a.inicioEm!));
    if (!p) continue;
    if (a.estado === 'CONCLUIDO') p.realizado += preco(a);
    else if (a.estado === 'CONFIRMADO') p.previsto += preco(a);
  }
  return [...m.values()];
}

export type LinhaServico = { nome: string; faturamento: number; atendimentos: number };

/** Serviços concluídos, por faturamento. */
export function porServico(todos: Agendamento[], periodo: Periodo): LinhaServico[] {
  const m = new Map<string, LinhaServico>();
  for (const a of todos.filter(no(periodo))) {
    if (a.estado !== 'CONCLUIDO') continue;
    const nome = a.servico?.nome ?? 'Serviço';
    const l = m.get(nome) ?? { nome, faturamento: 0, atendimentos: 0 };
    l.faturamento += preco(a);
    l.atendimentos += 1;
    m.set(nome, l);
  }
  return [...m.values()].sort((x, y) => y.faturamento - x.faturamento || y.atendimentos - x.atendimentos);
}

const fmtHora = new Intl.DateTimeFormat('en-GB', { timeZone: FUSO, hour: '2-digit', hourCycle: 'h23' });

/** Reservas não canceladas por dia da semana × hora de início (procura). */
export function mapaProcura(todos: Agendamento[], periodo: Periodo) {
  const horas = new Set<number>();
  const cel = new Map<string, number>();
  for (const a of todos.filter(no(periodo))) {
    if (a.estado === 'CANCELADO') continue;
    const dia = diaDaSemana(dataLocal(a.inicioEm!));
    const h = Number(fmtHora.format(new Date(a.inicioEm!)));
    horas.add(h);
    cel.set(`${dia}|${h}`, (cel.get(`${dia}|${h}`) ?? 0) + 1);
  }
  const lista = horas.size ? [...horas].sort((a, b) => a - b) : [];
  const faixa = lista.length ? Array.from({ length: lista.at(-1)! - lista[0] + 1 }, (_, i) => lista[0] + i) : [];
  const dias = DIAS_SEMANA.filter((d) => [...cel.keys()].some((k) => k.startsWith(d.valor + '|')));
  const max = Math.max(0, ...cel.values());
  return { horas: faixa, dias, valor: (dia: string, h: number) => cel.get(`${dia}|${h}`) ?? 0, max };
}

/**
 * Ocupação: minutos reservados (concluídos + ausências + confirmados) sobre os minutos de expediente
 * do período. Bloqueios não são descontados (aproximação).
 */
export function ocupacao(todos: Agendamento[], periodo: Periodo, janelas: S['DadosJanelaExpediente'][] = []) {
  const porDia = new Map<string, number>();
  for (const j of janelas) porDia.set(j.diaSemana!, (porDia.get(j.diaSemana!) ?? 0) + paraMinutos(j.fim!) - paraMinutos(j.inicio!));
  const disponivel = listaDeDias(periodo).reduce((s, d) => s + (porDia.get(diaDaSemana(d)) ?? 0), 0);
  const usado = todos
    .filter(no(periodo))
    .filter((a) => a.estado !== 'CANCELADO')
    .reduce((s, a) => s + (Date.parse(a.ocupadoAte ?? a.fimServicoEm!) - Date.parse(a.inicioEm!)) / 60_000, 0);
  return disponivel ? Math.min(1, usado / disponivel) : null;
}

/** Variação relativa; null quando não há base de comparação. */
export const variacao = (atual: number, anterior: number) => (anterior ? (atual - anterior) / anterior : null);
