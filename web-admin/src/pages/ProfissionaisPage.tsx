import { useState } from 'react';
import { Controller, useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { Pencil, Plus } from 'lucide-react';
import { toast } from 'sonner';
import type { S } from '@/api/client';
import { chaves, salvarProfissional, useAcao, useProfissionais } from '@/api/consultas';
import { Cabecalho, LinhasCarregando, Vazio } from '@/components/Estados';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogDescription, DialogFooter, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import { Switch } from '@/components/ui/switch';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { avisarErro, marcarErrosCampos } from '@/lib/erros';

const schema = z.object({ nome: z.string().trim().min(1, 'Informe o nome').max(120), ativo: z.boolean() });
type Dados = z.infer<typeof schema>;

function DialogoProfissional({ alvo, aoFechar }: { alvo: S['DadosProfissional'] | null; aoFechar: () => void }) {
  const salvar = useAcao(({ id, d }: { id?: string; d: Dados }) => salvarProfissional(id, d), [chaves.profissionais]);
  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<Dados>({ resolver: zodResolver(schema), values: { nome: alvo?.nome ?? '', ativo: alvo?.ativo ?? true } });

  const onSubmit = async (d: Dados) => {
    try {
      await salvar.mutateAsync({ id: alvo?.id, d });
      toast.success(alvo?.id ? 'Profissional atualizado.' : 'Profissional cadastrado.');
      aoFechar();
    } catch (e) {
      if (!marcarErrosCampos(e, setError, ['nome'])) avisarErro(e);
    }
  };

  return (
    <DialogContent>
      <form onSubmit={handleSubmit(onSubmit)} noValidate>
        <DialogHeader>
          <DialogTitle>{alvo?.id ? 'Editar profissional' : 'Novo profissional'}</DialogTitle>
          <DialogDescription>Quem realiza os atendimentos. Cada profissional tem seus serviços, expediente e agenda.</DialogDescription>
        </DialogHeader>
        <FieldGroup className="my-6">
          <Field data-invalid={!!errors.nome}>
            <FieldLabel htmlFor="nome">Nome</FieldLabel>
            <Input id="nome" autoFocus {...register('nome')} />
            <FieldError>{errors.nome?.message}</FieldError>
          </Field>
          <Controller
            control={control}
            name="ativo"
            render={({ field }) => (
              <Field orientation="horizontal">
                <Switch id="ativo" checked={field.value} onCheckedChange={field.onChange} />
                <FieldLabel htmlFor="ativo">Ativo</FieldLabel>
                <FieldDescription className="sr-only">Profissionais inativos não recebem agendamentos.</FieldDescription>
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

export function ProfissionaisPage() {
  const { data, isPending } = useProfissionais();
  const [alvo, setAlvo] = useState<S['DadosProfissional'] | null | undefined>(undefined);

  return (
    <>
      <Cabecalho
        titulo="Profissionais"
        descricao="Quem atende na Revitalle."
        acao={
          <Button onClick={() => setAlvo(null)}>
            <Plus aria-hidden="true" /> Novo profissional
          </Button>
        }
      />
      {isPending ? (
        <LinhasCarregando />
      ) : !data?.length ? (
        <Vazio
          titulo="Nenhum profissional ainda"
          acao={
            <Button onClick={() => setAlvo(null)}>
              <Plus aria-hidden="true" /> Cadastrar profissional
            </Button>
          }
        >
          Comece cadastrando quem realiza os atendimentos. Os serviços e o expediente são definidos por profissional.
        </Vazio>
      ) : (
        <div className="rounded-xl border bg-card">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Nome</TableHead>
                <TableHead>Situação</TableHead>
                <TableHead className="w-24 text-right">Ações</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {data.map((p) => (
                <TableRow key={p.id}>
                  <TableCell className="font-medium">{p.nome}</TableCell>
                  <TableCell>{p.ativo ? <Badge>Ativo</Badge> : <Badge variant="secondary">Inativo</Badge>}</TableCell>
                  <TableCell className="text-right">
                    <Button variant="ghost" size="icon" onClick={() => setAlvo(p)} aria-label={`Editar ${p.nome}`}>
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
        {alvo !== undefined && <DialogoProfissional alvo={alvo} aoFechar={() => setAlvo(undefined)} />}
      </Dialog>
    </>
  );
}
