package com.servicescheduling.api.agendamentos.infraestrutura.persistencia;

import java.util.UUID;

public record DadosRegistroIdempotencia(
        String hashRequisicao,
        Integer statusResposta,
        String corpoResposta,
        UUID agendamentoId) {
}
