package com.servicescheduling.api.servicos.aplicacao;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ComandoServico(
        UUID profissionalId,
        String nome,
        String descricao,
        int duracaoMinutos,
        int intervaloLimpezaMinutos,
        BigDecimal preco,
        boolean ativo,
        int ordemExibicao,
        List<ComandoRequisitoRecurso> requisitosRecursos) {

    public record ComandoRequisitoRecurso(UUID recursoId, int quantidade) {
    }
}
