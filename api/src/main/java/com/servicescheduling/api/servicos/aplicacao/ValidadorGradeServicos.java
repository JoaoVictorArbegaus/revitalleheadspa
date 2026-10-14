package com.servicescheduling.api.servicos.aplicacao;

import com.servicescheduling.api.compartilhado.infraestrutura.configuracao.PropriedadesNegocio;
import com.servicescheduling.api.servicos.dominio.Servico;
import com.servicescheduling.api.servicos.infraestrutura.persistencia.RepositorioServico;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "aplicacao.modo", havingValue = "normal", matchIfMissing = true)
public class ValidadorGradeServicos implements ApplicationRunner {

    private final RepositorioServico repositorio;
    private final PropriedadesNegocio propriedades;

    public ValidadorGradeServicos(RepositorioServico repositorio, PropriedadesNegocio propriedades) {
        this.repositorio = repositorio;
        this.propriedades = propriedades;
    }

    @Override
    public void run(ApplicationArguments argumentos) {
        validar();
    }

    public void validar() {
        int grade = propriedades.getIntervaloGradeMinutos();
        List<Servico> servicos = repositorio.findAll();
        boolean incompativel = servicos.stream()
                .anyMatch(servico -> (servico.getDuracaoMinutos() + servico.getIntervaloLimpezaMinutos()) % grade != 0);
        if (incompativel) {
            throw new IllegalStateException(
                    "NEGOCIO_INTERVALO_GRADE_MINUTOS e incompativel com os servicos persistidos");
        }
    }
}
