package com.servicescheduling.api.agendamentos.infraestrutura.persistencia;

import com.servicescheduling.api.agendamentos.aplicacao.DadosAgendamentoAdministrativo;
import com.servicescheduling.api.agendamentos.aplicacao.DadosAgendamentoAdministrativo.ClienteAgendamento;
import com.servicescheduling.api.agendamentos.aplicacao.DadosAgendamentoAdministrativo.ProfissionalAgendamento;
import com.servicescheduling.api.agendamentos.aplicacao.DadosAgendamentoAdministrativo.RecursoAlocado;
import com.servicescheduling.api.agendamentos.aplicacao.DadosAgendamentoAdministrativo.ServicoAgendamento;
import com.servicescheduling.api.agendamentos.dominio.EstadoAgendamento;
import com.servicescheduling.api.agendamentos.dominio.OrigemAgendamento;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
public class ConsultaAgendamentosAdministrativos {

    private static final String SELECAO = """
            select id, estado, origem, cliente_id, nome_cliente_informado, celular_cliente_informado,
                   servico_id, nome_servico_contratado, duracao_contratada_minutos,
                   profissional_id, nome_profissional_contratado,
                   inicio_em, fim_servico_em, ocupado_ate, preco_contratado, moeda_contratada,
                   versao_aviso_privacidade, ciencia_aviso_privacidade_em, criado_em, criado_por,
                   estado_alterado_em, estado_alterado_por, motivo_cancelamento
              from agendamento
            """;

    private final JdbcClient jdbc;

    public ConsultaAgendamentosAdministrativos(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<DadosAgendamentoAdministrativo> buscar(UUID id, ZoneId fuso) {
        List<Linha> linhas = jdbc.sql(SELECAO + " where id = :id")
                .param("id", id)
                .query((resultado, numero) -> linha(resultado))
                .list();
        return completar(linhas, fuso).stream().findFirst();
    }

    public List<DadosAgendamentoAdministrativo> listar(
            Instant inicio,
            Instant fim,
            UUID profissionalId,
            EstadoAgendamento estado,
            ZoneId fuso) {
        StringBuilder sql = new StringBuilder(SELECAO)
                .append(" where inicio_em >= :inicio and inicio_em < :fim");
        Map<String, Object> parametros = new HashMap<>();
        parametros.put("inicio", inicio.atOffset(ZoneOffset.UTC));
        parametros.put("fim", fim.atOffset(ZoneOffset.UTC));
        if (profissionalId != null) {
            sql.append(" and profissional_id = :profissionalId");
            parametros.put("profissionalId", profissionalId);
        }
        if (estado != null) {
            sql.append(" and estado = :estado");
            parametros.put("estado", estado.name());
        }
        sql.append(" order by inicio_em, id");
        List<Linha> linhas = jdbc.sql(sql.toString())
                .params(parametros)
                .query((resultado, numero) -> linha(resultado))
                .list();
        return completar(linhas, fuso);
    }

    private List<DadosAgendamentoAdministrativo> completar(List<Linha> linhas, ZoneId fuso) {
        if (linhas.isEmpty()) {
            return List.of();
        }
        Map<UUID, List<RecursoAlocado>> recursos = jdbc.sql("""
                        select alocacao.agendamento_id, alocacao.recurso_operacional_id,
                               recurso.nome, alocacao.quantidade
                          from alocacao_recurso_agendamento alocacao
                          join recurso_operacional recurso on recurso.id = alocacao.recurso_operacional_id
                         where alocacao.agendamento_id in (:ids)
                         order by recurso.nome, recurso.id
                        """)
                .param("ids", linhas.stream().map(Linha::id).toList())
                .query((resultado, numero) -> Map.entry(
                        resultado.getObject("agendamento_id", UUID.class),
                        new RecursoAlocado(
                                resultado.getObject("recurso_operacional_id", UUID.class),
                                resultado.getString("nome"),
                                resultado.getInt("quantidade"))))
                .list()
                .stream()
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
        return linhas.stream()
                .map(linha -> linha.dados(recursos.getOrDefault(linha.id(), List.of()), fuso))
                .toList();
    }

    private Linha linha(ResultSet resultado) throws SQLException {
        return new Linha(
                resultado.getObject("id", UUID.class),
                EstadoAgendamento.valueOf(resultado.getString("estado")),
                OrigemAgendamento.valueOf(resultado.getString("origem")),
                resultado.getObject("cliente_id", UUID.class),
                resultado.getString("nome_cliente_informado"),
                resultado.getString("celular_cliente_informado"),
                resultado.getObject("servico_id", UUID.class),
                resultado.getString("nome_servico_contratado"),
                resultado.getInt("duracao_contratada_minutos"),
                resultado.getObject("profissional_id", UUID.class),
                resultado.getString("nome_profissional_contratado"),
                instante(resultado, "inicio_em"),
                instante(resultado, "fim_servico_em"),
                instante(resultado, "ocupado_ate"),
                resultado.getBigDecimal("preco_contratado"),
                resultado.getString("moeda_contratada"),
                resultado.getString("versao_aviso_privacidade"),
                instante(resultado, "ciencia_aviso_privacidade_em"),
                instante(resultado, "criado_em"),
                resultado.getObject("criado_por", UUID.class),
                instante(resultado, "estado_alterado_em"),
                resultado.getObject("estado_alterado_por", UUID.class),
                resultado.getString("motivo_cancelamento"));
    }

    private Instant instante(ResultSet resultado, String coluna) throws SQLException {
        OffsetDateTime valor = resultado.getObject(coluna, OffsetDateTime.class);
        return valor == null ? null : valor.toInstant();
    }

    private record Linha(
            UUID id,
            EstadoAgendamento estado,
            OrigemAgendamento origem,
            UUID clienteId,
            String nomeCliente,
            String celularCliente,
            UUID servicoId,
            String nomeServico,
            int duracaoMinutos,
            UUID profissionalId,
            String nomeProfissional,
            Instant inicioEm,
            Instant fimServicoEm,
            Instant ocupadoAte,
            BigDecimal preco,
            String moeda,
            String versaoAviso,
            Instant cienciaAvisoEm,
            Instant criadoEm,
            UUID criadoPor,
            Instant estadoAlteradoEm,
            UUID estadoAlteradoPor,
            String motivoCancelamento) {

        DadosAgendamentoAdministrativo dados(List<RecursoAlocado> recursos, ZoneId fuso) {
            return new DadosAgendamentoAdministrativo(
                    id,
                    estado,
                    origem,
                    new ClienteAgendamento(clienteId, nomeCliente, celularCliente),
                    new ServicoAgendamento(servicoId, nomeServico, duracaoMinutos),
                    new ProfissionalAgendamento(profissionalId, nomeProfissional),
                    local(inicioEm, fuso),
                    local(fimServicoEm, fuso),
                    local(ocupadoAte, fuso),
                    fuso.getId(),
                    preco,
                    moeda,
                    versaoAviso,
                    local(cienciaAvisoEm, fuso),
                    List.copyOf(recursos),
                    local(criadoEm, fuso),
                    criadoPor,
                    local(estadoAlteradoEm, fuso),
                    estadoAlteradoPor,
                    motivoCancelamento);
        }

        private static OffsetDateTime local(Instant instante, ZoneId fuso) {
            return instante == null ? null : instante.atZone(fuso).toOffsetDateTime();
        }
    }
}
