package com.servicescheduling.api.acesso.dominio;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "usuario_administrativo")
public class UsuarioAdministrativo {

    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "hash_senha", nullable = false, length = 60)
    private String hashSenha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PapelUsuarioAdministrativo papel;

    @Column(name = "profissional_id")
    private UUID profissionalId;

    @Column(nullable = false)
    private boolean ativo;

    @Column(name = "deve_alterar_senha", nullable = false)
    private boolean deveAlterarSenha;

    @Version
    @Column(nullable = false)
    private long versao;

    protected UsuarioAdministrativo() {
    }

    private UsuarioAdministrativo(
            UUID id,
            String nome,
            String email,
            String hashSenha,
            PapelUsuarioAdministrativo papel,
            UUID profissionalId,
            boolean deveAlterarSenha) {
        this.id = id;
        this.nome = nome;
        this.email = normalizarEmail(email);
        this.hashSenha = hashSenha;
        this.papel = papel;
        this.profissionalId = profissionalId;
        this.ativo = true;
        this.deveAlterarSenha = deveAlterarSenha;
    }

    public static UsuarioAdministrativo proprietario(String nome, String email, String hashSenha) {
        return new UsuarioAdministrativo(
                UUID.randomUUID(), nome, email, hashSenha, PapelUsuarioAdministrativo.PROPRIETARIO, null, false);
    }

    public static UsuarioAdministrativo funcionario(
            String nome, String email, String hashSenha, UUID profissionalId) {
        return new UsuarioAdministrativo(
                UUID.randomUUID(), nome, email, hashSenha, PapelUsuarioAdministrativo.FUNCIONARIO,
                profissionalId, true);
    }

    public static String normalizarEmail(String email) {
        return email == null ? null : email.strip().toLowerCase(Locale.ROOT);
    }

    public void alterar(String nome, String email, PapelUsuarioAdministrativo papel, UUID profissionalId) {
        this.nome = nome;
        this.email = normalizarEmail(email);
        this.papel = papel;
        this.profissionalId = profissionalId;
    }

    public void alterarSenha(String hashSenha, boolean temporaria) {
        this.hashSenha = hashSenha;
        this.deveAlterarSenha = temporaria;
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getEmail() {
        return email;
    }

    public String getHashSenha() {
        return hashSenha;
    }

    public PapelUsuarioAdministrativo getPapel() {
        return papel;
    }

    public UUID getProfissionalId() {
        return profissionalId;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public boolean isDeveAlterarSenha() {
        return deveAlterarSenha;
    }
}
