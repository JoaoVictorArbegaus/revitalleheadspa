package com.servicescheduling.api.servicos.aplicacao;

import java.math.BigDecimal;
import java.util.UUID;

public record DadosServicoPublico(
        UUID id,
        String nome,
        String descricao,
        int duracaoMinutos,
        BigDecimal preco,
        String moeda,
        UUID profissionalId,
        String profissionalNome) {
}
