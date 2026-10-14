package com.servicescheduling.api.agendamentos.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "agendamento")
public class Agendamento {

    @Id
    private UUID id;

    @Column(name = "cliente_id", nullable = false)
    private UUID clienteId;

    @Column(name = "servico_id", nullable = false)
    private UUID servicoId;

    @Column(name = "profissional_id", nullable = false)
    private UUID profissionalId;

    @Column(name = "inicio_em", nullable = false)
    private Instant inicioEm;

    @Column(name = "fim_servico_em", nullable = false)
    private Instant fimServicoEm;

    @Column(name = "ocupado_ate", nullable = false)
    private Instant ocupadoAte;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EstadoAgendamento estado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigemAgendamento origem;

    @Column(name = "nome_servico_contratado", nullable = false, length = 120)
    private String nomeServicoContratado;

    @Column(name = "nome_profissional_contratado", nullable = false, length = 120)
    private String nomeProfissionalContratado;

    @Column(name = "duracao_contratada_minutos", nullable = false)
    private int duracaoContratadaMinutos;

    @Column(name = "preco_contratado", nullable = false, precision = 11, scale = 2)
    private BigDecimal precoContratado;

    @Column(name = "moeda_contratada", nullable = false, length = 3)
    private String moedaContratada;

    @Column(name = "nome_cliente_informado", length = 120)
    private String nomeClienteInformado;

    @Column(name = "celular_cliente_informado", length = 14)
    private String celularClienteInformado;

    @Column(name = "versao_aviso_privacidade", nullable = false, length = 64)
    private String versaoAvisoPrivacidade;

    @Column(name = "ciencia_aviso_privacidade_em", nullable = false)
    private Instant cienciaAvisoPrivacidadeEm;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm;

    @Column(name = "criado_por")
    private UUID criadoPor;

    @Column(name = "estado_alterado_em")
    private Instant estadoAlteradoEm;

    @Column(name = "estado_alterado_por")
    private UUID estadoAlteradoPor;

    @Column(name = "motivo_cancelamento", length = 500)
    private String motivoCancelamento;

    protected Agendamento() {
    }

    private Agendamento(
            UUID id,
            UUID clienteId,
            UUID servicoId,
            UUID profissionalId,
            Instant inicioEm,
            Instant fimServicoEm,
            Instant ocupadoAte,
            String nomeServicoContratado,
            String nomeProfissionalContratado,
            int duracaoContratadaMinutos,
            BigDecimal precoContratado,
            String moedaContratada,
            String nomeClienteInformado,
            String celularClienteInformado,
            String versaoAvisoPrivacidade,
            OrigemAgendamento origem,
            UUID criadoPor,
            Instant agora) {
        this.id = id;
        this.clienteId = clienteId;
        this.servicoId = servicoId;
        this.profissionalId = profissionalId;
        this.inicioEm = inicioEm;
        this.fimServicoEm = fimServicoEm;
        this.ocupadoAte = ocupadoAte;
        this.estado = EstadoAgendamento.CONFIRMADO;
        this.origem = origem;
        this.criadoPor = criadoPor;
        this.nomeServicoContratado = nomeServicoContratado;
        this.nomeProfissionalContratado = nomeProfissionalContratado;
        this.duracaoContratadaMinutos = duracaoContratadaMinutos;
        this.precoContratado = precoContratado;
        this.moedaContratada = moedaContratada;
        this.nomeClienteInformado = nomeClienteInformado;
        this.celularClienteInformado = celularClienteInformado;
        this.versaoAvisoPrivacidade = versaoAvisoPrivacidade;
        this.cienciaAvisoPrivacidadeEm = agora;
        this.criadoEm = agora;
        this.atualizadoEm = agora;
    }

    public static Agendamento criarPublico(
            UUID clienteId,
            UUID servicoId,
            UUID profissionalId,
            Instant inicioEm,
            Instant fimServicoEm,
            Instant ocupadoAte,
            String nomeServicoContratado,
            String nomeProfissionalContratado,
            int duracaoContratadaMinutos,
            BigDecimal precoContratado,
            String moedaContratada,
            String nomeClienteInformado,
            String celularClienteInformado,
            String versaoAvisoPrivacidade,
            Instant agora) {
        return new Agendamento(
                UUID.randomUUID(), clienteId, servicoId, profissionalId, inicioEm, fimServicoEm, ocupadoAte,
                nomeServicoContratado, nomeProfissionalContratado, duracaoContratadaMinutos, precoContratado,
                moedaContratada, nomeClienteInformado, celularClienteInformado, versaoAvisoPrivacidade,
                OrigemAgendamento.PUBLICO, null, agora);
    }

    public static Agendamento criarAdministrativo(
            UUID clienteId,
            UUID servicoId,
            UUID profissionalId,
            Instant inicioEm,
            Instant fimServicoEm,
            Instant ocupadoAte,
            String nomeServicoContratado,
            String nomeProfissionalContratado,
            int duracaoContratadaMinutos,
            BigDecimal precoContratado,
            String moedaContratada,
            String nomeClienteInformado,
            String celularClienteInformado,
            String versaoAvisoPrivacidade,
            UUID autorId,
            Instant agora) {
        return new Agendamento(
                UUID.randomUUID(), clienteId, servicoId, profissionalId, inicioEm, fimServicoEm, ocupadoAte,
                nomeServicoContratado, nomeProfissionalContratado, duracaoContratadaMinutos, precoContratado,
                moedaContratada, nomeClienteInformado, celularClienteInformado, versaoAvisoPrivacidade,
                OrigemAgendamento.ADMINISTRACAO, autorId, agora);
    }

    public boolean podeCancelar(Instant agora) {
        return estado == EstadoAgendamento.CONFIRMADO && agora.isBefore(inicioEm);
    }

    public boolean podeEncerrar(Instant agora) {
        return estado == EstadoAgendamento.CONFIRMADO && !agora.isBefore(inicioEm);
    }

    public void cancelar(String motivo, UUID autorId, Instant agora) {
        if (!podeCancelar(agora)) {
            throw new IllegalStateException("Cancelamento fora do estado ou da janela permitida");
        }
        this.motivoCancelamento = motivo;
        alterarEstado(EstadoAgendamento.CANCELADO, autorId, agora);
    }

    public void encerrar(EstadoAgendamento destino, UUID autorId, Instant agora) {
        if (destino != EstadoAgendamento.CONCLUIDO && destino != EstadoAgendamento.NAO_COMPARECEU) {
            throw new IllegalArgumentException("Estado de encerramento invalido");
        }
        if (!podeEncerrar(agora)) {
            throw new IllegalStateException("Encerramento fora do estado ou da janela permitida");
        }
        alterarEstado(destino, autorId, agora);
    }

    private void alterarEstado(EstadoAgendamento destino, UUID autorId, Instant agora) {
        this.estado = destino;
        this.estadoAlteradoPor = autorId;
        this.estadoAlteradoEm = agora;
        this.atualizadoEm = agora;
    }

    public UUID getId() {
        return id;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public UUID getProfissionalId() {
        return profissionalId;
    }

    public Instant getInicioEm() {
        return inicioEm;
    }

    public Instant getFimServicoEm() {
        return fimServicoEm;
    }

    public Instant getOcupadoAte() {
        return ocupadoAte;
    }

    public EstadoAgendamento getEstado() {
        return estado;
    }
}
