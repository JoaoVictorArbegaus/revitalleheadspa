package com.servicescheduling.api.servicos.aplicacao;

import com.servicescheduling.api.servicos.dominio.Servico;
import com.servicescheduling.api.servicos.infraestrutura.persistencia.RepositorioRequisitoRecursoServico;
import com.servicescheduling.api.servicos.infraestrutura.persistencia.RepositorioServico;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ConsultaServicoDisponibilidade {

    private final RepositorioServico repositorioServico;
    private final RepositorioRequisitoRecursoServico repositorioRequisito;

    public ConsultaServicoDisponibilidade(
            RepositorioServico repositorioServico,
            RepositorioRequisitoRecursoServico repositorioRequisito) {
        this.repositorioServico = repositorioServico;
        this.repositorioRequisito = repositorioRequisito;
    }

    public DadosServicoDisponibilidade obter(UUID id) {
        Servico servico = repositorioServico.findById(id).orElse(null);
        return dados(servico);
    }

    public DadosServicoDisponibilidade bloquear(UUID id) {
        Servico servico = repositorioServico.bloquearPorId(id).orElse(null);
        return dados(servico);
    }

    private DadosServicoDisponibilidade dados(Servico servico) {
        if (servico == null) {
            return null;
        }
        return new DadosServicoDisponibilidade(
                servico.getId(),
                servico.getProfissionalId(),
                servico.getNome(),
                servico.getDuracaoMinutos(),
                servico.getIntervaloLimpezaMinutos(),
                servico.getPreco(),
                servico.isAtivo(),
                repositorioRequisito.findAllByServicoId(servico.getId()).stream()
                        .map(requisito -> new DadosRequisitoDisponibilidade(
                                requisito.getRecursoOperacionalId(), requisito.getQuantidade()))
                        .toList());
    }
}
