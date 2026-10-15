import { useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Pencil, Plus } from 'lucide-react';
import { toast } from 'sonner';
import type { S } from '@/api/client';
import { chaves, salvarRecurso, useAcao, useRecursos } from '@/api/consultas';
import { Cabecalho, LinhasCarregando, Vazio } from '@/components/Estados';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import { Switch } from '@/components/ui/switch';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { avisarErro, marcarErrosCampos } from '@/lib/erros';
import { useSessao } from '@/lib/sessao';

const schema = z.object({
  nome: z.string().trim().min(1, 'Informe o nome').max(120),
  capacidade: z.string().trim().regex(/^\d+$/, 'Use um número inteiro').transform(Number).pipe(z.number().min(1, 'Mínimo 1').max(999, 'Máximo 999')),
  ativo: z.boolean(),
});
type Entrada = z.input<typeof schema>;
type Dados = z.output<typeof schema>;

function DialogoRecurso({ alvo, aoFechar }: { alvo: S['DadosRecursoOperacional'] | null; aoFechar: () => void }) {
  const salvar = useAcao(({ id, d }: { id?: string; d: Dados }) => salvarRecurso(id, d), [chaves.recursos, chaves.servicos]);
  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<Entrada, unknown, Dados>({
    resolver: zodResolver(schema),
    values: { nome: alvo?.nome ?? '', capacidade: String(alvo?.capacidade ?? 1), ativo: alvo?.ativo ?? true },
  });

  const onSubmit = async (d: Dados) => {
    try {
      await salvar.mutateAsync({ id: alvo?.id, d });
      toast.success(alvo?.id ? 'Recurso atualizado.' : 'Recurso cadastrado.');
      aoFechar();
    } catch (e) {
      if (!marcarErrosCampos(e, setError, ['nome', 'capacidade'])) avisarErro(e);
    }
  };

  return (
    <DialogContent>
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <DialogHeader>
          <DialogTitle>{alvo?.id ? 'Editar recurso' : 'Novo recurso'}</DialogTitle>
          <DialogDescription>
            Equipamentos ou espaços que os serviços ocupam, como a maca com lavatório. A capacidade limita quantos atendimentos usam o
            recurso ao mesmo tempo.
          </DialogDescription>
        </DialogHeader>
        <FieldGroup className="my-6">
          <Field data-invalid={!!errors.nome}>
            <FieldLabel htmlFor="nome">Nome</FieldLabel>
            <Input id="nome" autoFocus placeholder="Ex.: Maca com lavatório" {...register('nome')} />
            <FieldError>{errors.nome?.message}</FieldError>
          </Field>
          <Field data-invalid={!!errors.capacidade}>
            <FieldLabel htmlFor="capacidade">Capacidade</FieldLabel>
            <Input id="capacidade" type="number" min={1} max={999} inputMode="numeric" className="w-32" {...register('capacidade')} />
            <FieldDescription>Quantas unidades existem (ex.: 1 maca).</FieldDescription>
            <FieldError>{errors.capacidade?.message}</FieldError>
          </Field>
          <Controller
            control={control}
            name="ativo"
            render={({ field }) => (
              <Field orientation="horizontal">
                <Switch id="ativo" checked={field.value} onCheckedChange={field.onChange} />
                <FieldLabel htmlFor="ativo">Ativo</FieldLabel>
              </Field>
            )}
          />
        </FieldGroup>
        <DialogFooter>
          <Button type="button" variant="outline" onClick={aoFechar}>
            Cancelar
          </Button>
          <Button type="submit" disabled={isSubmitting}>
            {isSubmitting ? 'Salvando…' : 'Salvar'}
          </Button>
        </DialogFooter>
      </form>
    </DialogContent>
  );
}

export function RecursosPage() {
  const { proprietario } = useSessao();
  const { data, isPending } = useRecursos();
  const [alvo, setAlvo] = useState<S['DadosRecursoOperacional'] | null | undefined>(undefined);
  const novo = proprietario && (
    <Button onClick={() => setAlvo(null)}>
      <Plus aria-hidden="true" /> Novo recurso
    </Button>
  );

  return (
    <>
      <Cabecalho titulo="Recursos" descricao="Macas, lavatórios e outros equipamentos usados nos atendimentos." acao={novo} />
      {isPending ? (
        <LinhasCarregando />
      ) : !data?.length ? (
        <Vazio titulo="Nenhum recurso cadastrado" acao={novo}>
          Recursos são opcionais. Cadastre-os se quiser que a agenda respeite equipamentos limitados, como uma única maca.
        </Vazio>
      ) : (
        <div className="rounded-xl border bg-card">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nome</TableHead>
                <TableHead>Capacidade</TableHead>
                <TableHead>Situação</TableHead>
                {proprietario && <TableHead className="w-24 text-right">Ações</TableHead>}
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.map((r) => (
                <TableRow key={r.id}>
                  <TableCell className="font-medium">{r.nome}</TableCell>
                  <TableCell>{r.capacidade}</TableCell>
                  <TableCell>{r.ativo ? <Badge>Ativo</Badge> : <Badge variant="secondary">Inativo</Badge>}</TableCell>
                  {proprietario && (
                    <TableCell className="text-right">
                      <Button variant="ghost" size="icon" onClick={() => setAlvo(r)} aria-label={`Editar ${r.nome}`}>
                        <Pencil aria-hidden="true" />
                      </Button>
                    </TableCell>
                  )}
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      )}
      <Dialog open={alvo !== undefined} onOpenChange={(o) => !o && setAlvo(undefined)}>
        {alvo !== undefined && <DialogoRecurso alvo={alvo} aoFechar={() => setAlvo(undefined)} />}
      </Dialog>
    </>
  );
}
