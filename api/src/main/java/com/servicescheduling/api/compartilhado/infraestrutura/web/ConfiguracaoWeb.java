package com.servicescheduling.api.compartilhado.infraestrutura.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConfiguracaoWeb implements WebMvcConfigurer {

    private final RegistroRequisicaoInterceptor registroRequisicaoInterceptor;
    private final LimitePublicoInterceptor limitePublicoInterceptor;

    public ConfiguracaoWeb(
            RegistroRequisicaoInterceptor registroRequisicaoInterceptor,
            LimitePublicoInterceptor limitePublicoInterceptor) {
        this.registroRequisicaoInterceptor = registroRequisicaoInterceptor;
        this.limitePublicoInterceptor = limitePublicoInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(registroRequisicaoInterceptor);
        registro.addInterceptor(limitePublicoInterceptor).addPathPatterns("/api/v1/publico/**");
    }
}
