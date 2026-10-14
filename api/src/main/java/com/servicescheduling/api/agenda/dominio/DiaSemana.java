package com.servicescheduling.api.agenda.dominio;

import java.time.DayOfWeek;

public enum DiaSemana {
    SEGUNDA_FEIRA(DayOfWeek.MONDAY),
    TERCA_FEIRA(DayOfWeek.TUESDAY),
    QUARTA_FEIRA(DayOfWeek.WEDNESDAY),
    QUINTA_FEIRA(DayOfWeek.THURSDAY),
    SEXTA_FEIRA(DayOfWeek.FRIDAY),
    SABADO(DayOfWeek.SATURDAY),
    DOMINGO(DayOfWeek.SUNDAY);

    private final DayOfWeek diaJava;

    DiaSemana(DayOfWeek diaJava) {
        this.diaJava = diaJava;
    }

    public DayOfWeek diaJava() {
        return diaJava;
    }

    public static DiaSemana de(DayOfWeek dia) {
        for (DiaSemana valor : values()) {
            if (valor.diaJava == dia) {
                return valor;
            }
        }
        throw new IllegalArgumentException("Dia da semana invalido");
    }
}
