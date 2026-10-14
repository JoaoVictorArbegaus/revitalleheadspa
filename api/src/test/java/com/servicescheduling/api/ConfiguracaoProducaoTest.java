package com.servicescheduling.api;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.servicescheduling.api.compartilhado.infraestrutura.configuracao.PropriedadesNegocio;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

class ConfiguracaoProducaoTest {

    @Test
    void exigeVersaoDoAvisoEmProducao() {
        assertThatThrownBy(() -> iniciarProducao(Map.of()))
                .hasStackTraceContaining("versaoAvisoPrivacidade");
    }

    @Test
    void exigeTextoDoAvisoEmProducao() {
        assertThatThrownBy(() -> iniciarProducao(Map.of(
                        "NEGOCIO_VERSAO_AVISO_PRIVACIDADE", "2026-01")))
                .hasStackTraceContaining("textoAvisoPrivacidade");
    }

    @Test
    void rejeitaAvisoVazioEmProducao() {
        assertThatThrownBy(() -> iniciarProducao(Map.of(
                        "NEGOCIO_VERSAO_AVISO_PRIVACIDADE", " ",
                        "NEGOCIO_TEXTO_AVISO_PRIVACIDADE", " ")))
                .hasStackTraceContaining("versaoAvisoPrivacidade")
                .hasStackTraceContaining("textoAvisoPrivacidade");
    }

    private void iniciarProducao(Map<String, Object> aviso) {
        Map<String, Object> propriedades = new HashMap<>(aviso);
        propriedades.put("spring.config.location", "classpath:/application-prod.yml");
        propriedades.put("DATABASE_URL", "jdbc:postgresql://localhost:5432/teste");
        propriedades.put("DATABASE_USERNAME", "teste");
        propriedades.put("DATABASE_PASSWORD", "teste");

        SpringApplication aplicacao = new SpringApplication(Configuracao.class);
        aplicacao.setWebApplicationType(WebApplicationType.NONE);
        aplicacao.setBannerMode(Banner.Mode.OFF);
        aplicacao.setDefaultProperties(propriedades);
        aplicacao.run();
    }

    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(PropriedadesNegocio.class)
    static class Configuracao {
    }
}
