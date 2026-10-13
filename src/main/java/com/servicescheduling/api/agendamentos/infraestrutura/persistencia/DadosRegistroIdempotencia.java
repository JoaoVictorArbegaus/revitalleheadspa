package com.servicescheduling.api.agendamentos.infraestrutura.persistencia;

public record DadosRegistroIdempotencia(
        String hashRequisicao,
        Integer statusResposta,
        String corpoResposta) {
}
