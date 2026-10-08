package com.servicescheduling.api.servicos.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "servico")
public class Servico {

    @Id
    private UUID id;

    @Column(name = "profissional_id", nullable = false)
    private UUID profissionalId;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 1000)
    private String descricao;

    @Column(name = "duracao_minutos", nullable = false)
    private int duracaoMinutos;

    @Column(name = "intervalo_limpeza_minutos", nullable = false)
    private int intervaloLimpezaMinutos;

    @Column(nullable = false, precision = 11, scale = 2)
    private BigDecimal preco;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "ordem_exibicao", nullable = false)
    private int ordemExibicao;

    protected Servico() {
    }

    private Servico(
            UUID id,
            UUID profissionalId,
            String nome,
            String descricao,
            int duracaoMinutos,
            int intervaloLimpezaMinutos,
            BigDecimal preco,
            boolean ativo,
            int ordemExibicao) {
        this.id = id;
        alterar(profissionalId, nome, descricao, duracaoMinutos, intervaloLimpezaMinutos, preco, ativo, ordemExibicao);
    }

    public static Servico criar(
            UUID profissionalId,
            String nome,
            String descricao,
            int duracaoMinutos,
            int intervaloLimpezaMinutos,
            BigDecimal preco,
            boolean ativo,
            int ordemExibicao) {
        return new Servico(
                UUID.randomUUID(), profissionalId, nome, descricao, duracaoMinutos,
                intervaloLimpezaMinutos, preco, ativo, ordemExibicao);
    }

    public void alterar(
            UUID profissionalId,
            String nome,
            String descricao,
            int duracaoMinutos,
            int intervaloLimpezaMinutos,
            BigDecimal preco,
            boolean ativo,
            int ordemExibicao) {
        this.profissionalId = profissionalId;
        this.nome = nome.strip();
        this.descricao = descricao == null || descricao.isBlank() ? null : descricao.strip();
        this.duracaoMinutos = duracaoMinutos;
        this.intervaloLimpezaMinutos = intervaloLimpezaMinutos;
        this.preco = preco;
        this.ativo = ativo;
        this.ordemExibicao = ordemExibicao;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProfissionalId() {
        return profissionalId;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getDuracaoMinutos() {
        return duracaoMinutos;
    }

    public int getIntervaloLimpezaMinutos() {
        return intervaloLimpezaMinutos;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public int getOrdemExibicao() {
        return ordemExibicao;
    }
}
