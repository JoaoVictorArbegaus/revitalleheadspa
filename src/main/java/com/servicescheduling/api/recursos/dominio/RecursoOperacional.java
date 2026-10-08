package com.servicescheduling.api.recursos.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "recurso_operacional")
public class RecursoOperacional {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false)
    private int capacidade;

    @Column(nullable = false)
    private boolean ativo;

    protected RecursoOperacional() {
    }

    private RecursoOperacional(UUID id, String nome, int capacidade, boolean ativo) {
        this.id = id;
        alterar(nome, capacidade, ativo);
    }

    public static RecursoOperacional criar(String nome, int capacidade, boolean ativo) {
        return new RecursoOperacional(UUID.randomUUID(), nome, capacidade, ativo);
    }

    public void alterar(String nome, int capacidade, boolean ativo) {
        this.nome = nome.strip();
        this.capacidade = capacidade;
        this.ativo = ativo;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public int getCapacidade() {
        return capacidade;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
