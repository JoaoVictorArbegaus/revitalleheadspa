package com.servicescheduling.api.clientes.aplicacao;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class ServicoClientes {

    private final JdbcClient jdbc;

    public ServicoClientes(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public UUID localizarOuCriar(String nome, String celular, Instant agora) {
        return jdbc.sql("""
                        insert into cliente (id, nome, celular, criado_em, atualizado_em)
                        values (:id, :nome, :celular, :agora, :agora)
                        on conflict (celular) do update
                           set nome = excluded.nome,
                               atualizado_em = excluded.atualizado_em
                        returning id
                        """)
                .param("id", UUID.randomUUID())
                .param("nome", nome)
                .param("celular", celular)
                .param("agora", agora.atOffset(ZoneOffset.UTC))
                .query(UUID.class)
                .single();
    }
}
