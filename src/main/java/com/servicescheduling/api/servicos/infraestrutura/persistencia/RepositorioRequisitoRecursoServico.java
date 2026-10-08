package com.servicescheduling.api.servicos.infraestrutura.persistencia;

import com.servicescheduling.api.servicos.dominio.RequisitoRecursoServico;
import com.servicescheduling.api.servicos.dominio.RequisitoRecursoServicoId;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepositorioRequisitoRecursoServico
        extends JpaRepository<RequisitoRecursoServico, RequisitoRecursoServicoId> {

    List<RequisitoRecursoServico> findAllByServicoIdIn(Collection<UUID> servicosIds);

    List<RequisitoRecursoServico> findAllByServicoId(UUID servicoId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from RequisitoRecursoServico r where r.servicoId = :servicoId")
    void excluirPorServicoId(@Param("servicoId") UUID servicoId);

    @Query("select coalesce(max(r.quantidade), 0) from RequisitoRecursoServico r where r.recursoOperacionalId = :recursoId")
    int maiorQuantidadePorRecursoId(@Param("recursoId") UUID recursoId);
}
