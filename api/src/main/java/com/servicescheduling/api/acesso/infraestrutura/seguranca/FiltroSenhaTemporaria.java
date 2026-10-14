package com.servicescheduling.api.acesso.infraestrutura.seguranca;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class FiltroSenhaTemporaria extends OncePerRequestFilter {

    private static final Set<String> CAMINHOS_PERMITIDOS = Set.of(
            "/api/v1/administracao/autenticacao/eu",
            "/api/v1/administracao/autenticacao/senha",
            "/api/v1/administracao/autenticacao/sair",
            "/api/v1/administracao/autenticacao/csrf");

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao != null
                && autenticacao.getPrincipal() instanceof PrincipalAdministrativo principal
                && principal.deveAlterarSenha()
                && !CAMINHOS_PERMITIDOS.contains(request.getRequestURI())) {
            throw new AccessDeniedException("A senha temporaria deve ser substituida antes desta operacao");
        }
        filterChain.doFilter(request, response);
    }
}
