package com.servicescheduling.api.compartilhado.api.erro;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ErroCampo", description = "Violacao de validacao associada a um campo")
public record ErroCampo(
        @Schema(example = "cliente.celular") String campo,
        @Schema(example = "NAO_PODE_ESTAR_EM_BRANCO") String codigo,
        @Schema(example = "nao deve estar em branco") String mensagem) {
}
