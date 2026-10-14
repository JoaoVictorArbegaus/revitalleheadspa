package com.servicescheduling.api.acesso.aplicacao;

import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio;
import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio.Tipo;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class PoliticaSenha {

    public void validar(String senha) {
        int bytes = senha == null ? 0 : senha.getBytes(StandardCharsets.UTF_8).length;
        if (bytes < 12 || bytes > 72) {
            throw new ExcecaoRegraNegocio(
                    Tipo.ENTRADA_INVALIDA,
                    "SENHA_FORA_DA_POLITICA",
                    "A senha deve possuir entre 12 e 72 bytes em UTF-8");
        }
    }
}
