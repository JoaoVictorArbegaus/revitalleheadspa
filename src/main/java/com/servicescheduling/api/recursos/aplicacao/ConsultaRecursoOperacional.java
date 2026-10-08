package com.servicescheduling.api.recursos.aplicacao;

import com.servicescheduling.api.recursos.dominio.RecursoOperacional;
import com.servicescheduling.api.recursos.infraestrutura.persistencia.RepositorioRecursoOperacional;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ConsultaRecursoOperacional {

    private final RepositorioRecursoOperacional repositorio;

    public ConsultaRecursoOperacional(RepositorioRecursoOperacional repositorio) {
        this.repositorio = repositorio;
    }

    public List<RecursoOperacional> obterTodos(Collection<UUID> ids) {
        return repositorio.findAllByIdIn(ids);
    }

    public List<RecursoOperacional> bloquearTodos(Collection<UUID> ids) {
        return repositorio.bloquearTodosPorId(ids);
    }
}
