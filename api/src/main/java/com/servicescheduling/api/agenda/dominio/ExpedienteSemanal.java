package com.servicescheduling.api.agenda.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "expediente_semanal")
public class ExpedienteSemanal {

    @Id
    private UUID id;

    @Column(name = "profissional_id", nullable = false)
    private UUID profissionalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "dia_semana", nullable = false, length = 20)
    private DiaSemana diaSemana;

    @Column(nullable = false)
    private LocalTime inicio;

    @Column(nullable = false)
    private LocalTime fim;

    protected ExpedienteSemanal() {
    }

    private ExpedienteSemanal(UUID id, UUID profissionalId, DiaSemana diaSemana, LocalTime inicio, LocalTime fim) {
        this.id = id;
        this.profissionalId = profissionalId;
        this.diaSemana = diaSemana;
        this.inicio = inicio;
        this.fim = fim;
    }

    public static ExpedienteSemanal criar(
            UUID profissionalId, DiaSemana diaSemana, LocalTime inicio, LocalTime fim) {
        return new ExpedienteSemanal(UUID.randomUUID(), profissionalId, diaSemana, inicio, fim);
    }

    public UUID getId() {
        return id;
    }

    public UUID getProfissionalId() {
        return profissionalId;
    }

    public DiaSemana getDiaSemana() {
        return diaSemana;
    }

    public LocalTime getInicio() {
        return inicio;
    }

    public LocalTime getFim() {
        return fim;
    }
}
