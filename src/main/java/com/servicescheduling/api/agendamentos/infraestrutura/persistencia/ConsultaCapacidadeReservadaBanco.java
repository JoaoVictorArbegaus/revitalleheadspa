package com.servicescheduling.api.agendamentos.infraestrutura.persistencia;

import com.servicescheduling.api.recursos.aplicacao.ConsultaCapacidadeReservada;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ConsultaCapacidadeReservadaBanco implements ConsultaCapacidadeReservada {

    private final JdbcClient jdbc;

    public ConsultaCapacidadeReservadaBanco(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public int maiorConsumoSimultaneo(UUID recursoId, Instant agora) {
        List<IntervaloAlocado> intervalos = jdbc.sql("""
                        select agendamento.inicio_em, agendamento.ocupado_ate, alocacao.quantidade
                          from alocacao_recurso_agendamento alocacao
                          join agendamento on agendamento.id = alocacao.agendamento_id
                         where alocacao.recurso_operacional_id = :recursoId
                           and agendamento.estado = 'CONFIRMADO'
                           and agendamento.ocupado_ate > :agora
                        """)
                .param("recursoId", recursoId)
                .param("agora", agora.atOffset(ZoneOffset.UTC))
                .query((resultado, linha) -> new IntervaloAlocado(
                        resultado.getObject("inicio_em", OffsetDateTime.class).toInstant(),
                        resultado.getObject("ocupado_ate", OffsetDateTime.class).toInstant(),
                        resultado.getInt("quantidade")))
                .list();
        List<EventoCapacidade> eventos = new ArrayList<>(intervalos.size() * 2);
        for (IntervaloAlocado intervalo : intervalos) {
            eventos.add(new EventoCapacidade(intervalo.inicioEm(), intervalo.quantidade()));
            eventos.add(new EventoCapacidade(intervalo.ocupadoAte(), -intervalo.quantidade()));
        }
        eventos.sort(Comparator.comparing(EventoCapacidade::instante).thenComparingInt(EventoCapacidade::variacao));
        int consumo = 0;
        int maior = 0;
        for (EventoCapacidade evento : eventos) {
            consumo += evento.variacao();
            maior = Math.max(maior, consumo);
        }
        return maior;
    }

    private record IntervaloAlocado(Instant inicioEm, Instant ocupadoAte, int quantidade) {
    }

    private record EventoCapacidade(Instant instante, int variacao) {
    }
}
