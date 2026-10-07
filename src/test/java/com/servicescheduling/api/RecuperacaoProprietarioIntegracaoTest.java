package com.servicescheduling.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.servicescheduling.api.acesso.dominio.UsuarioAdministrativo;
import com.servicescheduling.api.acesso.infraestrutura.persistencia.RepositorioUsuarioAdministrativo;
import com.servicescheduling.api.acesso.infraestrutura.recuperacao.ResultadoRecuperacao;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
class RecuperacaoProprietarioIntegracaoTest {

    private static final String EMAIL = "recuperacao@example.test";

    @Container
    static final PostgreSQLContainer POSTGRESQL = new PostgreSQLContainer(
            DockerImageName.parse("postgres:17-alpine"));

    @Test
    void recuperaProprietarioSemIniciarServidorWeb() throws Exception {
        try (ConfigurableApplicationContext inicial = iniciar(Map.of(
                "aplicacao.modo", "normal",
                "aplicacao.acesso.bootstrap.nome", "Proprietario Recuperacao",
                "aplicacao.acesso.bootstrap.email", EMAIL,
                "aplicacao.acesso.bootstrap.senha", "senha-antiga-segura"))) {
            assertThat(inicial).isNotInstanceOf(WebApplicationContext.class);
        }

        assertThat(executarRecuperacaoNoMain(EMAIL, "senha-via-processo")).isZero();

        try (ConfigurableApplicationContext verificacao = iniciar(Map.of("aplicacao.modo", "normal"))) {
            UsuarioAdministrativo usuario = verificacao.getBean(RepositorioUsuarioAdministrativo.class)
                    .findByEmail(EMAIL).orElseThrow();
            PasswordEncoder encoder = verificacao.getBean(PasswordEncoder.class);
            assertThat(encoder.matches("senha-via-processo", usuario.getHashSenha())).isTrue();
        }

        assertThat(executarRecuperacaoNoMain("inexistente@example.test", "outra-senha-segura")).isEqualTo(2);
        assertThat(executarRecuperacaoNoMain(
                EMAIL, null, "--aplicacao.acesso.recuperacao.nova-senha=senha-por-argumento")).isEqualTo(3);

        try (ConfigurableApplicationContext recuperacao = iniciar(Map.of(
                "aplicacao.modo", "recuperacao",
                "aplicacao.acesso.recuperacao.email", EMAIL,
                "aplicacao.acesso.recuperacao.nova-senha", "senha-nova-recuperada"))) {
            assertThat(recuperacao).isNotInstanceOf(WebApplicationContext.class);
            assertThat(recuperacao.getBean(ResultadoRecuperacao.class).getCodigoSaida()).isZero();

            UsuarioAdministrativo usuario = recuperacao.getBean(RepositorioUsuarioAdministrativo.class)
                    .findByEmail(EMAIL).orElseThrow();
            PasswordEncoder encoder = recuperacao.getBean(PasswordEncoder.class);
            assertThat(encoder.matches("senha-nova-recuperada", usuario.getHashSenha())).isTrue();
            assertThat(encoder.matches("senha-antiga-segura", usuario.getHashSenha())).isFalse();
        }

        try (ConfigurableApplicationContext rejeitada = iniciar(Map.of(
                "aplicacao.modo", "recuperacao",
                "aplicacao.acesso.recuperacao.email", "inexistente@example.test",
                "aplicacao.acesso.recuperacao.nova-senha", "outra-senha-segura"))) {
            assertThat(rejeitada).isNotInstanceOf(WebApplicationContext.class);
            assertThat(rejeitada.getBean(ResultadoRecuperacao.class).getCodigoSaida()).isEqualTo(2);
        }
    }

    private ConfigurableApplicationContext iniciar(Map<String, Object> especificas) {
        Map<String, Object> propriedades = new HashMap<>();
        propriedades.put("spring.datasource.url", POSTGRESQL.getJdbcUrl());
        propriedades.put("spring.datasource.username", POSTGRESQL.getUsername());
        propriedades.put("spring.datasource.password", POSTGRESQL.getPassword());
        propriedades.put("spring.main.banner-mode", "off");
        propriedades.putAll(especificas);

        SpringApplication aplicacao = new SpringApplication(AplicacaoAgendamentoServicos.class);
        aplicacao.setWebApplicationType(WebApplicationType.NONE);
        aplicacao.addInitializers(contexto -> contexto.getEnvironment().getPropertySources()
                .addFirst(new MapPropertySource("teste-recuperacao", propriedades)));
        return aplicacao.run();
    }

    private int executarRecuperacaoNoMain(String email, String novaSenha, String... argumentos) throws Exception {
        String java = Path.of(System.getProperty("java.home"), "bin", "java").toString();
        String classpath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        List<String> comando = new ArrayList<>(List.of(
                java, "-cp", classpath, AplicacaoAgendamentoServicos.class.getName()));
        comando.addAll(List.of(argumentos));
        ProcessBuilder builder = new ProcessBuilder(comando);
        builder.redirectErrorStream(true);
        builder.redirectOutput(ProcessBuilder.Redirect.DISCARD);
        Map<String, String> ambiente = builder.environment();
        ambiente.put("MODO_APLICACAO", "recuperacao");
        ambiente.put("DATABASE_URL", POSTGRESQL.getJdbcUrl());
        ambiente.put("DATABASE_USERNAME", POSTGRESQL.getUsername());
        ambiente.put("DATABASE_PASSWORD", POSTGRESQL.getPassword());
        ambiente.put("RECUPERACAO_PROPRIETARIO_EMAIL", email);
        if (novaSenha != null) {
            ambiente.put("RECUPERACAO_PROPRIETARIO_NOVA_SENHA", novaSenha);
        } else {
            ambiente.remove("RECUPERACAO_PROPRIETARIO_NOVA_SENHA");
        }
        Process processo = builder.start();
        return processo.waitFor();
    }
}
