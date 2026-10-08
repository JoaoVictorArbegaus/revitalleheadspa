package com.servicescheduling.api.compartilhado.infraestrutura.documentacao;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracaoOpenApi {

    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    OpenAPI contratoOpenApi() {
        Schema<?> erroCampo = new ObjectSchema()
                .addProperty("campo", new StringSchema().example("cliente.celular"))
                .addProperty("codigo", new StringSchema().example("NAO_PODE_ESTAR_EM_BRANCO"))
                .addProperty("mensagem", new StringSchema().example("nao deve estar em branco"));

        Schema<?> problema = new ObjectSchema()
                .addProperty("type", new StringSchema().format("uri").example("/problemas/falha-validacao"))
                .addProperty("title", new StringSchema().example("Falha de validacao"))
                .addProperty("status", new IntegerSchema().example(422))
                .addProperty("detail", new StringSchema().example("A solicitacao possui campos invalidos"))
                .addProperty("instance", new StringSchema().format("uri").example("urn:rastreio:550e8400-e29b-41d4-a716-446655440000"))
                .addProperty("codigo", new StringSchema().example("FALHA_VALIDACAO"))
                .addProperty("idRastreio", new StringSchema().example("550e8400-e29b-41d4-a716-446655440000"))
                .addProperty("errosCampos", new ArraySchema().items(new Schema<>().$ref("#/components/schemas/ErroCampo")))
                .required(java.util.List.of("type", "title", "status", "detail", "instance", "codigo", "idRastreio"));

        io.swagger.v3.oas.models.media.MediaType tipoProblema = new io.swagger.v3.oas.models.media.MediaType()
                .schema(new Schema<>().$ref("#/components/schemas/Problema"));
        ApiResponse falhaValidacao = new ApiResponse()
                .description("A solicitacao viola validacoes de entrada")
                .content(new io.swagger.v3.oas.models.media.Content()
                        .addMediaType("application/problem+json", tipoProblema));
        ApiResponse problemaGenerico = new ApiResponse()
                .description("Falha no processamento da operacao")
                .content(new io.swagger.v3.oas.models.media.Content()
                        .addMediaType("application/problem+json", tipoProblema));

        Components componentes = new Components()
                .addSchemas("ErroCampo", erroCampo)
                .addSchemas("Problema", problema)
                .addSecuritySchemes("sessao", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.COOKIE)
                        .name("JSESSIONID")
                        .description("Sessao HTTP administrativa"))
                .addSecuritySchemes("csrf", new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .in(SecurityScheme.In.HEADER)
                        .name("X-CSRF-TOKEN")
                        .description("Token obtido em /api/v1/administracao/autenticacao/csrf"))
                .responses(new ApiResponses()
                        .addApiResponse("FalhaValidacao", falhaValidacao)
                        .addApiResponse("Problema", problemaGenerico));

        return new OpenAPI()
                .info(new Info()
                        .title("Service Scheduling API")
                        .description("API generica para agendamento de servicos")
                        .version("v1"))
                .components(componentes);
    }

    @Bean
    OpenApiCustomizer documentarSegurancaAcesso() {
        return openApi -> {
            configurar(openApi, "/api/v1/administracao/autenticacao/entrar", "post", false, true,
                    "204", "400", "401", "403", "415", "422", "429", "500");
            configurar(openApi, "/api/v1/administracao/autenticacao/eu", "get", true, false,
                    "200", "401", "500");
            configurar(openApi, "/api/v1/administracao/autenticacao/senha", "put", true, true,
                    "204", "400", "401", "403", "415", "422", "500");
            configurar(openApi, "/api/v1/administracao/autenticacao/sair", "post", true, true,
                    "204", "401", "403", "500");
            configurar(openApi, "/api/v1/administracao/usuarios", "get", true, false,
                    "200", "401", "403", "500");
            configurar(openApi, "/api/v1/administracao/usuarios", "post", true, true,
                    "201", "400", "401", "403", "409", "415", "422", "500");
            configurar(openApi, "/api/v1/administracao/usuarios/{id}", "get", true, false,
                    "200", "400", "401", "403", "404", "500");
            configurar(openApi, "/api/v1/administracao/usuarios/{id}", "put", true, true,
                    "200", "400", "401", "403", "404", "409", "415", "422", "500");
            configurar(openApi, "/api/v1/administracao/usuarios/{id}/senha-temporaria", "post", true, true,
                    "204", "400", "401", "403", "404", "409", "415", "422", "500");
            configurar(openApi, "/api/v1/administracao/usuarios/{id}/ativar", "post", true, true,
                    "204", "400", "401", "403", "404", "409", "422", "500");
            configurar(openApi, "/api/v1/administracao/usuarios/{id}/desativar", "post", true, true,
                    "204", "400", "401", "403", "404", "409", "422", "500");
            configurar(openApi, "/api/v1/administracao/profissionais", "get", true, false,
                    "200", "401", "403", "500");
            configurar(openApi, "/api/v1/administracao/profissionais", "post", true, true,
                    "201", "400", "401", "403", "415", "422", "500");
            configurar(openApi, "/api/v1/administracao/profissionais/{id}", "get", true, false,
                    "200", "400", "401", "403", "404", "500");
            configurar(openApi, "/api/v1/administracao/profissionais/{id}", "put", true, true,
                    "200", "400", "401", "403", "404", "415", "422", "500");
            configurar(openApi, "/api/v1/administracao/recursos", "get", true, false,
                    "200", "401", "403", "500");
            configurar(openApi, "/api/v1/administracao/recursos", "post", true, true,
                    "201", "400", "401", "403", "415", "422", "500");
            configurar(openApi, "/api/v1/administracao/recursos/{id}", "get", true, false,
                    "200", "400", "401", "403", "404", "500");
            configurar(openApi, "/api/v1/administracao/recursos/{id}", "put", true, true,
                    "200", "400", "401", "403", "404", "409", "415", "422", "500");
            configurar(openApi, "/api/v1/administracao/servicos", "get", true, false,
                    "200", "401", "403", "500");
            configurar(openApi, "/api/v1/administracao/servicos", "post", true, true,
                    "201", "400", "401", "403", "415", "422", "500");
            configurar(openApi, "/api/v1/administracao/servicos/{id}", "get", true, false,
                    "200", "400", "401", "403", "404", "500");
            configurar(openApi, "/api/v1/administracao/servicos/{id}", "put", true, true,
                    "200", "400", "401", "403", "404", "415", "422", "500");
            configurar(openApi, "/api/v1/publico/servicos", "get", false, false,
                    "200", "500");
        };
    }

    private void configurar(
            OpenAPI openApi,
            String caminho,
            String metodo,
            boolean sessao,
            boolean csrf,
            String... respostas) {
        if (openApi.getPaths() == null || openApi.getPaths().get(caminho) == null) {
            return;
        }
        Operation operacao = openApi.getPaths().get(caminho)
                .readOperationsMap().get(io.swagger.v3.oas.models.PathItem.HttpMethod.valueOf(metodo.toUpperCase()));
        if (operacao == null) {
            return;
        }
        SecurityRequirement requisito = new SecurityRequirement();
        if (sessao) {
            requisito.addList("sessao");
        }
        if (csrf) {
            requisito.addList("csrf");
        }
        operacao.setSecurity(java.util.List.of(requisito));
        ApiResponse sucessoGerado = operacao.getResponses().entrySet().stream()
                .filter(entrada -> entrada.getKey().startsWith("2"))
                .map(java.util.Map.Entry::getValue)
                .findFirst()
                .orElse(null);
        if (sucessoGerado != null
                && sucessoGerado.getContent() != null
                && sucessoGerado.getContent().containsKey("*/*")) {
            io.swagger.v3.oas.models.media.MediaType tipo = sucessoGerado.getContent().remove("*/*");
            sucessoGerado.getContent().addMediaType("application/json", tipo);
        }
        operacao.setResponses(new ApiResponses());
        for (String resposta : respostas) {
            ApiResponse documentada = resposta.startsWith("2") && sucessoGerado != null
                    ? sucessoGerado.description("Operacao concluida")
                    : new ApiResponse().$ref("#/components/responses/Problema");
            if ("204".equals(resposta)) {
                documentada = new ApiResponse().description("Operacao concluida");
            }
            operacao.getResponses().addApiResponse(resposta, documentada);
        }
    }
}
