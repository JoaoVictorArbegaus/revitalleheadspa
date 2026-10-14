package com.servicescheduling.api.clientes.dominio;

public record NumeroCelular(String valor) {

    public static NumeroCelular de(String informado) {
        if (informado == null || !informado.matches("[+0-9() -]+")) {
            throw new IllegalArgumentException("Formato de celular invalido");
        }
        String digitos = informado.replaceAll("[^0-9]", "");
        if ((digitos.length() == 12 || digitos.length() == 13) && digitos.startsWith("55")) {
            digitos = digitos.substring(2);
        }
        if (digitos.length() != 10 && digitos.length() != 11) {
            throw new IllegalArgumentException("Formato de celular invalido");
        }
        return new NumeroCelular("+55" + digitos);
    }
}
