package com.servicescheduling.api.compartilhado.infraestrutura.documentacao;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
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

        Components componentes = new Components()
                .addSchemas("ErroCampo", erroCampo)
                .addSchemas("Problema", problema)
                .responses(new ApiResponses().addApiResponse("FalhaValidacao", falhaValidacao));

        return new OpenAPI()
                .info(new Info()
                        .title("Service Scheduling API")
                        .description("API generica para agendamento de servicos")
                        .version("v1"))
                .components(componentes);
    }
}
