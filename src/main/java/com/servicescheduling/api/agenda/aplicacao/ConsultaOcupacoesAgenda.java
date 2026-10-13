package com.servicescheduling.api.agenda.aplicacao;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ConsultaOcupacoesAgenda {

    DadosOcupacoesAgenda consultar(
            UUID profissionalId,
            Collection<UUID> recursoIds,
            Instant inicioPeriodo,
            Instant fimPeriodo);

    record DadosOcupacoesAgenda(
            List<IntervaloOcupado> profissional,
            List<AlocacaoOcupada> recursos) {
    }

    record IntervaloOcupado(Instant inicioEm, Instant ocupadoAte) {
    }

    record AlocacaoOcupada(
            UUID recursoId,
            Instant inicioEm,
            Instant ocupadoAte,
            int quantidade) {
    }
}
