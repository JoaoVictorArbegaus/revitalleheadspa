package com.servicescheduling.api.clientes.aplicacao;

import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio;
import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio.Tipo;
import com.servicescheduling.api.compartilhado.infraestrutura.configuracao.PropriedadesNegocio;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicoClientes {

    private final JdbcClient jdbc;
    private final ReservasCliente reservas;
    private final Clock relogio;
    private final ZoneId fusoHorario;

    public ServicoClientes(
            JdbcClient jdbc, ReservasCliente reservas, Clock relogio, PropriedadesNegocio propriedades) {
        this.jdbc = jdbc;
        this.reservas = reservas;
        this.relogio = relogio;
        this.fusoHorario = propriedades.getFusoHorario();
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

    @Transactional(readOnly = true)
    public DadosCliente obter(UUID id) {
        return jdbc.sql("select id, nome, celular, anonimizado_em from cliente where id = :id")
                .param("id", id)
                .query((resultado, linha) -> {
                    OffsetDateTime anonimizadoEm = resultado.getObject("anonimizado_em", OffsetDateTime.class);
                    return new DadosCliente(
                            resultado.getObject("id", UUID.class),
                            resultado.getString("nome"),
                            resultado.getString("celular"),
                            anonimizadoEm == null
                                    ? null
                                    : anonimizadoEm.toInstant().atZone(fusoHorario).toOffsetDateTime());
                })
                .optional()
                .orElseThrow(this::clienteNaoEncontrado);
    }

    @Transactional
    public void anonimizar(UUID id, String motivo, UUID autorId) {
        String motivoNormalizado = motivo == null ? "" : motivo.strip();
        if (motivoNormalizado.isEmpty() || motivoNormalizado.length() > 500) {
            throw new ExcecaoRegraNegocio(
                    Tipo.ENTRADA_INVALIDA, "FALHA_VALIDACAO", "O motivo deve possuir de 1 a 500 caracteres");
        }
        Optional<Boolean> anonimizado = jdbc.sql("""
                        select anonimizado_em is not null as anonimizado
                          from cliente
                         where id = :id
                           for update
                        """)
                .param("id", id)
                .query(Boolean.class)
                .optional();
        if (anonimizado.isEmpty()) {
            throw clienteNaoEncontrado();
        }
        if (anonimizado.get()) {
            throw new ExcecaoRegraNegocio(
                    Tipo.CONFLITO, "CLIENTE_JA_ANONIMIZADO", "O cliente ja foi anonimizado");
        }
        Instant instante = relogio.instant();
        if (reservas.possuiReservaFuturaConfirmada(id, instante)) {
            throw new ExcecaoRegraNegocio(
                    Tipo.CONFLITO,
                    "CLIENTE_POSSUI_RESERVA_FUTURA",
                    "O cliente possui reserva futura confirmada");
        }

        OffsetDateTime agora = instante.atOffset(ZoneOffset.UTC);
        jdbc.sql("""
                        update cliente
                           set nome = null, celular = null, anonimizado_em = :agora, atualizado_em = :agora
                         where id = :id
                        """)
                .param("id", id)
                .param("agora", agora)
                .update();
        reservas.removerDadosPessoais(id, instante);
        jdbc.sql("""
                        insert into anonimizacao_cliente (id, cliente_id, autor_id, motivo, anonimizado_em)
                        values (:registroId, :id, :autorId, :motivo, :agora)
                        """)
                .param("registroId", UUID.randomUUID())
                .param("id", id)
                .param("autorId", autorId)
                .param("motivo", motivoNormalizado)
                .param("agora", agora)
                .update();
    }

    private ExcecaoRegraNegocio clienteNaoEncontrado() {
        return new ExcecaoRegraNegocio(Tipo.NAO_ENCONTRADO, "CLIENTE_NAO_ENCONTRADO", "Cliente nao encontrado");
    }
}
