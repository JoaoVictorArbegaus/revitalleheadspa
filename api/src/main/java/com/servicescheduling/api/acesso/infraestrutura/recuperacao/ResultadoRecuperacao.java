package com.servicescheduling.api.acesso.infraestrutura.recuperacao;

import org.springframework.stereotype.Component;

@Component
public class ResultadoRecuperacao {

    private int codigoSaida = 1;

    public int getCodigoSaida() {
        return codigoSaida;
    }

    void concluir(int codigoSaida) {
        this.codigoSaida = codigoSaida;
    }
}
