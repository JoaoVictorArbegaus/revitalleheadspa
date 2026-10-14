package com.servicescheduling.api;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio;
import com.servicescheduling.api.compartilhado.infraestrutura.seguranca.LimiteRequisicoesPublicas;
import com.servicescheduling.api.compartilhado.infraestrutura.seguranca.PropriedadesSegurancaPublica;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class SegurancaPublicaTest {

    @Test
    void limitaRequisicoesPublicasPorOrigem() {
        PropriedadesSegurancaPublica propriedades = new PropriedadesSegurancaPublica();
        propriedades.setRequisicoesMaximo(2);
        propriedades.setJanela(Duration.ofMinutes(1));
        LimiteRequisicoesPublicas limite = new LimiteRequisicoesPublicas(
                propriedades, Clock.fixed(Instant.parse("2026-10-08T12:00:00Z"), ZoneOffset.UTC));

        limite.consumir("192.0.2.1");
        limite.consumir("192.0.2.1");

        assertThatThrownBy(() -> limite.consumir("192.0.2.1"))
                .isInstanceOf(ExcecaoRegraNegocio.class)
                .extracting(excecao -> ((ExcecaoRegraNegocio) excecao).getCodigo())
                .isEqualTo("LIMITE_REQUISICOES_PUBLICAS");
        limite.consumir("192.0.2.2");
    }
}
