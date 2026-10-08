package com.servicescheduling.api.agenda.aplicacao;

import com.servicescheduling.api.agenda.dominio.DiaSemana;
import java.time.LocalTime;

public record ComandoJanelaExpediente(DiaSemana diaSemana, LocalTime inicio, LocalTime fim) {
}
