import { useMemo, useState } from 'react';
import { Link } from 'react-router';
import { CalendarCheck2, Check, ChevronLeft, ChevronRight, Circle, MessageCircle, MoreHorizontal, Plus, UserX, X } from 'lucide-react';
import { toast } from 'sonner';
import type { S } from '@/api/client';
import { transicionar, useAcao, useAgendamentos, useExpediente, useServicos, type EstadoAgendamento } from '@/api/consultas';
import { Cabecalho, LinhasCarregando } from '@/components/Estados';
import { NovoAgendamento } from '@/components/NovoAgendamento';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuSeparator, DropdownMenuTrigger } from '@/components/ui/dropdown-menu';
import { Field, FieldDescription, FieldLabel } from '@/components/ui/field';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Textarea } from '@/components/ui/textarea';
import { avisarErro } from '@/lib/erros';
import { diaPorExtenso, dataLocal, formatarCelular, formatarMoeda, hoje, hora, inicioDaSemana, somarDias, whatsappDoCliente } from '@/lib/formato';
import { useSessao } from '@/lib/sessao';
import { cn } from '@/lib/utils';

type Agendamento = S['DadosAgendamentoAdministrativo'];

const ESTADOS: Record<EstadoAgendamento, { rotulo: string; classe: string }> = {
  CONFIRMADO: { rotulo: 'Confirmado', classe: 'bg-[#4A2C1B] text-[#FBF6EE]' },
  CONCLUIDO: { rotulo: 'Concluído', classe: 'bg-[#E3EEDF] text-[#2F5A2A]' },
  NAO_COMPARECEU: { rotulo: 'Não compareceu', classe: 'bg-[#F6E3CF] text-[#7A4210]' },
  CANCELADO: { rotulo: 'Cancelado', classe: 'bg-muted text-muted-foreground line-through decoration-1' },
};

/** Primeiros passos enquanto faltar o básico para receber agendamentos. */
function PrimeirosPassos() {
  const { profissionalId, proprietario } = useSessao();
  const { data: servicos } = useServicos();
  const { data: expediente } = useExpediente(profissionalId);
  const temServico = (servicos ?? []).some((s) => s.ativo && (!proprietario || s.profissionalId === profissionalId));
  const temExpediente = (expediente?.janelas?.length ?? 0) > 0;
  const passos = [
    { feito: !!profissionalId, rotulo: 'Cadastrar o profissional', descricao: 'Quem realiza os atendimentos.', to: '/profissionais', soProprietario: true },
    { feito: temServico, rotulo: 'Cadastrar os serviços', descricao: 'Nome, preço e duração de cada tratamento.', to: '/servicos' },
    { feito: temExpediente, rotulo: 'Definir o expediente', descricao: 'Dias e horários em que a agenda abre.', to: '/expediente' },
  ].filter((p) => !p.soProprietario || proprietario);
  if (servicos === undefined || passos.every((p) => p.feito)) return null;

  return (
    <Card className="mb-8 border-[#C8A062]/50 bg-[#FFFCF7]">
      <CardHeader>
        <CardTitle className="text-xl">Prepare a agenda para receber clientes</CardTitle>
        <CardDescription>Assim que estes passos estiverem prontos, a página de agendamento já mostra os horários livres.</CardDescription>
      </CardHeader>
      <CardContent>
        <ol className="grid gap-3 sm:grid-cols-3">
          {passos.map((p, i) => (
            <li key={p.to}>
              <Link
                to={p.to}
                className={cn('flex h-full gap-3 rounded-lg border p-4 transition-colors hover:bg-accent', p.feito && 'opacity-60')}
              >
                {p.feito ? (
                  <Check className="mt-0.5 size-5 shrink-0 text-[#2F5A2A]" aria-hidden="true" />
                ) : (
                  <span className="grid size-6 shrink-0 place-items-center rounded-full bg-primary text-xs text-primary-foreground">{i + 1}</span>
                )}
                <span>
                  <span className="block font-medium">{p.rotulo}</span>
                  <span className="text-sm text-muted-foreground">{p.feito ? 'Feito' : p.descricao}</span>
                </span>
              </Link>
            </li>
          ))}
        </ol>
      </CardContent>
    </Card>
  );
}

function DialogoCancelar({ alvo, aoFechar }: { alvo: Agendamento; aoFechar: () => void }) {
  const [motivo, setMotivo] = useState('');
  const cancelar = useAcao((m: string) => transicionar(alvo.id!, 'cancelar', m), [['agendamentos']]);
  return (
    <DialogContent>
      <DialogHeader>
        <DialogTitle>Cancelar agendamento</DialogTitle>
        <DialogDescription>
          {alvo.servico?.nome} de {alvo.cliente?.nome ?? 'cliente'} em {diaPorExtenso(dataLocal(alvo.inicioEm!)).toLowerCase()}, às {hora(alvo.inicioEm)}. O
          horário volta a ficar livre na hora.
        </DialogDescription>
      </DialogHeader>
      <Field className="my-4">
        <FieldLabel htmlFor="motivo">Motivo</FieldLabel>
        <Textarea id="motivo" rows={3} value={motivo} onChange={(e) => setMotivo(e.target.value)} autoFocus />
        <FieldDescription>Fica registrado no histórico. Não escreva dados pessoais do cliente.</FieldDescription>
      </Field>
      <DialogFooter>
        <Button variant="outline" onClick={aoFechar}>
          Voltar
        </Button>
        <Button
          variant="destructive"
          disabled={!motivo.trim() || cancelar.isPending}
          onClick={async () => {
            try {
              await cancelar.mutateAsync(motivo.trim());
              toast.success('Agendamento cancelado.');
              aoFechar();
            } catch (e) {
              avisarErro(e);
            }
          }}
        >
          {cancelar.isPending ? 'Cancelando…' : 'Cancelar agendamento'}
        </Button>
      </DialogFooter>
    </DialogContent>
  );
}

function CartaoAgendamento({ a, aoCancelar }: { a: Agendamento; aoCancelar: (a: Agendamento) => void }) {
  const marcar = useAcao((acao: 'concluir' | 'nao-compareceu') => transicionar(a.id!, acao), [['agendamentos']]);
  const estado = ESTADOS[a.estado ?? 'CONFIRMADO'];
  const comecou = new Date(a.inicioEm!).getTime() <= Date.now();
  const confirmado = a.estado === 'CONFIRMADO';
  const wa = whatsappDoCliente(a.cliente?.celular);

  const executar = async (acao: 'concluir' | 'nao-compareceu') => {
    try {
      await marcar.mutateAsync(acao);
      toast.success(acao === 'concluir' ? 'Atendimento concluído.' : 'Ausência registrada.');
    } catch (e) {
      avisarErro(e);
    }
  };

  return (
    <li className={cn('grid grid-cols-[4.5rem_1fr_auto] items-start gap-4 rounded-xl border bg-card p-4', !confirmado && 'bg-card/60')}>
      <div className="tabular-nums">
        <p className="text-lg leading-none font-medium">{hora(a.inicioEm)}</p>
        <p className="mt-1 text-xs text-muted-foreground">até {hora(a.fimServicoEm)}</p>
      </div>
      <div className="min-w-0">
        <div className="flex flex-wrap items-center gap-2">
          <p className="font-medium">{a.cliente?.nome ?? <span className="text-muted-foreground italic">Cliente anonimizado</span>}</p>
          <Badge className={cn('border-0', estado.classe)}>{estado.rotulo}</Badge>
          {a.origem === 'PUBLICO' && (
            <Badge variant="outline" className="text-xs">
              Pelo site
            </Badge>
          )}
        </div>
        <p className="mt-0.5 text-sm text-muted-foreground">
          {a.servico?.nome} · {formatarMoeda(a.precoContratado)}
          {!!a.recursos?.length && ` · ${a.recursos.map((r) => r.nome).join(', ')}`}
        </p>
        {a.cliente?.celular && (
          <p className="mt-1 text-sm">
            {wa ? (
              <a href={wa} target="_blank" rel="noopener" className="inline-flex items-center gap-1 underline-offset-4 hover:underline">
                <MessageCircle className="size-3.5" aria-hidden="true" /> {formatarCelular(a.cliente.celular)}
              </a>
            ) : (
              formatarCelular(a.cliente.celular)
            )}
          </p>
        )}
        {a.motivoCancelamento && <p className="mt-1 text-sm text-muted-foreground">Motivo: {a.motivoCancelamento}</p>}
      </div>
      {confirmado && (
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" size="icon" aria-label="Ações do agendamento" disabled={marcar.isPending}>
              <MoreHorizontal aria-hidden="true" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            {comecou ? (
              <>
                <DropdownMenuItem onSelect={() => executar('concluir')}>
                  <CalendarCheck2 aria-hidden="true" /> Concluir atendimento
                </DropdownMenuItem>
                <DropdownMenuItem onSelect={() => executar('nao-compareceu')}>
                  <UserX aria-hidden="true" /> Cliente não compareceu
                </DropdownMenuItem>
              </>
            ) : (
              <>
                <DropdownMenuItem disabled>
                  <Circle aria-hidden="true" /> Concluir após o início
                </DropdownMenuItem>
                <DropdownMenuSeparator />
                <DropdownMenuItem variant="destructive" onSelect={() => aoCancelar(a)}>
                  <X aria-hidden="true" /> Cancelar agendamento
                </DropdownMenuItem>
              </>
            )}
          </DropdownMenuContent>
        </DropdownMenu>
      )}
    </li>
  );
}

export function AgendaPage() {
  const { profissionalId, proprietario } = useSessao();
  const [visao, setVisao] = useState<'dia' | 'semana'>('dia');
  const [data, setData] = useState(hoje);
  const [estado, setEstado] = useState<EstadoAgendamento | 'TODOS'>('TODOS');
  const [novo, setNovo] = useState(false);
  const [cancelando, setCancelando] = useState<Agendamento>();

  const inicio = visao === 'dia' ? data : inicioDaSemana(data);
  const fim = visao === 'dia' ? data : somarDias(inicio, 6);
  const passo = visao === 'dia' ? 1 : 7;
  const { data: lista, isPending } = useAgendamentos(
    { dataInicial: inicio, dataFinal: fim, profissionalId: proprietario ? profissionalId : undefined, estado: estado === 'TODOS' ? undefined : estado },
    !!profissionalId || !proprietario,
  );

  const porDia = useMemo(() => {
    const m = new Map<string, Agendamento[]>();
    for (let d = inicio; d <= fim; d = somarDias(d, 1)) m.set(d, []);
    for (const a of [...(lista ?? [])].sort((x, y) => x.inicioEm!.localeCompare(y.inicioEm!))) m.get(dataLocal(a.inicioEm!))?.push(a);
    return [...m.entries()];
  }, [lista, inicio, fim]);

  const confirmados = (lista ?? []).filter((a) => a.estado === 'CONFIRMADO').length;
  const titulo =
    visao === 'dia'
      ? data === hoje()
        ? 'Hoje'
        : diaPorExtenso(data)
      : `Semana de ${diaPorExtenso(inicio).split(',')[1]?.trim() ?? inicio}`;

  return (
    <>
      <PrimeirosPassos />
      <Cabecalho
        titulo="Agenda"
        descricao={isPending ? ' ' : `${confirmados} ${confirmados === 1 ? 'agendamento confirmado' : 'agendamentos confirmados'} no período`}
        acao={
          profissionalId && (
            <Button onClick={() => setNovo(true)}>
              <Plus aria-hidden="true" /> Novo agendamento
            </Button>
          )
        }
      />

      <div className="mb-6 flex flex-wrap items-center gap-3">
        <Tabs value={visao} onValueChange={(v) => setVisao(v as 'dia' | 'semana')}>
          <TabsList>
            <TabsTrigger value="dia">Dia</TabsTrigger>
            <TabsTrigger value="semana">Semana</TabsTrigger>
          </TabsList>
        </Tabs>
        <div className="flex items-center gap-1">
          <Button variant="outline" size="icon" aria-label="Anterior" onClick={() => setData(somarDias(data, -passo))}>
            <ChevronLeft aria-hidden="true" />
          </Button>
          <Button variant="outline" onClick={() => setData(hoje())}>
            Hoje
          </Button>
          <Button variant="outline" size="icon" aria-label="Próximo" onClick={() => setData(somarDias(data, passo))}>
            <ChevronRight aria-hidden="true" />
          </Button>
        </div>
        <p className="font-[family-name:var(--font-display)] text-xl font-semibold">{titulo}</p>
        <Select value={estado} onValueChange={(v) => setEstado(v as EstadoAgendamento | 'TODOS')}>
          <SelectTrigger className="ml-auto w-48 bg-card" aria-label="Filtrar por estado">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="TODOS">Todos os estados</SelectItem>
            {Object.entries(ESTADOS).map(([v, e]) => (
              <SelectItem key={v} value={v}>
                {e.rotulo}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {isPending ? (
        <LinhasCarregando />
      ) : (
        <div className="grid gap-8">
          {porDia.map(([dia, itens]) => (
            <section key={dia} aria-label={diaPorExtenso(dia)}>
              {visao === 'semana' && (
                <h2 className="mb-3 text-lg">
                  {diaPorExtenso(dia)} {dia === hoje() && <Badge className="ml-2 align-middle">Hoje</Badge>}
                </h2>
              )}
              {itens.length ? (
                <ul className="grid gap-2">
                  {itens.map((a) => (
                    <CartaoAgendamento key={a.id} a={a} aoCancelar={setCancelando} />
                  ))}
                </ul>
              ) : (
                <p className="rounded-xl border border-dashed px-4 py-6 text-sm text-muted-foreground">Nenhum agendamento.</p>
              )}
            </section>
          ))}
        </div>
      )}

      <Dialog open={novo} onOpenChange={setNovo}>
        {novo && profissionalId && <NovoAgendamento profissionalId={profissionalId} dataSugerida={data} aoFechar={() => setNovo(false)} />}
      </Dialog>
      <Dialog open={!!cancelando} onOpenChange={(o) => !o && setCancelando(undefined)}>
        {cancelando && <DialogoCancelar alvo={cancelando} aoFechar={() => setCancelando(undefined)} />}
      </Dialog>
    </>
  );
}
