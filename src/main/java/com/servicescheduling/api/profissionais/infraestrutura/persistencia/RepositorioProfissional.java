package com.servicescheduling.api.profissionais.infraestrutura.persistencia;

import com.servicescheduling.api.profissionais.dominio.Profissional;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface RepositorioProfissional extends JpaRepository<Profissional, UUID> {

    List<Profissional> findAllByOrderByNomeAscIdAsc();

    List<Profissional> findAllByIdIn(Collection<UUID> ids);

    boolean existsByIdAndAtivoTrue(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Profissional p where p.id = :id")
    java.util.Optional<Profissional> bloquearPorId(UUID id);
}
