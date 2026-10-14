package com.servicescheduling.api.acesso.infraestrutura.configuracao;

import com.servicescheduling.api.acesso.aplicacao.PoliticaSenha;
import com.servicescheduling.api.acesso.dominio.UsuarioAdministrativo;
import com.servicescheduling.api.acesso.infraestrutura.persistencia.RepositorioUsuarioAdministrativo;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Component
@ConditionalOnProperty(name = "aplicacao.modo", havingValue = "normal", matchIfMissing = true)
public class BootstrapProprietario implements ApplicationRunner {

    private final RepositorioUsuarioAdministrativo repositorio;
    private final PropriedadesAcesso propriedades;
    private final PoliticaSenha politicaSenha;
    private final PasswordEncoder passwordEncoder;
    private final Validator validator;

    public BootstrapProprietario(
            RepositorioUsuarioAdministrativo repositorio,
            PropriedadesAcesso propriedades,
            PoliticaSenha politicaSenha,
            PasswordEncoder passwordEncoder,
            Validator validator) {
        this.repositorio = repositorio;
        this.propriedades = propriedades;
        this.politicaSenha = politicaSenha;
        this.passwordEncoder = passwordEncoder;
        this.validator = validator;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (repositorio.count() > 0) {
            return;
        }
        PropriedadesAcesso.Bootstrap bootstrap = propriedades.bootstrap();
        if (bootstrap == null
                || !StringUtils.hasText(bootstrap.nome())
                || !StringUtils.hasText(bootstrap.email())
                || !StringUtils.hasText(bootstrap.senha())) {
            throw new IllegalStateException(
                    "BOOTSTRAP_PROPRIETARIO_NOME, BOOTSTRAP_PROPRIETARIO_EMAIL e "
                            + "BOOTSTRAP_PROPRIETARIO_SENHA sao obrigatorios quando nao existem usuarios");
        }
        politicaSenha.validar(bootstrap.senha());
        if (!validator.validate(new DadosBootstrap(bootstrap.nome(), bootstrap.email())).isEmpty()) {
            throw new IllegalStateException("Nome ou email do bootstrap possui formato ou tamanho invalido");
        }
        repositorio.save(UsuarioAdministrativo.proprietario(
                bootstrap.nome().strip(), bootstrap.email(), passwordEncoder.encode(bootstrap.senha())));
    }

    private record DadosBootstrap(
            @NotBlank @Size(max = 120) String nome,
            @NotBlank @Email @Size(max = 254) String email) {
    }
}
