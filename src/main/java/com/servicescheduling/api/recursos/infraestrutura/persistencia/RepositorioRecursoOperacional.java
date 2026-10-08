package com.servicescheduling.api.recursos.infraestrutura.persistencia;

import com.servicescheduling.api.recursos.dominio.RecursoOperacional;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepositorioRecursoOperacional extends JpaRepository<RecursoOperacional, UUID> {

    List<RecursoOperacional> findAllByOrderByNomeAscIdAsc();

    List<RecursoOperacional> findAllByIdIn(Collection<UUID> ids);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RecursoOperacional r where r.id = :id")
    Optional<RecursoOperacional> bloquearPorId(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RecursoOperacional r where r.id in :ids order by r.id")
    List<RecursoOperacional> bloquearTodosPorId(@Param("ids") Collection<UUID> ids);
}
