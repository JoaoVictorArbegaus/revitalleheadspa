package com.servicescheduling.api.servicos.aplicacao;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DadosServico(
        UUID id,
        UUID profissionalId,
        String profissionalNome,
        String nome,
        String descricao,
        int duracaoMinutos,
        int intervaloLimpezaMinutos,
        BigDecimal preco,
        String moeda,
        boolean ativo,
        int ordemExibicao,
        List<DadosRequisitoRecurso> requisitosRecursos) {

    public record DadosRequisitoRecurso(
            UUID recursoId,
            String recursoNome,
            int quantidade,
            boolean recursoAtivo) {
    }
}
