package com.servicescheduling.api.agendamentos.dominio;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class AlocacaoRecursoAgendamentoId implements Serializable {

    private UUID agendamentoId;
    private UUID recursoOperacionalId;

    public AlocacaoRecursoAgendamentoId() {
    }

    public AlocacaoRecursoAgendamentoId(UUID agendamentoId, UUID recursoOperacionalId) {
        this.agendamentoId = agendamentoId;
        this.recursoOperacionalId = recursoOperacionalId;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof AlocacaoRecursoAgendamentoId outro)) {
            return false;
        }
        return Objects.equals(agendamentoId, outro.agendamentoId)
                && Objects.equals(recursoOperacionalId, outro.recursoOperacionalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agendamentoId, recursoOperacionalId);
    }
}
