package com.servicescheduling.api.agendamentos.infraestrutura.persistencia;

import com.servicescheduling.api.agendamentos.dominio.Agendamento;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioAgendamento extends JpaRepository<Agendamento, UUID> {
}
