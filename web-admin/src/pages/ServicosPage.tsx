import { useState } from 'react';
import { Controller, useFieldArray, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Pencil, Plus, Trash2 } from 'lucide-react';
import { toast } from 'sonner';
import { Link } from 'react-router';
import type { S } from '@/api/client';
import { chaves, salvarServico, useAcao, useRecursos, useServicos } from '@/api/consultas';
import { Cabecalho, LinhasCarregando, Vazio } from '@/components/Estados';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel, FieldLegend, FieldSet } from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Switch } from '@/components/ui/switch';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { Textarea } from '@/components/ui/textarea';
import { avisarErro, marcarErrosCampos } from '@/lib/erros';
import { duracaoTexto, formatarMoeda, GRADE_MINUTOS } from '@/lib/formato';
import { useSessao } from '@/lib/sessao';

const dinheiro = z
  .string()
  .trim()
  .regex(/^\d{1,9}([.,]\d{1,2})?$/, 'Use um valor como 180 ou 180,00')
  .transform((v) => Number(v.replace(',', '.')));

const inteiro = (min: number, max: number) =>
  z.string().trim().regex(/^\d+$/, 'Use um número inteiro').transform(Number).pipe(z.number().min(min, `Mínimo ${min}`).max(max, `Máximo ${max}`));

const schema = z
  .object({
    nome: z.string().trim().min(1, 'Informe o nome').max(120),
    descricao: z.string().trim().max(1000),
    duracaoMinutos: inteiro(1, 1440),
    intervaloLimpezaMinutos: inteiro(0, 1440),
    preco: dinheiro,
    ordemExibicao: inteiro(0, 9999),
    ativo: z.boolean(),
    requisitosRecursos: z.array(z.object({ recursoId: z.string().min(1, 'Escolha o recurso'), quantidade: inteiro(1, 999) })),
  })
  .refine((d) => (d.duracaoMinutos + d.intervaloLimpezaMinutos) % GRADE_MINUTOS === 0, {
    path: ['intervaloLimpezaMinutos'],
    message: `Duração + limpeza precisa ser múltiplo de ${GRADE_MINUTOS} min`,
  })
  .refine((d) => new Set(d.requisitosRecursos.map((r) => r.recursoId)).size === d.requisitosRecursos.length, {
    path: ['requisitosRecursos'],
    message: 'O mesmo recurso aparece duas vezes',
  });
type Entrada = z.input<typeof schema>;
type Saida = z.output<typeof schema>;

const CAMPOS = ['nome', 'descricao', 'duracaoMinutos', 'intervaloLimpezaMinutos', 'preco', 'ordemExibicao'] as const;

function valoresIniciais(s: S['DadosServico'] | null, ordem: number): Entrada {
  return {
    nome: s?.nome ?? '',
    descricao: s?.descricao ?? '',
    duracaoMinutos: String(s?.duracaoMinutos ?? 60),
    intervaloLimpezaMinutos: String(s?.intervaloLimpezaMinutos ?? 0),
    preco: s?.preco !== undefined ? s.preco.toFixed(2).replace('.', ',') : '',
    ordemExibicao: String(s?.ordemExibicao ?? ordem),
    ativo: s?.ativo ?? true,
    requisitosRecursos: (s?.requisitosRecursos ?? []).map((r) => ({ recursoId: r.recursoId ?? '', quantidade: String(r.quantidade ?? 1) })),
  };
}

function DialogoServico({ alvo, proximaOrdem, aoFechar }: { alvo: S['DadosServico'] | null; proximaOrdem: number; aoFechar: () => void }) {
  const { profissionalId } = useSessao();
  const { data: recursos = [] } = useRecursos();
  const salvar = useAcao(({ id, d }: { id?: string; d: S['AlteracaoServico'] }) => salvarServico(id, d), [chaves.servicos]);
  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<Entrada, unknown, Saida>({ resolver: zodResolver(schema), values: valoresIniciais(alvo, proximaOrdem) });
  const req = useFieldArray({ control, name: 'requisitosRecursos' });

  const onSubmit = async (d: Saida) => {
    try {
      await salvar.mutateAsync({ id: alvo?.id, d: { ...d, profissionalId } });
      toast.success(alvo?.id ? 'Serviço atualizado.' : 'Serviço cadastrado.');
      aoFechar();
    } catch (e) {
      if (!marcarErrosCampos(e, setError, CAMPOS)) avisarErro(e);
    }
  };

  return (
    <DialogContent className="max-h-[92svh] overflow-y-auto sm:max-w-2xl">
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <DialogHeader>
          <DialogTitle>{alvo?.id ? 'Editar serviço' : 'Novo serviço'}</DialogTitle>
          <DialogDescription>Nome, preço e duração aparecem para o cliente na página de agendamento.</DialogDescription>
        </DialogHeader>

        <FieldGroup className="my-6">
          <Field data-invalid={!!errors.nome}>
            <FieldLabel htmlFor="nome">Nome</FieldLabel>
            <Input id="nome" autoFocus placeholder="Ex.: Hair Spa" {...register('nome')} />
            <FieldError>{errors.nome?.message}</FieldError>
          </Field>
          <Field data-invalid={!!errors.descricao}>
            <FieldLabel htmlFor="descricao">Descrição</FieldLabel>
            <Textarea id="descricao" rows={3} {...register('descricao')} />
            <FieldError>{errors.descricao?.message}</FieldError>
          </Field>

          <div className="grid gap-4 sm:grid-cols-3">
            <Field data-invalid={!!errors.preco}>
              <FieldLabel htmlFor="preco">Preço (R$)</FieldLabel>
              <Input id="preco" inputMode="decimal" placeholder="180,00" {...register('preco')} />
              <FieldError>{errors.preco?.message}</FieldError>
            </Field>
            <Field data-invalid={!!errors.duracaoMinutos}>
              <FieldLabel htmlFor="duracaoMinutos">Duração (min)</FieldLabel>
              <Input id="duracaoMinutos" type="number" min={1} step={GRADE_MINUTOS} inputMode="numeric" {...register('duracaoMinutos')} />
              <FieldError>{errors.duracaoMinutos?.message}</FieldError>
            </Field>
            <Field data-invalid={!!errors.intervaloLimpezaMinutos}>
              <FieldLabel htmlFor="intervaloLimpezaMinutos">Limpeza (min)</FieldLabel>
              <Input id="intervaloLimpezaMinutos" type="number" min={0} step={5} inputMode="numeric" {...register('intervaloLimpezaMinutos')} />
              <FieldError>{errors.intervaloLimpezaMinutos?.message}</FieldError>
            </Field>
          </div>
          <FieldDescription className="-mt-3">
            A limpeza é o tempo de preparo depois do atendimento. Duração + limpeza deve ser múltiplo de {GRADE_MINUTOS} minutos.
          </FieldDescription>

          <div className="grid gap-4 sm:grid-cols-3">
            <Field data-invalid={!!errors.ordemExibicao}>
              <FieldLabel htmlFor="ordemExibicao">Ordem na lista</FieldLabel>
              <Input id="ordemExibicao" type="number" min={0} inputMode="numeric" {...register('ordemExibicao')} />
              <FieldError>{errors.ordemExibicao?.message}</FieldError>
            </Field>
            <Controller
              control={control}
              name="ativo"
              render={({ field }) => (
                <Field orientation="horizontal" className="self-end pb-2 sm:col-span-2">
                  <Switch id="ativo" checked={field.value} onCheckedChange={field.onChange} />
                  <FieldLabel htmlFor="ativo">Disponível para agendamento</FieldLabel>
                </Field>
              )}
            />
          </div>

          <FieldSet>
            <FieldLegend variant="label">Recursos usados</FieldLegend>
            <FieldDescription>
              Opcional. Se o serviço ocupa a maca ou outro equipamento, a agenda não marca mais atendimentos do que a capacidade.
            </FieldDescription>
            {req.fields.map((f, i) => (
              <div key={f.id} className="flex items-start gap-2">
                <Controller
                  control={control}
                  name={`requisitosRecursos.${i}.recursoId`}
                  render={({ field }) => (
                    <Select value={field.value} onValueChange={field.onChange}>
                      <SelectTrigger className="flex-1" aria-label="Recurso">
                        <SelectValue placeholder="Escolha o recurso" />
                      </SelectTrigger>
                      <SelectContent>
                        {recursos.map((r) => (
                          <SelectItem key={r.id} value={r.id!}>
                            {r.nome} {r.ativo ? '' : '(inativo)'}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  )}
                />
                <Input aria-label="Quantidade" type="number" min={1} className="w-20" {...register(`requisitosRecursos.${i}.quantidade`)} />
                <Button type="button" variant="ghost" size="icon" onClick={() => req.remove(i)} aria-label="Remover recurso">
                  <Trash2 aria-hidden="true" />
                </Button>
              </div>
            ))}
            <FieldError>{errors.requisitosRecursos?.message ?? errors.requisitosRecursos?.root?.message}</FieldError>
            {recursos.length ? (
              <Button type="button" variant="outline" size="sm" className="justify-self-start" onClick={() => req.append({ recursoId: '', quantidade: '1' })}>
                <Plus aria-hidden="true" /> Adicionar recurso
              </Button>
            ) : (
              <p className="text-sm text-muted-foreground">
                Nenhum recurso cadastrado. <Link to="/recursos" className="underline underline-offset-4">Cadastrar recursos</Link>
              </p>
            )}
          </FieldSet>
        </FieldGroup>

        <DialogFooter>
          <Button type="button" variant="outline" onClick={aoFechar}>
            Cancelar
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Salvando…' : 'Salvar serviço'}
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  );
}

export function ServicosPage() {
  const { profissionalId, proprietario } = useSessao();
  const { data, isPending } = useServicos();
  const [alvo, setAlvo] = useState<S['DadosServico'] | null | undefined>(undefined);
  // PROPRIETARIO vê todos; a lista mostra só os do profissional em foco
  const servicos = (data ?? [])
    .filter((s) => !proprietario || s.profissionalId === profissionalId)
    .sort((a, b) => (a.ordemExibicao ?? 0) - (b.ordemExibicao ?? 0) || (a.nome ?? '').localeCompare(b.nome ?? ''));
  const proximaOrdem = servicos.reduce((m, s) => Math.max(m, (s.ordemExibicao ?? 0) + 1), 1);

  if (!profissionalId)
    return (
      <>
        <Cabecalho titulo="Serviços" />
        <Vazio
          titulo="Cadastre um profissional primeiro"
          acao={
            <Button asChild>
              <Link to="/profissionais">Ir para profissionais</Link>
            </Button>
          }
        >
          Cada serviço pertence a um profissional.
        </Vazio>
      </>
    );

  const novo = (
    <Button onClick={() => setAlvo(null)}>
      <Plus aria-hidden="true" /> Novo serviço
    </Button>
  );

  return (
    <>
      <Cabecalho titulo="Serviços" descricao="O que pode ser agendado, com preço e duração." acao={novo} />
      {isPending ? (
        <LinhasCarregando />
      ) : !servicos.length ? (
        <Vazio titulo="Nenhum serviço ainda" acao={novo}>
          Cadastre os tratamentos (Consulta, Hair Spa, Anti-Queda…) com preço e duração. Eles aparecem na página de agendamento.
        </Vazio>
      ) : (
        <div className="rounded-xl border bg-card">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className="w-12">#</TableHead>
                <TableHead>Serviço</TableHead>
                <TableHead>Duração</TableHead>
                <TableHead>Preço</TableHead>
                <TableHead>Situação</TableHead>
                <TableHead className="w-20 text-right">Ações</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {servicos.map((s) => (
                <TableRow key={s.id}>
                  <TableCell className="text-muted-foreground tabular-nums">{s.ordemExibicao}</TableCell>
                  <TableCell>
                    <p className="font-medium">{s.nome}</p>
                    {!!s.requisitosRecursos?.length && (
                      <p className="text-xs text-muted-foreground">Usa {s.requisitosRecursos.map((r) => `${r.recursoNome} ×${r.quantidade}`).join(', ')}</p>
                    )}
                  </TableCell>
                  <TableCell className="whitespace-nowrap">
                    {duracaoTexto(s.duracaoMinutos)}
                    {!!s.intervaloLimpezaMinutos && <span className="text-muted-foreground"> + {s.intervaloLimpezaMinutos}min</span>}
                  </TableCell>
                  <TableCell className="whitespace-nowrap tabular-nums">{formatarMoeda(s.preco)}</TableCell>
                  <TableCell>{s.ativo ? <Badge>Ativo</Badge> : <Badge variant="secondary">Oculto</Badge>}</TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="icon" onClick={() => setAlvo(s)} aria-label={`Editar ${s.nome}`}>
                      <Pencil aria-hidden="true" />
                    </Button>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
      <Dialog open={alvo !== undefined} onOpenChange={(o) => !o && setAlvo(undefined)}>
        {alvo !== undefined && <DialogoServico alvo={alvo} proximaOrdem={proximaOrdem} aoFechar={() => setAlvo(undefined)} />}
      </Dialog>
    </>
  );
}
