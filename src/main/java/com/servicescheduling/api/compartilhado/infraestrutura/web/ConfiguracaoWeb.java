package com.servicescheduling.api.compartilhado.infraestrutura.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ConfiguracaoWeb implements WebMvcConfigurer {

    private final RegistroRequisicaoInterceptor registroRequisicaoInterceptor;

    public ConfiguracaoWeb(RegistroRequisicaoInterceptor registroRequisicaoInterceptor) {
        this.registroRequisicaoInterceptor = registroRequisicaoInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registro) {
        registro.addInterceptor(registroRequisicaoInterceptor);
    }
}
