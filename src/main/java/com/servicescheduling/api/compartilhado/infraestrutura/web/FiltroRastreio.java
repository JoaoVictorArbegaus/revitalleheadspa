package com.servicescheduling.api.compartilhado.infraestrutura.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FiltroRastreio extends OncePerRequestFilter {

    public static final String CABECALHO_ID_RASTREIO = "X-Id-Rastreio";
    public static final String ATRIBUTO_ID_RASTREIO = FiltroRastreio.class.getName() + ".idRastreio";

    @Override
    protected void doFilterInternal(
            HttpServletRequest requisicao,
            HttpServletResponse resposta,
            FilterChain cadeia) throws ServletException, IOException {
        String idRastreio = obterIdRastreio(requisicao.getHeader(CABECALHO_ID_RASTREIO));
        requisicao.setAttribute(ATRIBUTO_ID_RASTREIO, idRastreio);
        resposta.setHeader(CABECALHO_ID_RASTREIO, idRastreio);

        try (MDC.MDCCloseable ignored = MDC.putCloseable("idRastreio", idRastreio)) {
            cadeia.doFilter(requisicao, resposta);
        }
    }

    private String obterIdRastreio(String valorRecebido) {
        if (valorRecebido != null) {
            try {
                return UUID.fromString(valorRecebido).toString();
            } catch (IllegalArgumentException ignored) {
                // Valores externos fora do formato nunca chegam aos logs.
            }
        }
        return UUID.randomUUID().toString();
    }
}
