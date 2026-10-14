package com.servicescheduling.api.recursos.api;

import com.servicescheduling.api.recursos.aplicacao.DadosRecursoOperacional;
import com.servicescheduling.api.recursos.aplicacao.ServicoRecursosOperacionais;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
@RequestMapping("/api/v1/administracao/recursos")
public class ControladorRecursosOperacionais {

    private final ServicoRecursosOperacionais servico;

    public ControladorRecursosOperacionais(ServicoRecursosOperacionais servico) {
        this.servico = servico;
    }

    @GetMapping
    List<DadosRecursoOperacional> listar() {
        return servico.listar();
    }

    @GetMapping("/{id}")
    DadosRecursoOperacional buscar(@PathVariable UUID id) {
        return servico.buscar(id);
    }

    @PostMapping
    ResponseEntity<DadosRecursoOperacional> criar(@Valid @RequestBody AlteracaoRecurso requisicao) {
        DadosRecursoOperacional criado = servico.criar(
                requisicao.nome(), requisicao.capacidade(), requisicao.ativo());
        return ResponseEntity.created(URI.create("/api/v1/administracao/recursos/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    DadosRecursoOperacional alterar(@PathVariable UUID id, @Valid @RequestBody AlteracaoRecurso requisicao) {
        return servico.alterar(id, requisicao.nome(), requisicao.capacidade(), requisicao.ativo());
    }

    public record AlteracaoRecurso(
            @NotBlank @Size(max = 120) String nome,
            @Min(1) @Max(999) int capacidade,
            @NotNull Boolean ativo) {
    }
}
