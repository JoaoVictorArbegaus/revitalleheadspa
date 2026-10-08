package com.servicescheduling.api.profissionais.aplicacao;

import java.util.UUID;

public record DadosProfissional(UUID id, String nome, boolean ativo) {
}
