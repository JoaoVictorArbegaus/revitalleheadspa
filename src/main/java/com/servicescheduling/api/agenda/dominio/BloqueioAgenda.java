package com.servicescheduling.api.agenda.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bloqueio_agenda")
public class BloqueioAgenda {

    @Id
    private UUID id;

    @Column(name = "profissional_id", nullable = false)
    private UUID profissionalId;

    @Column(name = "inicio_em", nullable = false)
    private Instant inicioEm;

    @Column(name = "fim_em", nullable = false)
    private Instant fimEm;

    protected BloqueioAgenda() {
    }

    private BloqueioAgenda(UUID id, UUID profissionalId, Instant inicioEm, Instant fimEm) {
        this.id = id;
        this.profissionalId = profissionalId;
        this.inicioEm = inicioEm;
        this.fimEm = fimEm;
    }

    public static BloqueioAgenda criar(UUID profissionalId, Instant inicioEm, Instant fimEm) {
        return new BloqueioAgenda(UUID.randomUUID(), profissionalId, inicioEm, fimEm);
    }

    public UUID getId() {
        return id;
    }

    public UUID getProfissionalId() {
        return profissionalId;
    }

    public Instant getInicioEm() {
        return inicioEm;
    }

    public Instant getFimEm() {
        return fimEm;
    }
}
