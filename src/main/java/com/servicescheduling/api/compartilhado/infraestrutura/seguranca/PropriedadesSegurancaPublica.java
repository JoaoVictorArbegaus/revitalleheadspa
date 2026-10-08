package com.servicescheduling.api.compartilhado.infraestrutura.seguranca;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "aplicacao.seguranca-publica")
public class PropriedadesSegurancaPublica {

    @Min(1)
    private int requisicoesMaximo = 120;

    @NotNull
    private Duration janela = Duration.ofMinutes(1);

    public int getRequisicoesMaximo() {
        return requisicoesMaximo;
    }

    public void setRequisicoesMaximo(int requisicoesMaximo) {
        this.requisicoesMaximo = requisicoesMaximo;
    }

    public Duration getJanela() {
        return janela;
    }

    public void setJanela(Duration janela) {
        if (janela != null && (janela.isZero() || janela.isNegative())) {
            throw new IllegalArgumentException("A janela do limite publico deve ser positiva");
        }
        this.janela = janela;
    }
}
