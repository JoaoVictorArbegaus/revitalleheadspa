package com.servicescheduling.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(properties = {
        "aplicacao.acesso.bootstrap.nome=Proprietario Operacao",
        "aplicacao.acesso.bootstrap.email=proprietario-operacao@example.test",
        "aplicacao.acesso.bootstrap.senha=senha-operacao-segura",
        "aplicacao.negocio.fuso-horario=America/Sao_Paulo",
        "aplicacao.negocio.antecedencia-minima-reserva-minutos=60",
        "aplicacao.negocio.horizonte-reserva-dias=90",
        "aplicacao.negocio.versao-aviso-privacidade=2026-01",
        "aplicacao.negocio.texto-aviso-privacidade=Aviso de teste vigente"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import(AgendamentoAdministrativoIntegracaoTest.ConfiguracaoRelogioTeste.class)
class AgendamentoAdministrativoIntegracaoTest {

    private static final Instant AGORA = Instant.parse("2026-10-07T12:00:00Z");
    private static final String EMAIL_PROPRIETARIO = "proprietario-operacao@example.test";
    private static final String SENHA_PROPRIETARIO = "senha-operacao-segura";
    private static final String SENHA_FUNCIONARIO = "senha-funcionario-segura";
    private static final String INICIO = "2026-10-08T10:00:00-03:00";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRESQL = new PostgreSQLContainer(
            DockerImageName.parse("postgres:17-alpine"));

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcClient jdbc;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    RelogioAjustavel relogio;

    @BeforeEach
    void limparDados() {
        relogio.ajustar(AGORA);
        jdbc.sql("delete from anonimizacao_cliente").update();
        jdbc.sql("delete from registro_idempotencia").update();
        jdbc.sql("delete from alocacao_recurso_agendamento").update();
        jdbc.sql("delete from agendamento").update();
        jdbc.sql("delete from cliente").update();
        jdbc.sql("delete from bloqueio_agenda").update();
        jdbc.sql("delete from expediente_semanal").update();
        jdbc.sql("delete from requisito_recurso_servico").update();
        jdbc.sql("delete from servico").update();
        jdbc.sql("delete from recurso_operacional").update();
        jdbc.sql("delete from usuario_administrativo where email <> :email")
                .param("email", EMAIL_PROPRIETARIO).update();
        jdbc.sql("delete from profissional").update();
    }

    @Test
    void proprietarioCriaForaDosLimitesPublicosSemIgnorarAgenda() throws Exception {
        MockHttpSession sessao = autenticar(EMAIL_PROPRIETARIO, SENHA_PROPRIETARIO);
        Cenario cenario = criarCenario("Principal", 1);
        inserirExpediente(cenario.profissionalId(), "QUARTA_FEIRA");
        UUID proprietarioId = usuarioId(EMAIL_PROPRIETARIO);

        String resposta = mockMvc.perform(criacao(
                        sessao, cenario.servicoId(), "2026-10-07T09:30:00-03:00",
                        "Cliente Balcao", "(11) 98888-1111", chave(1)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.origem").value("ADMINISTRACAO"))
                .andExpect(jsonPath("$.criadoPor").value(proprietarioId.toString()))
                .andExpect(jsonPath("$.cliente.nome").value("Cliente Balcao"))
                .andExpect(jsonPath("$.cliente.celular").value("+5511988881111"))
                .andExpect(jsonPath("$.inicioEm").value("2026-10-07T09:30:00-03:00"))
                .andExpect(jsonPath("$.ocupadoAte").value("2026-10-07T10:45:00-03:00"))
                .andExpect(jsonPath("$.recursos[0].nome").value("Recurso Principal"))
                .andExpect(jsonPath("$.recursos[0].quantidade").value(1))
                .andExpect(jsonPath("$.estadoAlteradoEm").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        UUID agendamentoId = UUID.fromString(JsonPath.read(resposta, "$.id"));

        mockMvc.perform(criacao(
                        sessao, cenario.servicoId(), "2027-01-07T10:00:00-03:00",
                        "Cliente Futuro", "11977772222", chave(2)))
                .andExpect(status().isCreated());

        mockMvc.perform(criacao(
                        sessao, cenario.servicoId(), "2026-10-06T10:00:00-03:00",
                        "Cliente Passado", "11966663333", chave(3)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("INICIO_NO_PASSADO"));
        mockMvc.perform(criacao(
                        sessao, cenario.servicoId(), "2026-10-08T07:00:00-03:00",
                        "Cliente Fora", "11966663333", chave(4)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLITO_AGENDAMENTO"));
        mockMvc.perform(criacao(
                        sessao, cenario.servicoId(), "2026-10-07T10:00:00-03:00",
                        "Cliente Sobreposto", "11966663333", chave(5)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLITO_AGENDAMENTO"));
        jdbc.sql("""
                        insert into bloqueio_agenda (id, profissional_id, inicio_em, fim_em)
                        values (:id, :profissional, '2026-10-08T13:00:00Z', '2026-10-08T14:00:00Z')
                        """)
                .param("id", UUID.randomUUID()).param("profissional", cenario.profissionalId()).update();
        mockMvc.perform(criacao(
                        sessao, cenario.servicoId(), INICIO,
                        "Cliente Bloqueado", "11966663333", chave(6)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLITO_AGENDAMENTO"));

        mockMvc.perform(post("/api/v1/administracao/agendamentos")
                        .session(sessao)
                        .header("Chave-Idempotencia", chave(7))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(cenario.servicoId(), INICIO, "Sem Csrf", "11955554444")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/administracao/agendamentos/{id}", agendamentoId).session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versaoAvisoPrivacidade").value("2026-01"));
        mockMvc.perform(get("/api/v1/administracao/agendamentos")
                        .session(sessao)
                        .param("dataInicial", "2026-10-07")
                        .param("dataFinal", "2026-10-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(agendamentoId.toString()));
        mockMvc.perform(get("/api/v1/administracao/agendamentos")
                        .session(sessao)
                        .param("dataInicial", "2026-10-01")
                        .param("dataFinal", "2026-11-01"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("PERIODO_CONSULTA_INVALIDO"));
        assertThat(contar("agendamento")).isEqualTo(2);
    }

    @Test
    void criacaoAdministrativaIdempotenteNaoGuardaDadosPessoais() throws Exception {
        MockHttpSession proprietario = autenticar(EMAIL_PROPRIETARIO, SENHA_PROPRIETARIO);
        Cenario cenario = criarCenario("Idempotente", 1);
        inserirFuncionario(cenario.profissionalId(), "funcionario-idempotente@example.test");
        MockHttpSession funcionario = autenticar("funcionario-idempotente@example.test", SENHA_FUNCIONARIO);

        String original = mockMvc.perform(criacao(
                        proprietario, cenario.servicoId(), INICIO, " Cliente Repetido ", "(11) 94444-0000", chave(10)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String repetido = mockMvc.perform(criacao(
                        proprietario, cenario.servicoId(), INICIO, "Cliente Repetido", "+5511944440000", chave(10)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        assertThat(JsonPath.<String>read(repetido, "$.id")).isEqualTo(JsonPath.read(original, "$.id"));
        assertThat(contar("agendamento")).isEqualTo(1);
        assertThat(jdbc.sql("select corpo_resposta::text from registro_idempotencia")
                .query(String.class).single())
                .doesNotContain("Cliente Repetido", "944440000");

        mockMvc.perform(criacao(
                        proprietario, cenario.servicoId(), "2026-10-08T12:00:00-03:00",
                        "Cliente Repetido", "+5511944440000", chave(10)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLITO_IDEMPOTENCIA"));
        mockMvc.perform(criacao(
                        funcionario, cenario.servicoId(), INICIO, "Cliente Repetido", "+5511944440000", chave(10)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLITO_IDEMPOTENCIA"));
    }

    @Test
    void funcionarioOperaSomenteReservasDoProfissionalVinculado() throws Exception {
        MockHttpSession proprietario = autenticar(EMAIL_PROPRIETARIO, SENHA_PROPRIETARIO);
        Cenario proprio = criarCenario("Proprio", 1);
        Cenario outro = criarCenario("Outro", 1);
        inserirFuncionario(proprio.profissionalId(), "funcionario-operacao@example.test");
        MockHttpSession funcionario = autenticar("funcionario-operacao@example.test", SENHA_FUNCIONARIO);

        UUID reservaOutro = idCriado(mockMvc.perform(criacao(
                        proprietario, outro.servicoId(), INICIO, "Cliente Outro", "11933330000", chave(20)))
                .andExpect(status().isCreated()).andReturn());
        String resposta = mockMvc.perform(criacao(
                        funcionario, proprio.servicoId(), INICIO, "Cliente Proprio", "11922220000", chave(21)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        UUID clienteProprio = UUID.fromString(JsonPath.read(resposta, "$.cliente.id"));

        mockMvc.perform(get("/api/v1/administracao/agendamentos")
                        .session(funcionario)
                        .param("dataInicial", "2026-10-08")
                        .param("dataFinal", "2026-10-08")
                        .param("profissionalId", outro.profissionalId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].profissional.id").value(proprio.profissionalId().toString()));
        mockMvc.perform(get("/api/v1/administracao/agendamentos")
                        .session(proprietario)
                        .param("dataInicial", "2026-10-08")
                        .param("dataFinal", "2026-10-08"))
                .andExpect(jsonPath("$.length()").value(2));
        mockMvc.perform(get("/api/v1/administracao/agendamentos")
                        .session(proprietario)
                        .param("dataInicial", "2026-10-08")
                        .param("dataFinal", "2026-10-08")
                        .param("profissionalId", outro.profissionalId().toString())
                        .param("estado", "CONFIRMADO"))
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/v1/administracao/agendamentos/{id}", reservaOutro).session(funcionario))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("AGENDAMENTO_NAO_ENCONTRADO"));
        mockMvc.perform(post("/api/v1/administracao/agendamentos/{id}/cancelar", reservaOutro)
                        .session(funcionario).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Tentativa fora do escopo\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(criacao(
                        funcionario, outro.servicoId(), "2026-10-08T12:00:00-03:00",
                        "Cliente Outro", "11933330000", chave(22)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("SERVICO_NAO_ENCONTRADO"));

        mockMvc.perform(get("/api/v1/administracao/clientes/{id}", clienteProprio).session(funcionario))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/administracao/clientes/{id}/anonimizar", clienteProprio)
                        .session(funcionario).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\":\"Solicitacao do titular\"}"))
                .andExpect(status().isForbidden());
        assertThat(contar("agendamento")).isEqualTo(2);
    }

    @Test
    void cancelamentoRegistraAutoriaELiberaHorario() throws Exception {
        MockHttpSession sessao = autenticar(EMAIL_PROPRIETARIO, SENHA_PROPRIETARIO);
        Cenario cenario = criarCenario("Cancelamento", 1);
        UUID agendamentoId = criarPublico(cenario.servicoId(), INICIO, "11911110000", chave(30));

        mockMvc.perform(disponibilidade(cenario.servicoId()))
                .andExpect(jsonPath("$.dias[0].horarios[?(@.inicioEm == '" + INICIO + "')]").isEmpty());
        mockMvc.perform(cancelamento(sessao, agendamentoId, "   "))
                .andExpect(status().isUnprocessableContent());
        mockMvc.perform(cancelamento(sessao, agendamentoId, "x".repeat(501)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("FALHA_VALIDACAO"));

        mockMvc.perform(cancelamento(sessao, agendamentoId, "  Cliente pediu por telefone  "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CANCELADO"))
                .andExpect(jsonPath("$.origem").value("PUBLICO"))
                .andExpect(jsonPath("$.criadoPor").doesNotExist())
                .andExpect(jsonPath("$.motivoCancelamento").value("Cliente pediu por telefone"))
                .andExpect(jsonPath("$.estadoAlteradoPor").value(usuarioId(EMAIL_PROPRIETARIO).toString()))
                .andExpect(jsonPath("$.estadoAlteradoEm").value("2026-10-07T09:00:00-03:00"));
        mockMvc.perform(disponibilidade(cenario.servicoId()))
                .andExpect(jsonPath("$.dias[0].horarios[?(@.inicioEm == '" + INICIO + "')]").exists());

        mockMvc.perform(cancelamento(sessao, agendamentoId, "Repetido"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("TRANSICAO_AGENDAMENTO_INVALIDA"));
        mockMvc.perform(post("/api/v1/administracao/agendamentos/{id}/concluir", agendamentoId)
                        .session(sessao).with(csrf()))
                .andExpect(status().isConflict());

        UUID iniciado = criarPublico(cenario.servicoId(), "2026-10-08T12:00:00-03:00", "11911112222", chave(31));
        relogio.ajustar(Instant.parse("2026-10-08T15:00:00Z"));
        mockMvc.perform(cancelamento(sessao, iniciado, "Tarde demais"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("TRANSICAO_AGENDAMENTO_INVALIDA"));
    }

    @Test
    void conclusaoEAusenciaSomenteAPartirDoInicio() throws Exception {
        MockHttpSession sessao = autenticar(EMAIL_PROPRIETARIO, SENHA_PROPRIETARIO);
        Cenario cenario = criarCenario("Encerramento", 1);
        UUID concluido = criarPublico(cenario.servicoId(), INICIO, "11921210000", chave(40));
        UUID ausente = criarPublico(cenario.servicoId(), "2026-10-08T12:00:00-03:00", "11921212222", chave(41));

        mockMvc.perform(post("/api/v1/administracao/agendamentos/{id}/concluir", concluido)
                        .session(sessao).with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("TRANSICAO_AGENDAMENTO_INVALIDA"));

        relogio.ajustar(Instant.parse("2026-10-08T13:00:00Z"));
        mockMvc.perform(post("/api/v1/administracao/agendamentos/{id}/concluir", concluido)
                        .session(sessao).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CONCLUIDO"))
                .andExpect(jsonPath("$.motivoCancelamento").doesNotExist());
        mockMvc.perform(post("/api/v1/administracao/agendamentos/{id}/nao-compareceu", concluido)
                        .session(sessao).with(csrf()))
                .andExpect(status().isConflict());
        mockMvc.perform(post("/api/v1/administracao/agendamentos/{id}/nao-compareceu", ausente)
                        .session(sessao).with(csrf()))
                .andExpect(status().isConflict());

        relogio.ajustar(Instant.parse("2026-10-08T15:00:00Z"));
        mockMvc.perform(post("/api/v1/administracao/agendamentos/{id}/nao-compareceu", ausente)
                        .session(sessao).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("NAO_COMPARECEU"));
        mockMvc.perform(post("/api/v1/administracao/agendamentos/{id}/concluir", UUID.randomUUID())
                        .session(sessao).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("AGENDAMENTO_NAO_ENCONTRADO"));
    }

    @Test
    void transicoesConcorrentesAplicamSomenteUmaMudanca() throws Exception {
        MockHttpSession sessao = autenticar(EMAIL_PROPRIETARIO, SENHA_PROPRIETARIO);
        Cenario cenario = criarCenario("Concorrencia", 1);
        UUID futuro = criarPublico(cenario.servicoId(), INICIO, "11931310000", chave(50));
        UUID passado = criarPublico(cenario.servicoId(), "2026-10-08T12:00:00-03:00", "11931312222", chave(51));

        List<MvcResult> cancelamentos = executarConcorrente(
                cancelamento(sessao, futuro, "Primeiro motivo"),
                cancelamento(sessao, futuro, "Segundo motivo"));
        assertThat(cancelamentos).extracting(resultado -> resultado.getResponse().getStatus())
                .containsExactlyInAnyOrder(200, 409);

        relogio.ajustar(Instant.parse("2026-10-08T16:00:00Z"));
        List<MvcResult> encerramentos = executarConcorrente(
                post("/api/v1/administracao/agendamentos/{id}/concluir", passado).session(sessao).with(csrf()),
                post("/api/v1/administracao/agendamentos/{id}/nao-compareceu", passado).session(sessao).with(csrf()));
        assertThat(encerramentos).extracting(resultado -> resultado.getResponse().getStatus())
                .containsExactlyInAnyOrder(200, 409);
        assertThat(jdbc.sql("select estado from agendamento where id = :id")
                .param("id", passado).query(String.class).single())
                .isIn("CONCLUIDO", "NAO_COMPARECEU");
    }

    @Test
    void anonimizacaoExigeAusenciaDeReservaFuturaEAuditaSemDadosPessoais() throws Exception {
        MockHttpSession sessao = autenticar(EMAIL_PROPRIETARIO, SENHA_PROPRIETARIO);
        Cenario cenario = criarCenario("Anonimizacao", 1);
        UUID historico = criarPublico(cenario.servicoId(), INICIO, "11941410000", chave(60));
        UUID clienteId = jdbc.sql("select cliente_id from agendamento where id = :id")
                .param("id", historico).query(UUID.class).single();

        mockMvc.perform(get("/api/v1/administracao/clientes/{id}", clienteId).session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Cliente Teste"))
                .andExpect(jsonPath("$.celular").value("+5511941410000"))
                .andExpect(jsonPath("$.anonimizadoEm").doesNotExist());
        mockMvc.perform(anonimizacao(sessao, clienteId, "Solicitacao do titular"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CLIENTE_POSSUI_RESERVA_FUTURA"));
        mockMvc.perform(anonimizacao(sessao, clienteId, " "))
                .andExpect(status().isUnprocessableContent());

        relogio.ajustar(Instant.parse("2026-10-09T12:00:00Z"));
        mockMvc.perform(anonimizacao(sessao, clienteId, "  Solicitacao do titular  "))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/administracao/clientes/{id}", clienteId).session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").doesNotExist())
                .andExpect(jsonPath("$.celular").doesNotExist())
                .andExpect(jsonPath("$.anonimizadoEm").value("2026-10-09T09:00:00-03:00"));
        mockMvc.perform(get("/api/v1/administracao/agendamentos/{id}", historico).session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente.id").value(clienteId.toString()))
                .andExpect(jsonPath("$.cliente.nome").doesNotExist())
                .andExpect(jsonPath("$.cliente.celular").doesNotExist())
                .andExpect(jsonPath("$.precoContratado").value(120.00));
        assertThat(jdbc.sql("""
                        select autor_id::text || '|' || motivo || '|' || cliente_id::text
                          from anonimizacao_cliente
                        """).query(String.class).single())
                .isEqualTo(usuarioId(EMAIL_PROPRIETARIO) + "|Solicitacao do titular|" + clienteId)
                .doesNotContain("Cliente Teste", "941410000");

        mockMvc.perform(anonimizacao(sessao, clienteId, "Repetida"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CLIENTE_JA_ANONIMIZADO"));
        mockMvc.perform(anonimizacao(sessao, UUID.randomUUID(), "Inexistente"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("CLIENTE_NAO_ENCONTRADO"));

        mockMvc.perform(criacao(
                        sessao, cenario.servicoId(), "2026-10-15T10:00:00-03:00",
                        "Cliente Teste", "11941410000", chave(61)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cliente.id").value(org.hamcrest.Matchers.not(clienteId.toString())));
        assertThat(contar("cliente")).isEqualTo(2);
    }

    @Test
    void publicaContratoDosEndpointsB4() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/agendamentos'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/agendamentos'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/agendamentos/{id}'].get.responses['404']")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/agendamentos/{id}/cancelar'].post.responses['409']")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/agendamentos/{id}/concluir'].post.responses['200']")
                        .exists())
                .andExpect(jsonPath(
                        "$.paths['/api/v1/administracao/agendamentos/{id}/nao-compareceu'].post.responses['200']")
                        .exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/clientes/{id}'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/clientes/{id}/anonimizar'].post.responses['204']")
                        .exists());
    }

    private Cenario criarCenario(String sufixo, int capacidade) {
        UUID profissionalId = UUID.randomUUID();
        UUID servicoId = UUID.randomUUID();
        UUID recursoId = UUID.randomUUID();
        jdbc.sql("insert into profissional (id, nome, ativo) values (:id, :nome, true)")
                .param("id", profissionalId).param("nome", "Profissional " + sufixo).update();
        jdbc.sql("insert into recurso_operacional (id, nome, capacidade, ativo) values (:id, :nome, :capacidade, true)")
                .param("id", recursoId).param("nome", "Recurso " + sufixo).param("capacidade", capacidade).update();
        jdbc.sql("""
                        insert into servico
                            (id, profissional_id, nome, descricao, duracao_minutos,
                             intervalo_limpeza_minutos, preco, ativo, ordem_exibicao)
                        values (:id, :profissionalId, :nome, null, 60, 15, 120.00, true, 0)
                        """)
                .param("id", servicoId).param("profissionalId", profissionalId)
                .param("nome", "Servico " + sufixo).update();
        jdbc.sql("""
                        insert into requisito_recurso_servico (servico_id, recurso_operacional_id, quantidade)
                        values (:servicoId, :recursoId, 1)
                        """)
                .param("servicoId", servicoId).param("recursoId", recursoId).update();
        inserirExpediente(profissionalId, "QUINTA_FEIRA");
        return new Cenario(profissionalId, servicoId);
    }

    private void inserirExpediente(UUID profissionalId, String dia) {
        jdbc.sql("""
                        insert into expediente_semanal (id, profissional_id, dia_semana, inicio, fim)
                        values (:id, :profissionalId, :dia, '09:00', '18:00')
                        """)
                .param("id", UUID.randomUUID()).param("profissionalId", profissionalId).param("dia", dia).update();
    }

    private void inserirFuncionario(UUID profissionalId, String email) {
        jdbc.sql("""
                        insert into usuario_administrativo
                        (id, nome, email, hash_senha, papel, profissional_id, ativo, deve_alterar_senha)
                        values (:id, 'Funcionario Operacao', :email, :hash, 'FUNCIONARIO', :profissional, true, false)
                        """)
                .param("id", UUID.randomUUID()).param("email", email)
                .param("hash", passwordEncoder.encode(SENHA_FUNCIONARIO))
                .param("profissional", profissionalId).update();
    }

    private UUID usuarioId(String email) {
        return jdbc.sql("select id from usuario_administrativo where email = :email")
                .param("email", email).query(UUID.class).single();
    }

    private MockHttpSession autenticar(String email, String senha) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/administracao/autenticacao/entrar")
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha)))
                .andExpect(status().isNoContent())
                .andReturn();
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }

    private UUID criarPublico(UUID servicoId, String inicio, String celular, String chave) throws Exception {
        mockMvc.perform(post("/api/v1/publico/agendamentos")
                        .header("Chave-Idempotencia", chave)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(servicoId, inicio, "Cliente Teste", celular)))
                .andExpect(status().isCreated());
        return jdbc.sql("""
                        select id from agendamento
                         where servico_id = :servicoId and inicio_em = cast(:inicio as timestamptz)
                        """)
                .param("servicoId", servicoId).param("inicio", inicio).query(UUID.class).single();
    }

    private MockHttpServletRequestBuilder criacao(
            MockHttpSession sessao, UUID servicoId, String inicio, String nome, String celular, String chave) {
        return post("/api/v1/administracao/agendamentos")
                .session(sessao)
                .with(csrf())
                .header("Chave-Idempotencia", chave)
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(servicoId, inicio, nome, celular));
    }

    private MockHttpServletRequestBuilder cancelamento(MockHttpSession sessao, UUID id, String motivo) {
        return post("/api/v1/administracao/agendamentos/{id}/cancelar", id)
                .session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\":\"%s\"}".formatted(motivo));
    }

    private MockHttpServletRequestBuilder anonimizacao(MockHttpSession sessao, UUID id, String motivo) {
        return post("/api/v1/administracao/clientes/{id}/anonimizar", id)
                .session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"motivo\":\"%s\"}".formatted(motivo));
    }

    private MockHttpServletRequestBuilder disponibilidade(UUID servicoId) {
        return get("/api/v1/publico/servicos/{id}/disponibilidade", servicoId)
                .param("dataInicial", "2026-10-08")
                .param("dataFinal", "2026-10-08");
    }

    private String corpo(UUID servicoId, String inicio, String nome, String celular) {
        return """
                {
                  "servicoId": "%s",
                  "inicioEm": "%s",
                  "cliente": {"nome": "%s", "celular": "%s"},
                  "versaoAvisoPrivacidade": "2026-01",
                  "cienteAvisoPrivacidade": true
                }
                """.formatted(servicoId, inicio, nome, celular);
    }

    private UUID idCriado(MvcResult resultado) throws Exception {
        return UUID.fromString(JsonPath.read(resultado.getResponse().getContentAsString(), "$.id"));
    }

    private List<MvcResult> executarConcorrente(
            MockHttpServletRequestBuilder primeira,
            MockHttpServletRequestBuilder segunda) throws Exception {
        CountDownLatch inicio = new CountDownLatch(1);
        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {
            Future<MvcResult> um = executor.submit(() -> {
                inicio.await();
                return mockMvc.perform(primeira).andReturn();
            });
            Future<MvcResult> dois = executor.submit(() -> {
                inicio.await();
                return mockMvc.perform(segunda).andReturn();
            });
            inicio.countDown();
            return List.of(um.get(), dois.get());
        }
    }

    private long contar(String tabela) {
        return jdbc.sql("select count(*) from " + tabela).query(Long.class).single();
    }

    private String chave(int numero) {
        return "chave-operacao-%05d".formatted(numero);
    }

    private record Cenario(UUID profissionalId, UUID servicoId) {
    }

    static class RelogioAjustavel extends Clock {

        private volatile Instant instante = AGORA;

        void ajustar(Instant novo) {
            instante = novo;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zona) {
            return Clock.fixed(instante, zona);
        }

        @Override
        public Instant instant() {
            return instante;
        }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ConfiguracaoRelogioTeste {

        @Bean
        @Primary
        RelogioAjustavel relogioAjustavel() {
            return new RelogioAjustavel();
        }
    }
}
