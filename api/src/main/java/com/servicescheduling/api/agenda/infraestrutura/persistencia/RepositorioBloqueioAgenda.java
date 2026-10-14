package com.servicescheduling.api.agenda.infraestrutura.persistencia;

import com.servicescheduling.api.agenda.dominio.BloqueioAgenda;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioBloqueioAgenda extends JpaRepository<BloqueioAgenda, UUID> {

    List<BloqueioAgenda> findAllByProfissionalIdAndInicioEmLessThanAndFimEmGreaterThanOrderByInicioEmAsc(
            UUID profissionalId, Instant fimEm, Instant inicioEm);

    boolean existsByProfissionalIdAndInicioEmLessThanAndFimEmGreaterThan(
            UUID profissionalId, Instant fimEm, Instant inicioEm);
}
