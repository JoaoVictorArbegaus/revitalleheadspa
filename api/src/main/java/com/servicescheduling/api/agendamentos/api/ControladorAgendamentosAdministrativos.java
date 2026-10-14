package com.servicescheduling.api.agendamentos.api;

import com.servicescheduling.api.acesso.infraestrutura.seguranca.PrincipalAdministrativo;
import com.servicescheduling.api.agendamentos.api.ControladorAgendamentosPublicos.CriacaoAgendamento;
import com.servicescheduling.api.agendamentos.aplicacao.ComandoCriacaoAgendamento;
import com.servicescheduling.api.agendamentos.aplicacao.DadosAgendamentoAdministrativo;
import com.servicescheduling.api.agendamentos.aplicacao.ServicoAgendamentosAdministrativos;
import com.servicescheduling.api.agendamentos.dominio.EstadoAgendamento;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/administracao/agendamentos")
public class ControladorAgendamentosAdministrativos {

    private final ServicoAgendamentosAdministrativos servico;

    public ControladorAgendamentosAdministrativos(ServicoAgendamentosAdministrativos servico) {
        this.servico = servico;
    }

    @GetMapping
    public List<DadosAgendamentoAdministrativo> listar(
            @Parameter(required = true) @RequestParam(required = false) LocalDate dataInicial,
            @Parameter(required = true) @RequestParam(required = false) LocalDate dataFinal,
            @RequestParam(required = false) UUID profissionalId,
            @RequestParam(required = false) EstadoAgendamento estado,
            @AuthenticationPrincipal PrincipalAdministrativo principal) {
        return servico.listar(dataInicial, dataFinal, profissionalId, estado, principal);
    }

    @GetMapping("/{id}")
    public DadosAgendamentoAdministrativo obter(
            @PathVariable UUID id,
            @AuthenticationPrincipal PrincipalAdministrativo principal) {
        return servico.obter(id, principal);
    }

    @PostMapping
    @ApiResponse(
            responseCode = "201",
            useReturnTypeSchema = true,
            headers = @Header(name = "Location", description = "URI do agendamento criado"))
    public ResponseEntity<DadosAgendamentoAdministrativo> criar(
            @Parameter(required = true)
            @RequestHeader(value = "Chave-Idempotencia", required = false)
            @NotBlank
            @Pattern(regexp = "[A-Za-z0-9._~-]{16,128}") String chaveIdempotencia,
            @RequestBody @Valid CriacaoAgendamento requisicao,
            @AuthenticationPrincipal PrincipalAdministrativo principal) {
        DadosAgendamentoAdministrativo criado = servico.criar(
                new ComandoCriacaoAgendamento(
                        requisicao.servicoId(),
                        requisicao.inicioEm(),
                        requisicao.cliente().nome(),
                        requisicao.cliente().celular(),
                        requisicao.versaoAvisoPrivacidade(),
                        requisicao.cienteAvisoPrivacidade()),
                chaveIdempotencia,
                principal);
        return ResponseEntity.created(URI.create("/api/v1/administracao/agendamentos/" + criado.id()))
                .body(criado);
    }

    @PostMapping("/{id}/cancelar")
    public DadosAgendamentoAdministrativo cancelar(
            @PathVariable UUID id,
            @RequestBody @Valid Cancelamento requisicao,
            @AuthenticationPrincipal PrincipalAdministrativo principal) {
        return servico.cancelar(id, requisicao.motivo(), principal);
    }

    @PostMapping("/{id}/concluir")
    public DadosAgendamentoAdministrativo concluir(
            @PathVariable UUID id,
            @AuthenticationPrincipal PrincipalAdministrativo principal) {
        return servico.encerrar(id, EstadoAgendamento.CONCLUIDO, principal);
    }

    @PostMapping("/{id}/nao-compareceu")
    public DadosAgendamentoAdministrativo registrarAusencia(
            @PathVariable UUID id,
            @AuthenticationPrincipal PrincipalAdministrativo principal) {
        return servico.encerrar(id, EstadoAgendamento.NAO_COMPARECEU, principal);
    }

    public record Cancelamento(@NotBlank String motivo) {
    }
}
