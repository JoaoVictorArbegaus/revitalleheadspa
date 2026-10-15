import { useNavigate } from 'react-router';
import { useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { FormSenha } from '@/components/FormSenha';

/** Primeiro acesso com senha temporária: a API só libera a troca de senha. */
export function TrocarSenhaPage() {
  const navigate = useNavigate();
  const qc = useQueryClient();
  return (
    <main className="grid min-h-svh place-items-center px-6 py-12">
      <div className="w-full max-w-sm">
        <img src="/img/logo-h-marrom.webp" width={473} height={160} alt="Revitalle Head Spa" className="mb-8 h-10 w-auto" />
        <h1 className="text-3xl">Crie sua senha</h1>
        <p className="mt-1 mb-8 text-sm text-muted-foreground">Sua conta usa uma senha temporária. Defina uma senha sua para continuar.</p>
        <FormSenha
          aoConcluir={() => {
            qc.clear();
            toast.success('Senha alterada. Entre com a nova senha.');
            navigate('/entrar', { replace: true });
          }}
        />
      </div>
    </main>
  );
}
