package com.servicescheduling.api.acesso.infraestrutura.recuperacao;

import com.servicescheduling.api.acesso.aplicacao.PoliticaSenha;
import com.servicescheduling.api.acesso.dominio.PapelUsuarioAdministrativo;
import com.servicescheduling.api.acesso.dominio.UsuarioAdministrativo;
import com.servicescheduling.api.acesso.infraestrutura.configuracao.PropriedadesAcesso;
import com.servicescheduling.api.acesso.infraestrutura.persistencia.RepositorioUsuarioAdministrativo;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnNotWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(name = "aplicacao.modo", havingValue = "recuperacao")
@ConditionalOnNotWebApplication
public class RecuperacaoProprietario implements ApplicationRunner {

    private static final Logger LOG = LoggerFactory.getLogger(RecuperacaoProprietario.class);

    private final PropriedadesAcesso propriedades;
    private final RepositorioUsuarioAdministrativo repositorio;
    private final PoliticaSenha politicaSenha;
    private final PasswordEncoder passwordEncoder;
    private final ResultadoRecuperacao resultado;

    public RecuperacaoProprietario(
            PropriedadesAcesso propriedades,
            RepositorioUsuarioAdministrativo repositorio,
            PoliticaSenha politicaSenha,
            PasswordEncoder passwordEncoder,
            ResultadoRecuperacao resultado) {
        this.propriedades = propriedades;
        this.repositorio = repositorio;
        this.politicaSenha = politicaSenha;
        this.passwordEncoder = passwordEncoder;
        this.resultado = resultado;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        PropriedadesAcesso.Recuperacao recuperacao = propriedades.recuperacao();
        if (recuperacao == null
                || !StringUtils.hasText(recuperacao.email())
                || !StringUtils.hasText(recuperacao.novaSenha())) {
            LOG.error("recuperacao administrativa rejeitada motivo=configuracao-ausente");
            resultado.concluir(3);
            return;
        }
        try {
            politicaSenha.validar(recuperacao.novaSenha());
        } catch (RuntimeException excecao) {
            LOG.error("recuperacao administrativa rejeitada motivo=senha-fora-politica");
            resultado.concluir(3);
            return;
        }
        UsuarioAdministrativo usuario = repositorio
                .findByEmail(UsuarioAdministrativo.normalizarEmail(recuperacao.email()))
                .orElse(null);
        if (usuario == null) {
            LOG.error("recuperacao administrativa rejeitada usuarioId=indisponivel resultado=conta-invalida");
            resultado.concluir(2);
            return;
        }
        if (!usuario.isAtivo() || usuario.getPapel() != PapelUsuarioAdministrativo.PROPRIETARIO) {
            LOG.error("recuperacao administrativa rejeitada usuarioId={} resultado=conta-invalida", usuario.getId());
            resultado.concluir(2);
            return;
        }
        usuario.alterarSenha(passwordEncoder.encode(recuperacao.novaSenha()), false);
        repositorio.saveAndFlush(usuario);
        UUID idUsuario = usuario.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                LOG.info("recuperacao administrativa concluida usuarioId={} resultado=sucesso", idUsuario);
            }
        });
        resultado.concluir(0);
    }
}
