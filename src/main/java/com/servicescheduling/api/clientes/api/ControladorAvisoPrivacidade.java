package com.servicescheduling.api.clientes.api;

import com.servicescheduling.api.clientes.aplicacao.DadosAvisoPrivacidade;
import com.servicescheduling.api.compartilhado.infraestrutura.configuracao.PropriedadesNegocio;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/publico/aviso-privacidade")
public class ControladorAvisoPrivacidade {

    private final PropriedadesNegocio propriedades;

    public ControladorAvisoPrivacidade(PropriedadesNegocio propriedades) {
        this.propriedades = propriedades;
    }

    @GetMapping
    public DadosAvisoPrivacidade consultar() {
        return new DadosAvisoPrivacidade(
                propriedades.getVersaoAvisoPrivacidade(), propriedades.getTextoAvisoPrivacidade());
    }
}
