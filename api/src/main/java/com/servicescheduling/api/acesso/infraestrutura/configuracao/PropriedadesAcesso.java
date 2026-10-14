package com.servicescheduling.api.acesso.infraestrutura.configuracao;

import java.time.Duration;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("aplicacao.acesso")
@Validated
public record PropriedadesAcesso(
        Bootstrap bootstrap, Recuperacao recuperacao, @Valid LimiteTentativas limiteTentativas) {

    public record Bootstrap(String nome, String email, String senha) {
    }

    public record Recuperacao(String email, String novaSenha) {
    }

    public record LimiteTentativas(@Min(1) int maximo, @NotNull Duration janela) {

        public LimiteTentativas {
            if (janela != null && (janela.isZero() || janela.isNegative())) {
                throw new IllegalArgumentException("A janela do limite de tentativas deve ser positiva");
            }
        }
    }
}
