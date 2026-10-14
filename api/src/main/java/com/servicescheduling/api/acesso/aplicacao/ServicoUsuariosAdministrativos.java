package com.servicescheduling.api.acesso.aplicacao;

import com.servicescheduling.api.acesso.dominio.PapelUsuarioAdministrativo;
import com.servicescheduling.api.acesso.dominio.UsuarioAdministrativo;
import com.servicescheduling.api.acesso.infraestrutura.persistencia.RepositorioUsuarioAdministrativo;
import com.servicescheduling.api.acesso.infraestrutura.seguranca.GerenciadorSessoesAdministrativas;
import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio;
import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio.Tipo;
import com.servicescheduling.api.profissionais.aplicacao.ConsultaProfissional;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ServicoUsuariosAdministrativos {

    private final RepositorioUsuarioAdministrativo repositorio;
    private final ConsultaProfissional consultaProfissional;
    private final PasswordEncoder passwordEncoder;
    private final PoliticaSenha politicaSenha;
    private final GerenciadorSessoesAdministrativas gerenciadorSessoes;

    public ServicoUsuariosAdministrativos(
            RepositorioUsuarioAdministrativo repositorio,
            ConsultaProfissional consultaProfissional,
            PasswordEncoder passwordEncoder,
            PoliticaSenha politicaSenha,
            GerenciadorSessoesAdministrativas gerenciadorSessoes) {
        this.repositorio = repositorio;
        this.consultaProfissional = consultaProfissional;
        this.passwordEncoder = passwordEncoder;
        this.politicaSenha = politicaSenha;
        this.gerenciadorSessoes = gerenciadorSessoes;
    }

    public List<DadosUsuarioAdministrativo> listar() {
        return repositorio.findAllByOrderByNomeAscIdAsc().stream().map(this::dados).toList();
    }

    public DadosUsuarioAdministrativo buscar(UUID id) {
        return dados(obter(id));
    }

    @Transactional
    public DadosUsuarioAdministrativo criarFuncionario(
            String nome, String email, String senhaTemporaria, UUID profissionalId) {
        validarNomeEmail(nome, email, null);
        validarProfissional(profissionalId, null);
        politicaSenha.validar(senhaTemporaria);
        UsuarioAdministrativo usuario = UsuarioAdministrativo.funcionario(
                nome.strip(), email, passwordEncoder.encode(senhaTemporaria), profissionalId);
        return dados(repositorio.saveAndFlush(usuario));
    }

    @Transactional
    public DadosUsuarioAdministrativo alterar(
            UUID id, String nome, String email, PapelUsuarioAdministrativo papel, UUID profissionalId) {
        UsuarioAdministrativo usuario = obter(id);
        validarNomeEmail(nome, email, id);
        if (papel == PapelUsuarioAdministrativo.FUNCIONARIO) {
            validarProfissional(profissionalId, id);
        } else if (profissionalId != null) {
            validarProfissional(profissionalId, id);
        }
        if (usuario.isAtivo()
                && usuario.getPapel() == PapelUsuarioAdministrativo.PROPRIETARIO
                && papel != PapelUsuarioAdministrativo.PROPRIETARIO) {
            validarNaoEhUltimoProprietario(usuario.getId());
        }
        usuario.alterar(nome.strip(), email, papel, profissionalId);
        gerenciadorSessoes.invalidar(id);
        return dados(repositorio.saveAndFlush(usuario));
    }

    @Transactional
    public void redefinirSenhaTemporaria(UUID id, String senhaTemporaria) {
        UsuarioAdministrativo usuario = obter(id);
        if (usuario.getPapel() != PapelUsuarioAdministrativo.FUNCIONARIO) {
            throw new ExcecaoRegraNegocio(
                    Tipo.ENTRADA_INVALIDA,
                    "SENHA_TEMPORARIA_APENAS_FUNCIONARIO",
                    "Somente funcionario recebe senha temporaria");
        }
        politicaSenha.validar(senhaTemporaria);
        usuario.alterarSenha(passwordEncoder.encode(senhaTemporaria), true);
        repositorio.saveAndFlush(usuario);
        gerenciadorSessoes.invalidar(id);
    }

    @Transactional
    public void ativar(UUID id) {
        UsuarioAdministrativo usuario = obter(id);
        if (usuario.getPapel() == PapelUsuarioAdministrativo.FUNCIONARIO) {
            validarProfissional(usuario.getProfissionalId(), usuario.getId());
        }
        usuario.ativar();
    }

    @Transactional
    public void desativar(UUID id) {
        UsuarioAdministrativo usuario = obter(id);
        if (usuario.isAtivo() && usuario.getPapel() == PapelUsuarioAdministrativo.PROPRIETARIO) {
            validarNaoEhUltimoProprietario(usuario.getId());
        }
        usuario.desativar();
        gerenciadorSessoes.invalidar(id);
    }

    private void validarNaoEhUltimoProprietario(UUID id) {
        List<UsuarioAdministrativo> proprietarios =
                repositorio.bloquearAtivosPorPapel(PapelUsuarioAdministrativo.PROPRIETARIO);
        if (proprietarios.size() == 1 && proprietarios.getFirst().getId().equals(id)) {
            conflito("ULTIMO_PROPRIETARIO_ATIVO", "O ultimo proprietario ativo deve ser preservado");
        }
    }

    private void validarNomeEmail(String nome, String email, UUID idAtual) {
        if (nome == null || nome.isBlank() || email == null || email.isBlank()) {
            throw new ExcecaoRegraNegocio(
                    Tipo.ENTRADA_INVALIDA, "DADOS_USUARIO_INVALIDOS", "Nome e email sao obrigatorios");
        }
        repositorio.findByEmail(UsuarioAdministrativo.normalizarEmail(email))
                .filter(existente -> !existente.getId().equals(idAtual))
                .ifPresent(existente -> conflito("EMAIL_JA_UTILIZADO", "O email ja esta em uso"));
    }

    private void validarProfissional(UUID profissionalId, UUID usuarioAtual) {
        if (profissionalId == null) {
            throw new ExcecaoRegraNegocio(
                    Tipo.ENTRADA_INVALIDA,
                    "PROFISSIONAL_OBRIGATORIO",
                    "Funcionario deve possuir profissional");
        }
        if (!consultaProfissional.existeAtivo(profissionalId)) {
            throw new ExcecaoRegraNegocio(
                    Tipo.ENTRADA_INVALIDA,
                    "PROFISSIONAL_INVALIDO",
                    "O profissional deve existir e estar ativo");
        }
        if (repositorio.existsByProfissionalId(profissionalId)) {
            UsuarioAdministrativo atual = usuarioAtual == null ? null : obter(usuarioAtual);
            if (atual == null || !profissionalId.equals(atual.getProfissionalId())) {
                conflito("PROFISSIONAL_JA_VINCULADO", "O profissional ja possui usuario vinculado");
            }
        }
    }

    private UsuarioAdministrativo obter(UUID id) {
        return repositorio.findById(id).orElseThrow(() -> new ExcecaoRegraNegocio(
                Tipo.NAO_ENCONTRADO, "USUARIO_NAO_ENCONTRADO", "Usuario nao encontrado"));
    }

    private DadosUsuarioAdministrativo dados(UsuarioAdministrativo usuario) {
        return new DadosUsuarioAdministrativo(
                usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getPapel(),
                usuario.getProfissionalId(), usuario.isAtivo(), usuario.isDeveAlterarSenha());
    }

    private void conflito(String codigo, String mensagem) {
        throw new ExcecaoRegraNegocio(Tipo.CONFLITO, codigo, mensagem);
    }
}
