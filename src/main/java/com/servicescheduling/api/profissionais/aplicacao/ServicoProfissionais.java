package com.servicescheduling.api.profissionais.aplicacao;

import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio;
import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio.Tipo;
import com.servicescheduling.api.profissionais.dominio.Profissional;
import com.servicescheduling.api.profissionais.infraestrutura.persistencia.RepositorioProfissional;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ServicoProfissionais {

    private final RepositorioProfissional repositorio;

    public ServicoProfissionais(RepositorioProfissional repositorio) {
        this.repositorio = repositorio;
    }

    public List<DadosProfissional> listar() {
        return repositorio.findAllByOrderByNomeAscIdAsc().stream().map(this::dados).toList();
    }

    public DadosProfissional buscar(UUID id) {
        return dados(obter(id));
    }

    @Transactional
    public DadosProfissional criar(String nome, boolean ativo) {
        return dados(repositorio.saveAndFlush(Profissional.criar(nome, ativo)));
    }

    @Transactional
    public DadosProfissional alterar(UUID id, String nome, boolean ativo) {
        Profissional profissional = obter(id);
        profissional.alterar(nome, ativo);
        return dados(repositorio.saveAndFlush(profissional));
    }

    private Profissional obter(UUID id) {
        return repositorio.findById(id).orElseThrow(() -> new ExcecaoRegraNegocio(
                Tipo.NAO_ENCONTRADO, "PROFISSIONAL_NAO_ENCONTRADO", "Profissional nao encontrado"));
    }

    private DadosProfissional dados(Profissional profissional) {
        return new DadosProfissional(profissional.getId(), profissional.getNome(), profissional.isAtivo());
    }
}
