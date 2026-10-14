package com.servicescheduling.api.clientes.aplicacao;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DadosCliente(
        UUID id,
        String nome,
        String celular,
        OffsetDateTime anonimizadoEm) {
}
