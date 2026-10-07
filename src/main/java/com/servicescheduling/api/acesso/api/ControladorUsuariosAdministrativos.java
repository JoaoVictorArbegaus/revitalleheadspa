package com.servicescheduling.api.acesso.api;

import com.servicescheduling.api.acesso.aplicacao.DadosUsuarioAdministrativo;
import com.servicescheduling.api.acesso.aplicacao.ServicoUsuariosAdministrativos;
import com.servicescheduling.api.acesso.dominio.PapelUsuarioAdministrativo;
import com.servicescheduling.api.acesso.dominio.UsuarioAdministrativo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@RequestMapping("/api/v1/administracao/usuarios")
public class ControladorUsuariosAdministrativos {

    private final ServicoUsuariosAdministrativos servico;

    public ControladorUsuariosAdministrativos(ServicoUsuariosAdministrativos servico) {
        this.servico = servico;
    }

    @GetMapping
    List<DadosUsuarioAdministrativo> listar() {
        return servico.listar();
    }

    @GetMapping("/{id}")
    DadosUsuarioAdministrativo buscar(@PathVariable UUID id) {
        return servico.buscar(id);
    }

    @PostMapping
    ResponseEntity<DadosUsuarioAdministrativo> criar(@Valid @RequestBody CriacaoFuncionario requisicao) {
        DadosUsuarioAdministrativo criado = servico.criarFuncionario(
                requisicao.nome(), requisicao.email(), requisicao.senhaTemporaria(), requisicao.profissionalId());
        return ResponseEntity.created(URI.create("/api/v1/administracao/usuarios/" + criado.id())).body(criado);
    }

    @PutMapping("/{id}")
    DadosUsuarioAdministrativo substituir(
            @PathVariable UUID id, @Valid @RequestBody AlteracaoUsuario requisicao) {
        return alterar(id, requisicao);
    }

    @PatchMapping("/{id}")
    DadosUsuarioAdministrativo atualizar(
            @PathVariable UUID id, @Valid @RequestBody AlteracaoUsuario requisicao) {
        return alterar(id, requisicao);
    }

    @PostMapping("/{id}/senha-temporaria")
    ResponseEntity<Void> redefinirSenha(
            @PathVariable UUID id, @Valid @RequestBody RedefinicaoSenha requisicao) {
        servico.redefinirSenhaTemporaria(id, requisicao.senhaTemporaria());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/ativar")
    ResponseEntity<Void> ativar(@PathVariable UUID id) {
        servico.ativar(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/desativar")
    ResponseEntity<Void> desativar(@PathVariable UUID id) {
        servico.desativar(id);
        return ResponseEntity.noContent().build();
    }

    private DadosUsuarioAdministrativo alterar(UUID id, AlteracaoUsuario requisicao) {
        return servico.alterar(
                id, requisicao.nome(), requisicao.email(), requisicao.papel(), requisicao.profissionalId());
    }

    public record CriacaoFuncionario(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Email @Size(max = 254) String email,
            @NotBlank String senhaTemporaria,
            @NotNull UUID profissionalId) {
        public CriacaoFuncionario {
            email = UsuarioAdministrativo.normalizarEmail(email);
        }
    }

    public record AlteracaoUsuario(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Email @Size(max = 254) String email,
            @NotNull PapelUsuarioAdministrativo papel,
            UUID profissionalId) {
        public AlteracaoUsuario {
            email = UsuarioAdministrativo.normalizarEmail(email);
        }
    }

    public record RedefinicaoSenha(@NotBlank String senhaTemporaria) {
    }
}
