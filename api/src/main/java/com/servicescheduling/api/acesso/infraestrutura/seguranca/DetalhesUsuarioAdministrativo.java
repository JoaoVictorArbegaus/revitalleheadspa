package com.servicescheduling.api.acesso.infraestrutura.seguranca;

import com.servicescheduling.api.acesso.dominio.UsuarioAdministrativo;
import com.servicescheduling.api.acesso.infraestrutura.persistencia.RepositorioUsuarioAdministrativo;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DetalhesUsuarioAdministrativo implements UserDetailsService {

    private final RepositorioUsuarioAdministrativo repositorio;

    public DetalhesUsuarioAdministrativo(RepositorioUsuarioAdministrativo repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UsuarioAdministrativo usuario = repositorio.findByEmail(UsuarioAdministrativo.normalizarEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("credenciais invalidas"));
        return new PrincipalAdministrativo(usuario);
    }
}
