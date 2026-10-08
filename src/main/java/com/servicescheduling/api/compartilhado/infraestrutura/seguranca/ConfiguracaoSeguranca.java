package com.servicescheduling.api.compartilhado.infraestrutura.seguranca;

import com.servicescheduling.api.acesso.infraestrutura.configuracao.PropriedadesAcesso;
import com.servicescheduling.api.acesso.infraestrutura.seguranca.FiltroSenhaTemporaria;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableConfigurationProperties(PropriedadesAcesso.class)
public class ConfiguracaoSeguranca {

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    SecurityFilterChain filtroSeguranca(
            HttpSecurity http,
            CookieCsrfTokenRepository repositorioCsrf,
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolvedorExcecoes) throws Exception {
        http.authorizeHttpRequests(autorizacao -> autorizacao
                        .requestMatchers(
                                "/actuator/health/liveness",
                                "/actuator/health/readiness",
                                "/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/error")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/administracao/autenticacao/csrf")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/administracao/autenticacao/entrar")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/publico/servicos")
                        .permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/publico/servicos/*/disponibilidade")
                        .permitAll()
                        .requestMatchers("/api/v1/administracao/usuarios/**")
                        .hasRole("PROPRIETARIO")
                        .requestMatchers("/api/v1/administracao/profissionais/**")
                        .hasRole("PROPRIETARIO")
                        .requestMatchers(HttpMethod.GET, "/api/v1/administracao/recursos")
                        .authenticated()
                        .requestMatchers("/api/v1/administracao/recursos/**")
                        .hasRole("PROPRIETARIO")
                        .requestMatchers("/api/v1/administracao/servicos/**")
                        .authenticated()
                        .requestMatchers("/api/v1/administracao/**")
                        .authenticated()
                        .anyRequest()
                        .denyAll())
                .httpBasic(configuracao -> configuracao.disable())
                .formLogin(configuracao -> configuracao.disable())
                .logout(configuracao -> configuracao.disable())
                .cors(configuracao -> configuracao.disable())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(repositorioCsrf)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .sessionManagement(sessao -> sessao
                        .sessionFixation(fixacao -> fixacao.changeSessionId())
                        .maximumSessions(-1)
                        .sessionRegistry(registroSessoes())
                        .expiredSessionStrategy(evento -> resolvedorExcecoes.resolveException(
                                evento.getRequest(),
                                evento.getResponse(),
                                null,
                                new org.springframework.security.authentication.InsufficientAuthenticationException(
                                        "sessao expirada"))))
                .addFilterBefore(new FiltroSenhaTemporaria(), AuthorizationFilter.class)
                .exceptionHandling(excecoes -> excecoes
                        .authenticationEntryPoint((requisicao, resposta, excecao) ->
                                resolvedorExcecoes.resolveException(requisicao, resposta, null, excecao))
                        .accessDeniedHandler((requisicao, resposta, excecao) ->
                                resolvedorExcecoes.resolveException(requisicao, resposta, null, excecao)));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    CookieCsrfTokenRepository repositorioCsrf() {
        CookieCsrfTokenRepository repositorio = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repositorio.setCookieName("CSRF-TOKEN");
        repositorio.setHeaderName("X-CSRF-TOKEN");
        repositorio.setCookiePath("/api");
        return repositorio;
    }

    @Bean
    SessionRegistry registroSessoes() {
        return new SessionRegistryImpl();
    }

    @Bean
    HttpSessionEventPublisher publicadorEventosSessao() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuracao) throws Exception {
        return configuracao.getAuthenticationManager();
    }
}
