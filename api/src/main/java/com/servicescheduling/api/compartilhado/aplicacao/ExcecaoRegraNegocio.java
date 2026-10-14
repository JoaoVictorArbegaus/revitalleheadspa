package com.servicescheduling.api.compartilhado.aplicacao;

public class ExcecaoRegraNegocio extends RuntimeException {

    private final Tipo tipo;
    private final String codigo;
    private final Long retryAfterSegundos;

    public ExcecaoRegraNegocio(Tipo tipo, String codigo, String mensagem) {
        this(tipo, codigo, mensagem, null);
    }

    public ExcecaoRegraNegocio(Tipo tipo, String codigo, String mensagem, Long retryAfterSegundos) {
        super(mensagem);
        this.tipo = tipo;
        this.codigo = codigo;
        this.retryAfterSegundos = retryAfterSegundos;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public String getCodigo() {
        return codigo;
    }

    public Long getRetryAfterSegundos() {
        return retryAfterSegundos;
    }

    public enum Tipo {
        ENTRADA_INVALIDA,
        CONFLITO,
        NAO_ENCONTRADO,
        LIMITE_EXCEDIDO
    }
}
