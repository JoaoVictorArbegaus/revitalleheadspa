package com.servicescheduling.api.servicos.aplicacao;

import java.util.List;
import java.util.UUID;

public record DadosServicoDisponibilidade(
        UUID id,
        UUID profissionalId,
        int duracaoMinutos,
        int intervaloLimpezaMinutos,
        boolean ativo,
        List<DadosRequisitoDisponibilidade> requisitos) {
}
