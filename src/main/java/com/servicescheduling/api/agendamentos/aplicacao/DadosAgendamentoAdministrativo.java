package com.servicescheduling.api.agendamentos.aplicacao;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record DadosAgendamentoAdministrativo(
        UUID id,
        String estado,
        String origem,
        ClienteAgendamento cliente,
        ServicoAgendamento servico,
        ProfissionalAgendamento profissional,
        OffsetDateTime inicioEm,
        OffsetDateTime fimServicoEm,
        OffsetDateTime ocupadoAte,
        String fusoHorario,
        BigDecimal precoContratado,
        String moedaContratada,
        String versaoAvisoPrivacidade,
        OffsetDateTime cienciaAvisoPrivacidadeEm,
        List<RecursoAlocado> recursos,
        OffsetDateTime criadoEm,
        UUID criadoPor,
        OffsetDateTime estadoAlteradoEm,
        UUID estadoAlteradoPor,
        String motivoCancelamento) {

    public record ClienteAgendamento(UUID id, String nome, String celular) {
    }

    public record ServicoAgendamento(UUID id, String nome, int duracaoMinutos) {
    }

    public record ProfissionalAgendamento(UUID id, String nome) {
    }

    public record RecursoAlocado(UUID recursoId, String nome, int quantidade) {
    }
}
