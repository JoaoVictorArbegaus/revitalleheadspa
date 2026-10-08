package com.servicescheduling.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import com.servicescheduling.api.acesso.aplicacao.ServicoUsuariosAdministrativos;
import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio;
import com.servicescheduling.api.acesso.infraestrutura.configuracao.BootstrapProprietario;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
        "aplicacao.acesso.bootstrap.nome=Proprietario Teste",
        "aplicacao.acesso.bootstrap.email=proprietario@example.test",
        "aplicacao.acesso.bootstrap.senha=senha-teste-segura"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class AcessoAdministrativoIntegracaoTest {

    private static final String EMAIL_PROPRIETARIO = "proprietario@example.test";
    private static final String SENHA_PROPRIETARIO = "senha-teste-segura";

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
    BootstrapProprietario bootstrapProprietario;

    @Autowired
    ServicoUsuariosAdministrativos servicoUsuarios;

    @BeforeEach
    void restaurarProprietario() {
        jdbcClient.sql("delete from usuario_administrativo where email <> :email")
                .param("email", EMAIL_PROPRIETARIO)
                .update();
        jdbcClient.sql("delete from profissional").update();
        jdbcClient.sql("""
                        update usuario_administrativo
                           set hash_senha = :hash, ativo = true, deve_alterar_senha = false,
                               nome = 'Proprietario Teste', email = :email, profissional_id = null
                         where email = :email
                        """)
                .param("hash", passwordEncoder.encode(SENHA_PROPRIETARIO))
                .param("email", EMAIL_PROPRIETARIO)
                .update();
    }

    @Test
    void bootstrapCriaUmProprietarioComBcryptCustoDoze() {
        Integer quantidade = jdbcClient.sql("select count(*) from usuario_administrativo")
                .query(Integer.class).single();
        String hash = jdbcClient.sql("select hash_senha from usuario_administrativo")
                .query(String.class).single();

        assertThat(quantidade).isEqualTo(1);
        assertThat(hash).startsWith("$2a$12$");
        assertThat(passwordEncoder.matches(SENHA_PROPRIETARIO, hash)).isTrue();
    }

    @Test
    void bootstrapRepetidoNaoRedefineSenha() throws Exception {
        String hashAnterior = jdbcClient.sql("select hash_senha from usuario_administrativo")
                .query(String.class).single();

        bootstrapProprietario.run(new DefaultApplicationArguments());

        String hashPosterior = jdbcClient.sql("select hash_senha from usuario_administrativo")
                .query(String.class).single();
        assertThat(hashPosterior).isEqualTo(hashAnterior);
    }

    @Test
    void forneceCsrfEmCookieECabecalhoDocumentado() throws Exception {
        mockMvc.perform(get("/api/v1/administracao/autenticacao/csrf"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("CSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.nomeCabecalho").value("X-CSRF-TOKEN"));
    }

    @Test
    void usaCookieETokenCsrfReaisParaEntrar() throws Exception {
        MvcResult csrf = mockMvc.perform(get("/api/v1/administracao/autenticacao/csrf"))
                .andExpect(status().isOk())
                .andReturn();
        String token = csrf.getResponse().getContentAsString()
                .replaceFirst(".*\\\"token\\\":\\\"([^\\\"]+)\\\".*", "$1");
        Cookie cookieCsrf = csrf.getResponse().getCookie("CSRF-TOKEN");
        MockHttpSession sessao = (MockHttpSession) csrf.getRequest().getSession(false);

        MockHttpServletRequestBuilder login = post("/api/v1/administracao/autenticacao/entrar")
                .cookie(cookieCsrf)
                .header("X-CSRF-TOKEN", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(credenciais(EMAIL_PROPRIETARIO, SENHA_PROPRIETARIO));
        if (sessao != null) {
            login.session(sessao);
        }
        mockMvc.perform(login)
                .andExpect(status().isNoContent());
    }

    @Test
    void documentaRotasDeAcessoNoOpenApi() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/autenticacao/entrar'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/autenticacao/senha'].put").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/usuarios'].post").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/usuarios/{id}'].put").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/usuarios/{id}'].patch").doesNotExist())
                .andExpect(jsonPath("$.components.schemas.DadosUsuarioAdministrativo").exists())
                .andExpect(jsonPath("$.components.securitySchemes.sessao").exists())
                .andExpect(jsonPath("$.components.securitySchemes.csrf").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/autenticacao/entrar'].post.security[0].csrf").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/autenticacao/senha'].put.security[0].sessao").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/autenticacao/senha'].put.security[0].csrf").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/autenticacao/entrar'].post.responses['429']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/autenticacao/entrar'].post.responses['415']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/autenticacao/entrar'].post.responses['500']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/autenticacao/entrar'].post.responses['200']").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/usuarios'].post.responses['201']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/usuarios'].post.responses['201'].content['application/json'].schema").exists())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/usuarios'].post.responses['200']").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/v1/administracao/usuarios'].post.responses['409']").exists());
    }

    @Test
    void loginTrocaIdentificadorDaSessaoEExpoeIdentidade() throws Exception {
        MockHttpSession sessao = new MockHttpSession();
        sessao.setAttribute("atributo-anterior", "preservado");
        String idAnterior = sessao.getId();

        mockMvc.perform(post("/api/v1/administracao/autenticacao/entrar")
                        .session(sessao)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciais(EMAIL_PROPRIETARIO, SENHA_PROPRIETARIO)))
                .andExpect(status().isNoContent());

        assertThat(sessao.getId()).isNotEqualTo(idAnterior);
        mockMvc.perform(get("/api/v1/administracao/autenticacao/eu").session(sessao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL_PROPRIETARIO))
                .andExpect(jsonPath("$.papel").value("PROPRIETARIO"));
    }

    @Test
    void escritaSemCsrfEhRejeitada() throws Exception {
        MockHttpSession sessao = autenticar(EMAIL_PROPRIETARIO, SENHA_PROPRIETARIO);
        mockMvc.perform(post("/api/v1/administracao/autenticacao/sair").session(sessao))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.codigo").value("FALHA_VALIDACAO_CSRF"));
    }

    @Test
    void bloqueiaSextaTentativaDaMesmaOrigem() throws Exception {
        for (int tentativa = 0; tentativa < 5; tentativa++) {
            mockMvc.perform(post("/api/v1/administracao/autenticacao/entrar")
                            .with(csrf())
                            .with(request -> {
                                request.setRemoteAddr("198.51.100.10");
                                return request;
                            })
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(credenciais(EMAIL_PROPRIETARIO, "senha-incorreta")))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/api/v1/administracao/autenticacao/entrar")
                        .with(csrf())
                        .with(request -> {
                            request.setRemoteAddr("198.51.100.10");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciais(EMAIL_PROPRIETARIO, "senha-incorreta")))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.codigo").value("LIMITE_TENTATIVAS_LOGIN"));
    }

    @Test
    void criaFuncionarioComVinculoUnicoESenhaTemporaria() throws Exception {
        UUID profissionalId = inserirProfissional("Profissional Um");
        MockHttpSession sessao = autenticar(EMAIL_PROPRIETARIO, SENHA_PROPRIETARIO);

        mockMvc.perform(post("/api/v1/administracao/usuarios")
                        .session(sessao)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Funcionario", "email":"FUNCIONARIO@EXAMPLE.TEST ",
                                 "senhaTemporaria":"senha-temporaria", "profissionalId":"%s"}
                                """.formatted(profissionalId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("funcionario@example.test"))
                .andExpect(jsonPath("$.papel").value("FUNCIONARIO"))
                .andExpect(jsonPath("$.deveAlterarSenha").value(true));

        mockMvc.perform(post("/api/v1/administracao/usuarios")
                        .session(sessao)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Outro", "email":"outro@example.test",
                                 "senhaTemporaria":"senha-temporaria", "profissionalId":"%s"}
                                """.formatted(profissionalId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("PROFISSIONAL_JA_VINCULADO"));
    }

    @Test
    void preservaPutERejeitaPatchNaAlteracaoDeUsuario() throws Exception {
        UUID profissionalId = inserirProfissional("Profissional Alteracao");
        UUID funcionarioId = servicoUsuarios.criarFuncionario(
                "Funcionario", "alteracao@example.test", "senha-temporaria", profissionalId).id();
        MockHttpSession sessao = autenticar(EMAIL_PROPRIETARIO, SENHA_PROPRIETARIO);
        String alteracao = """
                {"nome":"Funcionario Atualizado", "email":"atualizado@example.test",
                 "papel":"FUNCIONARIO", "profissionalId":"%s"}
                """.formatted(profissionalId);

        mockMvc.perform(put("/api/v1/administracao/usuarios/{id}", funcionarioId)
                        .session(sessao)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alteracao))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Funcionario Atualizado"))
                .andExpect(jsonPath("$.email").value("atualizado@example.test"));

        mockMvc.perform(patch("/api/v1/administracao/usuarios/{id}", funcionarioId)
                        .session(sessao)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(alteracao))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.codigo").value("METODO_NAO_PERMITIDO"));
    }

    @Test
    void preservaUltimoProprietarioAtivo() throws Exception {
        UUID proprietarioId = jdbcClient.sql("select id from usuario_administrativo where papel = 'PROPRIETARIO'")
                .query(UUID.class).single();
        MockHttpSession sessao = autenticar(EMAIL_PROPRIETARIO, SENHA_PROPRIETARIO);

        mockMvc.perform(post("/api/v1/administracao/usuarios/{id}/desativar", proprietarioId)
                        .session(sessao)
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ULTIMO_PROPRIETARIO_ATIVO"));
    }

    @Test
    void preservaProprietarioAtivoEmDesativacoesConcorrentes() throws Exception {
        UUID primeiroId = jdbcClient.sql("select id from usuario_administrativo where papel = 'PROPRIETARIO'")
                .query(UUID.class).single();
        UUID segundoId = UUID.randomUUID();
        jdbcClient.sql("""
                        insert into usuario_administrativo
                        (id, nome, email, hash_senha, papel, ativo, deve_alterar_senha)
                        values (:id, 'Segundo Proprietario', 'segundo@example.test', :hash,
                                'PROPRIETARIO', true, false)
                        """)
                .param("id", segundoId)
                .param("hash", passwordEncoder.encode("senha-segundo-proprietario"))
                .update();

        CountDownLatch inicio = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<Boolean> primeira = executor.submit(() -> desativarAposSinal(inicio, primeiroId));
            Future<Boolean> segunda = executor.submit(() -> desativarAposSinal(inicio, segundoId));
            inicio.countDown();

            assertThat(java.util.List.of(primeira.get(), segunda.get()))
                    .containsExactlyInAnyOrder(true, false);
            Integer ativos = jdbcClient.sql("""
                            select count(*) from usuario_administrativo
                             where papel = 'PROPRIETARIO' and ativo = true
                            """)
                    .query(Integer.class).single();
            assertThat(ativos).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void senhaTemporariaRestringeAteSerAlterada() throws Exception {
        UUID profissionalId = inserirProfissional("Profissional Dois");
        String hash = passwordEncoder.encode("senha-temporaria");
        jdbcClient.sql("""
                        insert into usuario_administrativo
                        (id, nome, email, hash_senha, papel, profissional_id, ativo, deve_alterar_senha)
                        values (:id, 'Funcionario', 'temporario@example.test', :hash, 'FUNCIONARIO', :profissional, true, true)
                        """)
                .param("id", UUID.randomUUID())
                .param("hash", hash)
                .param("profissional", profissionalId)
                .update();

        MockHttpSession sessao = autenticar("temporario@example.test", "senha-temporaria");
        mockMvc.perform(get("/api/v1/administracao/recurso-inexistente").session(sessao))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/administracao/autenticacao/senha")
                        .session(sessao)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"senhaAtual":"senha-temporaria", "novaSenha":"senha-nova-segura"}
                                """))
                .andExpect(status().isNoContent());

        autenticar("temporario@example.test", "senha-nova-segura");
    }

    @Test
    void funcionarioNaoAcessaGestaoDeUsuarios() throws Exception {
        UUID profissionalId = inserirProfissional("Profissional Tres");
        jdbcClient.sql("""
                        insert into usuario_administrativo
                        (id, nome, email, hash_senha, papel, profissional_id, ativo, deve_alterar_senha)
                        values (:id, 'Funcionario', 'ativo@example.test', :hash, 'FUNCIONARIO', :profissional, true, false)
                        """)
                .param("id", UUID.randomUUID())
                .param("hash", passwordEncoder.encode("senha-funcionario"))
                .param("profissional", profissionalId)
                .update();

        MockHttpSession sessao = autenticar("ativo@example.test", "senha-funcionario");
        mockMvc.perform(get("/api/v1/administracao/usuarios").session(sessao))
                .andExpect(status().isForbidden());
    }

    @Test
    void desativacaoInvalidaSessaoDoFuncionario() throws Exception {
        UUID profissionalId = inserirProfissional("Profissional Quatro");
        UUID funcionarioId = UUID.randomUUID();
        jdbcClient.sql("""
                        insert into usuario_administrativo
                        (id, nome, email, hash_senha, papel, profissional_id, ativo, deve_alterar_senha)
                        values (:id, 'Funcionario', 'desativado@example.test', :hash, 'FUNCIONARIO', :profissional, true, false)
                        """)
                .param("id", funcionarioId)
                .param("hash", passwordEncoder.encode("senha-funcionario"))
                .param("profissional", profissionalId)
                .update();
        MockHttpSession sessaoFuncionario = autenticar("desativado@example.test", "senha-funcionario");
        MockHttpSession sessaoProprietario = autenticar(EMAIL_PROPRIETARIO, SENHA_PROPRIETARIO);

        mockMvc.perform(post("/api/v1/administracao/usuarios/{id}/desativar", funcionarioId)
                        .session(sessaoProprietario)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/administracao/autenticacao/eu").session(sessaoFuncionario))
                .andExpect(status().isUnauthorized());
    }

    private MockHttpSession autenticar(String email, String senha) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/administracao/autenticacao/entrar")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(credenciais(email, senha)))
                .andExpect(status().isNoContent())
                .andReturn();
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }

    private boolean desativarAposSinal(CountDownLatch inicio, UUID id) throws InterruptedException {
        inicio.await();
        try {
            servicoUsuarios.desativar(id);
            return true;
        } catch (ExcecaoRegraNegocio excecao) {
            assertThat(excecao.getCodigo()).isEqualTo("ULTIMO_PROPRIETARIO_ATIVO");
            return false;
        }
    }

    private UUID inserirProfissional(String nome) {
        UUID id = UUID.randomUUID();
        jdbcClient.sql("insert into profissional (id, nome, ativo) values (:id, :nome, true)")
                .param("id", id)
                .param("nome", nome)
                .update();
        return id;
    }

    private String credenciais(String email, String senha) {
        return "{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}";
    }
}
