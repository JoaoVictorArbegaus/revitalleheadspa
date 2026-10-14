package com.servicescheduling.api.acesso.aplicacao;

import com.servicescheduling.api.acesso.dominio.PapelUsuarioAdministrativo;
import java.util.UUID;

public record DadosUsuarioAdministrativo(
        UUID id,
        String nome,
        String email,
        PapelUsuarioAdministrativo papel,
        UUID profissionalId,
        boolean ativo,
        boolean deveAlterarSenha) {
}
