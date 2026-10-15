import { useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { CalendarOff, ChevronLeft, ChevronRight, Plus, Trash2, Wand2 } from 'lucide-react';
import { toast } from 'sonner';
import { Link } from 'react-router';
import { chaves, criarBloqueio, excluirBloqueio, salvarExpediente, useAcao, useBloqueios, useExpediente } from '@/api/consultas';
import { Cabecalho, LinhasCarregando, Vazio } from '@/components/Estados';
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '@/components/ui/alert-dialog';
import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Checkbox } from '@/components/ui/checkbox';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Field, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import { avisarErro } from '@/lib/erros';
import { dataHora, dataLocal, DIAS_SEMANA, diaCurto, hoje, instante, somarDias, type DiaSemana } from '@/lib/formato';
import { useSessao } from '@/lib/sessao';

type Janela = { inicio: string; fim: string };
type Semana = Record<DiaSemana, Janela[]>;

const semanaVazia = (): Semana => Object.fromEntries(DIAS_SEMANA.map((d) => [d.valor, []])) as unknown as Semana;

// Horário de funcionamento informado no briefing (perfil do Google): seg–qui 10h–21h, sex 10h–17h.
const HORARIO_REVITALLE: Semana = {
  ...semanaVazia(),
  SEGUNDA_FEIRA: [{ inicio: '10:00', fim: '21:00' }],
  TERCA_FEIRA: [{ inicio: '10:00', fim: '21:00' }],
  QUARTA_FEIRA: [{ inicio: '10:00', fim: '21:00' }],
  QUINTA_FEIRA: [{ inicio: '10:00', fim: '21:00' }],
  SEXTA_FEIRA: [{ inicio: '10:00', fim: '17:00' }],
};

function EditorExpediente({ profissionalId }: { profissionalId: string }) {
  const { data, isPending } = useExpediente(profissionalId);
  const salvar = useAcao(salvarExpediente, [chaves.expediente(profissionalId)]);
  const [semana, setSemana] = useState<Semana>(semanaVazia);
  const [alterado, setAlterado] = useState(false);

  useEffect(() => {
    if (!data) return;
    const s = semanaVazia();
    for (const j of data.janelas ?? []) if (j.diaSemana) s[j.diaSemana].push({ inicio: j.inicio!.slice(0, 5), fim: j.fim!.slice(0, 5) });
    for (const d of DIAS_SEMANA) s[d.valor].sort((a, b) => a.inicio.localeCompare(b.inicio));
    setSemana(s);
    setAlterado(false);
  }, [data]);

  const editar = (fn: (s: Semana) => Semana) => {
    setSemana((s) => fn(structuredClone(s)));
    setAlterado(true);
  };

  const problemas = useMemo(() => {
    const p: Partial<Record<DiaSemana, string>> = {};
    for (const d of DIAS_SEMANA) {
      const js = [...semana[d.valor]].sort((a, b) => a.inicio.localeCompare(b.inicio));
      if (js.some((j) => !j.inicio || !j.fim || j.inicio >= j.fim)) p[d.valor] = 'O início deve ser antes do fim.';
      else if (js.some((j, i) => i > 0 && j.inicio < js[i - 1].fim)) p[d.valor] = 'Há janelas sobrepostas.';
    }
    return p;
  }, [semana]);

  const enviar = async () => {
    try {
      await salvar.mutateAsync({
        profissionalId,
        janelas: DIAS_SEMANA.flatMap((d) => semana[d.valor].map((j) => ({ diaSemana: d.valor, inicio: j.inicio, fim: j.fim }))),
      });
      toast.success('Expediente salvo.');
    } catch (e) {
      avisarErro(e);
    }
  };

  if (isPending) return <LinhasCarregando linhas={7} />;

  return (
    <Card>
      <CardHeader className="flex flex-row flex-wrap items-start justify-between gap-3">
        <div>
          <CardTitle className="text-xl">Expediente semanal</CardTitle>
          <CardDescription>Janelas em que o agendamento é possível. Dias sem janela ficam fechados.</CardDescription>
        </div>
        <Button variant="outline" size="sm" onClick={() => editar(() => structuredClone(HORARIO_REVITALLE))}>
          <Wand2 aria-hidden="true" /> Usar horário de funcionamento
        </Button>
      </CardHeader>
      <CardContent className="grid gap-1">
        {DIAS_SEMANA.map((d) => (
          <div key={d.valor} className="grid gap-2 border-b py-3 last:border-0 sm:grid-cols-[8rem_1fr] sm:items-start">
            <p className="pt-1.5 font-medium">{d.rotulo}</p>
            <div className="grid gap-2">
              {semana[d.valor].length === 0 && <p className="pt-1.5 text-sm text-muted-foreground">Fechado</p>}
              {semana[d.valor].map((j, i) => (
                <div key={i} className="flex flex-wrap items-center gap-2">
                  <Input
                    type="time"
                    step={900}
                    className="w-32"
                    aria-label={`${d.rotulo}, início da janela ${i + 1}`}
                    value={j.inicio}
                    onChange={(e) => editar((s) => ((s[d.valor][i].inicio = e.target.value), s))}
                  />
                  <span className="text-muted-foreground">até</span>
                  <Input
                    type="time"
                    step={900}
                    className="w-32"
                    aria-label={`${d.rotulo}, fim da janela ${i + 1}`}
                    value={j.fim}
                    onChange={(e) => editar((s) => ((s[d.valor][i].fim = e.target.value), s))}
                  />
                  <Button variant="ghost" size="icon" aria-label={`Remover janela ${i + 1} de ${d.rotulo}`} onClick={() => editar((s) => (s[d.valor].splice(i, 1), s))}>
                    <Trash2 aria-hidden="true" />
                  </Button>
                </div>
              ))}
              {problemas[d.valor] && <p className="text-sm text-destructive">{problemas[d.valor]}</p>}
              <Button
                variant="ghost"
                size="sm"
                className="justify-self-start text-muted-foreground"
                onClick={() =>
                  editar((s) => {
                    const ult = s[d.valor].at(-1);
                    s[d.valor].push(ult ? { inicio: ult.fim, fim: ult.fim < '21:00' ? '21:00' : '23:45' } : { inicio: '10:00', fim: '18:00' });
                    return s;
                  })
                }
              >
                <Plus aria-hidden="true" /> Adicionar janela
              </Button>
            </div>
          </div>
        ))}
        <div className="flex justify-end gap-2 pt-4">
          <Button onClick={enviar} disabled={!alterado || salvar.isPending || Object.keys(problemas).length > 0}>
            {salvar.isPending ? 'Salvando…' : 'Salvar expediente'}
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}

const schemaBloqueio = z
  .object({ dataInicio: z.string().min(1, 'Informe a data'), horaInicio: z.string(), dataFim: z.string().min(1, 'Informe a data'), horaFim: z.string(), diaInteiro: z.boolean() })
  .refine((d) => d.diaInteiro || (!!d.horaInicio && !!d.horaFim), { path: ['horaFim'], message: 'Informe os horários' })
  .refine((d) => (d.diaInteiro ? d.dataFim >= d.dataInicio : `${d.dataFim}T${d.horaFim}` > `${d.dataInicio}T${d.horaInicio}`), {
    path: ['dataFim'],
    message: 'O fim deve ser depois do início',
  });
type DadosBloqueio = z.infer<typeof schemaBloqueio>;

function DialogoBloqueio({ profissionalId, aoFechar }: { profissionalId: string; aoFechar: () => void }) {
  const criar = useAcao(criarBloqueio, [['bloqueios']]);
  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors, isSubmitting },
  } = useForm<DadosBloqueio>({
    resolver: zodResolver(schemaBloqueio),
    defaultValues: { dataInicio: hoje(), horaInicio: '10:00', dataFim: hoje(), horaFim: '12:00', diaInteiro: false },
  });
  const diaInteiro = watch('diaInteiro');

  const onSubmit = async (d: DadosBloqueio) => {
    try {
      await criar.mutateAsync({
        profissionalId,
        // dia inteiro: da meia-noite do primeiro dia à meia-noite do dia seguinte ao último
        inicioEm: instante(d.dataInicio, d.diaInteiro ? '00:00' : d.horaInicio),
        fimEm: d.diaInteiro ? instante(somarDias(d.dataFim, 1), '00:00') : instante(d.dataFim, d.horaFim),
      });
      toast.success('Bloqueio criado.');
      aoFechar();
    } catch (e) {
      avisarErro(e);
    }
  };

  return (
    <DialogContent>
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <DialogHeader>
          <DialogTitle>Novo bloqueio</DialogTitle>
          <DialogDescription>Férias, feriados ou compromissos. Ninguém consegue agendar nesse período.</DialogDescription>
        </DialogHeader>
        <FieldGroup className="my-6">
          <Field orientation="horizontal">
            <Checkbox id="diaInteiro" checked={diaInteiro} onCheckedChange={(v) => setValue('diaInteiro', v === true)} />
            <FieldLabel htmlFor="diaInteiro">Dia inteiro</FieldLabel>
          </Field>
          <div className="grid grid-cols-2 gap-3">
            <Field data-invalid={!!errors.dataInicio}>
              <FieldLabel htmlFor="dataInicio">Início</FieldLabel>
              <Input id="dataInicio" type="date" {...register('dataInicio')} />
              <FieldError>{errors.dataInicio?.message}</FieldError>
            </Field>
            {!diaInteiro && (
              <Field>
                <FieldLabel htmlFor="horaInicio">Hora</FieldLabel>
                <Input id="horaInicio" type="time" step={900} {...register('horaInicio')} />
              </Field>
            )}
            <Field data-invalid={!!errors.dataFim}>
              <FieldLabel htmlFor="dataFim">Fim</FieldLabel>
              <Input id="dataFim" type="date" {...register('dataFim')} />
              <FieldError>{errors.dataFim?.message}</FieldError>
            </Field>
            {!diaInteiro && (
              <Field data-invalid={!!errors.horaFim}>
                <FieldLabel htmlFor="horaFim">Hora</FieldLabel>
                <Input id="horaFim" type="time" step={900} {...register('horaFim')} />
                <FieldError>{errors.horaFim?.message}</FieldError>
              </Field>
            )}
          </div>
        </FieldGroup>
        <DialogFooter>
          <Button type="button" variant="outline" onClick={aoFechar}>
            Cancelar
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Salvando…' : 'Bloquear período'}
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  );
}

function Bloqueios({ profissionalId }: { profissionalId: string }) {
  const [inicio, setInicio] = useState(hoje);
  const fim = somarDias(inicio, 30); // 31 datas: limite da API por consulta
  const { data, isPending } = useBloqueios(profissionalId, inicio, fim);
  const excluir = useAcao(excluirBloqueio, [['bloqueios']]);
  const [novo, setNovo] = useState(false);
  const [remover, setRemover] = useState<string>();

  return (
    <Card>
      <CardHeader className="flex flex-row flex-wrap items-start justify-between gap-3">
        <div>
          <CardTitle className="text-xl">Bloqueios</CardTitle>
          <CardDescription>
            {diaCurto(inicio)} a {diaCurto(fim)}
          </CardDescription>
        </div>
        <div className="flex items-center gap-1">
          <Button variant="outline" size="icon" aria-label="Período anterior" onClick={() => setInicio(somarDias(inicio, -31))}>
            <ChevronLeft aria-hidden="true" />
          </Button>
          <Button variant="outline" size="icon" aria-label="Próximo período" onClick={() => setInicio(somarDias(inicio, 31))}>
            <ChevronRight aria-hidden="true" />
          </Button>
          <Button size="sm" className="ml-2" onClick={() => setNovo(true)}>
            <Plus aria-hidden="true" /> Bloquear
          </Button>
        </div>
      </CardHeader>
      <CardContent>
        {isPending ? (
          <LinhasCarregando linhas={2} />
        ) : !data?.length ? (
          <p className="flex items-center gap-2 py-6 text-sm text-muted-foreground">
            <CalendarOff className="size-4" aria-hidden="true" /> Nenhum bloqueio neste período.
          </p>
        ) : (
          <ul className="divide-y">
            {data.map((b) => (
              <li key={b.id} className="flex items-center justify-between gap-3 py-3">
                <span className="tabular-nums">
                  {dataHora(b.inicioEm)} <span className="text-muted-foreground">até</span> {dataHora(b.fimEm)}
                </span>
                <Button variant="ghost" size="icon" aria-label="Excluir bloqueio" onClick={() => setRemover(b.id)}>
                  <Trash2 aria-hidden="true" />
                </Button>
              </li>
            ))}
          </ul>
        )}
      </CardContent>

      <Dialog open={novo} onOpenChange={setNovo}>
        {novo && <DialogoBloqueio profissionalId={profissionalId} aoFechar={() => setNovo(false)} />}
      </Dialog>
      <AlertDialog open={!!remover} onOpenChange={(o) => !o && setRemover(undefined)}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Excluir bloqueio?</AlertDialogTitle>
            <AlertDialogDescription>O período volta a aceitar agendamentos.</AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Manter</AlertDialogCancel>
            <AlertDialogAction
              onClick={async () => {
                try {
                  await excluir.mutateAsync(remover!);
                  toast.success('Bloqueio excluído.');
                } catch (e) {
                  avisarErro(e);
                }
              }}
            >
              Excluir
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </Card>
  );
}

export function ExpedientePage() {
  const { profissionalId } = useSessao();
  if (!profissionalId)
    return (
      <>
        <Cabecalho titulo="Expediente e bloqueios" />
        <Vazio
          titulo="Cadastre um profissional primeiro"
          acao={
            <Button asChild>
              <Link to="/profissionais">Ir para profissionais</Link>
            </Button>
          }
        >
          O expediente é definido por profissional.
        </Vazio>
      </>
    );
  return (
    <>
      <Cabecalho titulo="Expediente e bloqueios" descricao={`Horários no fuso de Brasília · hoje é ${dataLocal(new Date()).split('-').reverse().join('/')}`} />
      <div className="grid gap-6">
        <EditorExpediente profissionalId={profissionalId} />
        <Bloqueios profissionalId={profissionalId} />
      </div>
    </>
  );
}
