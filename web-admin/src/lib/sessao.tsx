import { createContext, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import type { S } from '@/api/client';
import { useProfissionais } from '@/api/consultas';

type Usuario = S['DadosUsuarioAdministrativo'];

type Contexto = {
  usuario: Usuario;
  proprietario: boolean;
  /** Profissional cujos serviços e agenda estão em foco. */
  profissionalId?: string;
  setProfissionalId: (id: string) => void;
  profissionais: S['DadosProfissional'][];
};

const Ctx = createContext<Contexto | null>(null);
const CHAVE = 'revitalle.admin.profissional';

/**
 * PROPRIETARIO escolhe o profissional (lembrado no navegador; o único ativo é escolhido sozinho).
 * FUNCIONARIO sempre usa o profissional vinculado à sessão.
 */
export function SessaoProvider({ usuario, children }: { usuario: Usuario; children: ReactNode }) {
  const proprietario = usuario.papel === 'PROPRIETARIO';
  const { data: profissionais = [] } = useProfissionais(proprietario);
  const [escolhido, setEscolhido] = useState<string | undefined>(() => {
    try {
      return localStorage.getItem(CHAVE) ?? undefined;
    } catch {
      return undefined;
    }
  });

  const ativos = useMemo(() => profissionais.filter((p) => p.ativo), [profissionais]);
  const profissionalId = proprietario
    ? (ativos.find((p) => p.id === escolhido)?.id ?? ativos[0]?.id)
    : (usuario.profissionalId ?? undefined);

  useEffect(() => {
    if (!proprietario || !profissionalId) return;
    try {
      localStorage.setItem(CHAVE, profissionalId);
    } catch {
      /* armazenamento indisponível: só não lembra a escolha */
    }
  }, [proprietario, profissionalId]);

  const valor = useMemo(
    () => ({ usuario, proprietario, profissionalId, setProfissionalId: setEscolhido, profissionais }),
    [usuario, proprietario, profissionalId, profissionais],
  );
  return <Ctx.Provider value={valor}>{children}</Ctx.Provider>;
}

export function useSessao() {
  const c = useContext(Ctx);
  if (!c) throw new Error('useSessao fora do SessaoProvider');
  return c;
}
