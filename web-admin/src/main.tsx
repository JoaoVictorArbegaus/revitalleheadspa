import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { RouterProvider } from 'react-router';
import { ApiError, quandoSessaoExpirar } from '@/api/client';
import { chaves } from '@/api/consultas';
import { Toaster } from '@/components/ui/sonner';
import { TooltipProvider } from '@/components/ui/tooltip';
import { router } from '@/router';
import './index.css';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // 401/403/404 não melhoram com nova tentativa
      retry: (n, e) => !(e instanceof ApiError && e.status < 500) && n < 2,
      refetchOnWindowFocus: true,
    },
  },
});

// sessão expirada (30 min sem uso ou API reiniciada): limpa o usuário e volta ao login
quandoSessaoExpirar(() => {
  queryClient.setQueryData(chaves.eu, undefined);
  queryClient.removeQueries({ predicate: (q) => q.queryKey[0] !== 'eu' });
  if (!location.pathname.endsWith('/entrar')) router.navigate('/entrar', { replace: true });
});

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <TooltipProvider>
        <RouterProvider router={router} />
        <Toaster position="top-right" richColors />
      </TooltipProvider>
    </QueryClientProvider>
  </StrictMode>,
);
