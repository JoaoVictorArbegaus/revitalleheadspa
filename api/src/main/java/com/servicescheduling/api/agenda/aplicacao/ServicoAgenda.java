package com.servicescheduling.api.agenda.aplicacao;

import com.servicescheduling.api.acesso.dominio.PapelUsuarioAdministrativo;
import com.servicescheduling.api.acesso.infraestrutura.seguranca.PrincipalAdministrativo;
import com.servicescheduling.api.agenda.dominio.BloqueioAgenda;
import com.servicescheduling.api.agenda.dominio.ExpedienteSemanal;
import com.servicescheduling.api.agenda.infraestrutura.persistencia.RepositorioBloqueioAgenda;
import com.servicescheduling.api.agenda.infraestrutura.persistencia.RepositorioExpedienteSemanal;
import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio;
import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio.Tipo;
import com.servicescheduling.api.compartilhado.infraestrutura.configuracao.PropriedadesNegocio;
import com.servicescheduling.api.profissionais.aplicacao.ConsultaProfissional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ServicoAgenda {

    private static final Comparator<ComandoJanelaExpediente> ORDEM_JANELAS = Comparator
            .comparing((ComandoJanelaExpediente janela) -> janela.diaSemana().ordinal())
            .thenComparing(ComandoJanelaExpediente::inicio);

    private final RepositorioExpedienteSemanal repositorioExpediente;
    private final RepositorioBloqueioAgenda repositorioBloqueio;
    private final ConsultaProfissional consultaProfissional;
    private final ZoneId fusoHorario;

    public ServicoAgenda(
            RepositorioExpedienteSemanal repositorioExpediente,
            RepositorioBloqueioAgenda repositorioBloqueio,
            ConsultaProfissional consultaProfissional,
            PropriedadesNegocio propriedades) {
        this.repositorioExpediente = repositorioExpediente;
        this.repositorioBloqueio = repositorioBloqueio;
        this.consultaProfissional = consultaProfissional;
        this.fusoHorario = propriedades.getFusoHorario();
    }

    public DadosExpedienteSemanal consultarExpediente(
            UUID profissionalSolicitado, PrincipalAdministrativo principal) {
        UUID profissionalId = resolverConsulta(profissionalSolicitado, principal);
        validarProfissional(profissionalId);
        return dadosExpediente(profissionalId, repositorioExpediente.findAllByProfissionalId(profissionalId));
    }

    @Transactional
    public DadosExpedienteSemanal substituirExpediente(
            UUID profissionalSolicitado,
            List<ComandoJanelaExpediente> janelas,
            PrincipalAdministrativo principal) {
        UUID profissionalId = resolverComando(profissionalSolicitado, principal);
        bloquearProfissional(profissionalId);
        List<ComandoJanelaExpediente> ordenadas = validarJanelas(janelas);

        repositorioExpediente.excluirTodosPorProfissionalId(profissionalId);
        List<ExpedienteSemanal> persistidas = repositorioExpediente.saveAll(ordenadas.stream()
                .map(janela -> ExpedienteSemanal.criar(
                        profissionalId, janela.diaSemana(), janela.inicio(), janela.fim()))
                .toList());
        return dadosExpediente(profissionalId, persistidas);
    }

    public List<DadosBloqueioAgenda> listarBloqueios(
            UUID profissionalSolicitado,
            LocalDate dataInicial,
            LocalDate dataFinal,
            PrincipalAdministrativo principal) {
        validarPeriodo(dataInicial, dataFinal);
        UUID profissionalId = resolverConsulta(profissionalSolicitado, principal);
        validarProfissional(profissionalId);
        Instant inicio = dataInicial.atStartOfDay(fusoHorario).toInstant();
        Instant fim = dataFinal.plusDays(1).atStartOfDay(fusoHorario).toInstant();
        return repositorioBloqueio
                .findAllByProfissionalIdAndInicioEmLessThanAndFimEmGreaterThanOrderByInicioEmAsc(
                        profissionalId, fim, inicio)
                .stream()
                .map(this::dadosBloqueio)
                .toList();
    }

    @Transactional
    public DadosBloqueioAgenda criarBloqueio(
            UUID profissionalSolicitado,
            OffsetDateTime inicioEm,
            OffsetDateTime fimEm,
            PrincipalAdministrativo principal) {
        UUID profissionalId = resolverComando(profissionalSolicitado, principal);
        Instant inicio = inicioEm.toInstant();
        Instant fim = fimEm.toInstant();
        if (!inicio.isBefore(fim)) {
            throw entradaInvalida("INTERVALO_BLOQUEIO_INVALIDO", "O inicio do bloqueio deve ser anterior ao fim");
        }
        bloquearProfissional(profissionalId);
        if (repositorioBloqueio.existsByProfissionalIdAndInicioEmLessThanAndFimEmGreaterThan(
                profissionalId, fim, inicio)) {
            throw entradaInvalida("BLOQUEIO_SOBREPOSTO", "O bloqueio se sobrepoe a outro bloqueio");
        }
        return dadosBloqueio(repositorioBloqueio.save(BloqueioAgenda.criar(profissionalId, inicio, fim)));
    }

    @Transactional
    public void excluirBloqueio(UUID id, PrincipalAdministrativo principal) {
        BloqueioAgenda bloqueio = repositorioBloqueio.findById(id).orElseThrow(this::bloqueioNaoEncontrado);
        validarEscopo(bloqueio.getProfissionalId(), principal);
        bloquearProfissional(bloqueio.getProfissionalId());
        BloqueioAgenda atual = repositorioBloqueio.findById(id).orElseThrow(this::bloqueioNaoEncontrado);
        repositorioBloqueio.delete(atual);
    }

    private List<ComandoJanelaExpediente> validarJanelas(List<ComandoJanelaExpediente> janelas) {
        if (janelas == null) {
            throw entradaInvalida("EXPEDIENTE_INVALIDO", "As janelas do expediente sao obrigatorias");
        }
        List<ComandoJanelaExpediente> ordenadas = new ArrayList<>(janelas);
        for (ComandoJanelaExpediente janela : ordenadas) {
            if (janela == null || janela.diaSemana() == null || janela.inicio() == null || janela.fim() == null
                    || janela.inicio().getSecond() != 0 || janela.inicio().getNano() != 0
                    || janela.fim().getSecond() != 0 || janela.fim().getNano() != 0
                    || !janela.inicio().isBefore(janela.fim())) {
                throw entradaInvalida(
                        "INTERVALO_EXPEDIENTE_INVALIDO",
                        "Cada janela deve possuir dia e inicio anterior ao fim no mesmo dia");
            }
        }
        ordenadas.sort(ORDEM_JANELAS);
        for (int indice = 1; indice < ordenadas.size(); indice++) {
            ComandoJanelaExpediente anterior = ordenadas.get(indice - 1);
            ComandoJanelaExpediente atual = ordenadas.get(indice);
            if (anterior.diaSemana() == atual.diaSemana() && atual.inicio().isBefore(anterior.fim())) {
                throw entradaInvalida("EXPEDIENTE_SOBREPOSTO", "As janelas do expediente nao podem se sobrepor");
            }
        }
        return List.copyOf(ordenadas);
    }

    private void validarPeriodo(LocalDate dataInicial, LocalDate dataFinal) {
        if (dataInicial == null || dataFinal == null || dataFinal.isBefore(dataInicial)
                || ChronoUnit.DAYS.between(dataInicial, dataFinal) + 1 > 31) {
            throw entradaInvalida(
                    "PERIODO_CONSULTA_INVALIDO",
                    "O periodo deve conter de uma a 31 datas locais inclusivas");
        }
    }

    private UUID resolverConsulta(UUID solicitado, PrincipalAdministrativo principal) {
        if (proprietario(principal)) {
            return profissionalObrigatorio(solicitado);
        }
        return principal.profissionalId();
    }

    private UUID resolverComando(UUID solicitado, PrincipalAdministrativo principal) {
        if (proprietario(principal)) {
            return profissionalObrigatorio(solicitado);
        }
        if (solicitado != null && !solicitado.equals(principal.profissionalId())) {
            throw new AccessDeniedException("Profissional fora do escopo do funcionario");
        }
        return principal.profissionalId();
    }

    private UUID profissionalObrigatorio(UUID solicitado) {
        if (solicitado == null) {
            throw entradaInvalida("PROFISSIONAL_OBRIGATORIO", "O profissional e obrigatorio");
        }
        return solicitado;
    }

    private void validarProfissional(UUID profissionalId) {
        if (consultaProfissional.obter(profissionalId) == null) {
            throw profissionalNaoEncontrado();
        }
    }

    private void bloquearProfissional(UUID profissionalId) {
        if (consultaProfissional.bloquear(profissionalId) == null) {
            throw profissionalNaoEncontrado();
        }
    }

    private void validarEscopo(UUID profissionalId, PrincipalAdministrativo principal) {
        if (!proprietario(principal) && !profissionalId.equals(principal.profissionalId())) {
            throw bloqueioNaoEncontrado();
        }
    }

    private boolean proprietario(PrincipalAdministrativo principal) {
        return principal.papel() == PapelUsuarioAdministrativo.PROPRIETARIO;
    }

    private DadosExpedienteSemanal dadosExpediente(UUID profissionalId, List<ExpedienteSemanal> janelas) {
        List<DadosJanelaExpediente> dados = janelas.stream()
                .sorted(Comparator.comparing((ExpedienteSemanal janela) -> janela.getDiaSemana().ordinal())
                        .thenComparing(ExpedienteSemanal::getInicio))
                .map(janela -> new DadosJanelaExpediente(
                        janela.getDiaSemana(), janela.getInicio(), janela.getFim()))
                .toList();
        return new DadosExpedienteSemanal(profissionalId, dados);
    }

    private DadosBloqueioAgenda dadosBloqueio(BloqueioAgenda bloqueio) {
        return new DadosBloqueioAgenda(
                bloqueio.getId(),
                bloqueio.getProfissionalId(),
                bloqueio.getInicioEm().atZone(fusoHorario).toOffsetDateTime(),
                bloqueio.getFimEm().atZone(fusoHorario).toOffsetDateTime());
    }

    private ExcecaoRegraNegocio entradaInvalida(String codigo, String mensagem) {
        return new ExcecaoRegraNegocio(Tipo.ENTRADA_INVALIDA, codigo, mensagem);
    }

    private ExcecaoRegraNegocio profissionalNaoEncontrado() {
        return new ExcecaoRegraNegocio(
                Tipo.NAO_ENCONTRADO, "PROFISSIONAL_NAO_ENCONTRADO", "Profissional nao encontrado");
    }

    private ExcecaoRegraNegocio bloqueioNaoEncontrado() {
        return new ExcecaoRegraNegocio(
                Tipo.NAO_ENCONTRADO, "BLOQUEIO_NAO_ENCONTRADO", "Bloqueio nao encontrado");
    }
}
