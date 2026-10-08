package com.servicescheduling.api.servicos.aplicacao;

import java.util.UUID;

public record DadosRequisitoDisponibilidade(UUID recursoId, int quantidade) {
}
