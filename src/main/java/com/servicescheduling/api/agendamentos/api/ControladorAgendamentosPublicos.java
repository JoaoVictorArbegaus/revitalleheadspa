package com.servicescheduling.api.agendamentos.api;

import com.servicescheduling.api.agendamentos.aplicacao.ComandoAgendamentoPublico;
import com.servicescheduling.api.agendamentos.aplicacao.DadosConfirmacaoAgendamento;
import com.servicescheduling.api.agendamentos.aplicacao.ServicoAgendamentoPublico;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/publico/agendamentos")
public class ControladorAgendamentosPublicos {

    private final ServicoAgendamentoPublico servico;

    public ControladorAgendamentosPublicos(ServicoAgendamentoPublico servico) {
        this.servico = servico;
    }

    @PostMapping
    @ApiResponse(responseCode = "201", useReturnTypeSchema = true)
    public ResponseEntity<DadosConfirmacaoAgendamento> criar(
            @Parameter(required = true)
            @RequestHeader(value = "Chave-Idempotencia", required = false)
            @NotBlank
            @Pattern(regexp = "[A-Za-z0-9._~-]{16,128}") String chaveIdempotencia,
            @RequestBody @Valid CriacaoAgendamento requisicao) {
        DadosConfirmacaoAgendamento resposta = servico.criar(
                new ComandoAgendamentoPublico(
                        requisicao.servicoId(),
                        requisicao.inicioEm(),
                        requisicao.cliente().nome(),
                        requisicao.cliente().celular(),
                        requisicao.versaoAvisoPrivacidade(),
                        requisicao.cienteAvisoPrivacidade()),
                chaveIdempotencia);
        return ResponseEntity.status(HttpStatus.CREATED).body(resposta);
    }

    public record CriacaoAgendamento(
            @NotNull UUID servicoId,
            @NotNull OffsetDateTime inicioEm,
            @NotNull @Valid ClienteInformado cliente,
            @NotBlank @Size(max = 64) String versaoAvisoPrivacidade,
            @AssertTrue boolean cienteAvisoPrivacidade) {
    }

    public record ClienteInformado(
            @NotBlank String nome,
            @NotBlank String celular) {
    }
}
