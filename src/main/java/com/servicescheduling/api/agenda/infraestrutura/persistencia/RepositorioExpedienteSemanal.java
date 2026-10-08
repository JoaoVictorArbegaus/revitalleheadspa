package com.servicescheduling.api.agenda.infraestrutura.persistencia;

import com.servicescheduling.api.agenda.dominio.ExpedienteSemanal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RepositorioExpedienteSemanal extends JpaRepository<ExpedienteSemanal, UUID> {

    List<ExpedienteSemanal> findAllByProfissionalId(UUID profissionalId);

    @Modifying
    @Query("delete from ExpedienteSemanal e where e.profissionalId = :profissionalId")
    void excluirTodosPorProfissionalId(UUID profissionalId);
}
