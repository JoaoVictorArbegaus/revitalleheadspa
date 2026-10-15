import { useEffect, useMemo, useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { toast } from 'sonner';
import { ApiError } from '@/api/client';
import { criarAgendamento, useAcao, useAgendamentos, useAvisoPrivacidade, useBloqueios, useExpediente, useServicos } from '@/api/consultas';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { Checkbox } from '@/components/ui/checkbox';
import { DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { avisarErro, marcarErrosCampos, mensagemErro } from '@/lib/erros';
import { diaDaSemana, duracaoTexto, formatarMoeda, GRADE_MINUTOS, instante, paraHhmm, paraMinutos } from '@/lib/formato';
import { useSessao } from '@/lib/sessao';

const schema = z.object({
  servicoId: z.string().min(1, 'Escolha o serviço'),
  data: z.string().min(1, 'Informe a data'),
  hora: z.string().regex(/^\d{2}:\d{2}$/, 'Escolha um horário'),
  nome: z.string().trim().min(1, 'Informe o nome do cliente'),
  celular: z.string().trim().min(8, 'Informe o celular com DDD'),
  ciente: z.literal(true, 'Confirme que o cliente conhece o aviso de privacidade'),
});
type Entrada = z.input<typeof schema>;
type Saida = z.output<typeof schema>;

/**
 * Agendamento feito pelo painel (ex.: pedido pelo WhatsApp). Mesmas regras da reserva pública:
 * respeita expediente, bloqueios e ocupação; aceita horários dentro da antecedência mínima, mas não no passado.
 */
export function NovoAgendamento({ profissionalId, dataSugerida, aoFechar }: { profissionalId: string; dataSugerida: string; aoFechar: () => void }) {
  const { proprietario } = useSessao();
  const { data: servicos = [] } = useServicos();
  const aviso = useAvisoPrivacidade(true);
  // uma chave por intenção: repetir após falha de rede não duplica; mudar de horário após conflito gera nova
  const [chave, setChave] = useState(() => crypto.randomUUID());
  const criar = useAcao((d: Saida) => criarAgendamento(
    {
      servicoId: d.servicoId,
      inicioEm: instante(d.data, d.hora),
      cliente: { nome: d.nome, celular: d.celular },
      versaoAvisoPrivacidade: aviso.data?.versao ?? '',
      cienteAvisoPrivacidade: true,
    },
    chave,
  ), [['agendamentos']]);

  const ativos = servicos.filter((s) => s.ativo && (!proprietario || s.profissionalId === profissionalId));
  const {
    register,
    control,
    handleSubmit,
    watch,
    setValue,
    setError,
    formState: { errors },
  } = useForm<Entrada, unknown, Saida>({
    resolver: zodResolver(schema),
    defaultValues: { servicoId: '', data: dataSugerida, hora: '', nome: '', celular: '', ciente: false as unknown as true },
  });
  const servico = ativos.find((s) => s.id === watch('servicoId'));
  const data = watch('data');

  // Horários oferecidos seguem as regras da API: grade de 15 min, duração + limpeza inteira dentro de
  // uma janela do expediente, fora de bloqueios e sem colidir com reservas confirmadas. Capacidade de
  // recursos e corridas com outras reservas continuam sendo decididas pela API na gravação.
  const { data: expediente } = useExpediente(profissionalId);
  const { data: bloqueios = [] } = useBloqueios(profissionalId, data || dataSugerida, data || dataSugerida);
  const { data: doDia = [] } = useAgendamentos(
    { dataInicial: data || dataSugerida, dataFinal: data || dataSugerida, profissionalId: proprietario ? profissionalId : undefined, estado: 'CONFIRMADO' },
    !!data,
  );
  const horarios = useMemo(() => {
    if (!servico || !data || !expediente) return [];
    const ocupa = (servico.duracaoMinutos ?? 0) + (servico.intervaloLimpezaMinutos ?? 0);
    const ocupados = [
      ...bloqueios.map((b) => [new Date(b.inicioEm!).getTime(), new Date(b.fimEm!).getTime()]),
      ...doDia.map((a) => [new Date(a.inicioEm!).getTime(), new Date(a.ocupadoAte ?? a.fimServicoEm!).getTime()]),
    ];
    const agora = Date.now();
    const lista: string[] = [];
    for (const j of (expediente.janelas ?? []).filter((j) => j.diaSemana === diaDaSemana(data))) {
      const ini = Math.ceil(paraMinutos(j.inicio!) / GRADE_MINUTOS) * GRADE_MINUTOS;
      for (let m = ini; m + ocupa <= paraMinutos(j.fim!); m += GRADE_MINUTOS) {
        const h = paraHhmm(m);
        const t0 = new Date(instante(data, h)).getTime();
        const t1 = t0 + ocupa * 60_000;
        if (t0 > agora && !ocupados.some(([a, b]) => t0 < b && a < t1)) lista.push(h);
      }
    }
    return lista;
  }, [servico, data, expediente, bloqueios, doDia]);
  // trocar serviço ou data pode invalidar o horário escolhido
  const horaAtual = watch('hora');
  useEffect(() => {
    if (horaAtual && !horarios.includes(horaAtual)) setValue('hora', '');
  }, [horarios, horaAtual, setValue]);
  const diaFechado = !!expediente && !!data && !(expediente.janelas ?? []).some((j) => j.diaSemana === diaDaSemana(data));

  const onSubmit = async (d: Saida) => {
    try {
      await criar.mutateAsync(d);
      toast.success('Agendamento criado.');
      aoFechar();
    } catch (e) {
      const codigo = e instanceof ApiError ? e.codigo : undefined;
      if (codigo === 'CONFLITO_AGENDAMENTO' || codigo === 'CONFLITO_IDEMPOTENCIA') setChave(crypto.randomUUID());
      if (codigo === 'AVISO_PRIVACIDADE_DESATUALIZADO') aviso.refetch();
      if (codigo === 'CONFLITO_AGENDAMENTO' || codigo === 'INICIO_NO_PASSADO') setError('hora', { message: mensagemErro(e) });
      else if (!marcarErrosCampos(e, setError, ['nome', 'celular'])) avisarErro(e);
    }
  };

  return (
    <DialogContent className="max-h-[92svh] overflow-y-auto sm:max-w-lg">
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <DialogHeader>
          <DialogTitle>Novo agendamento</DialogTitle>
          <DialogDescription>Para pedidos recebidos por WhatsApp ou telefone. O horário precisa estar dentro do expediente e livre.</DialogDescription>
        </DialogHeader>

        {!ativos.length ? (
          <Alert className="my-6">
            <AlertDescription>Nenhum serviço ativo. Cadastre um serviço antes de agendar.</AlertDescription>
          </Alert>
        ) : (
          <FieldGroup className="my-6">
            <Controller
              control={control}
              name="servicoId"
              render={({ field }) => (
                <Field data-invalid={!!errors.servicoId}>
                  <FieldLabel htmlFor="servicoId">Serviço</FieldLabel>
                  <Select value={field.value} onValueChange={field.onChange}>
                    <SelectTrigger id="servicoId">
                      <SelectValue placeholder="Escolha o serviço" />
                    </SelectTrigger>
                    <SelectContent>
                      {ativos.map((s) => (
                        <SelectItem key={s.id} value={s.id!}>
                          {s.nome} · {duracaoTexto(s.duracaoMinutos)} · {formatarMoeda(s.preco)}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <FieldError>{errors.servicoId?.message}</FieldError>
                </Field>
              )}
            />
            <div className="grid grid-cols-2 gap-3">
              <Field data-invalid={!!errors.data}>
                <FieldLabel htmlFor="data">Data</FieldLabel>
                <Input id="data" type="date" {...register('data')} />
                <FieldError>{errors.data?.message}</FieldError>
              </Field>
              <Controller
                control={control}
                name="hora"
                render={({ field }) => (
                  <Field data-invalid={!!errors.hora}>
                    <FieldLabel htmlFor="hora">Horário</FieldLabel>
                    <Select value={field.value} onValueChange={field.onChange} disabled={!servico || !horarios.length}>
                      <SelectTrigger id="hora">
                        <SelectValue placeholder={!servico ? 'Escolha o serviço' : horarios.length ? 'Escolha' : 'Sem horários'} />
                      </SelectTrigger>
                      <SelectContent className="max-h-72">
                        {horarios.map((h) => (
                          <SelectItem key={h} value={h}>
                            {h}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </Field>
                )}
              />
            </div>
            {errors.hora && <FieldError className="-mt-3">{errors.hora.message}</FieldError>}
            {servico && !errors.hora && (
              <FieldDescription className="-mt-3">
                {diaFechado
                  ? 'Sem expediente neste dia. Escolha outra data ou ajuste o expediente.'
                  : !horarios.length
                    ? 'Nenhum horário livre neste dia para este serviço.'
                    : `Duração: ${duracaoTexto(servico.duracaoMinutos)}${servico.intervaloLimpezaMinutos ? ` + ${servico.intervaloLimpezaMinutos}min de limpeza` : ''}. Horários de ${GRADE_MINUTOS} em ${GRADE_MINUTOS} minutos.`}
              </FieldDescription>
            )}

            <Field data-invalid={!!errors.nome}>
              <FieldLabel htmlFor="nome">Nome do cliente</FieldLabel>
              <Input id="nome" autoComplete="off" {...register('nome')} />
              <FieldError>{errors.nome?.message}</FieldError>
            </Field>
            <Field data-invalid={!!errors.celular}>
              <FieldLabel htmlFor="celular">Celular</FieldLabel>
              <Input id="celular" type="tel" inputMode="tel" placeholder="(49) 99999-9999" autoComplete="off" {...register('celular')} />
              <FieldError>{errors.celular?.message}</FieldError>
            </Field>

            <Controller
              control={control}
              name="ciente"
              render={({ field }) => (
                <Field orientation="horizontal" data-invalid={!!errors.ciente}>
                  <Checkbox id="ciente" checked={field.value === true} onCheckedChange={(v) => field.onChange(v === true)} />
                  <div className="grid gap-1">
                    <FieldLabel htmlFor="ciente" className="font-normal">
                      O cliente recebeu o aviso de privacidade{aviso.data?.versao ? ` (versão ${aviso.data.versao})` : ''}.
                    </FieldLabel>
                    {aviso.data?.texto && (
                      <details className="text-xs text-muted-foreground">
                        <summary className="cursor-pointer">Ver o texto do aviso</summary>
                        <p className="mt-1 whitespace-pre-line">{aviso.data.texto}</p>
                      </details>
                    )}
                    <FieldError>{errors.ciente?.message}</FieldError>
                  </div>
                </Field>
              )}
            />
          </FieldGroup>
        )}

        <DialogFooter>
          <Button type="button" variant="outline" onClick={aoFechar}>
            Cancelar
          </Button>
          <Button type="submit" disabled={!ativos.length || criar.isPending || !aviso.data}>
            {criar.isPending ? 'Agendando…' : 'Agendar'}
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  );
}
