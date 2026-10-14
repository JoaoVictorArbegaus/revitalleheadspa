package com.servicescheduling.api.agenda.api;

import com.servicescheduling.api.acesso.infraestrutura.seguranca.PrincipalAdministrativo;
import com.servicescheduling.api.agenda.aplicacao.ComandoJanelaExpediente;
import com.servicescheduling.api.agenda.aplicacao.DadosBloqueioAgenda;
import com.servicescheduling.api.agenda.aplicacao.DadosExpedienteSemanal;
import com.servicescheduling.api.agenda.aplicacao.ServicoAgenda;
import com.servicescheduling.api.agenda.dominio.DiaSemana;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/administracao/agenda")
public class ControladorAgenda {

    private final ServicoAgenda servico;

    public ControladorAgenda(ServicoAgenda servico) {
        this.servico = servico;
    }

    @GetMapping("/semanal")
    public DadosExpedienteSemanal consultarExpediente(
            @RequestParam(required = false) UUID profissionalId,
            @AuthenticationPrincipal PrincipalAdministrativo principal) {
        return servico.consultarExpediente(profissionalId, principal);
    }

    @PutMapping("/semanal")
    public DadosExpedienteSemanal substituirExpediente(
            @RequestBody @Valid AlteracaoExpediente requisicao,
            @AuthenticationPrincipal PrincipalAdministrativo principal) {
        return servico.substituirExpediente(
                requisicao.profissionalId(),
                requisicao.janelas().stream()
                        .map(janela -> new ComandoJanelaExpediente(
                                janela.diaSemana(), janela.inicio(), janela.fim()))
                        .toList(),
                principal);
    }

    @GetMapping("/bloqueios")
    public List<DadosBloqueioAgenda> listarBloqueios(
            @RequestParam(required = false) UUID profissionalId,
            @Parameter(required = true) @RequestParam(required = false) LocalDate dataInicial,
            @Parameter(required = true) @RequestParam(required = false) LocalDate dataFinal,
            @AuthenticationPrincipal PrincipalAdministrativo principal) {
        return servico.listarBloqueios(profissionalId, dataInicial, dataFinal, principal);
    }

    @PostMapping("/bloqueios")
    @ApiResponse(
            responseCode = "201",
            useReturnTypeSchema = true,
            headers = @Header(name = "Location", description = "URI do bloqueio criado"))
    public ResponseEntity<DadosBloqueioAgenda> criarBloqueio(
            @RequestBody @Valid CriacaoBloqueio requisicao,
            @AuthenticationPrincipal PrincipalAdministrativo principal) {
        DadosBloqueioAgenda criado = servico.criarBloqueio(
                requisicao.profissionalId(), requisicao.inicioEm(), requisicao.fimEm(), principal);
        return ResponseEntity.created(URI.create("/api/v1/administracao/agenda/bloqueios/" + criado.id()))
                .body(criado);
    }

    @DeleteMapping("/bloqueios/{id}")
    public ResponseEntity<Void> excluirBloqueio(
            @PathVariable UUID id,
            @AuthenticationPrincipal PrincipalAdministrativo principal) {
        servico.excluirBloqueio(id, principal);
        return ResponseEntity.noContent().build();
    }

    public record AlteracaoExpediente(
            UUID profissionalId,
            @NotNull List<@NotNull @Valid JanelaExpediente> janelas) {
    }

    public record JanelaExpediente(
            @NotNull DiaSemana diaSemana,
            @NotNull @JsonFormat(pattern = "HH:mm") LocalTime inicio,
            @NotNull @JsonFormat(pattern = "HH:mm") LocalTime fim) {
    }

    public record CriacaoBloqueio(
            UUID profissionalId,
            @NotNull OffsetDateTime inicioEm,
            @NotNull OffsetDateTime fimEm) {
    }
}
