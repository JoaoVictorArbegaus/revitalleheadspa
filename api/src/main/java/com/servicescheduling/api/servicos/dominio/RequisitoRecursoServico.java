package com.servicescheduling.api.servicos.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

@Entity
@IdClass(RequisitoRecursoServicoId.class)
@Table(name = "requisito_recurso_servico")
public class RequisitoRecursoServico implements Persistable<RequisitoRecursoServicoId> {

    @Id
    @Column(name = "servico_id", nullable = false)
    private UUID servicoId;

    @Id
    @Column(name = "recurso_operacional_id", nullable = false)
    private UUID recursoOperacionalId;

    @Column(nullable = false)
    private int quantidade;

    protected RequisitoRecursoServico() {
    }

    public RequisitoRecursoServico(UUID servicoId, UUID recursoOperacionalId, int quantidade) {
        this.servicoId = servicoId;
        this.recursoOperacionalId = recursoOperacionalId;
        this.quantidade = quantidade;
    }

    public UUID getServicoId() {
        return servicoId;
    }

    public UUID getRecursoOperacionalId() {
        return recursoOperacionalId;
    }

    public int getQuantidade() {
        return quantidade;
    }

    @Override
    public RequisitoRecursoServicoId getId() {
        return new RequisitoRecursoServicoId(servicoId, recursoOperacionalId);
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
