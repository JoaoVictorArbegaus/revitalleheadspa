import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useNavigate } from 'react-router';
import { useQueryClient } from '@tanstack/react-query';
import { ApiError } from '@/api/client';
import { chaves, entrar } from '@/api/consultas';
import { Button } from '@/components/ui/button';
import { Field, FieldError, FieldGroup, FieldLabel } from '@/components/ui/field';
import { Input } from '@/components/ui/input';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { mensagemErro } from '@/lib/erros';

const schema = z.object({
  email: z.email('Informe um email válido'),
  senha: z.string().min(1, 'Informe a senha'),
});
type Dados = z.infer<typeof schema>;

export function EntrarPage() {
  const navigate = useNavigate();
  const qc = useQueryClient();
  const {
    register,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<Dados>({ resolver: zodResolver(schema) });

  const onSubmit = async (d: Dados) => {
    try {
      await entrar(d);
      await qc.invalidateQueries({ queryKey: chaves.eu });
      navigate('/', { replace: true });
    } catch (e) {
      if (e instanceof ApiError && e.status === 401) setError('root', { message: 'Email ou senha incorretos.' });
      else if (e instanceof ApiError && e.status === 429)
        setError('root', { message: `Muitas tentativas. Tente novamente em ${Math.ceil((e.retryAfter ?? 60) / 60)} min.` });
      else setError('root', { message: mensagemErro(e) });
    }
  };

  return (
    <div className="grid min-h-svh md:grid-cols-2">
      <aside className="relative hidden overflow-hidden bg-[#23140F] md:block">
        <img src="/img/hero-sala.webp" alt="" className="absolute inset-0 size-full object-cover opacity-40" />
        <div className="absolute inset-0 bg-gradient-to-t from-[#23140F] via-[#23140F]/60 to-transparent" />
        <div className="relative flex h-full flex-col justify-between p-12 text-[#EDE3D4]">
          <img src="/img/logo-h-dourado.webp" width={473} height={160} alt="Revitalle Head Spa" className="h-12 w-auto self-start" />
          <div>
            <p className="font-[family-name:var(--font-display)] text-4xl font-semibold leading-tight text-[#FBF6EE]">Agenda, serviços e horários em um só lugar.</p>
            <p className="mt-3 text-sm text-[#CDBBA5]">Painel administrativo da Revitalle Head Spa.</p>
          </div>
        </div>
      </aside>

      <main className="grid place-items-center px-6 py-12">
        <form onSubmit={handleSubmit(onSubmit)} className="w-full max-w-sm" noValidate>
          <img src="/img/logo-h-marrom.webp" width={473} height={160} alt="Revitalle Head Spa" className="mb-8 h-10 w-auto md:hidden" />
          <h1 className="text-3xl">Entrar</h1>
          <p className="mt-1 mb-8 text-sm text-muted-foreground">Use o email e a senha da sua conta.</p>
          {errors.root && (
            <Alert variant="destructive" className="mb-5">
              <AlertDescription>{errors.root.message}</AlertDescription>
            </Alert>
          )}
          <FieldGroup>
            <Field data-invalid={!!errors.email}>
              <FieldLabel htmlFor="email">Email</FieldLabel>
              <Input id="email" type="email" autoComplete="username" autoFocus aria-invalid={!!errors.email} {...register('email')} />
              <FieldError>{errors.email?.message}</FieldError>
            </Field>
            <Field data-invalid={!!errors.senha}>
              <FieldLabel htmlFor="senha">Senha</FieldLabel>
              <Input id="senha" type="password" autoComplete="current-password" aria-invalid={!!errors.senha} {...register('senha')} />
              <FieldError>{errors.senha?.message}</FieldError>
            </Field>
            <Button type="submit" size="lg" className="h-11" disabled={isSubmitting}>
              {isSubmitting ? 'Entrando…' : 'Entrar'}
            </Button>
          </FieldGroup>
          <p className="mt-6 text-xs text-muted-foreground">Esqueceu a senha? Fale com a responsável pelo painel.</p>
        </form>
      </main>
    </div>
  );
}
