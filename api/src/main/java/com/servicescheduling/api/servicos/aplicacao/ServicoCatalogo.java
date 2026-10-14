package com.servicescheduling.api.servicos.aplicacao;

import com.servicescheduling.api.acesso.dominio.PapelUsuarioAdministrativo;
import com.servicescheduling.api.acesso.infraestrutura.seguranca.PrincipalAdministrativo;
import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio;
import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio.Tipo;
import com.servicescheduling.api.compartilhado.infraestrutura.configuracao.PropriedadesNegocio;
import com.servicescheduling.api.profissionais.aplicacao.ConsultaProfissional;
import com.servicescheduling.api.profissionais.dominio.Profissional;
import com.servicescheduling.api.recursos.aplicacao.ConsultaRecursoOperacional;
import com.servicescheduling.api.recursos.dominio.RecursoOperacional;
import com.servicescheduling.api.servicos.aplicacao.ComandoServico.ComandoRequisitoRecurso;
import com.servicescheduling.api.servicos.aplicacao.DadosServico.DadosRequisitoRecurso;
import com.servicescheduling.api.servicos.dominio.RequisitoRecursoServico;
import com.servicescheduling.api.servicos.dominio.Servico;
import com.servicescheduling.api.servicos.infraestrutura.persistencia.RepositorioRequisitoRecursoServico;
import com.servicescheduling.api.servicos.infraestrutura.persistencia.RepositorioServico;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ServicoCatalogo {

    private final RepositorioServico repositorioServico;
    private final RepositorioRequisitoRecursoServico repositorioRequisito;
    private final ConsultaProfissional consultaProfissional;
    private final ConsultaRecursoOperacional consultaRecurso;
    private final PropriedadesNegocio propriedades;

    public ServicoCatalogo(
            RepositorioServico repositorioServico,
            RepositorioRequisitoRecursoServico repositorioRequisito,
            ConsultaProfissional consultaProfissional,
            ConsultaRecursoOperacional consultaRecurso,
            PropriedadesNegocio propriedades) {
        this.repositorioServico = repositorioServico;
        this.repositorioRequisito = repositorioRequisito;
        this.consultaProfissional = consultaProfissional;
        this.consultaRecurso = consultaRecurso;
        this.propriedades = propriedades;
    }

    public List<DadosServico> listar(PrincipalAdministrativo principal) {
        List<Servico> servicos = proprietario(principal)
                ? repositorioServico.findAllByOrderByOrdemExibicaoAscNomeAscIdAsc()
                : repositorioServico.findAllByProfissionalIdOrderByOrdemExibicaoAscNomeAscIdAsc(
                        principal.profissionalId());
        return dados(servicos);
    }

    public DadosServico buscar(UUID id, PrincipalAdministrativo principal) {
        Servico servico = obter(id);
        validarLeitura(servico, principal);
        return dados(List.of(servico)).getFirst();
    }

    public List<DadosServicoPublico> listarPublicos() {
        List<Servico> servicos = repositorioServico.listarPublicos();
        Map<UUID, Profissional> profissionais = profissionais(servicos);
        return servicos.stream().map(servico -> {
            Profissional profissional = profissionais.get(servico.getProfissionalId());
            return new DadosServicoPublico(
                    servico.getId(), servico.getNome(), servico.getDescricao(), servico.getDuracaoMinutos(),
                    servico.getPreco(), propriedades.getMoeda(), profissional.getId(), profissional.getNome());
        }).toList();
    }

    @Transactional
    public DadosServico criar(ComandoServico comando, PrincipalAdministrativo principal) {
        UUID profissionalId = resolverProfissional(comando.profissionalId(), principal);
        validarProfissional(profissionalId);
        validarGrade(comando);
        Map<UUID, RecursoOperacional> recursos = validarRecursos(comando.requisitosRecursos());
        Servico servico = Servico.criar(
                profissionalId,
                comando.nome(),
                comando.descricao(),
                comando.duracaoMinutos(),
                comando.intervaloLimpezaMinutos(),
                comando.preco(),
                comando.ativo(),
                comando.ordemExibicao());
        repositorioServico.saveAndFlush(servico);
        salvarRequisitos(servico.getId(), comando.requisitosRecursos());
        return dados(servico, recursos, comando.requisitosRecursos());
    }

    @Transactional
    public DadosServico alterar(UUID id, ComandoServico comando, PrincipalAdministrativo principal) {
        Servico servico = repositorioServico.bloquearPorId(id).orElseThrow(this::naoEncontrado);
        validarLeitura(servico, principal);
        UUID profissionalId = resolverProfissional(comando.profissionalId(), principal);
        validarProfissional(profissionalId);
        validarGrade(comando);
        Map<UUID, RecursoOperacional> recursos = validarRecursos(comando.requisitosRecursos());
        servico.alterar(
                profissionalId,
                comando.nome(),
                comando.descricao(),
                comando.duracaoMinutos(),
                comando.intervaloLimpezaMinutos(),
                comando.preco(),
                comando.ativo(),
                comando.ordemExibicao());
        repositorioServico.saveAndFlush(servico);
        repositorioRequisito.excluirPorServicoId(id);
        salvarRequisitos(id, comando.requisitosRecursos());
        return dados(servico, recursos, comando.requisitosRecursos());
    }

    private void validarGrade(ComandoServico comando) {
        int ocupado = comando.duracaoMinutos() + comando.intervaloLimpezaMinutos();
        if (ocupado % propriedades.getIntervaloGradeMinutos() != 0) {
            throw new ExcecaoRegraNegocio(
                    Tipo.ENTRADA_INVALIDA,
                    "DURACAO_INCOMPATIVEL_COM_GRADE",
                    "A duracao ocupada deve ser multipla da grade da instalacao");
        }
    }

    private Map<UUID, RecursoOperacional> validarRecursos(List<ComandoRequisitoRecurso> requisitos) {
        Set<UUID> ids = new HashSet<>();
        for (ComandoRequisitoRecurso requisito : requisitos) {
            if (!ids.add(requisito.recursoId())) {
                throw new ExcecaoRegraNegocio(
                        Tipo.ENTRADA_INVALIDA,
                        "RECURSO_REPETIDO_NO_SERVICO",
                        "Cada recurso pode aparecer uma unica vez no servico");
            }
        }
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<UUID> ordenados = ids.stream().sorted().toList();
        Map<UUID, RecursoOperacional> recursos = consultaRecurso.bloquearTodos(ordenados).stream()
                .collect(Collectors.toMap(RecursoOperacional::getId, Function.identity()));
        if (recursos.size() != ids.size()) {
            throw new ExcecaoRegraNegocio(
                    Tipo.ENTRADA_INVALIDA, "RECURSO_INVALIDO", "Um recurso informado nao existe");
        }
        for (ComandoRequisitoRecurso requisito : requisitos) {
            if (requisito.quantidade() > recursos.get(requisito.recursoId()).getCapacidade()) {
                throw new ExcecaoRegraNegocio(
                        Tipo.ENTRADA_INVALIDA,
                        "QUANTIDADE_SUPERIOR_A_CAPACIDADE",
                        "A quantidade exigida nao pode superar a capacidade do recurso");
            }
        }
        return recursos;
    }

    private void salvarRequisitos(UUID servicoId, List<ComandoRequisitoRecurso> requisitos) {
        if (requisitos.isEmpty()) {
            return;
        }
        repositorioRequisito.saveAllAndFlush(requisitos.stream()
                .map(requisito -> new RequisitoRecursoServico(
                        servicoId, requisito.recursoId(), requisito.quantidade()))
                .toList());
    }

    private UUID resolverProfissional(UUID solicitado, PrincipalAdministrativo principal) {
        if (proprietario(principal)) {
            if (solicitado == null) {
                throw new ExcecaoRegraNegocio(
                        Tipo.ENTRADA_INVALIDA,
                        "PROFISSIONAL_OBRIGATORIO",
                        "O profissional do servico e obrigatorio");
            }
            return solicitado;
        }
        if (solicitado != null && !solicitado.equals(principal.profissionalId())) {
            throw new AccessDeniedException("Profissional fora do escopo do funcionario");
        }
        return principal.profissionalId();
    }

    private void validarProfissional(UUID id) {
        if (consultaProfissional.obter(id) == null) {
            throw new ExcecaoRegraNegocio(
                    Tipo.ENTRADA_INVALIDA, "PROFISSIONAL_INVALIDO", "O profissional informado nao existe");
        }
    }

    private void validarLeitura(Servico servico, PrincipalAdministrativo principal) {
        if (!proprietario(principal) && !servico.getProfissionalId().equals(principal.profissionalId())) {
            throw naoEncontrado();
        }
    }

    private boolean proprietario(PrincipalAdministrativo principal) {
        return principal.papel() == PapelUsuarioAdministrativo.PROPRIETARIO;
    }

    private Servico obter(UUID id) {
        return repositorioServico.findById(id).orElseThrow(this::naoEncontrado);
    }

    private ExcecaoRegraNegocio naoEncontrado() {
        return new ExcecaoRegraNegocio(Tipo.NAO_ENCONTRADO, "SERVICO_NAO_ENCONTRADO", "Servico nao encontrado");
    }

    private List<DadosServico> dados(List<Servico> servicos) {
        if (servicos.isEmpty()) {
            return List.of();
        }
        Map<UUID, Profissional> profissionais = profissionais(servicos);
        List<RequisitoRecursoServico> requisitos = repositorioRequisito.findAllByServicoIdIn(
                servicos.stream().map(Servico::getId).toList());
        Map<UUID, RecursoOperacional> recursos = recursos(requisitos.stream()
                .map(RequisitoRecursoServico::getRecursoOperacionalId).toList());
        Map<UUID, List<RequisitoRecursoServico>> porServico = requisitos.stream()
                .collect(Collectors.groupingBy(RequisitoRecursoServico::getServicoId));
        return servicos.stream()
                .map(servico -> dados(servico, profissionais.get(servico.getProfissionalId()),
                        porServico.getOrDefault(servico.getId(), List.of()), recursos))
                .toList();
    }

    private DadosServico dados(
            Servico servico,
            Map<UUID, RecursoOperacional> recursos,
            List<ComandoRequisitoRecurso> requisitos) {
        Profissional profissional = consultaProfissional.obter(servico.getProfissionalId());
        List<DadosRequisitoRecurso> dadosRequisitos = requisitos.stream()
                .map(requisito -> dados(requisito, recursos.get(requisito.recursoId())))
                .sorted(Comparator.comparing(DadosRequisitoRecurso::recursoNome)
                        .thenComparing(DadosRequisitoRecurso::recursoId))
                .toList();
        return dados(servico, profissional, dadosRequisitos);
    }

    private DadosServico dados(
            Servico servico,
            Profissional profissional,
            List<RequisitoRecursoServico> requisitos,
            Map<UUID, RecursoOperacional> recursos) {
        List<DadosRequisitoRecurso> dadosRequisitos = requisitos.stream()
                .map(requisito -> dados(
                        new ComandoRequisitoRecurso(
                                requisito.getRecursoOperacionalId(), requisito.getQuantidade()),
                        recursos.get(requisito.getRecursoOperacionalId())))
                .sorted(Comparator.comparing(DadosRequisitoRecurso::recursoNome)
                        .thenComparing(DadosRequisitoRecurso::recursoId))
                .toList();
        return dados(servico, profissional, dadosRequisitos);
    }

    private DadosServico dados(
            Servico servico, Profissional profissional, List<DadosRequisitoRecurso> requisitos) {
        return new DadosServico(
                servico.getId(), servico.getProfissionalId(), profissional.getNome(), servico.getNome(),
                servico.getDescricao(), servico.getDuracaoMinutos(), servico.getIntervaloLimpezaMinutos(),
                servico.getPreco(), propriedades.getMoeda(), servico.isAtivo(), servico.getOrdemExibicao(), requisitos);
    }

    private DadosRequisitoRecurso dados(ComandoRequisitoRecurso requisito, RecursoOperacional recurso) {
        return new DadosRequisitoRecurso(
                recurso.getId(), recurso.getNome(), requisito.quantidade(), recurso.isAtivo());
    }

    private Map<UUID, Profissional> profissionais(Collection<Servico> servicos) {
        return consultaProfissional.obterTodos(servicos.stream().map(Servico::getProfissionalId).distinct().toList())
                .stream().collect(Collectors.toMap(Profissional::getId, Function.identity()));
    }

    private Map<UUID, RecursoOperacional> recursos(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return new HashMap<>();
        }
        return consultaRecurso.obterTodos(ids).stream()
                .collect(Collectors.toMap(RecursoOperacional::getId, Function.identity()));
    }
}
