package com.servicescheduling.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
@Import({
        FundacaoIntegracaoTest.ControladorValidacaoTeste.class,
        FundacaoIntegracaoTest.ConfiguracaoSegurancaTeste.class
})
class FundacaoIntegracaoTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer POSTGRESQL = new PostgreSQLContainer(
            DockerImageName.parse("postgres:17-alpine"));

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JdbcClient jdbcClient;

    @Value("${management.endpoint.health.group.readiness.include}")
    String indicadoresReadiness;

    @Test
    void aplicaMigrationInicialEmBancoVazio() {
        Integer quantidade = jdbcClient.sql("select count(*) from flyway_schema_history where version = '1'")
                .query(Integer.class)
                .single();

        assertThat(quantidade).isEqualTo(1);
    }

    @Test
    void expoeLivenessEReadiness() throws Exception {
        assertThat(indicadoresReadiness).contains("db");

        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));

        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void devolveErroDeValidacaoRfc9457SemValorRejeitado() throws Exception {
        String idRastreio = "550e8400-e29b-41d4-a716-446655440000";
        mockMvc.perform(post("/api/v1/publico/teste-validacao")
                        .header("X-Id-Rastreio", idRastreio)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\"}"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string("X-Id-Rastreio", idRastreio))
                .andExpect(jsonPath("$.type").value("/problemas/falha-validacao"))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.codigo").value("FALHA_VALIDACAO"))
                .andExpect(jsonPath("$.idRastreio").value(idRastreio))
                .andExpect(jsonPath("$.instance").value("urn:rastreio:" + idRastreio))
                .andExpect(jsonPath("$.errosCampos[0].campo").value("nome"))
                .andExpect(jsonPath("$.errosCampos[0].codigo").value("NAO_PODE_ESTAR_EM_BRANCO"))
                .andExpect(jsonPath("$.errosCampos[0].valor").doesNotExist());
    }

    @Test
    void criaNovoRastreioQuandoCabecalhoForInvalido() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness")
                        .header("X-Id-Rastreio", "12345678901"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Id-Rastreio"))
                .andExpect(header().string("X-Id-Rastreio", org.hamcrest.Matchers.matchesPattern(
                        "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")));
    }

    @Test
    void devolveErroDeValidacaoParaParametro() throws Exception {
        mockMvc.perform(get("/api/v1/publico/teste-parametro").param("quantidade", "0"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.codigo").value("FALHA_VALIDACAO"))
                .andExpect(jsonPath("$.errosCampos[0].campo").value("quantidade"))
                .andExpect(jsonPath("$.errosCampos[0].codigo").value("VALOR_ABAIXO_DO_MINIMO"));
    }

    @Test
    void documentaProblemaERespostaDeValidacaoNoOpenApi() throws Exception {
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.components.schemas.Problema.properties.codigo").exists())
                .andExpect(jsonPath("$.components.schemas.ErroCampo.properties.campo").exists())
                .andExpect(jsonPath("$.components.responses.FalhaValidacao.content['application/problem+json']").exists());
    }

    @Test
    void exigeAutenticacaoParaRotasAdministrativasENaoReconhecidas() throws Exception {
        mockMvc.perform(get("/api/v1/administracao/recurso-inexistente"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.codigo").value("AUTENTICACAO_NECESSARIA"));

        mockMvc.perform(get("/rota-nao-reconhecida"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.codigo").value("AUTENTICACAO_NECESSARIA"));
    }

    @Test
    @WithMockUser
    void identificaFalhaCsrf() throws Exception {
        mockMvc.perform(post("/api/v1/administracao/recurso-inexistente"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.codigo").value("FALHA_VALIDACAO_CSRF"));
    }

    @RestController
    static class ControladorValidacaoTeste {

        @PostMapping("/api/v1/publico/teste-validacao")
        void validar(@Valid @RequestBody RequisicaoTeste requisicao) {
        }

        @GetMapping("/api/v1/publico/teste-parametro")
        void validarParametro(@RequestParam @Min(1) int quantidade) {
        }
    }

    record RequisicaoTeste(@NotBlank String nome) {
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class ConfiguracaoSegurancaTeste {

        @Bean
        @Order(0)
        SecurityFilterChain filtroRotasTeste(HttpSecurity http) throws Exception {
            http.securityMatcher(
                            "/api/v1/publico/teste-validacao",
                            "/api/v1/publico/teste-parametro")
                    .authorizeHttpRequests(autorizacao -> autorizacao.anyRequest().permitAll())
                    .csrf(csrf -> csrf.disable());
            return http.build();
        }
    }
}
