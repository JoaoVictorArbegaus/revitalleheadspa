package com.servicescheduling.api.acesso.infraestrutura.seguranca;

import com.servicescheduling.api.acesso.infraestrutura.configuracao.PropriedadesAcesso;
import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio;
import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio.Tipo;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LimiteTentativasLogin {

    private final Map<String, Tentativas> tentativasPorOrigem = new HashMap<>();
    private final PropriedadesAcesso.LimiteTentativas propriedades;
    private final Clock relogio = Clock.systemUTC();

    public LimiteTentativasLogin(PropriedadesAcesso propriedades) {
        this.propriedades = propriedades.limiteTentativas();
    }

    public synchronized void verificar(String origem) {
        Instant agora = relogio.instant();
        tentativasPorOrigem.entrySet().removeIf(
                entrada -> !agora.isBefore(entrada.getValue().inicio().plus(propriedades.janela())));
        Tentativas tentativas = tentativasPorOrigem.get(origem);
        if (tentativas == null) {
            return;
        }
        if (tentativas.quantidade() >= propriedades.maximo()) {
            long espera = Math.max(1, java.time.Duration.between(
                    agora, tentativas.inicio().plus(propriedades.janela())).toSeconds());
            throw new ExcecaoRegraNegocio(
                    Tipo.LIMITE_EXCEDIDO,
                    "LIMITE_TENTATIVAS_LOGIN",
                    "O limite de tentativas de autenticacao foi atingido",
                    espera);
        }
    }

    public synchronized void registrarFalha(String origem) {
        Instant agora = relogio.instant();
        tentativasPorOrigem.compute(origem, (chave, atual) -> {
            if (atual == null || !agora.isBefore(atual.inicio().plus(propriedades.janela()))) {
                return new Tentativas(agora, 1);
            }
            return new Tentativas(atual.inicio(), atual.quantidade() + 1);
        });
    }

    public synchronized void limpar(String origem) {
        tentativasPorOrigem.remove(origem);
    }

    private record Tentativas(Instant inicio, int quantidade) {
    }
}
