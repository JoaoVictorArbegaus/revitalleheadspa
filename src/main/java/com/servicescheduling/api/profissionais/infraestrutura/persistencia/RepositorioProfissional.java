package com.servicescheduling.api.profissionais.infraestrutura.persistencia;

import com.servicescheduling.api.profissionais.dominio.Profissional;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioProfissional extends JpaRepository<Profissional, UUID> {

    List<Profissional> findAllByOrderByNomeAscIdAsc();

    List<Profissional> findAllByIdIn(Collection<UUID> ids);

    boolean existsByIdAndAtivoTrue(UUID id);
}
