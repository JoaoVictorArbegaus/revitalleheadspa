package com.servicescheduling.api.agenda.api;

import com.servicescheduling.api.agenda.aplicacao.CalculadoraDisponibilidade;
import com.servicescheduling.api.agenda.aplicacao.DadosDisponibilidade;
import java.time.LocalDate;
import java.util.UUID;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/publico/servicos/{servicoId}/disponibilidade")
public class ControladorDisponibilidade {

    private final CalculadoraDisponibilidade calculadora;

    public ControladorDisponibilidade(CalculadoraDisponibilidade calculadora) {
        this.calculadora = calculadora;
    }

    @GetMapping
    public DadosDisponibilidade consultar(
            @PathVariable UUID servicoId,
            @Parameter(required = true) @RequestParam(required = false) LocalDate dataInicial,
            @Parameter(required = true) @RequestParam(required = false) LocalDate dataFinal) {
        return calculadora.calcular(servicoId, dataInicial, dataFinal);
    }
}
