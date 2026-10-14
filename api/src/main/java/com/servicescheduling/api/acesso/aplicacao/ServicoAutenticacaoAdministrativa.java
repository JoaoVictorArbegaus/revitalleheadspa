package com.servicescheduling.api.acesso.aplicacao;

import com.servicescheduling.api.acesso.dominio.UsuarioAdministrativo;
import com.servicescheduling.api.acesso.infraestrutura.persistencia.RepositorioUsuarioAdministrativo;
import com.servicescheduling.api.acesso.infraestrutura.seguranca.GerenciadorSessoesAdministrativas;
import com.servicescheduling.api.acesso.infraestrutura.seguranca.LimiteTentativasLogin;
import com.servicescheduling.api.acesso.infraestrutura.seguranca.PrincipalAdministrativo;
import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio;
import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio.Tipo;
import java.util.UUID;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ServicoAutenticacaoAdministrativa {

    private final AuthenticationManager authenticationManager;
    private final LimiteTentativasLogin limiteTentativas;
    private final RepositorioUsuarioAdministrativo repositorio;
    private final PasswordEncoder passwordEncoder;
    private final PoliticaSenha politicaSenha;
    private final GerenciadorSessoesAdministrativas gerenciadorSessoes;

    public ServicoAutenticacaoAdministrativa(
            AuthenticationManager authenticationManager,
            LimiteTentativasLogin limiteTentativas,
            RepositorioUsuarioAdministrativo repositorio,
            PasswordEncoder passwordEncoder,
            PoliticaSenha politicaSenha,
            GerenciadorSessoesAdministrativas gerenciadorSessoes) {
        this.authenticationManager = authenticationManager;
        this.limiteTentativas = limiteTentativas;
        this.repositorio = repositorio;
        this.passwordEncoder = passwordEncoder;
        this.politicaSenha = politicaSenha;
        this.gerenciadorSessoes = gerenciadorSessoes;
    }

    public Authentication autenticar(String email, String senha, String origem) {
        limiteTentativas.verificar(origem);
        try {
            Authentication autenticacao = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            UsuarioAdministrativo.normalizarEmail(email), senha));
            limiteTentativas.limpar(origem);
            return autenticacao;
        } catch (AuthenticationException excecao) {
            limiteTentativas.registrarFalha(origem);
            throw excecao;
        }
    }

    @Transactional
    public void alterarSenha(UUID usuarioId, String senhaAtual, String novaSenha) {
        UsuarioAdministrativo usuario = repositorio.findById(usuarioId).orElseThrow();
        if (!passwordEncoder.matches(senhaAtual, usuario.getHashSenha())) {
            throw new ExcecaoRegraNegocio(
                    Tipo.ENTRADA_INVALIDA, "SENHA_ATUAL_INVALIDA", "A senha atual nao confere");
        }
        politicaSenha.validar(novaSenha);
        usuario.alterarSenha(passwordEncoder.encode(novaSenha), false);
        repositorio.saveAndFlush(usuario);
        gerenciadorSessoes.invalidar(usuarioId);
    }

    public DadosUsuarioAdministrativo dadosAtuais(PrincipalAdministrativo principal) {
        return new DadosUsuarioAdministrativo(
                principal.id(), principal.nome(), principal.getUsername(), principal.papel(),
                principal.profissionalId(), principal.isEnabled(), principal.deveAlterarSenha());
    }
}
