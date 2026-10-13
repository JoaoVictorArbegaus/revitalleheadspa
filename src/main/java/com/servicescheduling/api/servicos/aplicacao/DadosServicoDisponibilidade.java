package com.servicescheduling.api.servicos.aplicacao;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DadosServicoDisponibilidade(
        UUID id,
        UUID profissionalId,
        String nome,
        int duracaoMinutos,
        int intervaloLimpezaMinutos,
        BigDecimal preco,
        boolean ativo,
        List<DadosRequisitoDisponibilidade> requisitos) {
}
