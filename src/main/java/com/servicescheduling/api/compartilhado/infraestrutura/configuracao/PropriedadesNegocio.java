package com.servicescheduling.api.compartilhado.infraestrutura.configuracao;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "aplicacao.negocio")
public class PropriedadesNegocio {

    @Pattern(regexp = "[A-Z]{3}")
    private String moeda = "BRL";

    @Min(1)
    @Max(1440)
    private int intervaloGradeMinutos = 15;

    public String getMoeda() {
        return moeda;
    }

    public void setMoeda(String moeda) {
        this.moeda = moeda;
    }

    public int getIntervaloGradeMinutos() {
        return intervaloGradeMinutos;
    }

    public void setIntervaloGradeMinutos(int intervaloGradeMinutos) {
        this.intervaloGradeMinutos = intervaloGradeMinutos;
    }
}
