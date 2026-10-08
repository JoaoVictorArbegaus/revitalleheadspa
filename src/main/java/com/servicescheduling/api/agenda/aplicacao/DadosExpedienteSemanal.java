package com.servicescheduling.api.agenda.aplicacao;

import java.util.List;
import java.util.UUID;

public record DadosExpedienteSemanal(UUID profissionalId, List<DadosJanelaExpediente> janelas) {
}
