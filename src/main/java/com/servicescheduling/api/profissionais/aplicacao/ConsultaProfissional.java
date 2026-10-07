package com.servicescheduling.api.profissionais.aplicacao;

import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

@Service
public class ConsultaProfissional {

    private final JdbcClient jdbcClient;

    public ConsultaProfissional(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public boolean existeAtivo(UUID id) {
        return jdbcClient.sql("select exists(select 1 from profissional where id = :id and ativo = true)")
                .param("id", id)
                .query(Boolean.class)
                .single();
    }
}
