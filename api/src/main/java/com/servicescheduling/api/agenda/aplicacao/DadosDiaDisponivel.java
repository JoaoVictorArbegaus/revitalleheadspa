package com.servicescheduling.api.agenda.aplicacao;

import java.time.LocalDate;
import java.util.List;

public record DadosDiaDisponivel(LocalDate data, List<DadosHorarioDisponivel> horarios) {
}
