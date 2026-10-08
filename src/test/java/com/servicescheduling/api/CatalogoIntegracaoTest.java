package com.servicescheduling.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import com.servicescheduling.api.compartilhado.infraestrutura.configuracao.PropriedadesNegocio;
import com.servicescheduling.api.servicos.aplicacao.ValidadorGradeServicos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
        "aplicacao.acesso.bootstrap.nome=Proprietario Catalogo",
        "aplicacao.acesso.bootstrap.email=proprietario-catalogo@example.test",
        "aplicacao.acesso.bootstrap.senha=senha-catalogo-segura"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class CatalogoIntegracaoTest {

    private static final String EMAIL_PROPRIETARIO = "proprietario-catalogo@example.test";
    private static final String SENHA_PROPRIETARIO = "senha-catalogo-segura";

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

    @Autowired
    PropriedadesNegocio propriedadesNegocio;

    @Autowired
    ValidadorGradeServicos validadorGrade;

    @BeforeEach
    void limparCatalogo() {
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
    void proprietarioCriaCatalogoCompletoEPublicaServico() throws Exception {
        MockHttpSession sessao = autenticar(EMAIL_PROPRIETARIO, SENHA_PROPRIETARIO);
        UUID profissionalId = id(criar(sessao, "/api/v1/administracao/profissionais", """
                {"nome":"Profissional Um","ativo":true}
                """));
        UUID recursoId = id(criar(sessao, "/api/v1/administracao/recursos", """
                {"nome":"Sala Um","capacidade":2,"ativo":true}
                """));

        MvcResult resultado = criar(sessao, "/api/v1/administracao/servicos",
                servicoJson(profissionalId, "Consulta", 60, 0, "120.00", true, 2, recursoId, 1));
        UUID servicoId = id(resultado);

        mockMvc.perform(get("/api/v1/administracao/servicos/{id}", servicoId).session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.moeda").value("BRL"))
                .andExpect(jsonPath("$.requisitosRecursos[0].recursoNome").value("Sala Um"));

        mockMvc.perform(get("/api/v1/publico/servicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(servicoId.toString()))
                .andExpect(jsonPath("$[0].preco").value(120.00))
                .andExpect(jsonPath("$[0].profissionalNome").value("Profissional Um"));
    }

    @Test
    void funcionarioMantemSomenteOsPropriosServicos() throws Exception {
        UUID proprio = inserirProfissional("Profissional Proprio", true);
        UUID outro = inserirProfissional("Profissional Outro", true);
        UUID recursoId = inserirRecurso("Equipamento", 3, true);
        UUID servicoOutro = inserirServico(outro, "Servico Outro", true, 0);
        inserirFuncionario(proprio, "funcionario-catalogo@example.test");
        MockHttpSession sessao = autenticar("funcionario-catalogo@example.test", "senha-funcionario");

        mockMvc.perform(get("/api/v1/administracao/recursos").session(sessao))
                .andExpect(status().isOk());

        UUID servicoId = id(criar(sessao, "/api/v1/administracao/servicos",
                servicoJson(null, "Servico Proprio", 45, 0, "80.00", true, 0, recursoId, 1)));
        mockMvc.perform(put("/api/v1/administracao/servicos/{id}", servicoId)
                        .session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(servicoJson(proprio, "Servico Atualizado", 45, 0, "90.00", false, 0, recursoId, 1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Servico Atualizado"))
                .andExpect(jsonPath("$.ativo").value(false));

        mockMvc.perform(get("/api/v1/administracao/servicos").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
        mockMvc.perform(get("/api/v1/administracao/servicos/{id}", servicoOutro).session(sessao))
                .andExpect(status().isNotFound());
        mockMvc.perform(put("/api/v1/administracao/servicos/{id}", servicoOutro)
                        .session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(servicoJson(proprio, "Invasao", 45, 0, "90.00", true, 0, recursoId, 1)))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/v1/administracao/servicos")
                        .session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(servicoJson(outro, "Fora do Escopo", 45, 0, "90.00", true, 0, recursoId, 1)))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/api/v1/administracao/recursos")
                        .session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Novo\",\"capacidade\":1,\"ativo\":true}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/v1/administracao/recursos/{id}", recursoId).session(sessao))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejeitaServicoInvalidoSemPersistenciaParcial() throws Exception {
        MockHttpSession sessao = autenticar(EMAIL_PROPRIETARIO, SENHA_PROPRIETARIO);
        UUID profissionalId = inserirProfissional("Profissional", true);
        UUID recursoId = inserirRecurso("Sala", 1, true);

        mockMvc.perform(post("/api/v1/administracao/profissionais")
                        .session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Sem estado\"}"))
                .andExpect(status().is(422));

        mockMvc.perform(post("/api/v1/administracao/servicos")
                        .session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(servicoJson(profissionalId, "Invalido", 20, 0, "50.00", true, 0, recursoId, 2)))
                .andExpect(status().is(422));

        mockMvc.perform(post("/api/v1/administracao/servicos")
                        .session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content(servicoJson(profissionalId, "Nulo", 30, 0, "50.00", true, 0, recursoId, 1)
                                .replace("[{\"recursoId\":\"" + recursoId + "\",\"quantidade\":1}]", "[null]")))
                .andExpect(status().is(422));

        Integer servicos = jdbcClient.sql("select count(*) from servico").query(Integer.class).single();
        Integer requisitos = jdbcClient.sql("select count(*) from requisito_recurso_servico")
                .query(Integer.class).single();
        assertThat(servicos).isZero();
        assertThat(requisitos).isZero();
    }

    @Test
    void rejeitaReducaoDeCapacidadeAbaixoDoRequisito() throws Exception {
        MockHttpSession sessao = autenticar(EMAIL_PROPRIETARIO, SENHA_PROPRIETARIO);
        UUID profissionalId = inserirProfissional("Profissional", true);
        UUID recursoId = inserirRecurso("Sala", 3, true);
        criar(sessao, "/api/v1/administracao/servicos",
                servicoJson(profissionalId, "Servico", 60, 0, "50.00", true, 0, recursoId, 2));

        mockMvc.perform(put("/api/v1/administracao/recursos/{id}", recursoId)
                        .session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Sala\",\"capacidade\":1,\"ativo\":true}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("CAPACIDADE_INFERIOR_AO_REQUISITO"));
    }

    @Test
    void catalogoPublicoFiltraInativosEOrdenaServicos() throws Exception {
        UUID profissionalAtivo = inserirProfissional("Ativo", true);
        UUID profissionalInativo = inserirProfissional("Inativo", false);
        inserirServico(profissionalAtivo, "Segundo", true, 2);
        inserirServico(profissionalAtivo, "Primeiro", true, 1);
        inserirServico(profissionalAtivo, "Servico Inativo", false, 0);
        inserirServico(profissionalInativo, "Profissional Inativo", true, 0);

        mockMvc.perform(get("/api/v1/publico/servicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("Primeiro"))
                .andExpect(jsonPath("$[1].nome").value("Segundo"));
    }

    @Test
    void rejeitaConfiguracaoDeGradeIncompativelComServicoPersistido() {
        UUID profissionalId = inserirProfissional("Profissional", true);
        inserirServico(profissionalId, "Servico", true, 0);
        propriedadesNegocio.setIntervaloGradeMinutos(45);
        try {
            assertThatThrownBy(validadorGrade::validar)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("NEGOCIO_INTERVALO_GRADE_MINUTOS");
        } finally {
            propriedadesNegocio.setIntervaloGradeMinutos(15);
        }
    }

    @Test
    void exigeCsrfNasMutacoesDoCatalogo() throws Exception {
        MockHttpSession sessao = autenticar(EMAIL_PROPRIETARIO, SENHA_PROPRIETARIO);
        mockMvc.perform(post("/api/v1/administracao/profissionais")
                        .session(sessao).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"Sem CSRF\",\"ativo\":true}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("FALHA_VALIDACAO_CSRF"));
    }

    @Test
    void documentaCatalogoNoOpenApiSemPatch() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/publico/servicos'].get").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/profissionais'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/recursos/{id}'].put").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/servicos/{id}'].put").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/servicos/{id}'].patch").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.DadosServico").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/servicos'].post.security[0].sessao").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/servicos'].post.security[0].csrf").exists());
    }

    private MvcResult criar(MockHttpSession sessao, String rota, String corpo) throws Exception {
        return mockMvc.perform(post(rota)
                        .session(sessao).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(corpo))
                .andExpect(status().isCreated())
                .andReturn();
    }

    private UUID id(MvcResult resultado) throws Exception {
        String corpo = resultado.getResponse().getContentAsString();
        return UUID.fromString(corpo.replaceFirst(".*\\\"id\\\":\\\"([^\\\"]+)\\\".*", "$1"));
    }

    private MockHttpSession autenticar(String email, String senha) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/administracao/autenticacao/entrar")
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha)))
                .andExpect(status().isNoContent())
                .andReturn();
        return (MockHttpSession) resultado.getRequest().getSession(false);
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

    private void inserirFuncionario(UUID profissionalId, String email) {
        jdbcClient.sql("""
                        insert into usuario_administrativo
                        (id, nome, email, hash_senha, papel, profissional_id, ativo, deve_alterar_senha)
                        values (:id, 'Funcionario Catalogo', :email, :hash, 'FUNCIONARIO', :profissional, true, false)
                        """)
                .param("id", UUID.randomUUID()).param("email", email)
                .param("hash", passwordEncoder.encode("senha-funcionario"))
                .param("profissional", profissionalId).update();
    }

    private UUID inserirServico(UUID profissionalId, String nome, boolean ativo, int ordem) {
        UUID id = UUID.randomUUID();
        jdbcClient.sql("""
                        insert into servico
                        (id, profissional_id, nome, duracao_minutos, intervalo_limpeza_minutos,
                         preco, ativo, ordem_exibicao)
                        values (:id, :profissional, :nome, 60, 0, 50.00, :ativo, :ordem)
                        """)
                .param("id", id).param("profissional", profissionalId)
                .param("nome", nome).param("ativo", ativo).param("ordem", ordem).update();
        return id;
    }

    private String servicoJson(
            UUID profissionalId,
            String nome,
            int duracao,
            int limpeza,
            String preco,
            boolean ativo,
            int ordem,
            UUID recursoId,
            int quantidade) {
        String profissional = profissionalId == null ? "null" : "\"" + profissionalId + "\"";
        return """
                {"profissionalId":%s,"nome":"%s","descricao":"Descricao",
                 "duracaoMinutos":%d,"intervaloLimpezaMinutos":%d,"preco":%s,
                 "ativo":%s,"ordemExibicao":%d,
                 "requisitosRecursos":[{"recursoId":"%s","quantidade":%d}]}
                """.formatted(
                profissional, nome, duracao, limpeza, preco, ativo, ordem, recursoId, quantidade);
    }
}
