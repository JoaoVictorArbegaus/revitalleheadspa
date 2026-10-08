package com.servicescheduling.api.compartilhado.infraestrutura.seguranca;

import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio;
import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio.Tipo;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LimiteRequisicoesPublicas {

    private final Map<String, Consumo> consumoPorOrigem = new HashMap<>();
    private final PropriedadesSegurancaPublica propriedades;
    private final Clock relogio;

    public LimiteRequisicoesPublicas(PropriedadesSegurancaPublica propriedades, Clock relogio) {
        this.propriedades = propriedades;
        this.relogio = relogio;
    }

    public synchronized void consumir(String origem) {
        Instant agora = relogio.instant();
        Duration janela = propriedades.getJanela();
        consumoPorOrigem.entrySet().removeIf(
                entrada -> !agora.isBefore(entrada.getValue().inicio().plus(janela)));
        Consumo atual = consumoPorOrigem.get(origem);
        if (atual == null) {
            consumoPorOrigem.put(origem, new Consumo(agora, 1));
            return;
        }
        if (atual.quantidade() >= propriedades.getRequisicoesMaximo()) {
            long espera = Math.max(1, Duration.between(agora, atual.inicio().plus(janela)).toSeconds());
            throw new ExcecaoRegraNegocio(
                    Tipo.LIMITE_EXCEDIDO,
                    "LIMITE_REQUISICOES_PUBLICAS",
                    "O limite de requisicoes publicas foi atingido",
                    espera);
        }
        consumoPorOrigem.put(origem, new Consumo(atual.inicio(), atual.quantidade() + 1));
    }

    private record Consumo(Instant inicio, int quantidade) {
    }
}
