package com.servicescheduling.api.agendamentos.infraestrutura.persistencia;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class RepositorioRegistroIdempotencia {

    private final JdbcClient jdbc;

    public RepositorioRegistroIdempotencia(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void removerExpirados(String hashChave, Instant agora) {
        Optional<Instant> expiracao = jdbc.sql("""
                        select expira_em
                          from registro_idempotencia
                         where hash_chave = :hashChave
                           for update
                        """)
                .param("hashChave", hashChave)
                .query((resultado, linha) -> resultado.getObject("expira_em", OffsetDateTime.class).toInstant())
                .optional();
        if (expiracao.isPresent() && !agora.isBefore(expiracao.get())) {
            jdbc.sql("delete from registro_idempotencia where hash_chave = :hashChave")
                    .param("hashChave", hashChave)
                    .update();
        }
        jdbc.sql("""
                        delete from registro_idempotencia
                         where hash_chave in (
                               select hash_chave
                                 from registro_idempotencia
                                where expira_em <= :agora
                                order by expira_em
                                limit 100
                                  for update skip locked
                         )
                        """)
                .param("agora", agora.atOffset(ZoneOffset.UTC))
                .update();
    }

    public boolean reivindicar(String hashChave, String hashRequisicao, Instant criadoEm, Instant expiraEm) {
        return jdbc.sql("""
                        insert into registro_idempotencia
                            (hash_chave, hash_requisicao, criado_em, expira_em)
                        values (:hashChave, :hashRequisicao, :criadoEm, :expiraEm)
                        on conflict (hash_chave) do nothing
                        """)
                .param("hashChave", hashChave)
                .param("hashRequisicao", hashRequisicao)
                .param("criadoEm", criadoEm.atOffset(ZoneOffset.UTC))
                .param("expiraEm", expiraEm.atOffset(ZoneOffset.UTC))
                .update() == 1;
    }

    public Optional<DadosRegistroIdempotencia> buscar(String hashChave) {
        return jdbc.sql("""
                        select hash_requisicao, status_resposta, corpo_resposta::text as corpo_resposta,
                               agendamento_id
                          from registro_idempotencia
                         where hash_chave = :hashChave
                        """)
                .param("hashChave", hashChave)
                .query((resultado, linha) -> {
                    Number status = (Number) resultado.getObject("status_resposta");
                    return new DadosRegistroIdempotencia(
                            resultado.getString("hash_requisicao").strip(),
                            status == null ? null : status.intValue(),
                            resultado.getString("corpo_resposta"),
                            resultado.getObject("agendamento_id", UUID.class));
                })
                .optional();
    }

    public void concluir(String hashChave, int statusResposta, String corpoResposta, UUID agendamentoId) {
        int alterados = jdbc.sql("""
                        update registro_idempotencia
                           set status_resposta = :statusResposta,
                               corpo_resposta = cast(:corpoResposta as jsonb),
                               agendamento_id = :agendamentoId
                         where hash_chave = :hashChave
                           and status_resposta is null
                        """)
                .param("hashChave", hashChave)
                .param("statusResposta", statusResposta)
                .param("corpoResposta", corpoResposta)
                .param("agendamentoId", agendamentoId)
                .update();
        if (alterados != 1) {
            throw new IllegalStateException("Registro idempotente nao pode ser concluido");
        }
    }
}
