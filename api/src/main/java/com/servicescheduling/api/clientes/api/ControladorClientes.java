package com.servicescheduling.api.clientes.api;

import com.servicescheduling.api.acesso.infraestrutura.seguranca.PrincipalAdministrativo;
import com.servicescheduling.api.clientes.aplicacao.DadosCliente;
import com.servicescheduling.api.clientes.aplicacao.ServicoClientes;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/administracao/clientes")
public class ControladorClientes {

    private final ServicoClientes servico;

    public ControladorClientes(ServicoClientes servico) {
        this.servico = servico;
    }

    @GetMapping("/{id}")
    public DadosCliente obter(@PathVariable UUID id) {
        return servico.obter(id);
    }

    @PostMapping("/{id}/anonimizar")
    public ResponseEntity<Void> anonimizar(
            @PathVariable UUID id,
            @RequestBody @Valid Anonimizacao requisicao,
            @AuthenticationPrincipal PrincipalAdministrativo principal) {
        servico.anonimizar(id, requisicao.motivo(), principal.id());
        return ResponseEntity.noContent().build();
    }

    public record Anonimizacao(@NotBlank String motivo) {
    }
}
