package com.servicescheduling.api.profissionais.api;

import com.servicescheduling.api.profissionais.aplicacao.DadosProfissional;
import com.servicescheduling.api.profissionais.aplicacao.ServicoProfissionais;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/administracao/profissionais")
public class ControladorProfissionais {

    private final ServicoProfissionais servico;

    public ControladorProfissionais(ServicoProfissionais servico) {
        this.servico = servico;
    }

    @GetMapping
    List<DadosProfissional> listar() {
        return servico.listar();
    }

    @GetMapping("/{id}")
    DadosProfissional buscar(@PathVariable UUID id) {
        return servico.buscar(id);
    }

    @PostMapping
    ResponseEntity<DadosProfissional> criar(@Valid @RequestBody AlteracaoProfissional requisicao) {
        DadosProfissional criado = servico.criar(requisicao.nome(), requisicao.ativo());
        return ResponseEntity.created(URI.create("/api/v1/administracao/profissionais/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    DadosProfissional alterar(@PathVariable UUID id, @Valid @RequestBody AlteracaoProfissional requisicao) {
        return servico.alterar(id, requisicao.nome(), requisicao.ativo());
    }

    public record AlteracaoProfissional(
            @NotBlank @Size(max = 120) String nome,
            @NotNull Boolean ativo) {
    }
}
