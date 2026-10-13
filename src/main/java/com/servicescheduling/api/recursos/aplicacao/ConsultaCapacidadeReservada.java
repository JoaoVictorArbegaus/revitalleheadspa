package com.servicescheduling.api.recursos.aplicacao;

import java.time.Instant;
import java.util.UUID;

public interface ConsultaCapacidadeReservada {

    int maiorConsumoSimultaneo(UUID recursoId, Instant agora);
}
