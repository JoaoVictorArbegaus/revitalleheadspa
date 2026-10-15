import { createBrowserRouter, Navigate, Outlet } from 'react-router';
import { useEu } from '@/api/consultas';
import { Layout } from '@/components/Layout';
import { Carregando } from '@/components/Estados';
import { SessaoProvider } from '@/lib/sessao';
import { AgendaPage } from '@/pages/AgendaPage';
import { ContaPage } from '@/pages/ContaPage';
import { EntrarPage } from '@/pages/EntrarPage';
import { ExpedientePage } from '@/pages/ExpedientePage';
import { ProfissionaisPage } from '@/pages/ProfissionaisPage';
import { RecursosPage } from '@/pages/RecursosPage';
import { ServicosPage } from '@/pages/ServicosPage';
import { TrocarSenhaPage } from '@/pages/TrocarSenhaPage';
import { VisaoGeralPage } from '@/pages/VisaoGeralPage';

/** Exige sessão; com senha temporária, só a troca de senha fica disponível. */
function ComSessao({ trocaDeSenha = false }: { trocaDeSenha?: boolean }) {
  const { data: usuario, isPending, isError } = useEu();
  if (isPending) return <Carregando tela />;
  if (isError || !usuario) return <Navigate to="/entrar" replace />;
  if (usuario.deveAlterarSenha && !trocaDeSenha) return <Navigate to="/senha" replace />;
  if (!usuario.deveAlterarSenha && trocaDeSenha) return <Navigate to="/" replace />;
  return (
    <SessaoProvider usuario={usuario}>
      <Outlet />
    </SessaoProvider>
  );
}

function SoProprietario() {
  const { data } = useEu();
  return data?.papel === 'PROPRIETARIO' ? <Outlet /> : <Navigate to="/" replace />;
}

export const router = createBrowserRouter(
  [
    { path: '/entrar', element: <EntrarPage /> },
    { element: <ComSessao trocaDeSenha />, children: [{ path: '/senha', element: <TrocarSenhaPage /> }] },
    {
      element: <ComSessao />,
      children: [
        {
          element: <Layout />,
          children: [
            { index: true, element: <AgendaPage /> },
            { path: 'visao-geral', element: <VisaoGeralPage /> },
            { path: 'servicos', element: <ServicosPage /> },
            { path: 'expediente', element: <ExpedientePage /> },
            { path: 'recursos', element: <RecursosPage /> },
            { element: <SoProprietario />, children: [{ path: 'profissionais', element: <ProfissionaisPage /> }] },
            { path: 'conta', element: <ContaPage /> },
            { path: '*', element: <Navigate to="/" replace /> },
          ],
        },
      ],
    },
  ],
  { basename: '/admin' },
);
