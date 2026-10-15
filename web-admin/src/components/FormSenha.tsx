import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { ApiError } from '@/api/client';
import { alterarSenha } from '@/api/consultas';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { Field, FieldDescription, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import { mensagemErro } from '@/lib/erros';

const bytes = (s: string) => new TextEncoder().encode(s).length;
const schema = z
  .object({
    senhaAtual: z.string().min(1, 'Informe a senha atual'),
    novaSenha: z.string().refine((s) => bytes(s) >= 12 && bytes(s) <= 72, 'Use entre 12 e 72 caracteres'),
    confirmacao: z.string(),
  })
  .refine((d) => d.novaSenha === d.confirmacao, { path: ['confirmacao'], message: 'As senhas não conferem' });
type Dados = z.infer<typeof schema>;

/** Troca de senha: a API encerra todas as sessões depois dela, então `aoConcluir` leva ao login. */
export function FormSenha({ aoConcluir }: { aoConcluir: () => void }) {
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<Dados>({ resolver: zodResolver(schema) });

  const onSubmit = async ({ senhaAtual, novaSenha }: Dados) => {
    try {
      await alterarSenha({ senhaAtual, novaSenha });
      aoConcluir();
    } catch (e) {
      if (e instanceof ApiError && e.codigo === 'SENHA_ATUAL_INVALIDA') setError('senhaAtual', { message: 'Senha atual incorreta' });
      else setError('root', { message: mensagemErro(e) });
    }
  };

  return (
    <form onSubmit={handleSubmit(onSubmit)} noValidate className="max-w-sm">
      {errors.root && (
        <Alert variant="destructive" className="mb-5">
          <AlertDescription>{errors.root.message}</AlertDescription>
        </Alert>
      )}
      <FieldGroup>
        <Field data-invalid={!!errors.senhaAtual}>
          <FieldLabel htmlFor="senhaAtual">Senha atual</FieldLabel>
          <Input id="senhaAtual" type="password" autoComplete="current-password" {...register('senhaAtual')} />
          <FieldError>{errors.senhaAtual?.message}</FieldError>
        </Field>
        <Field data-invalid={!!errors.novaSenha}>
          <FieldLabel htmlFor="novaSenha">Nova senha</FieldLabel>
          <Input id="novaSenha" type="password" autoComplete="new-password" {...register('novaSenha')} />
          <FieldDescription>Entre 12 e 72 caracteres. Uma frase longa é mais segura e fácil de lembrar.</FieldDescription>
          <FieldError>{errors.novaSenha?.message}</FieldError>
        </Field>
        <Field data-invalid={!!errors.confirmacao}>
          <FieldLabel htmlFor="confirmacao">Repita a nova senha</FieldLabel>
          <Input id="confirmacao" type="password" autoComplete="new-password" {...register('confirmacao')} />
          <FieldError>{errors.confirmacao?.message}</FieldError>
        </Field>
        <Button type="submit" size="lg" className="h-11" disabled={isSubmitting}>
          {isSubmitting ? 'Salvando…' : 'Trocar senha'}
        </Button>
      </FieldGroup>
    </form>
  );
}
