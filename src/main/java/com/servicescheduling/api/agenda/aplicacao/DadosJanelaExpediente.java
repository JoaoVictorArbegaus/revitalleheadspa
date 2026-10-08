package com.servicescheduling.api.agenda.aplicacao;

import com.servicescheduling.api.agenda.dominio.DiaSemana;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record DadosJanelaExpediente(
        DiaSemana diaSemana,
        @JsonFormat(pattern = "HH:mm") LocalTime inicio,
        @JsonFormat(pattern = "HH:mm") LocalTime fim) {
}
