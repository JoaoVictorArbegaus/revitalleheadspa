package com.servicescheduling.api.agendamentos.infraestrutura.persistencia;

import com.servicescheduling.api.agenda.aplicacao.ConsultaOcupacoesAgenda;
import com.servicescheduling.api.agenda.aplicacao.ConsultaOcupacoesAgenda.AlocacaoOcupada;
import com.servicescheduling.api.agenda.aplicacao.ConsultaOcupacoesAgenda.DadosOcupacoesAgenda;
import com.servicescheduling.api.agenda.aplicacao.ConsultaOcupacoesAgenda.IntervaloOcupado;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ConsultaOcupacoesAgendamentosBanco implements ConsultaOcupacoesAgenda {

    private final JdbcClient jdbc;

    public ConsultaOcupacoesAgendamentosBanco(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public DadosOcupacoesAgenda consultar(
            UUID profissionalId,
            Collection<UUID> recursoIds,
            Instant inicioPeriodo,
            Instant fimPeriodo) {
        List<IntervaloOcupado> profissional = jdbc.sql("""
                        select inicio_em, ocupado_ate
                          from agendamento
                         where profissional_id = :profissionalId
                           and estado = 'CONFIRMADO'
                           and inicio_em < :fimPeriodo
                           and ocupado_ate > :inicioPeriodo
                         order by inicio_em, id
                        """)
                .param("profissionalId", profissionalId)
                .param("inicioPeriodo", inicioPeriodo.atOffset(ZoneOffset.UTC))
                .param("fimPeriodo", fimPeriodo.atOffset(ZoneOffset.UTC))
                .query((resultado, linha) -> new IntervaloOcupado(
                        resultado.getObject("inicio_em", java.time.OffsetDateTime.class).toInstant(),
                        resultado.getObject("ocupado_ate", java.time.OffsetDateTime.class).toInstant()))
                .list();

        if (recursoIds.isEmpty()) {
            return new DadosOcupacoesAgenda(profissional, List.of());
        }
        List<AlocacaoOcupada> recursos = jdbc.sql("""
                        select alocacao.recurso_operacional_id, agendamento.inicio_em,
                               agendamento.ocupado_ate, alocacao.quantidade
                          from alocacao_recurso_agendamento alocacao
                          join agendamento on agendamento.id = alocacao.agendamento_id
                         where alocacao.recurso_operacional_id in (:recursoIds)
                           and agendamento.estado = 'CONFIRMADO'
                           and agendamento.inicio_em < :fimPeriodo
                           and agendamento.ocupado_ate > :inicioPeriodo
                         order by alocacao.recurso_operacional_id, agendamento.inicio_em, agendamento.id
                        """)
                .param("recursoIds", recursoIds)
                .param("inicioPeriodo", inicioPeriodo.atOffset(ZoneOffset.UTC))
                .param("fimPeriodo", fimPeriodo.atOffset(ZoneOffset.UTC))
                .query((resultado, linha) -> new AlocacaoOcupada(
                        resultado.getObject("recurso_operacional_id", UUID.class),
                        resultado.getObject("inicio_em", java.time.OffsetDateTime.class).toInstant(),
                        resultado.getObject("ocupado_ate", java.time.OffsetDateTime.class).toInstant(),
                        resultado.getInt("quantidade")))
                .list();
        return new DadosOcupacoesAgenda(profissional, recursos);
    }
}
