package com.servicescheduling.api.profissionais.aplicacao;

import com.servicescheduling.api.profissionais.dominio.Profissional;
import com.servicescheduling.api.profissionais.infraestrutura.persistencia.RepositorioProfissional;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ConsultaProfissional {

    private final RepositorioProfissional repositorio;

    public ConsultaProfissional(RepositorioProfissional repositorio) {
        this.repositorio = repositorio;
    }

    public boolean existeAtivo(UUID id) {
        return repositorio.existsByIdAndAtivoTrue(id);
    }

    public Profissional obter(UUID id) {
        return repositorio.findById(id).orElse(null);
    }

    public List<Profissional> obterTodos(Collection<UUID> ids) {
        return repositorio.findAllByIdIn(ids);
    }

    public Profissional bloquear(UUID id) {
        return repositorio.bloquearPorId(id).orElse(null);
    }
}
