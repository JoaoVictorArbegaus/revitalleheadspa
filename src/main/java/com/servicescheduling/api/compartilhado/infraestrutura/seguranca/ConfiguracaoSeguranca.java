package com.servicescheduling.api.compartilhado.infraestrutura.seguranca;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
public class ConfiguracaoSeguranca {

    @Bean
    SecurityFilterChain filtroSeguranca(
            HttpSecurity http,
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
                        .requestMatchers("/api/v1/administracao/**")
                        .authenticated()
                        .anyRequest()
                        .denyAll())
                .httpBasic(configuracao -> configuracao.disable())
                .formLogin(configuracao -> configuracao.disable())
                .logout(configuracao -> configuracao.disable())
                .exceptionHandling(excecoes -> excecoes
                        .authenticationEntryPoint((requisicao, resposta, excecao) ->
                                resolvedorExcecoes.resolveException(requisicao, resposta, null, excecao))
                        .accessDeniedHandler((requisicao, resposta, excecao) ->
                                resolvedorExcecoes.resolveException(requisicao, resposta, null, excecao)));
        return http.build();
    }
}
