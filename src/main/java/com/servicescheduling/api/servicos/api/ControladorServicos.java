package com.servicescheduling.api.servicos.api;

import com.servicescheduling.api.acesso.infraestrutura.seguranca.PrincipalAdministrativo;
import com.servicescheduling.api.servicos.aplicacao.ComandoServico;
import com.servicescheduling.api.servicos.aplicacao.ComandoServico.ComandoRequisitoRecurso;
import com.servicescheduling.api.servicos.aplicacao.DadosServico;
import com.servicescheduling.api.servicos.aplicacao.ServicoCatalogo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/administracao/servicos")
public class ControladorServicos {

    private final ServicoCatalogo servico;

    public ControladorServicos(ServicoCatalogo servico) {
        this.servico = servico;
    }

    @GetMapping
    List<DadosServico> listar(@AuthenticationPrincipal PrincipalAdministrativo principal) {
        return servico.listar(principal);
    }

    @GetMapping("/{id}")
    DadosServico buscar(
            @PathVariable UUID id,
            @AuthenticationPrincipal PrincipalAdministrativo principal) {
        return servico.buscar(id, principal);
    }

    @PostMapping
    ResponseEntity<DadosServico> criar(
            @Valid @RequestBody AlteracaoServico requisicao,
            @AuthenticationPrincipal PrincipalAdministrativo principal) {
        DadosServico criado = servico.criar(requisicao.comando(), principal);
        return ResponseEntity.created(URI.create("/api/v1/administracao/servicos/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    DadosServico alterar(
            @PathVariable UUID id,
            @Valid @RequestBody AlteracaoServico requisicao,
            @AuthenticationPrincipal PrincipalAdministrativo principal) {
        return servico.alterar(id, requisicao.comando(), principal);
    }

    public record AlteracaoServico(
            UUID profissionalId,
            @NotBlank @Size(max = 120) String nome,
            @Size(max = 1000) String descricao,
            @Min(1) @Max(1440) int duracaoMinutos,
            @Min(0) @Max(1440) int intervaloLimpezaMinutos,
            @NotNull @DecimalMin("0.00") @DecimalMax("999999999.99") @Digits(integer = 9, fraction = 2)
            BigDecimal preco,
            @NotNull Boolean ativo,
            @Min(0) @Max(9999) int ordemExibicao,
            @NotNull List<@NotNull @Valid RequisitoRecurso> requisitosRecursos) {

        ComandoServico comando() {
            return new ComandoServico(
                    profissionalId,
                    nome,
                    descricao,
                    duracaoMinutos,
                    intervaloLimpezaMinutos,
                    preco,
                    ativo,
                    ordemExibicao,
                    requisitosRecursos.stream()
                            .map(requisito -> new ComandoRequisitoRecurso(
                                    requisito.recursoId(), requisito.quantidade()))
                            .toList());
        }
    }

    public record RequisitoRecurso(
            @NotNull UUID recursoId,
            @Min(1) @Max(999) int quantidade) {
    }
}
