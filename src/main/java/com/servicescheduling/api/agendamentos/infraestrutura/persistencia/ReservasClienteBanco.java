package com.servicescheduling.api.agendamentos.infraestrutura.persistencia;

import com.servicescheduling.api.clientes.aplicacao.ReservasCliente;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ReservasClienteBanco implements ReservasCliente {

    private final JdbcClient jdbc;

    public ReservasClienteBanco(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean possuiReservaFuturaConfirmada(UUID clienteId, Instant agora) {
        return jdbc.sql("""
                        select exists (
                               select 1
                                 from agendamento
                                where cliente_id = :clienteId
                                  and estado = 'CONFIRMADO'
                                  and inicio_em > :agora)
                        """)
                .param("clienteId", clienteId)
                .param("agora", agora.atOffset(ZoneOffset.UTC))
                .query(Boolean.class)
                .single();
    }

    @Override
    public void removerDadosPessoais(UUID clienteId, Instant agora) {
        jdbc.sql("""
                        update agendamento
                           set nome_cliente_informado = null,
                               celular_cliente_informado = null,
                               atualizado_em = :agora
                         where cliente_id = :clienteId
                        """)
                .param("clienteId", clienteId)
                .param("agora", agora.atOffset(ZoneOffset.UTC))
                .update();
    }
}
