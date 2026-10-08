package com.servicescheduling.api.recursos.aplicacao;

import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio;
import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio.Tipo;
import com.servicescheduling.api.recursos.dominio.RecursoOperacional;
import com.servicescheduling.api.recursos.infraestrutura.persistencia.RepositorioRecursoOperacional;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ServicoRecursosOperacionais {

    private final RepositorioRecursoOperacional repositorio;
    private final ConsultaRequisitosRecurso consultaRequisitos;

    public ServicoRecursosOperacionais(
            RepositorioRecursoOperacional repositorio,
            ConsultaRequisitosRecurso consultaRequisitos) {
        this.repositorio = repositorio;
        this.consultaRequisitos = consultaRequisitos;
    }

    public List<DadosRecursoOperacional> listar() {
        return repositorio.findAllByOrderByNomeAscIdAsc().stream().map(this::dados).toList();
    }

    public DadosRecursoOperacional buscar(UUID id) {
        return dados(obter(id));
    }

    @Transactional
    public DadosRecursoOperacional criar(String nome, int capacidade, boolean ativo) {
        return dados(repositorio.saveAndFlush(RecursoOperacional.criar(nome, capacidade, ativo)));
    }

    @Transactional
    public DadosRecursoOperacional alterar(UUID id, String nome, int capacidade, boolean ativo) {
        RecursoOperacional recurso = repositorio.bloquearPorId(id).orElseThrow(this::naoEncontrado);
        if (capacidade < consultaRequisitos.maiorQuantidadeExigida(id)) {
            throw new ExcecaoRegraNegocio(
                    Tipo.CONFLITO,
                    "CAPACIDADE_INFERIOR_AO_REQUISITO",
                    "A capacidade nao pode ser menor que a quantidade exigida por um servico");
        }
        recurso.alterar(nome, capacidade, ativo);
        return dados(repositorio.saveAndFlush(recurso));
    }

    private RecursoOperacional obter(UUID id) {
        return repositorio.findById(id).orElseThrow(this::naoEncontrado);
    }

    private ExcecaoRegraNegocio naoEncontrado() {
        return new ExcecaoRegraNegocio(
                Tipo.NAO_ENCONTRADO, "RECURSO_NAO_ENCONTRADO", "Recurso operacional nao encontrado");
    }

    private DadosRecursoOperacional dados(RecursoOperacional recurso) {
        return new DadosRecursoOperacional(
                recurso.getId(), recurso.getNome(), recurso.getCapacidade(), recurso.isAtivo());
    }
}
