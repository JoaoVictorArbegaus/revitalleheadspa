import { NavLink, Outlet, useLocation, useNavigate } from 'react-router';
import { useQueryClient } from '@tanstack/react-query';
import { Boxes, CalendarDays, ChartNoAxesColumn, Clock, LogOut, Sparkles, UserRound, Users } from 'lucide-react';
import { sair } from '@/api/consultas';
import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarGroup,
  SidebarGroupContent,
  SidebarGroupLabel,
  SidebarHeader,
  SidebarInset,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  SidebarProvider,
  SidebarTrigger,
  useSidebar,
} from '@/components/ui/sidebar';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Separator } from '@/components/ui/separator';
import { useSessao } from '@/lib/sessao';

const NAV = [
  { to: '/', rotulo: 'Agenda', icone: CalendarDays, end: true },
  { to: '/visao-geral', rotulo: 'Visão geral', icone: ChartNoAxesColumn },
  { to: '/servicos', rotulo: 'Serviços', icone: Sparkles },
  { to: '/expediente', rotulo: 'Expediente e bloqueios', icone: Clock },
  { to: '/recursos', rotulo: 'Recursos', icone: Boxes },
  { to: '/profissionais', rotulo: 'Profissionais', icone: Users, soProprietario: true },
];

function Navegacao() {
  const { proprietario } = useSessao();
  const { setOpenMobile } = useSidebar();
  const { pathname } = useLocation();
  return (
    <SidebarMenu>
      {NAV.filter((n) => !n.soProprietario || proprietario).map((n) => (
        <SidebarMenuItem key={n.to}>
          <SidebarMenuButton asChild isActive={n.end ? pathname === n.to : pathname.startsWith(n.to)} size="lg" className="text-[0.95rem]">
            <NavLink to={n.to} end={n.end} onClick={() => setOpenMobile(false)}>
              <n.icone aria-hidden="true" />
              <span>{n.rotulo}</span>
            </NavLink>
          </SidebarMenuButton>
        </SidebarMenuItem>
      ))}
    </SidebarMenu>
  );
}

/** Seletor do profissional em foco (só PROPRIETARIO com mais de um profissional ativo). */
function SeletorProfissional() {
  const { proprietario, profissionais, profissionalId, setProfissionalId } = useSessao();
  const ativos = profissionais.filter((p) => p.ativo);
  if (!proprietario || ativos.length < 2) return null;
  return (
    <Select value={profissionalId} onValueChange={setProfissionalId}>
      <SelectTrigger className="w-56 bg-card" aria-label="Profissional em foco">
        <SelectValue placeholder="Profissional" />
      </SelectTrigger>
      <SelectContent>
        {ativos.map((p) => (
          <SelectItem key={p.id} value={p.id!}>
            {p.nome}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
}

export function Layout() {
  const { usuario } = useSessao();
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const qc = useQueryClient();

  const encerrar = async () => {
    try {
      await sair();
    } finally {
      qc.clear();
      navigate('/entrar', { replace: true });
    }
  };

  return (
    <SidebarProvider>
      <Sidebar collapsible="offcanvas">
        <SidebarHeader className="px-4 pt-5 pb-3">
          <img src="/img/logo-h-dourado.webp" width={473} height={160} alt="Revitalle Head Spa" className="h-10 w-auto self-start" />
          <p className="mt-1 text-xs tracking-[0.18em] text-sidebar-foreground/60 uppercase">Painel</p>
        </SidebarHeader>
        <SidebarContent>
          <SidebarGroup>
            <SidebarGroupLabel className="text-sidebar-foreground/50">Gestão</SidebarGroupLabel>
            <SidebarGroupContent>
              <Navegacao />
            </SidebarGroupContent>
          </SidebarGroup>
        </SidebarContent>
        <SidebarFooter className="p-3">
          <SidebarMenu>
            <SidebarMenuItem>
              <SidebarMenuButton asChild isActive={pathname === '/conta'} size="lg">
                <NavLink to="/conta">
                  <UserRound aria-hidden="true" />
                  <span className="grid leading-tight">
                    <span className="truncate font-medium">{usuario.nome}</span>
                    <span className="truncate text-xs text-sidebar-foreground/60">
                      {usuario.papel === 'PROPRIETARIO' ? 'Proprietária' : 'Profissional'}
                    </span>
                  </span>
                </NavLink>
              </SidebarMenuButton>
            </SidebarMenuItem>
            <SidebarMenuItem>
              <SidebarMenuButton onClick={encerrar}>
                <LogOut aria-hidden="true" />
                <span>Sair</span>
              </SidebarMenuButton>
            </SidebarMenuItem>
          </SidebarMenu>
        </SidebarFooter>
      </Sidebar>

      <SidebarInset className="bg-background">
        <header className="sticky top-0 z-10 flex h-14 items-center gap-3 border-b bg-background/85 px-4 backdrop-blur md:px-8">
          <SidebarTrigger className="-ml-1" />
          <Separator orientation="vertical" className="h-5" />
          <span className="text-sm text-muted-foreground">Revitalle Head Spa</span>
          <div className="ml-auto">
            <SeletorProfissional />
          </div>
        </header>
        <main className="mx-auto w-full max-w-6xl px-4 py-8 md:px-8">
          <Outlet />
        </main>
      </SidebarInset>
    </SidebarProvider>
  );
}
