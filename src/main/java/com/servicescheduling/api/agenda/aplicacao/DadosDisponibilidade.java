package com.servicescheduling.api.agenda.aplicacao;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DadosDisponibilidade(
        UUID servicoId,
        String fusoHorario,
        LocalDate dataInicial,
        LocalDate dataFinal,
        List<DadosDiaDisponivel> dias) {
}
