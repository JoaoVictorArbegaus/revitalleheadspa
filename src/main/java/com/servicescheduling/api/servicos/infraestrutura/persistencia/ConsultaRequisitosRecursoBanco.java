package com.servicescheduling.api.servicos.infraestrutura.persistencia;

import com.servicescheduling.api.recursos.aplicacao.ConsultaRequisitosRecurso;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ConsultaRequisitosRecursoBanco implements ConsultaRequisitosRecurso {

    private final RepositorioRequisitoRecursoServico repositorio;

    public ConsultaRequisitosRecursoBanco(RepositorioRequisitoRecursoServico repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public int maiorQuantidadeExigida(UUID recursoId) {
        return repositorio.maiorQuantidadePorRecursoId(recursoId);
    }
}
