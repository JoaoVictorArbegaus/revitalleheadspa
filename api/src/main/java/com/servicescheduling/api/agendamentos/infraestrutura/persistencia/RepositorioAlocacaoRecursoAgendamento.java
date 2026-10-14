package com.servicescheduling.api.agendamentos.infraestrutura.persistencia;

import com.servicescheduling.api.agendamentos.dominio.AlocacaoRecursoAgendamento;
import com.servicescheduling.api.agendamentos.dominio.AlocacaoRecursoAgendamentoId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RepositorioAlocacaoRecursoAgendamento
        extends JpaRepository<AlocacaoRecursoAgendamento, AlocacaoRecursoAgendamentoId> {
}
