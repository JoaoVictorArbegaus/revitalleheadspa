package com.servicescheduling.api.agenda.aplicacao;

import java.time.OffsetDateTime;

public record DadosHorarioDisponivel(OffsetDateTime inicioEm, OffsetDateTime fimServicoEm) {
}
