package com.servicescheduling.api.acesso.api;

import com.servicescheduling.api.acesso.aplicacao.DadosUsuarioAdministrativo;
import com.servicescheduling.api.acesso.aplicacao.ServicoAutenticacaoAdministrativa;
import com.servicescheduling.api.acesso.dominio.UsuarioAdministrativo;
import com.servicescheduling.api.acesso.infraestrutura.seguranca.PrincipalAdministrativo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@RequestMapping("/api/v1/administracao/autenticacao")
public class ControladorAutenticacao {

    private final ServicoAutenticacaoAdministrativa servico;
    private final SessionRegistry registroSessoes;
    private final CookieCsrfTokenRepository repositorioCsrf;
    private final HttpSessionSecurityContextRepository repositorioContexto =
            new HttpSessionSecurityContextRepository();

    public ControladorAutenticacao(
            ServicoAutenticacaoAdministrativa servico,
            SessionRegistry registroSessoes,
            CookieCsrfTokenRepository repositorioCsrf) {
        this.servico = servico;
        this.registroSessoes = registroSessoes;
        this.repositorioCsrf = repositorioCsrf;
    }

    @GetMapping("/csrf")
    TokenCsrf obterCsrf(CsrfToken token, HttpServletRequest request, HttpServletResponse response) {
        repositorioCsrf.saveToken(token, request, response);
        return new TokenCsrf(token.getToken(), token.getHeaderName());
    }

    @PostMapping("/entrar")
    ResponseEntity<Void> entrar(
            @Valid @RequestBody Credenciais credenciais,
            HttpServletRequest request,
            HttpServletResponse response) {
        Authentication autenticacao = servico.autenticar(
                credenciais.email(), credenciais.senha(), request.getRemoteAddr());
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(autenticacao);
        SecurityContextHolder.setContext(contexto);
        repositorioContexto.saveContext(contexto, request, response);
        registroSessoes.registerNewSession(request.getSession().getId(), autenticacao.getPrincipal());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/eu")
    DadosUsuarioAdministrativo eu(@AuthenticationPrincipal PrincipalAdministrativo principal) {
        return servico.dadosAtuais(principal);
    }

    @PutMapping("/senha")
    ResponseEntity<Void> alterarSenha(
            @AuthenticationPrincipal PrincipalAdministrativo principal,
            @Valid @RequestBody AlteracaoSenha requisicao,
            HttpServletRequest request) {
        servico.alterarSenha(principal.id(), requisicao.senhaAtual(), requisicao.novaSenha());
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/sair")
    ResponseEntity<Void> sair(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.getSession(false).invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    public record TokenCsrf(String token, String nomeCabecalho) {
    }

    public record Credenciais(@NotBlank @Email String email, @NotBlank String senha) {
        public Credenciais {
            email = UsuarioAdministrativo.normalizarEmail(email);
        }
    }

    public record AlteracaoSenha(@NotBlank String senhaAtual, @NotBlank String novaSenha) {
    }
}
