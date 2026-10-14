package com.servicescheduling.api.compartilhado.infraestrutura.configuracao;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.ZoneId;
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

    @NotNull
    private ZoneId fusoHorario = ZoneId.of("America/Sao_Paulo");

    @Min(0)
    @Max(43200)
    private int antecedenciaMinimaReservaMinutos = 60;

    @Min(1)
    @Max(365)
    private int horizonteReservaDias = 90;

    @NotBlank
    @Size(max = 64)
    private String versaoAvisoPrivacidade = "desenvolvimento";

    @NotBlank
    @Size(max = 10000)
    private String textoAvisoPrivacidade = "Aviso de privacidade para desenvolvimento local.";

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

    public ZoneId getFusoHorario() {
        return fusoHorario;
    }

    public void setFusoHorario(ZoneId fusoHorario) {
        this.fusoHorario = fusoHorario;
    }

    public int getAntecedenciaMinimaReservaMinutos() {
        return antecedenciaMinimaReservaMinutos;
    }

    public void setAntecedenciaMinimaReservaMinutos(int antecedenciaMinimaReservaMinutos) {
        this.antecedenciaMinimaReservaMinutos = antecedenciaMinimaReservaMinutos;
    }

    public int getHorizonteReservaDias() {
        return horizonteReservaDias;
    }

    public void setHorizonteReservaDias(int horizonteReservaDias) {
        this.horizonteReservaDias = horizonteReservaDias;
    }

    public String getVersaoAvisoPrivacidade() {
        return versaoAvisoPrivacidade;
    }

    public void setVersaoAvisoPrivacidade(String versaoAvisoPrivacidade) {
        this.versaoAvisoPrivacidade = versaoAvisoPrivacidade;
    }

    public String getTextoAvisoPrivacidade() {
        return textoAvisoPrivacidade;
    }

    public void setTextoAvisoPrivacidade(String textoAvisoPrivacidade) {
        this.textoAvisoPrivacidade = textoAvisoPrivacidade;
    }
}
