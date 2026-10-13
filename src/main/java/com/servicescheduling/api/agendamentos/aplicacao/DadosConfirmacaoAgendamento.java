package com.servicescheduling.api.agendamentos.aplicacao;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record DadosConfirmacaoAgendamento(
        String estado,
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
