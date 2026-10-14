package com.servicescheduling.api.agendamentos.aplicacao;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.servicescheduling.api.agenda.aplicacao.CalculadoraDisponibilidade;
import com.servicescheduling.api.agendamentos.dominio.Agendamento;
import com.servicescheduling.api.agendamentos.dominio.AlocacaoRecursoAgendamento;
import com.servicescheduling.api.agendamentos.infraestrutura.persistencia.DadosRegistroIdempotencia;
import com.servicescheduling.api.agendamentos.infraestrutura.persistencia.RepositorioAgendamento;
import com.servicescheduling.api.agendamentos.infraestrutura.persistencia.RepositorioAlocacaoRecursoAgendamento;
import com.servicescheduling.api.agendamentos.infraestrutura.persistencia.RepositorioRegistroIdempotencia;
import com.servicescheduling.api.clientes.aplicacao.ServicoClientes;
import com.servicescheduling.api.clientes.dominio.NumeroCelular;
import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio;
import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio.Tipo;
import com.servicescheduling.api.compartilhado.infraestrutura.configuracao.PropriedadesNegocio;
import com.servicescheduling.api.profissionais.aplicacao.ConsultaProfissional;
import com.servicescheduling.api.profissionais.dominio.Profissional;
import com.servicescheduling.api.recursos.aplicacao.ConsultaRecursoOperacional;
import com.servicescheduling.api.recursos.dominio.RecursoOperacional;
import com.servicescheduling.api.servicos.aplicacao.ConsultaServicoDisponibilidade;
import com.servicescheduling.api.servicos.aplicacao.DadosRequisitoDisponibilidade;
import com.servicescheduling.api.servicos.aplicacao.DadosServicoDisponibilidade;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
public class CriacaoAgendamento {

    private static final Pattern FORMATO_CHAVE = Pattern.compile("[A-Za-z0-9._~-]{16,128}");

    private final ConsultaServicoDisponibilidade consultaServico;
    private final ConsultaProfissional consultaProfissional;
    private final ConsultaRecursoOperacional consultaRecurso;
    private final CalculadoraDisponibilidade calculadoraDisponibilidade;
    private final ServicoClientes servicoClientes;
    private final RepositorioAgendamento repositorioAgendamento;
    private final RepositorioAlocacaoRecursoAgendamento repositorioAlocacao;
    private final RepositorioRegistroIdempotencia repositorioIdempotencia;
    private final PropriedadesNegocio propriedades;
    private final ObjectMapper objectMapper;

    public CriacaoAgendamento(
            ConsultaServicoDisponibilidade consultaServico,
            ConsultaProfissional consultaProfissional,
            ConsultaRecursoOperacional consultaRecurso,
            CalculadoraDisponibilidade calculadoraDisponibilidade,
            ServicoClientes servicoClientes,
            RepositorioAgendamento repositorioAgendamento,
            RepositorioAlocacaoRecursoAgendamento repositorioAlocacao,
            RepositorioRegistroIdempotencia repositorioIdempotencia,
            PropriedadesNegocio propriedades,
            ObjectMapper objectMapper) {
        this.consultaServico = consultaServico;
        this.consultaProfissional = consultaProfissional;
        this.consultaRecurso = consultaRecurso;
        this.calculadoraDisponibilidade = calculadoraDisponibilidade;
        this.servicoClientes = servicoClientes;
        this.repositorioAgendamento = repositorioAgendamento;
        this.repositorioAlocacao = repositorioAlocacao;
        this.repositorioIdempotencia = repositorioIdempotencia;
        this.propriedades = propriedades;
        this.objectMapper = objectMapper;
    }

    ComandoNormalizado normalizar(ComandoCriacaoAgendamento comando, String chaveIdempotencia) {
        if (chaveIdempotencia == null || !FORMATO_CHAVE.matcher(chaveIdempotencia).matches()) {
            throw new ExcecaoRegraNegocio(
                    Tipo.ENTRADA_INVALIDA, "FALHA_VALIDACAO",
                    "A chave de idempotencia possui formato invalido");
        }
        if (comando.servicoId() == null || comando.inicioEm() == null) {
            throw new ExcecaoRegraNegocio(
                    Tipo.ENTRADA_INVALIDA, "FALHA_VALIDACAO", "Servico e inicio sao obrigatorios");
        }
        String nome = comando.nomeCliente() == null ? "" : comando.nomeCliente().strip();
        if (nome.isEmpty() || nome.length() > 120) {
            throw new ExcecaoRegraNegocio(
                    Tipo.ENTRADA_INVALIDA, "FALHA_VALIDACAO", "O nome do cliente possui formato invalido");
        }
        String celular;
        try {
            celular = NumeroCelular.de(comando.celularCliente()).valor();
        } catch (IllegalArgumentException excecao) {
            throw new ExcecaoRegraNegocio(
                    Tipo.ENTRADA_INVALIDA, "FALHA_VALIDACAO", "O celular do cliente possui formato invalido");
        }
        String versao = comando.versaoAvisoPrivacidade() == null
                ? ""
                : comando.versaoAvisoPrivacidade().strip();
        if (versao.isEmpty() || versao.length() > 64 || !comando.cienteAvisoPrivacidade()) {
            throw new ExcecaoRegraNegocio(
                    Tipo.ENTRADA_INVALIDA, "FALHA_VALIDACAO",
                    "A versao e a ciencia do aviso de privacidade sao obrigatorias");
        }
        return new ComandoNormalizado(
                comando.servicoId(), comando.inicioEm().toInstant(), nome, celular, versao);
    }

    Optional<DadosRegistroIdempotencia> reivindicar(String chaveIdempotencia, Object conteudo, Instant agora) {
        String hashChave = hash(chaveIdempotencia);
        String hashRequisicao = hash(serializar(conteudo));
        repositorioIdempotencia.removerExpirados(hashChave, agora);
        if (repositorioIdempotencia.reivindicar(
                hashChave, hashRequisicao, agora, agora.plus(24, ChronoUnit.HOURS))) {
            return Optional.empty();
        }
        DadosRegistroIdempotencia registro = repositorioIdempotencia.buscar(hashChave)
                .orElseThrow(() -> new IllegalStateException("Registro idempotente nao encontrado"));
        if (!registro.hashRequisicao().equals(hashRequisicao)) {
            throw new ExcecaoRegraNegocio(
                    Tipo.CONFLITO,
                    "CONFLITO_IDEMPOTENCIA",
                    "A chave de idempotencia ja foi usada com outro conteudo");
        }
        if (registro.statusResposta() == null || registro.corpoResposta() == null
                || registro.agendamentoId() == null) {
            throw new IllegalStateException("Registro idempotente sem resultado");
        }
        return Optional.of(registro);
    }

    void concluirIdempotencia(String chaveIdempotencia, Object resposta, UUID agendamentoId) {
        repositorioIdempotencia.concluir(
                hash(chaveIdempotencia), HttpStatus.CREATED.value(), serializar(resposta), agendamentoId);
    }

    <T> T lerResposta(String corpo, Class<T> tipo) {
        try {
            return objectMapper.readValue(corpo, tipo);
        } catch (JacksonException excecao) {
            throw new IllegalStateException("Resultado idempotente invalido", excecao);
        }
    }

    ReservaCriada reservar(
            ComandoNormalizado comando,
            UUID autorId,
            UUID profissionalPermitido,
            Instant agora) {
        validarAviso(comando);
        DadosServicoDisponibilidade leituraInicial = consultaServico.obter(comando.servicoId());
        if (leituraInicial == null || !leituraInicial.ativo() || foraDoEscopo(leituraInicial, profissionalPermitido)) {
            throw servicoNaoEncontrado();
        }
        Profissional profissional = consultaProfissional.bloquear(leituraInicial.profissionalId());
        if (profissional == null || !profissional.isAtivo()) {
            throw servicoNaoEncontrado();
        }
        DadosServicoDisponibilidade servico = consultaServico.bloquear(comando.servicoId());
        if (servico == null || !servico.ativo()
                || !servico.profissionalId().equals(profissional.getId())
                || foraDoEscopo(servico, profissionalPermitido)) {
            throw conflitoAgenda();
        }

        List<UUID> recursoIds = servico.requisitos().stream()
                .map(DadosRequisitoDisponibilidade::recursoId)
                .sorted()
                .toList();
        Map<UUID, RecursoOperacional> recursos = recursoIds.isEmpty()
                ? Map.of()
                : consultaRecurso.bloquearTodos(recursoIds).stream()
                        .collect(Collectors.toMap(RecursoOperacional::getId, Function.identity()));
        boolean disponivel = autorId == null
                ? calculadoraDisponibilidade.horarioDisponivel(servico, comando.inicioEm())
                : calculadoraDisponibilidade.horarioDisponivelAdministrativo(servico, comando.inicioEm());
        if (!recursosValidos(servico.requisitos(), recursos) || !disponivel) {
            throw conflitoAgenda();
        }

        UUID clienteId = servicoClientes.localizarOuCriar(comando.nomeCliente(), comando.celularCliente(), agora);
        Instant fimServico = comando.inicioEm().plus(servico.duracaoMinutos(), ChronoUnit.MINUTES);
        Instant ocupadoAte = fimServico.plus(servico.intervaloLimpezaMinutos(), ChronoUnit.MINUTES);
        Agendamento agendamento = autorId == null
                ? Agendamento.criarPublico(
                        clienteId, servico.id(), servico.profissionalId(), comando.inicioEm(), fimServico,
                        ocupadoAte, servico.nome(), profissional.getNome(), servico.duracaoMinutos(),
                        servico.preco(), propriedades.getMoeda(), comando.nomeCliente(),
                        comando.celularCliente(), comando.versaoAvisoPrivacidade(), agora)
                : Agendamento.criarAdministrativo(
                        clienteId, servico.id(), servico.profissionalId(), comando.inicioEm(), fimServico,
                        ocupadoAte, servico.nome(), profissional.getNome(), servico.duracaoMinutos(),
                        servico.preco(), propriedades.getMoeda(), comando.nomeCliente(),
                        comando.celularCliente(), comando.versaoAvisoPrivacidade(), autorId, agora);
        repositorioAgendamento.saveAndFlush(agendamento);
        repositorioAlocacao.saveAllAndFlush(servico.requisitos().stream()
                .map(requisito -> new AlocacaoRecursoAgendamento(
                        agendamento.getId(), requisito.recursoId(), requisito.quantidade()))
                .toList());
        return new ReservaCriada(agendamento, servico, profissional, fimServico);
    }

    private boolean foraDoEscopo(DadosServicoDisponibilidade servico, UUID profissionalPermitido) {
        return profissionalPermitido != null && !profissionalPermitido.equals(servico.profissionalId());
    }

    private void validarAviso(ComandoNormalizado comando) {
        if (!propriedades.getVersaoAvisoPrivacidade().equals(comando.versaoAvisoPrivacidade())) {
            throw new ExcecaoRegraNegocio(
                    Tipo.CONFLITO,
                    "AVISO_PRIVACIDADE_DESATUALIZADO",
                    "O aviso de privacidade apresentado nao e mais o vigente");
        }
    }

    private boolean recursosValidos(
            List<DadosRequisitoDisponibilidade> requisitos,
            Map<UUID, RecursoOperacional> recursos) {
        return requisitos.stream().allMatch(requisito -> {
            RecursoOperacional recurso = recursos.get(requisito.recursoId());
            return recurso != null && recurso.isAtivo() && requisito.quantidade() <= recurso.getCapacidade();
        });
    }

    private String serializar(Object valor) {
        try {
            return objectMapper.writeValueAsString(valor);
        } catch (JacksonException excecao) {
            throw new IllegalStateException("Nao foi possivel serializar o resultado", excecao);
        }
    }

    private String hash(String valor) {
        try {
            byte[] resultado = MessageDigest.getInstance("SHA-256")
                    .digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resultado);
        } catch (NoSuchAlgorithmException excecao) {
            throw new IllegalStateException("SHA-256 indisponivel", excecao);
        }
    }

    private ExcecaoRegraNegocio servicoNaoEncontrado() {
        return new ExcecaoRegraNegocio(
                Tipo.NAO_ENCONTRADO, "SERVICO_NAO_ENCONTRADO", "Servico nao encontrado");
    }

    private ExcecaoRegraNegocio conflitoAgenda() {
        return new ExcecaoRegraNegocio(
                Tipo.CONFLITO, "CONFLITO_AGENDAMENTO", "O horario solicitado nao esta mais disponivel");
    }

    record ComandoNormalizado(
            UUID servicoId,
            Instant inicioEm,
            String nomeCliente,
            String celularCliente,
            String versaoAvisoPrivacidade) {
    }

    record ReservaCriada(
            Agendamento agendamento,
            DadosServicoDisponibilidade servico,
            Profissional profissional,
            Instant fimServico) {
    }
}
