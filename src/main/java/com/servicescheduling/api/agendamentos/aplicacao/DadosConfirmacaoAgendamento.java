package com.servicescheduling.api.agendamentos.aplicacao;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.servicescheduling.api.agendamentos.dominio.EstadoAgendamento;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DadosConfirmacaoAgendamento(
        @Schema(allowableValues = "CONFIRMADO") EstadoAgendamento estado,
        ServicoConfirmado servico,
        ProfissionalConfirmado profissional,
        @JsonFormat(without = JsonFormat.Feature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE) OffsetDateTime inicioEm,
        @JsonFormat(without = JsonFormat.Feature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE) OffsetDateTime fimServicoEm,
        String fusoHorario,
        BigDecimal precoContratado,
        String moedaContratada) {

    public record ServicoConfirmado(UUID id, String nome, int duracaoMinutos) {
    }

    public record ProfissionalConfirmado(UUID id, String nome) {
    }
}
