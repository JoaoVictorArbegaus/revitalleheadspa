package com.servicescheduling.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
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
        "aplicacao.acesso.bootstrap.nome=Proprietario Contrato",
        "aplicacao.acesso.bootstrap.email=proprietario-contrato@example.test",
        "aplicacao.acesso.bootstrap.senha=senha-contrato-segura",
        "aplicacao.seguranca-publica.requisicoes-maximo=1000"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class ContratoDocumentacaoTest {

    private static final Path RAIZ = Path.of("").toAbsolutePath();
    private static final Pattern LITERAL_CODIGO = Pattern.compile("\"([A-Z][A-Z0-9_]{3,}[A-Z0-9])\"");
    private static final Pattern CODIGO_CATALOGO = Pattern.compile("^\\| `([A-Z][A-Z0-9_]+)` \\|", Pattern.MULTILINE);
    private static final Pattern REQUISICAO_EXEMPLO = Pattern.compile(
            "^(GET|POST|PUT|DELETE) \\{\\{baseUrl}}([^?\\s]+)", Pattern.MULTILINE);
    private static final Pattern LINK_MARKDOWN = Pattern.compile("\\]\\(([^)#\\s]+)(#[^)]*)?\\)");
    private static final Set<String> LITERAIS_SEM_ERRO = Set.of(
            "AUTENTICADO",
            "CONFIRMADO",
            "FUNCIONARIO",
            "JSESSIONID",
            "MODO_APLICACAO",
            "NAO_MAPEADA",
            "PROPRIETARIO",
            "PUBLICO",
            "RECUPERACAO_PROPRIETARIO_EMAIL",
            "RECUPERACAO_PROPRIETARIO_NOVA_SENHA");

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

    @Test
    void catalogoDeErrosCoincideComOsCodigosEmitidos() throws IOException {
        Set<String> emitidos = new TreeSet<>();
        try (Stream<Path> arquivos = Files.walk(RAIZ.resolve("src/main/java"))) {
            for (Path arquivo : arquivos.filter(caminho -> caminho.toString().endsWith(".java")).toList()) {
                Matcher matcher = LITERAL_CODIGO.matcher(Files.readString(arquivo, StandardCharsets.UTF_8));
                while (matcher.find()) {
                    emitidos.add(matcher.group(1));
                }
            }
        }
        emitidos.removeAll(LITERAIS_SEM_ERRO);

        Set<String> catalogados = new TreeSet<>();
        Matcher matcher = CODIGO_CATALOGO.matcher(ler("docs/catalogo-erros.md"));
        while (matcher.find()) {
            catalogados.add(matcher.group(1));
        }

        assertThat(catalogados).as("codigos catalogados").isEqualTo(emitidos);
    }

    @Test
    void exemplosUsamSomenteOperacoesDoOpenApi() throws Exception {
        Map<String, Map<String, Object>> caminhos = JsonPath.read(openApi(), "$.paths");
        Matcher matcher = REQUISICAO_EXEMPLO.matcher(ler("docs/catalogo-api.http"));
        List<String> ausentes = new ArrayList<>();
        int requisicoes = 0;
        while (matcher.find()) {
            requisicoes++;
            String metodo = matcher.group(1).toLowerCase();
            String caminho = matcher.group(2).replaceAll("\\{\\{[^}]+}}", "{}");
            boolean existe = caminhos.entrySet().stream().anyMatch(entrada ->
                    entrada.getKey().replaceAll("\\{[^}]+}", "{}").equals(caminho)
                            && entrada.getValue().containsKey(metodo));
            if (!existe) {
                ausentes.add(metodo + " " + caminho);
            }
        }
        assertThat(requisicoes).isPositive();
        assertThat(ausentes).as("requisicoes de exemplo sem operacao no OpenAPI").isEmpty();
    }

    @Test
    void papeisDoOpenApiCorrespondemAAutorizacao() throws Exception {
        UUID profissionalId = UUID.randomUUID();
        jdbc.sql("insert into profissional (id, nome, ativo) values (:id, 'Profissional Contrato', true)")
                .param("id", profissionalId).update();
        jdbc.sql("""
                        insert into usuario_administrativo
                        (id, nome, email, hash_senha, papel, profissional_id, ativo, deve_alterar_senha)
                        values (:id, 'Funcionario Contrato', 'funcionario-contrato@example.test', :hash,
                                'FUNCIONARIO', :profissional, true, false)
                        """)
                .param("id", UUID.randomUUID())
                .param("hash", passwordEncoder.encode("senha-funcionario-contrato"))
                .param("profissional", profissionalId)
                .update();
        MockHttpSession funcionario = autenticar("funcionario-contrato@example.test", "senha-funcionario-contrato");

        Map<String, Map<String, Map<String, Object>>> caminhos = JsonPath.read(openApi(), "$.paths");
        List<String> divergencias = new ArrayList<>();
        for (var caminho : caminhos.entrySet()) {
            for (var operacao : caminho.getValue().entrySet()) {
                HttpMethod metodo = HttpMethod.valueOf(operacao.getKey().toUpperCase());
                String rota = caminho.getKey().replaceAll("\\{[^}]+}", UUID.randomUUID().toString());
                @SuppressWarnings("unchecked")
                List<String> papeis = (List<String>) operacao.getValue().get("x-papeis");
                String descricao = operacao.getKey().toUpperCase() + " " + caminho.getKey();
                if (papeis == null || papeis.isEmpty()) {
                    divergencias.add(descricao + " sem x-papeis");
                    continue;
                }

                int anonimo = executar(metodo, rota, null);
                boolean publico = papeis.contains("PUBLICO");
                if (publico == (anonimo == 401 || anonimo == 403)) {
                    divergencias.add(descricao + " anonimo recebeu " + anonimo);
                }
                if (publico || rota.endsWith("/autenticacao/sair")) {
                    continue;
                }
                int comFuncionario = executar(metodo, rota, funcionario);
                boolean permitido = papeis.contains("FUNCIONARIO") || papeis.contains("AUTENTICADO");
                if (permitido == (comFuncionario == 401 || comFuncionario == 403)) {
                    divergencias.add(descricao + " funcionario recebeu " + comFuncionario);
                }
            }
        }
        assertThat(divergencias).as("papeis divergentes da autorizacao").isEmpty();
    }

    @Test
    void linksInternosDaDocumentacaoResolvem() throws IOException {
        List<Path> documentos = new ArrayList<>(List.of(RAIZ.resolve("README.md")));
        try (Stream<Path> arquivos = Stream.concat(
                Files.list(RAIZ.resolve("docs")), Files.list(RAIZ.resolve("sdd_kit")))) {
            documentos.addAll(arquivos.filter(caminho -> caminho.toString().endsWith(".md")).toList());
        }
        List<String> quebrados = new ArrayList<>();
        for (Path documento : documentos) {
            Matcher matcher = LINK_MARKDOWN.matcher(Files.readString(documento, StandardCharsets.UTF_8));
            while (matcher.find()) {
                String destino = matcher.group(1);
                if (destino.contains("://") || destino.startsWith("mailto:")) {
                    continue;
                }
                if (!Files.exists(documento.getParent().resolve(destino).normalize())) {
                    quebrados.add(RAIZ.relativize(documento) + " -> " + destino);
                }
            }
        }
        assertThat(quebrados).as("links internos quebrados").isEmpty();
    }

    private int executar(HttpMethod metodo, String rota, MockHttpSession sessao) throws Exception {
        var requisicao = request(metodo, rota).with(csrf());
        if (sessao != null) {
            requisicao.session(sessao);
        }
        if (metodo == HttpMethod.POST || metodo == HttpMethod.PUT) {
            requisicao.contentType(MediaType.APPLICATION_JSON).content("{}");
        }
        return mockMvc.perform(requisicao).andReturn().getResponse().getStatus();
    }

    private String openApi() throws Exception {
        return mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private MockHttpSession autenticar(String email, String senha) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/v1/administracao/autenticacao/entrar")
                        .with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"%s\",\"senha\":\"%s\"}".formatted(email, senha)))
                .andExpect(status().isNoContent())
                .andReturn();
        return (MockHttpSession) resultado.getRequest().getSession(false);
    }

    private String ler(String caminho) throws IOException {
        return Files.readString(RAIZ.resolve(caminho), StandardCharsets.UTF_8);
    }
}
