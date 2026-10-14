package com.servicescheduling.api.servicos.api;

import com.servicescheduling.api.servicos.aplicacao.DadosServicoPublico;
import com.servicescheduling.api.servicos.aplicacao.ServicoCatalogo;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/publico/servicos")
public class ControladorCatalogoPublico {

    private final ServicoCatalogo servico;

    public ControladorCatalogoPublico(ServicoCatalogo servico) {
        this.servico = servico;
    }

    @GetMapping
    List<DadosServicoPublico> listar() {
        return servico.listarPublicos();
    }
}
