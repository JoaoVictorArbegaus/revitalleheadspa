import { useNavigate } from 'react-router';
import { useQueryClient } from '@tanstack/react-query';
import { toast } from 'sonner';
import { Cabecalho } from '@/components/Estados';
import { FormSenha } from '@/components/FormSenha';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { useSessao } from '@/lib/sessao';

export function ContaPage() {
  const { usuario } = useSessao();
  const navigate = useNavigate();
  const qc = useQueryClient();
  return (
    <>
      <Cabecalho titulo="Minha conta" descricao={`${usuario.nome} · ${usuario.email}`} />
      <Card className="max-w-xl">
        <CardHeader>
          <CardTitle className="text-xl">Trocar senha</CardTitle>
          <CardDescription>Depois da troca, todas as suas sessões são encerradas e você entra de novo.</CardDescription>
        </CardHeader>
        <CardContent>
          <FormSenha
            aoConcluir={() => {
              qc.clear();
              toast.success('Senha alterada. Entre com a nova senha.');
              navigate('/entrar', { replace: true });
            }}
          />
        </CardContent>
      </Card>
    </>
  );
}
