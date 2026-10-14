package com.servicescheduling.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(properties = {
        "aplicacao.acesso.bootstrap.nome=Proprietario Agenda",
        "aplicacao.acesso.bootstrap.email=proprietario-agenda@example.test",
        "aplicacao.acesso.bootstrap.senha=senha-agenda-segura",
        "aplicacao.negocio.fuso-horario=America/Sao_Paulo",
        "aplicacao.negocio.antecedencia-minima-reserva-minutos=60",
        "aplicacao.negocio.horizonte-reserva-dias=90"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import(AgendaIntegracaoTest.ConfiguracaoRelogioTeste.class)
class AgendaIntegracaoTest {

    private static final String EMAIL_PROPRIETARIO = "proprietario-agenda@example.test";
    private static final String SENHA_PROPRIETARIO = "senha-agenda-segura";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRESQL = new PostgreSQLContainer(
            DockerImageName.parse("postgres:17-alpine"));

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcClient jdbcClient;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void limparAgenda() {
        jdbcClient.sql("delete from bloqueio_agenda").update();
        jdbcClient.sql("delete from expediente_semanal").update();
        jdbcClient.sql("delete from requisito_recurso_servico").update();
        jdbcClient.sql("delete from servico").update();
        jdbcClient.sql("delete from recurso_operacional").update();
        jdbcClient.sql("delete from usuario_administrativo where email <> :email")
                .param("email", EMAIL_PROPRIETARIO).update();
        jdbcClient.sql("delete from profissional").update();
        jdbcClient.sql("""
                        update usuario_administrativo
                           set hash_senha = :hash, ativo = true, deve_alterar_senha = false
                         where email = :email
                        """)
                .param("hash", passwordEncoder.encode(SENHA_PROPRIETARIO))
                .param("email", EMAIL_PROPRIETARIO)
                .update();
    }

    @Test
    void proprietarioSubstituiExpedienteIntegralmenteSemPersistenciaParcial() throws Exception {
        MockHttpSession sessao = autenticar(EMAIL_PROPRIETARIO, SENHA_PROPRIETARIO);
        UUID profissionalId = inserirProfissional("Profissional", true);

        mockMvc.perform(put("/api/v1/administracao/agenda/semanal")
                        .session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(expedienteJson(profissionalId,
                                """
                                {"diaSemana":"QUINTA_FEIRA","inicio":"09:00","fim":"12:00"},
                                {"diaSemana":"QUINTA_FEIRA","inicio":"12:00","fim":"13:00"}
                                """)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.janelas.length()").value(2));

        mockMvc.perform(put("/api/v1/administracao/agenda/semanal")
                        .session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(expedienteJson(profissionalId,
                                """
                                {"diaSemana":"QUINTA_FEIRA","inicio":"09:00","fim":"12:00"},
                                {"diaSemana":"QUINTA_FEIRA","inicio":"11:00","fim":"13:00"}
                                """)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.codigo").value("EXPEDIENTE_SOBREPOSTO"));

        mockMvc.perform(put("/api/v1/administracao/agenda/semanal")
                        .session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(expedienteJson(profissionalId,
                                "{\"diaSemana\":\"QUINTA_FEIRA\",\"inicio\":\"09:00\",\"fim\":\"09:00\"}")))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.codigo").value("INTERVALO_EXPEDIENTE_INVALIDO"));

        mockMvc.perform(get("/api/v1/administracao/agenda/semanal")
                        .session(sessao).param("profissionalId", profissionalId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.janelas.length()").value(2))
                .andExpect(jsonPath("$.janelas[0].inicio").value("09:00"));

        mockMvc.perform(put("/api/v1/administracao/agenda/semanal")
                        .session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(expedienteJson(profissionalId, "")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.janelas").isEmpty());
    }

    @Test
    void funcionarioOperaSomenteAgendaDoProfissionalVinculado() throws Exception {
        UUID proprio = inserirProfissional("Proprio", true);
        UUID outro = inserirProfissional("Outro", true);
        inserirFuncionario(proprio, "funcionario-agenda@example.test");
        inserirExpediente(proprio, "QUARTA_FEIRA", "09:00", "12:00");
        UUID bloqueioOutro = inserirBloqueio(outro, "2026-10-08T13:00:00Z", "2026-10-08T14:00:00Z");
        MockHttpSession sessao = autenticar("funcionario-agenda@example.test", "senha-funcionario");

        mockMvc.perform(get("/api/v1/administracao/agenda/semanal")
                        .session(sessao).param("profissionalId", outro.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profissionalId").value(proprio.toString()));

        mockMvc.perform(put("/api/v1/administracao/agenda/semanal")
                        .session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(expedienteJson(outro,
                                "{\"diaSemana\":\"QUINTA_FEIRA\",\"inicio\":\"09:00\",\"fim\":\"12:00\"}")))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/administracao/agenda/bloqueios/{id}", bloqueioOutro)
                        .session(sessao).with(csrf()))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/v1/administracao/agenda/semanal")
                        .session(sessao).contentType(MediaType.APPLICATION_JSON)
                        .content(expedienteJson(null, "")))
                .andExpect(status().isForbidden());
    }

    @Test
    void criaListaEExcluiBloqueiosRejeitandoSobreposicao() throws Exception {
        MockHttpSession sessao = autenticar(EMAIL_PROPRIETARIO, SENHA_PROPRIETARIO);
        UUID profissionalId = inserirProfissional("Profissional", true);
        MvcResult criado = criarBloqueio(
                sessao, profissionalId, "2026-10-08T10:00:00-03:00", "2026-10-08T11:00:00-03:00");
        UUID bloqueioId = id(criado);

        mockMvc.perform(post("/api/v1/administracao/agenda/bloqueios")
                        .session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(bloqueioJson(
                                profissionalId,
                                "2026-10-08T10:30:00-03:00",
                                "2026-10-08T11:30:00-03:00")))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.codigo").value("BLOQUEIO_SOBREPOSTO"));
        criarBloqueio(sessao, profissionalId, "2026-10-08T11:00:00-03:00", "2026-10-08T12:00:00-03:00");

        mockMvc.perform(get("/api/v1/administracao/agenda/bloqueios")
                        .session(sessao)
                        .param("profissionalId", profissionalId.toString())
                        .param("dataInicial", "2026-10-08")
                        .param("dataFinal", "2026-10-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(delete("/api/v1/administracao/agenda/bloqueios/{id}", bloqueioId)
                        .session(sessao).with(csrf()))
                .andExpect(status().isNoContent());
        assertThat(jdbcClient.sql("select count(*) from bloqueio_agenda")
                .query(Integer.class).single()).isEqualTo(1);
    }

    @Test
    void calculaDisponibilidadePublicaPorGradeExpedienteLimpezaEBloqueio() throws Exception {
        UUID profissionalId = inserirProfissional("Profissional", true);
        UUID recursoId = inserirRecurso("Sala", 1, true);
        UUID servicoId = inserirServico(profissionalId, 30, 15, true);
        inserirRequisito(servicoId, recursoId, 1);
        inserirExpediente(profissionalId, "QUINTA_FEIRA", "09:00", "11:00");
        inserirBloqueio(profissionalId, "2026-10-08T12:30:00Z", "2026-10-08T13:00:00Z");

        mockMvc.perform(get("/api/v1/publico/servicos/{id}/disponibilidade", servicoId)
                        .param("dataInicial", "2026-10-08")
                        .param("dataFinal", "2026-10-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fusoHorario").value("America/Sao_Paulo"))
                .andExpect(jsonPath("$.dias[0].data").value("2026-10-08"))
                .andExpect(jsonPath("$.dias[0].horarios.length()").value(2))
                .andExpect(jsonPath("$.dias[0].horarios[0].inicioEm").value("2026-10-08T10:00:00-03:00"))
                .andExpect(jsonPath("$.dias[0].horarios[0].fimServicoEm")
                        .value("2026-10-08T10:30:00-03:00"));

        jdbcClient.sql("update recurso_operacional set ativo = false where id = :id")
                .param("id", recursoId).update();
        mockMvc.perform(get("/api/v1/publico/servicos/{id}/disponibilidade", servicoId)
                        .param("dataInicial", "2026-10-08")
                        .param("dataFinal", "2026-10-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dias").isEmpty());

        jdbcClient.sql("update recurso_operacional set ativo = true where id = :id")
                .param("id", recursoId).update();
        jdbcClient.sql("update profissional set ativo = false where id = :id")
                .param("id", profissionalId).update();
        mockMvc.perform(get("/api/v1/publico/servicos/{id}/disponibilidade", servicoId)
                        .param("dataInicial", "2026-10-08")
                        .param("dataFinal", "2026-10-08"))
                .andExpect(status().isNotFound());

        jdbcClient.sql("update profissional set ativo = true where id = :id")
                .param("id", profissionalId).update();
        jdbcClient.sql("update servico set ativo = false where id = :id")
                .param("id", servicoId).update();
        mockMvc.perform(get("/api/v1/publico/servicos/{id}/disponibilidade", servicoId)
                        .param("dataInicial", "2026-10-08")
                        .param("dataFinal", "2026-10-08"))
                .andExpect(status().isNotFound());
    }

    @Test
    void aplicaPeriodoAntecedenciaEHorizontePorDataLocal() throws Exception {
        UUID profissionalId = inserirProfissional("Profissional", true);
        UUID servicoId = inserirServico(profissionalId, 30, 0, true);
        inserirExpediente(profissionalId, "QUARTA_FEIRA", "09:00", "11:00");
        inserirExpediente(profissionalId, "SEGUNDA_FEIRA", "09:00", "10:00");
        inserirExpediente(profissionalId, "TERCA_FEIRA", "09:00", "10:00");

        mockMvc.perform(get("/api/v1/publico/servicos/{id}/disponibilidade", servicoId)
                        .param("dataInicial", "2026-10-07")
                        .param("dataFinal", "2026-10-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dias[0].horarios[0].inicioEm").value("2026-10-07T10:00:00-03:00"));

        mockMvc.perform(get("/api/v1/publico/servicos/{id}/disponibilidade", servicoId)
                        .param("dataInicial", "2027-01-04")
                        .param("dataFinal", "2027-01-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dias.length()").value(1))
                .andExpect(jsonPath("$.dias[0].data").value("2027-01-04"));

        mockMvc.perform(get("/api/v1/publico/servicos/{id}/disponibilidade", servicoId)
                        .param("dataInicial", "2026-10-07")
                        .param("dataFinal", "2026-11-06"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/publico/servicos/{id}/disponibilidade", servicoId))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.codigo").value("PERIODO_CONSULTA_INVALIDO"));
        mockMvc.perform(get("/api/v1/publico/servicos/{id}/disponibilidade", servicoId)
                        .param("dataInicial", "2026-10-01")
                        .param("dataFinal", "2026-11-01"))
                .andExpect(status().is(422));
    }

    @Test
    void documentaAgendaNoOpenApi() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/agenda/semanal'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/agenda/semanal'].put").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/agenda/bloqueios'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/agenda/bloqueios/{id}'].delete").exists())
                .andExpect(jsonPath("$.paths['/api/v1/publico/servicos/{servicoId}/disponibilidade'].get")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/agenda/semanal'].put.security[0].sessao")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/agenda/semanal'].put.security[0].csrf")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v1/publico/servicos/{servicoId}/disponibilidade'].get.security[0]")
                        .isEmpty())
                .andExpect(jsonPath("$.paths['/api/v1/publico/servicos/{servicoId}/disponibilidade'].get.parameters[1].required")
                        .value(true))
                .andExpect(jsonPath("$.paths['/api/v1/administracao/agenda/bloqueios'].post.responses['201'].headers.Location")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/agenda/bloqueios'].post.responses['201'].content['application/json'].schema")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v1/publico/servicos/{servicoId}/disponibilidade'].get.responses['429']")
                        .exists())
                .andExpect(jsonPath("$.components.responses.LimiteExcedido.headers['Retry-After']").exists());
    }

    private MockHttpSession autenticar(String email, String senha) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/administracao/autenticacao/entrar")
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha)))
                .andExpect(status().isNoContent())
                .andReturn();
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }

    private MvcResult criarBloqueio(MockHttpSession sessao, UUID profissionalId, String inicio, String fim)
            throws Exception {
        return mockMvc.perform(post("/api/v1/administracao/agenda/bloqueios")
                        .session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(bloqueioJson(profissionalId, inicio, fim)))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private UUID id(MvcResult resultado) throws Exception {
        return UUID.fromString(resultado.getResponse().getContentAsString()
                .replaceFirst(".*\\\"id\\\":\\\"([^\\\"]+)\\\".*", "$1"));
    }

    private String expedienteJson(UUID profissionalId, String janelas) {
        String profissional = profissionalId == null ? "null" : "\"" + profissionalId + "\"";
        return "{\"profissionalId\":%s,\"janelas\":[%s]}".formatted(profissional, janelas);
    }

    private String bloqueioJson(UUID profissionalId, String inicio, String fim) {
        return """
                {"profissionalId":"%s","inicioEm":"%s","fimEm":"%s"}
                """.formatted(profissionalId, inicio, fim);
    }

    private UUID inserirProfissional(String nome, boolean ativo) {
        UUID id = UUID.randomUUID();
        jdbcClient.sql("insert into profissional (id, nome, ativo) values (:id, :nome, :ativo)")
                .param("id", id).param("nome", nome).param("ativo", ativo).update();
        return id;
    }

    private UUID inserirRecurso(String nome, int capacidade, boolean ativo) {
        UUID id = UUID.randomUUID();
        jdbcClient.sql("""
                        insert into recurso_operacional (id, nome, capacidade, ativo)
                        values (:id, :nome, :capacidade, :ativo)
                        """)
                .param("id", id).param("nome", nome).param("capacidade", capacidade)
                .param("ativo", ativo).update();
        return id;
    }

    private UUID inserirServico(UUID profissionalId, int duracao, int limpeza, boolean ativo) {
        UUID id = UUID.randomUUID();
        jdbcClient.sql("""
                        insert into servico
                        (id, profissional_id, nome, duracao_minutos, intervalo_limpeza_minutos,
                         preco, ativo, ordem_exibicao)
                        values (:id, :profissional, 'Servico Agenda', :duracao, :limpeza, 50.00, :ativo, 0)
                        """)
                .param("id", id).param("profissional", profissionalId)
                .param("duracao", duracao).param("limpeza", limpeza).param("ativo", ativo).update();
        return id;
    }

    private void inserirRequisito(UUID servicoId, UUID recursoId, int quantidade) {
        jdbcClient.sql("""
                        insert into requisito_recurso_servico (servico_id, recurso_operacional_id, quantidade)
                        values (:servico, :recurso, :quantidade)
                        """)
                .param("servico", servicoId).param("recurso", recursoId)
                .param("quantidade", quantidade).update();
    }

    private void inserirExpediente(UUID profissionalId, String dia, String inicio, String fim) {
        jdbcClient.sql("""
                        insert into expediente_semanal (id, profissional_id, dia_semana, inicio, fim)
                        values (:id, :profissional, :dia, cast(:inicio as time), cast(:fim as time))
                        """)
                .param("id", UUID.randomUUID()).param("profissional", profissionalId)
                .param("dia", dia).param("inicio", inicio).param("fim", fim).update();
    }

    private UUID inserirBloqueio(UUID profissionalId, String inicio, String fim) {
        UUID id = UUID.randomUUID();
        jdbcClient.sql("""
                        insert into bloqueio_agenda (id, profissional_id, inicio_em, fim_em)
                        values (:id, :profissional, cast(:inicio as timestamptz), cast(:fim as timestamptz))
                        """)
                .param("id", id).param("profissional", profissionalId)
                .param("inicio", inicio).param("fim", fim).update();
        return id;
    }

    private void inserirFuncionario(UUID profissionalId, String email) {
        jdbcClient.sql("""
                        insert into usuario_administrativo
                        (id, nome, email, hash_senha, papel, profissional_id, ativo, deve_alterar_senha)
                        values (:id, 'Funcionario Agenda', :email, :hash, 'FUNCIONARIO', :profissional, true, false)
                        """)
                .param("id", UUID.randomUUID()).param("email", email)
                .param("hash", passwordEncoder.encode("senha-funcionario"))
                .param("profissional", profissionalId).update();
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ConfiguracaoRelogioTeste {

        @Bean
        @Primary
        Clock relogioFixo() {
            return Clock.fixed(Instant.parse("2026-10-07T12:00:00Z"), ZoneOffset.UTC);
        }
    }
}
