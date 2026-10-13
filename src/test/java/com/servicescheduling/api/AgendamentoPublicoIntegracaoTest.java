package com.servicescheduling.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(properties = {
        "aplicacao.acesso.bootstrap.nome=Proprietario Agendamento",
        "aplicacao.acesso.bootstrap.email=proprietario-agendamento@example.test",
        "aplicacao.acesso.bootstrap.senha=senha-agendamento-segura",
        "aplicacao.negocio.fuso-horario=America/Sao_Paulo",
        "aplicacao.negocio.antecedencia-minima-reserva-minutos=60",
        "aplicacao.negocio.horizonte-reserva-dias=90",
        "aplicacao.negocio.versao-aviso-privacidade=2026-01",
        "aplicacao.negocio.texto-aviso-privacidade=Aviso de teste vigente"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import(AgendamentoPublicoIntegracaoTest.ConfiguracaoRelogioTeste.class)
class AgendamentoPublicoIntegracaoTest {

    private static final String INICIO = "2026-10-08T10:00:00-03:00";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRESQL = new PostgreSQLContainer(
            DockerImageName.parse("postgres:17-alpine"));

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcClient jdbc;

    @BeforeEach
    void limparDados() {
        jdbc.sql("delete from registro_idempotencia").update();
        jdbc.sql("delete from alocacao_recurso_agendamento").update();
        jdbc.sql("delete from agendamento").update();
        jdbc.sql("delete from cliente").update();
        jdbc.sql("delete from bloqueio_agenda").update();
        jdbc.sql("delete from expediente_semanal").update();
        jdbc.sql("delete from requisito_recurso_servico").update();
        jdbc.sql("delete from servico").update();
        jdbc.sql("delete from recurso_operacional").update();
        jdbc.sql("delete from usuario_administrativo where profissional_id is not null").update();
        jdbc.sql("delete from profissional").update();
    }

    @Test
    void consultaAvisoCriaReservaComFotografiasEAtualizaDisponibilidade() throws Exception {
        Cenario cenario = criarCenario(null, 1, 1, "Principal");

        mockMvc.perform(get("/api/v1/publico/aviso-privacidade"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.versao").value("2026-01"))
                .andExpect(jsonPath("$.texto").value("Aviso de teste vigente"));

        mockMvc.perform(criacao(cenario.servicoId(), INICIO, "Cliente Teste", "(11) 99999-0000", chave(1)))
                .andExpect(status().isCreated())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"))
                .andExpect(jsonPath("$.servico.id").value(cenario.servicoId().toString()))
                .andExpect(jsonPath("$.servico.nome").value("Servico Principal"))
                .andExpect(jsonPath("$.profissional.nome").value("Profissional Principal"))
                .andExpect(jsonPath("$.inicioEm").value(INICIO))
                .andExpect(jsonPath("$.fimServicoEm").value("2026-10-08T11:00:00-03:00"))
                .andExpect(jsonPath("$.precoContratado").value(120.00))
                .andExpect(jsonPath("$.moedaContratada").value("BRL"))
                .andExpect(jsonPath("$.agendamentoId").doesNotExist())
                .andExpect(jsonPath("$.clienteId").doesNotExist())
                .andExpect(jsonPath("$.cliente").doesNotExist());

        assertThat(contar("agendamento")).isEqualTo(1);
        assertThat(contar("cliente")).isEqualTo(1);
        assertThat(contar("alocacao_recurso_agendamento")).isEqualTo(1);
        assertThat(jdbc.sql("select celular_cliente_informado from agendamento")
                .query(String.class).single()).isEqualTo("+5511999990000");

        mockMvc.perform(get("/api/v1/publico/servicos/{id}/disponibilidade", cenario.servicoId())
                        .param("dataInicial", "2026-10-08")
                        .param("dataFinal", "2026-10-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dias[0].horarios[?(@.inicioEm == '2026-10-08T10:00:00-03:00')]").isEmpty())
                .andExpect(jsonPath("$.dias[0].horarios[?(@.inicioEm == '2026-10-08T11:15:00-03:00')]").exists());
    }

    @Test
    void repeteResultadoNormalizadoERejeitaReusoDivergenteSemConsumirFalhas() throws Exception {
        Cenario cenario = criarCenario(null, 1, 1, "Idempotencia");
        String chave = chave(2);

        String original = mockMvc.perform(
                        criacao(cenario.servicoId(), INICIO, "  Cliente Idempotente  ", "(11) 98888-0000", chave))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String repetido = mockMvc.perform(
                        criacao(cenario.servicoId(), INICIO, "Cliente Idempotente", "+5511988880000", chave))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        assertThat(repetido).isEqualTo(original);
        assertThat(contar("agendamento")).isEqualTo(1);
        assertThat(contar("registro_idempotencia")).isEqualTo(1);

        mockMvc.perform(criacao(
                        cenario.servicoId(), "2026-10-08T12:00:00-03:00",
                        "Outro Cliente", "11977770000", chave))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLITO_IDEMPOTENCIA"));

        String chaveFalha = chave(3);
        mockMvc.perform(criacao(
                        cenario.servicoId(), INICIO, "Outro Cliente", "11977770000", chaveFalha))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CONFLITO_AGENDAMENTO"));
        assertThat(jdbc.sql("select count(*) from registro_idempotencia where hash_chave <> ''")
                .query(Long.class).single()).isEqualTo(1);
    }

    @Test
    void validaCabecalhoCelularEAvisoSemExporDados() throws Exception {
        Cenario cenario = criarCenario(null, 1, 1, "Validacao");
        String corpo = corpo(cenario.servicoId(), INICIO, "Nome Sensivel", "1199999/0000", "2026-01");

        mockMvc.perform(post("/api/v1/publico/agendamentos")
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("FALHA_VALIDACAO"));

        String resposta = mockMvc.perform(post("/api/v1/publico/agendamentos")
                        .header("Chave-Idempotencia", chave(4))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.codigo").value("FALHA_VALIDACAO"))
                .andReturn().getResponse().getContentAsString();
        assertThat(resposta).doesNotContain("Nome Sensivel", "1199999/0000", chave(4));

        mockMvc.perform(post("/api/v1/publico/agendamentos")
                        .header("Chave-Idempotencia", chave(5))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo(cenario.servicoId(), INICIO, "Cliente", "11999990000", "anterior")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("AVISO_PRIVACIDADE_DESATUALIZADO"));
        assertThat(contar("registro_idempotencia")).isZero();
    }

    @Test
    void falhaInesperadaNaoConsomeChaveIdempotente() throws Exception {
        Cenario cenario = criarCenario(null, 1, 1, "Falha Inesperada");
        String chave = chave(50);
        jdbc.sql("""
                        create function falhar_criacao_agendamento() returns trigger as $$
                        begin
                            raise exception 'falha controlada de teste';
                        end;
                        $$ language plpgsql
                        """).update();
        jdbc.sql("""
                        create trigger tg_falhar_criacao_agendamento
                        before insert on agendamento
                        for each row execute function falhar_criacao_agendamento()
                        """).update();
        try {
            mockMvc.perform(criacao(
                            cenario.servicoId(), INICIO,
                            "Cliente Falha", "11912120000", chave))
                    .andExpect(status().isInternalServerError());
        } finally {
            jdbc.sql("drop trigger if exists tg_falhar_criacao_agendamento on agendamento").update();
            jdbc.sql("drop function if exists falhar_criacao_agendamento()").update();
        }

        assertThat(contar("registro_idempotencia")).isZero();
        mockMvc.perform(criacao(
                        cenario.servicoId(), INICIO,
                        "Cliente Falha", "11912120000", chave))
                .andExpect(status().isCreated());
        assertThat(contar("agendamento")).isEqualTo(1);
        assertThat(contar("registro_idempotencia")).isEqualTo(1);
    }

    @Test
    void serializaReservasConcorrentesDoMesmoProfissional() throws Exception {
        Cenario cenario = criarCenario(null, 1, 1, "Concorrencia");
        List<MvcResult> resultados = executarConcorrente(
                criacao(cenario.servicoId(), INICIO, "Cliente Um", "11911110000", chave(10)),
                criacao(cenario.servicoId(), INICIO, "Cliente Dois", "11922220000", chave(11)));

        assertThat(resultados).extracting(resultado -> resultado.getResponse().getStatus())
                .containsExactlyInAnyOrder(201, 409);
        assertThat(contar("agendamento")).isEqualTo(1);
    }

    @Test
    void serializaProfissionaisQueDisputamRecursoUnitario() throws Exception {
        UUID recursoId = inserirRecurso(1, "Compartilhado");
        Cenario primeiro = criarCenario(recursoId, 1, 1, "Recurso Um");
        Cenario segundo = criarCenario(recursoId, 1, 1, "Recurso Dois");

        List<MvcResult> resultados = executarConcorrente(
                criacao(primeiro.servicoId(), INICIO, "Cliente Um", "11933330000", chave(12)),
                criacao(segundo.servicoId(), INICIO, "Cliente Dois", "11944440000", chave(13)));

        assertThat(resultados).extracting(resultado -> resultado.getResponse().getStatus())
                .containsExactlyInAnyOrder(201, 409);
        assertThat(contar("agendamento")).isEqualTo(1);
    }

    @Test
    void protegeUltimaUnidadeEIdempotenciaConcorrente() throws Exception {
        UUID recursoId = inserirRecurso(2, "Capacidade Dois");
        Cenario existente = criarCenario(recursoId, 1, 1, "Existente");
        Cenario primeiro = criarCenario(recursoId, 1, 1, "Ultima Um");
        Cenario segundo = criarCenario(recursoId, 1, 1, "Ultima Dois");
        mockMvc.perform(criacao(
                        existente.servicoId(), INICIO, "Cliente Existente", "11955550000", chave(14)))
                .andExpect(status().isCreated());

        List<MvcResult> disputa = executarConcorrente(
                criacao(primeiro.servicoId(), INICIO, "Cliente Um", "11966660000", chave(15)),
                criacao(segundo.servicoId(), INICIO, "Cliente Dois", "11977770000", chave(16)));
        assertThat(disputa).extracting(resultado -> resultado.getResponse().getStatus())
                .containsExactlyInAnyOrder(201, 409);
        assertThat(contar("agendamento")).isEqualTo(2);

        Cenario idempotente = criarCenario(null, 1, 1, "Mesma Chave");
        var requisicao = criacao(
                idempotente.servicoId(), "2026-10-08T14:00:00-03:00",
                "Cliente Igual", "11988880000", chave(17));
        List<MvcResult> repeticoes = executarConcorrente(requisicao, criacao(
                idempotente.servicoId(), "2026-10-08T14:00:00-03:00",
                "Cliente Igual", "+5511988880000", chave(17)));
        assertThat(repeticoes).extracting(resultado -> resultado.getResponse().getStatus())
                .containsOnly(201);
        assertThat(repeticoes.get(0).getResponse().getContentAsString())
                .isEqualTo(repeticoes.get(1).getResponse().getContentAsString());
        assertThat(jdbc.sql("select count(*) from agendamento where servico_id = :servicoId")
                .param("servicoId", idempotente.servicoId()).query(Long.class).single()).isEqualTo(1);
    }

    @Test
    void consideraPicoSimultaneoEmVezDeSomarReservasSequenciais() throws Exception {
        UUID recursoId = inserirRecurso(2, "Capacidade Sequencial");
        Cenario primeiro = criarCenario(recursoId, 2, 1, "Sequencial Um");
        Cenario segundo = criarCenario(recursoId, 2, 1, "Sequencial Dois");
        Cenario longo = criarCenario(recursoId, 2, 1, "Sequencial Longo");
        jdbc.sql("update servico set duracao_minutos = 135, intervalo_limpeza_minutos = 0 where id = :id")
                .param("id", longo.servicoId()).update();

        mockMvc.perform(criacao(primeiro.servicoId(), INICIO, "Cliente Um", "11930300000", chave(18)))
                .andExpect(status().isCreated());
        mockMvc.perform(criacao(
                        segundo.servicoId(), "2026-10-08T11:15:00-03:00",
                        "Cliente Dois", "11940400000", chave(19)))
                .andExpect(status().isCreated());

        mockMvc.perform(criacao(longo.servicoId(), INICIO, "Cliente Longo", "11950500000", chave(20)))
                .andExpect(status().isCreated());
        assertThat(contar("agendamento")).isEqualTo(3);
    }

    @Test
    void aceitaLimitesERejeitaValoresForaDosLimites() throws Exception {
        Cenario cenario = criarCenario(null, 1, 1, "Limites");

        mockMvc.perform(criacao(cenario.servicoId(), INICIO, "A", "11960600000", "a".repeat(16)))
                .andExpect(status().isCreated());
        mockMvc.perform(criacao(
                        cenario.servicoId(), "2026-10-08T12:00:00-03:00",
                        "B".repeat(120), "11970700000", "b".repeat(128)))
                .andExpect(status().isCreated());

        for (String chaveInvalida : List.of("a".repeat(15), "b".repeat(129), "chave com espaco")) {
            mockMvc.perform(criacao(
                            cenario.servicoId(), "2026-10-08T14:00:00-03:00",
                            "Cliente", "11980800000", chaveInvalida))
                    .andExpect(status().isUnprocessableContent());
        }
        mockMvc.perform(criacao(
                        cenario.servicoId(), "2026-10-08T14:00:00-03:00",
                        "   ", "11980800000", chave(30)))
                .andExpect(status().isUnprocessableContent());
        mockMvc.perform(criacao(
                        cenario.servicoId(), "2026-10-08T14:00:00-03:00",
                        "C".repeat(121), "11980800000", chave(31)))
                .andExpect(status().isUnprocessableContent());

        assertThat(contar("agendamento")).isEqualTo(2);
        assertThat(contar("registro_idempotencia")).isEqualTo(2);
    }

    @Test
    void reservaSemRecursosERejeitaServicoDesativadoAposConsulta() throws Exception {
        Cenario semRecursos = criarCenario(null, 1, 1, "Sem Recursos");
        jdbc.sql("delete from requisito_recurso_servico where servico_id = :servicoId")
                .param("servicoId", semRecursos.servicoId()).update();
        mockMvc.perform(criacao(
                        semRecursos.servicoId(), INICIO,
                        "Cliente Sem Recurso", "11990900000", chave(32)))
                .andExpect(status().isCreated());
        assertThat(contar("alocacao_recurso_agendamento")).isZero();

        Cenario desativado = criarCenario(null, 1, 1, "Desativado");
        mockMvc.perform(get("/api/v1/publico/servicos/{id}/disponibilidade", desativado.servicoId())
                        .param("dataInicial", "2026-10-08")
                        .param("dataFinal", "2026-10-08"))
                .andExpect(status().isOk());
        jdbc.sql("update servico set ativo = false where id = :id")
                .param("id", desativado.servicoId()).update();
        mockMvc.perform(criacao(
                        desativado.servicoId(), INICIO,
                        "Cliente Desativado", "11901000000", chave(33)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("SERVICO_NAO_ENCONTRADO"));
    }

    @Test
    void reutilizaChaveNoLimiteDeExpiracaoELimpaRegistro() throws Exception {
        Cenario cenario = criarCenario(null, 1, 1, "Expiracao");
        String chave = chave(40);
        String original = mockMvc.perform(
                        criacao(cenario.servicoId(), INICIO, "Cliente Um", "11910100000", chave))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        jdbc.sql("""
                        update registro_idempotencia
                           set criado_em = :agora - interval '1 second',
                               expira_em = :agora + interval '1 second'
                        """)
                .param("agora", OffsetDateTime.parse("2026-10-07T12:00:00Z"))
                .update();
        String antesDoLimite = mockMvc.perform(
                        criacao(cenario.servicoId(), INICIO, "Cliente Um", "+5511910100000", chave))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        assertThat(antesDoLimite).isEqualTo(original);

        jdbc.sql("""
                        update registro_idempotencia
                           set criado_em = :agora - interval '1 second',
                               expira_em = :agora
                        """)
                .param("agora", OffsetDateTime.parse("2026-10-07T12:00:00Z"))
                .update();

        mockMvc.perform(criacao(
                        cenario.servicoId(), "2026-10-08T12:00:00-03:00",
                        "Cliente Dois", "11920200000", chave))
                .andExpect(status().isCreated());

        assertThat(contar("agendamento")).isEqualTo(2);
        assertThat(contar("registro_idempotencia")).isEqualTo(1);
    }

    @Test
    void publicaContratoDosEndpointsB3() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/publico/aviso-privacidade'].get.responses['200']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/publico/agendamentos'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/publico/agendamentos'].post.responses['409']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/publico/agendamentos'].post.responses['429']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/publico/agendamentos'].post.parameters[0].name")
                        .value("Chave-Idempotencia"))
                .andExpect(jsonPath("$.paths['/api/v1/publico/agendamentos'].post.parameters[0].required")
                        .value(true))
                .andExpect(jsonPath("$.paths['/api/v1/publico/agendamentos'].post.parameters[0].schema.pattern")
                        .value("[A-Za-z0-9._~-]{16,128}"));
    }

    private Cenario criarCenario(UUID recursoExistente, int capacidade, int quantidade, String sufixo) {
        UUID profissionalId = UUID.randomUUID();
        UUID servicoId = UUID.randomUUID();
        UUID recursoId = recursoExistente == null ? inserirRecurso(capacidade, sufixo) : recursoExistente;
        jdbc.sql("insert into profissional (id, nome, ativo) values (:id, :nome, true)")
                .param("id", profissionalId).param("nome", "Profissional " + sufixo).update();
        jdbc.sql("""
                        insert into servico
                            (id, profissional_id, nome, descricao, duracao_minutos,
                             intervalo_limpeza_minutos, preco, ativo, ordem_exibicao)
                        values (:id, :profissionalId, :nome, null, 60, 15, 120.00, true, 0)
                        """)
                .param("id", servicoId).param("profissionalId", profissionalId)
                .param("nome", "Servico " + sufixo).update();
        jdbc.sql("""
                        insert into requisito_recurso_servico
                            (servico_id, recurso_operacional_id, quantidade)
                        values (:servicoId, :recursoId, :quantidade)
                        """)
                .param("servicoId", servicoId).param("recursoId", recursoId)
                .param("quantidade", quantidade).update();
        jdbc.sql("""
                        insert into expediente_semanal (id, profissional_id, dia_semana, inicio, fim)
                        values (:id, :profissionalId, 'QUINTA_FEIRA', '09:00', '18:00')
                        """)
                .param("id", UUID.randomUUID()).param("profissionalId", profissionalId).update();
        return new Cenario(profissionalId, servicoId, recursoId);
    }

    private UUID inserirRecurso(int capacidade, String sufixo) {
        UUID id = UUID.randomUUID();
        jdbc.sql("insert into recurso_operacional (id, nome, capacidade, ativo) values (:id, :nome, :capacidade, true)")
                .param("id", id).param("nome", "Recurso " + sufixo).param("capacidade", capacidade).update();
        return id;
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder criacao(
            UUID servicoId, String inicio, String nome, String celular, String chave) {
        return post("/api/v1/publico/agendamentos")
                .header("Chave-Idempotencia", chave)
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo(servicoId, inicio, nome, celular, "2026-01"));
    }

    private String corpo(UUID servicoId, String inicio, String nome, String celular, String versao) {
        return """
                {
                  "servicoId": "%s",
                  "inicioEm": "%s",
                  "cliente": {"nome": "%s", "celular": "%s"},
                  "versaoAvisoPrivacidade": "%s",
                  "cienteAvisoPrivacidade": true
                }
                """.formatted(servicoId, inicio, nome, celular, versao);
    }

    private List<MvcResult> executarConcorrente(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder primeira,
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder segunda) throws Exception {
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
        return "chave-teste-%05d".formatted(numero);
    }

    private record Cenario(UUID profissionalId, UUID servicoId, UUID recursoId) {
    }

    @TestConfiguration
    static class ConfiguracaoRelogioTeste {

        @Bean
        @Primary
        Clock relogioFixoAgendamento() {
            return Clock.fixed(Instant.parse("2026-10-07T12:00:00Z"), ZoneOffset.UTC);
        }
    }
}
