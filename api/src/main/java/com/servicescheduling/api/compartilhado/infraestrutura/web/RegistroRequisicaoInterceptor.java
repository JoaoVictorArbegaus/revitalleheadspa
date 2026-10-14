package com.servicescheduling.api.compartilhado.infraestrutura.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;

@Component
public class RegistroRequisicaoInterceptor implements HandlerInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(RegistroRequisicaoInterceptor.class);
    private static final String ATRIBUTO_INICIO = RegistroRequisicaoInterceptor.class.getName() + ".inicioNanos";

    @Override
    public boolean preHandle(HttpServletRequest requisicao, HttpServletResponse resposta, Object handler) {
        requisicao.setAttribute(ATRIBUTO_INICIO, System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest requisicao,
            HttpServletResponse resposta,
            Object handler,
            Exception excecao) {
        Object inicio = requisicao.getAttribute(ATRIBUTO_INICIO);
        long duracaoMs = inicio instanceof Long inicioNanos
                ? (System.nanoTime() - inicioNanos) / 1_000_000
                : 0;
        Object rotaMapeada = requisicao.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        String rota = rotaMapeada == null ? "NAO_MAPEADA" : rotaMapeada.toString();

        LOG.info(
                "requisicao metodo={} rota={} estado={} duracaoMs={}",
                requisicao.getMethod(),
                rota,
                resposta.getStatus(),
                duracaoMs);
    }
}
