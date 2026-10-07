package com.servicescheduling.api.acesso.infraestrutura.seguranca;

import com.servicescheduling.api.acesso.dominio.PapelUsuarioAdministrativo;
import com.servicescheduling.api.acesso.dominio.UsuarioAdministrativo;
import java.io.Serial;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class PrincipalAdministrativo implements UserDetails, CredentialsContainer {

    @Serial
    private static final long serialVersionUID = 1L;

    private final UUID id;
    private final String nome;
    private final String email;
    private String hashSenha;
    private final PapelUsuarioAdministrativo papel;
    private final UUID profissionalId;
    private final boolean ativo;
    private final boolean deveAlterarSenha;

    public PrincipalAdministrativo(UsuarioAdministrativo usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.hashSenha = usuario.getHashSenha();
        this.papel = usuario.getPapel();
        this.profissionalId = usuario.getProfissionalId();
        this.ativo = usuario.isAtivo();
        this.deveAlterarSenha = usuario.isDeveAlterarSenha();
    }

    public UUID id() {
        return id;
    }

    public String nome() {
        return nome;
    }

    public PapelUsuarioAdministrativo papel() {
        return papel;
    }

    public UUID profissionalId() {
        return profissionalId;
    }

    public boolean deveAlterarSenha() {
        return deveAlterarSenha;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + papel.name()));
    }

    @Override
    public String getPassword() {
        return hashSenha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }

    @Override
    public void eraseCredentials() {
        hashSenha = null;
    }
}
