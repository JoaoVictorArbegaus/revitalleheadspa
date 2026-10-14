package com.servicescheduling.api.servicos.dominio;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class RequisitoRecursoServicoId implements Serializable {

    private UUID servicoId;
    private UUID recursoOperacionalId;

    public RequisitoRecursoServicoId() {
    }

    public RequisitoRecursoServicoId(UUID servicoId, UUID recursoOperacionalId) {
        this.servicoId = servicoId;
        this.recursoOperacionalId = recursoOperacionalId;
    }

    @Override
    public boolean equals(Object objeto) {
        if (this == objeto) {
            return true;
        }
        if (!(objeto instanceof RequisitoRecursoServicoId outro)) {
            return false;
        }
        return Objects.equals(servicoId, outro.servicoId)
                && Objects.equals(recursoOperacionalId, outro.recursoOperacionalId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(servicoId, recursoOperacionalId);
    }
}
