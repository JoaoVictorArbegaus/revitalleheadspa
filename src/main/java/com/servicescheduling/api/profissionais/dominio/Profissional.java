package com.servicescheduling.api.profissionais.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "profissional")
public class Profissional {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false)
    private boolean ativo;

    protected Profissional() {
    }

    private Profissional(UUID id, String nome, boolean ativo) {
        this.id = id;
        alterar(nome, ativo);
    }

    public static Profissional criar(String nome, boolean ativo) {
        return new Profissional(UUID.randomUUID(), nome, ativo);
    }

    public void alterar(String nome, boolean ativo) {
        this.nome = nome.strip();
        this.ativo = ativo;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
