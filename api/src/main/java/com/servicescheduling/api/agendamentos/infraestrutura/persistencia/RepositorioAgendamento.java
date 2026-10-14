package com.servicescheduling.api.agendamentos.infraestrutura.persistencia;

import com.servicescheduling.api.agendamentos.dominio.Agendamento;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface RepositorioAgendamento extends JpaRepository<Agendamento, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Agendamento a where a.id = :id")
    Optional<Agendamento> bloquearPorId(UUID id);
}
