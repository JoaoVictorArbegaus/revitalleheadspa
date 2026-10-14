package com.servicescheduling.api.clientes.aplicacao;

import java.time.Instant;
import java.util.UUID;

public interface ReservasCliente {

    boolean possuiReservaFuturaConfirmada(UUID clienteId, Instant agora);

    void removerDadosPessoais(UUID clienteId, Instant agora);
}
