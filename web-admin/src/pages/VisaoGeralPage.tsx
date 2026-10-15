import { useMemo, useState, type ReactNode } from 'react';
import { ArrowDownRight, ArrowUpRight } from 'lucide-react';
import { useAgendamentosPeriodo, useExpediente } from '@/api/consultas';
import { Cabecalho, LinhasCarregando, Vazio } from '@/components/Estados';
import { Aneis } from '@/components/graficos/Aneis';
import { CartaoMetrica } from '@/components/graficos/CartaoMetrica';
import { DistribuicaoEstados, FaturamentoPorDia, MapaDeCalor, RankingServicos } from '@/components/graficos/Graficos';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { diaCurto, diaPorExtenso, DIAS_SEMANA, formatarMoeda, hoje, somarDias } from '@/lib/formato';
import {
  diasEntre,
  faturamentoDiario,
  listaDeDias,
  mapaProcura,
  ocupacao,
  porServico,
  primeirasVisitas,
  resumir,
  variacao,
  type Periodo,
} from '@/lib/metricas';
import { useSessao } from '@/lib/sessao';
import { cn } from '@/lib/utils';

type Preset = { id: string; rotulo: string; comparacao: string; calcular: (h: string) => { atual: Periodo; anterior: Periodo } };

const inicioMes = (d: string) => `${d.slice(0, 7)}-01`;
const fimMes = (d: string) => somarDias(inicioMes(somarDias(inicioMes(d), 32)), -1);

const PRESETS: Preset[] = [
  {
    id: 'mes',
    rotulo: 'Este mês',
    comparacao: 'vs. mesmo período do mês anterior',
    calcular: (h) => {
      const ant = inicioMes(somarDias(inicioMes(h), -1));
      return { atual: { inicio: inicioMes(h), fim: fimMes(h) }, anterior: { inicio: ant, fim: fimMes(ant) } };
    },
  },
  {
    id: 'mes-passado',
    rotulo: 'Mês passado',
    comparacao: 'vs. mês anterior',
    calcular: (h) => {
      const ini = inicioMes(somarDias(inicioMes(h), -1));
      const ant = inicioMes(somarDias(ini, -1));
      return { atual: { inicio: ini, fim: fimMes(ini) }, anterior: { inicio: ant, fim: fimMes(ant) } };
    },
  },
  ...[30, 90].map((n) => ({
    id: `${n}d`,
    rotulo: `Últimos ${n} dias`,
    comparacao: `vs. ${n} dias anteriores`,
    calcular: (h: string) => ({ atual: { inicio: somarDias(h, -(n - 1)), fim: h }, anterior: { inicio: somarDias(h, -(2 * n - 1)), fim: somarDias(h, -n) } }),
  })),
];

function Delta({ v, invertido = false }: { v: number | null; invertido?: boolean }) {
  if (v === null) return <span className="text-[var(--viz-muted)]">sem base de comparação</span>;
  const bom = invertido ? v <= 0 : v >= 0;
  return (
    <span className={cn('inline-flex items-center gap-0.5 font-semibold', bom ? 'text-[var(--viz-up)]' : 'text-[var(--viz-down)]')}>
      {v >= 0 ? <ArrowUpRight className="size-3.5" aria-hidden="true" /> : <ArrowDownRight className="size-3.5" aria-hidden="true" />}
      {v >= 0 ? '+' : '−'}
      {Math.abs(Math.round(v * 100))}%
    </span>
  );
}

function Indicador({ rotulo, valor, detalhe }: { rotulo: string; valor: string; detalhe: ReactNode }) {
  return (
    <div className="flex flex-col justify-between rounded-xl border bg-card p-5">
      <p className="text-sm font-medium text-[var(--viz-ink-2)]">{rotulo}</p>
      <p className="mt-3 text-3xl font-semibold tracking-tight text-[var(--viz-ink)]">{valor}</p>
      <p className="mt-1 text-xs text-[var(--viz-ink-2)]">{detalhe}</p>
    </div>
  );
}

function Bloco({ titulo, descricao, className, children }: { titulo: string; descricao?: string; className?: string; children: ReactNode }) {
  return (
    <Card className={className}>
      <CardHeader>
        <CardTitle>{titulo}</CardTitle>
        {descricao && <CardDescription>{descricao}</CardDescription>}
      </CardHeader>
      <CardContent>{children}</CardContent>
    </Card>
  );
}

export function VisaoGeralPage() {
  const { profissionalId, proprietario } = useSessao();
  const [presetId, setPresetId] = useState('mes');
  const preset = PRESETS.find((p) => p.id === presetId)!;
  const dia = hoje();
  const { atual, anterior } = preset.calcular(dia);
  // carrega também 180 dias antes, para saber se a cliente já tinha vindo (nova x recorrente)
  const inicioCarga = somarDias(anterior.inicio, -180);
  const { dados, carregando, atualizando } = useAgendamentosPeriodo(inicioCarga, atual.fim, proprietario ? profissionalId : undefined, !!profissionalId || !proprietario);
  const { data: expediente } = useExpediente(profissionalId);

  const m = useMemo(() => {
    const visitas = primeirasVisitas(dados);
    const r = resumir(dados, atual, visitas);
    const ra = resumir(dados, anterior, visitas);
    const diario = faturamentoDiario(dados, atual);
    const diarioAnt = faturamentoDiario(dados, anterior);
    // compara só até hoje: dias futuros do período atual ainda não têm realizado
    const ate = listaDeDias(atual).indexOf(dia);
    const ant = diarioAnt.map((p, i) => (ate >= 0 && i > ate ? 0 : p.realizado));
    const antAlinhado = Array.from({ length: diario.length }, (_, i) => ant[i] ?? 0);
    // a linha do realizado vai só até hoje (dias futuros ainda não têm realizado, não são zero)
    const corte = ate >= 0 ? ate + 1 : diario.length;
    const linha = diario.slice(0, corte).map((p) => ({ data: p.data, valor: p.realizado }));
    const linhaAnt = antAlinhado.slice(0, corte);
    const raAte = ate >= 0 ? resumir(dados, { inicio: anterior.inicio, fim: somarDias(anterior.inicio, ate) }, visitas) : ra;
    return {
      r,
      raAte,
      ra,
      diario,
      linha,
      linhaAnt,
      servicos: porServico(dados, atual),
      mapa: mapaProcura(dados, atual),
      ocup: ocupacao(dados, atual, expediente?.janelas),
    };
  }, [dados, atual.inicio, atual.fim, anterior.inicio, anterior.fim, dia, expediente]);

  const filtros = (
    <div className="mb-6 flex flex-wrap items-center gap-2" role="radiogroup" aria-label="Período">
      {PRESETS.map((p) => (
        <button
          key={p.id}
          role="radio"
          aria-checked={p.id === presetId}
          onClick={() => setPresetId(p.id)}
          className={cn(
            'h-8 rounded-full border px-3.5 text-sm font-medium transition-colors',
            p.id === presetId ? 'border-primary bg-primary text-primary-foreground' : 'bg-card text-[var(--viz-ink-2)] hover:bg-accent',
          )}
        >
          {p.rotulo}
        </button>
      ))}
      <span className="ml-1 text-sm text-[var(--viz-muted)]">
        {diaCurto(atual.inicio)} a {diaCurto(atual.fim)}
      </span>
    </div>
  );

  if (proprietario && !profissionalId)
    return (
      <>
        <Cabecalho titulo="Visão geral" />
        <Vazio titulo="Ainda não há dados">Cadastre o profissional, os serviços e o expediente para começar a receber agendamentos.</Vazio>
      </>
    );

  const { r } = m;
  const semDados = !carregando && r.reservas + r.cancelados === 0;

  return (
    <>
      <Cabecalho titulo="Visão geral" descricao="Faturamento estimado pelo preço contratado de cada reserva. Pagamentos, descontos e gorjetas não entram na conta." />
      {filtros}

      {carregando ? (
        <LinhasCarregando linhas={6} />
      ) : semDados ? (
        <Vazio titulo="Sem reservas neste período">Escolha outro período acima.</Vazio>
      ) : (
        // ao trocar o período, mantém o quadro anterior esmaecido até chegar o novo (sem pular o layout)
        <div className={cn('grid gap-4 transition-opacity md:grid-cols-4', atualizando && 'opacity-60')}>
          <CartaoMetrica
            className="md:col-span-2 md:row-span-2"
            rotulo="Faturamento realizado"
            atual={m.linha}
            anterior={m.linhaAnt}
            formatar={(v) => formatarMoeda(v)}
            rotuloDia={(d) => diaPorExtenso(d)}
            comparacao={preset.comparacao}
          />
          <Indicador rotulo="Previsto" valor={formatarMoeda(r.previsto)} detalhe={`${r.confirmados} ${r.confirmados === 1 ? 'reserva confirmada' : 'reservas confirmadas'} a atender`} />
          <Indicador
            rotulo="Ticket médio"
            valor={r.ticketMedio === null ? '—' : formatarMoeda(r.ticketMedio)}
            detalhe={<Delta v={r.ticketMedio !== null && m.ra.ticketMedio !== null ? variacao(r.ticketMedio, m.ra.ticketMedio) : null} />}
          />
          <Indicador
            rotulo="Atendimentos concluídos"
            valor={String(r.concluidos)}
            detalhe={
              variacao(r.concluidos, m.raAte.concluidos) === null ? (
                <Delta v={null} />
              ) : (
                <>
                  <Delta v={variacao(r.concluidos, m.raAte.concluidos)} /> {preset.comparacao}
                </>
              )
            }
          />
          <Indicador rotulo="Clientes atendidas" valor={String(r.clientes)} detalhe={`${r.clientesNovas} ${r.clientesNovas === 1 ? 'nova' : 'novas'} · ${r.clientesRecorrentes} ${r.clientesRecorrentes === 1 ? 'recorrente' : 'recorrentes'}`} />

          <Bloco titulo="Faturamento por dia" descricao={`${formatarMoeda(r.realizado)} realizados e ${formatarMoeda(r.previsto)} previstos no período.`} className="md:col-span-4">
            <FaturamentoPorDia pontos={m.diario} rotuloDia={diaPorExtenso} rotuloCurto={(d) => `${Number(d.slice(8))}/${d.slice(5, 7)}`} />
          </Bloco>

          <Bloco titulo="Reservas por estado" descricao={`Perdido com ausências e cancelamentos: ${formatarMoeda(r.perdido)}.`} className="md:col-span-2">
            <DistribuicaoEstados valores={{ concluidos: r.concluidos, confirmados: r.confirmados, ausencias: r.ausencias, cancelados: r.cancelados }} />
          </Bloco>

          <Bloco titulo="Saúde da agenda" descricao="Proporções do período, de 0 a 100%." className="md:col-span-2">
            <Aneis
              key={presetId}
              aneis={[
                { rotulo: 'Comparecimento', descricao: 'Concluídos entre concluídos e ausências', valor: r.comparecimento },
                { rotulo: 'Ocupação da agenda', descricao: 'Tempo reservado sobre o expediente', valor: m.ocup },
                { rotulo: 'Reservas pelo site', descricao: `${r.pelosite} de ${r.reservas} reservas vieram da página de agendamento`, valor: r.reservas ? r.pelosite / r.reservas : null },
              ]}
            />
          </Bloco>

          <Bloco titulo="Serviços que mais faturaram" descricao="Somente atendimentos concluídos." className="md:col-span-2">
            {m.servicos.length ? <RankingServicos linhas={m.servicos} /> : <p className="text-sm text-[var(--viz-ink-2)]">Nenhum atendimento concluído no período.</p>}
          </Bloco>

          <Bloco titulo="Dias e horários mais procurados" descricao="Reservas não canceladas pela hora de início." className="md:col-span-2">
            <MapaDeCalor
              dias={m.mapa.dias.map((d) => ({ valor: d.valor, rotulo: DIAS_SEMANA.find((x) => x.valor === d.valor)!.rotulo }))}
              horas={m.mapa.horas}
              valor={m.mapa.valor}
              max={m.mapa.max}
            />
          </Bloco>
        </div>
      )}
      <p className="mt-6 text-xs text-[var(--viz-muted)]">
        Período de {diasEntre(atual.inicio, atual.fim)} dias. Clientes "novas" não tinham atendimento concluído nos 180 dias anteriores.
      </p>
    </>
  );
}
