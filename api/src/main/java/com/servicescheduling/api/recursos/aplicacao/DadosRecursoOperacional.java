package com.servicescheduling.api.recursos.aplicacao;

import java.util.UUID;

public record DadosRecursoOperacional(UUID id, String nome, int capacidade, boolean ativo) {
}
