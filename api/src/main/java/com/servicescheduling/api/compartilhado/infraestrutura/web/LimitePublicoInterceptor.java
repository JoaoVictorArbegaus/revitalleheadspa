package com.servicescheduling.api.compartilhado.infraestrutura.web;

import com.servicescheduling.api.compartilhado.infraestrutura.seguranca.LimiteRequisicoesPublicas;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LimitePublicoInterceptor implements HandlerInterceptor {

    private final LimiteRequisicoesPublicas limite;

    public LimitePublicoInterceptor(LimiteRequisicoesPublicas limite) {
        this.limite = limite;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        limite.consumir(request.getRemoteAddr());
        return true;
    }
}
