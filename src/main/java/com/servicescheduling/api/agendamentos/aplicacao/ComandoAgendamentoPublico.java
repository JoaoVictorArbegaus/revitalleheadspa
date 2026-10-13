package com.servicescheduling.api.agendamentos.aplicacao;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ComandoAgendamentoPublico(
        UUID servicoId,
        OffsetDateTime inicioEm,
        String nomeCliente,
        String celularCliente,
        String versaoAvisoPrivacidade,
        boolean cienteAvisoPrivacidade) {
}
