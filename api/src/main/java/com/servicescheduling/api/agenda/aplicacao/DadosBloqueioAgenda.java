package com.servicescheduling.api.agenda.aplicacao;

import java.time.OffsetDateTime;
import java.util.UUID;

public record DadosBloqueioAgenda(
        UUID id,
        UUID profissionalId,
        OffsetDateTime inicioEm,
        OffsetDateTime fimEm) {
}
