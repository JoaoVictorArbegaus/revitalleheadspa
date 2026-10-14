package com.servicescheduling.api.agendamentos.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

@Entity
@IdClass(AlocacaoRecursoAgendamentoId.class)
@Table(name = "alocacao_recurso_agendamento")
public class AlocacaoRecursoAgendamento implements Persistable<AlocacaoRecursoAgendamentoId> {

    @Id
    @Column(name = "agendamento_id", nullable = false)
    private UUID agendamentoId;

    @Id
    @Column(name = "recurso_operacional_id", nullable = false)
    private UUID recursoOperacionalId;

    @Column(nullable = false)
    private int quantidade;

    protected AlocacaoRecursoAgendamento() {
    }

    public AlocacaoRecursoAgendamento(UUID agendamentoId, UUID recursoOperacionalId, int quantidade) {
        this.agendamentoId = agendamentoId;
        this.recursoOperacionalId = recursoOperacionalId;
        this.quantidade = quantidade;
    }

    @Override
    public AlocacaoRecursoAgendamentoId getId() {
        return new AlocacaoRecursoAgendamentoId(agendamentoId, recursoOperacionalId);
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
