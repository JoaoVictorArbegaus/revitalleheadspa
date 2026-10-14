package com.servicescheduling.api.agendamentos.aplicacao;

import com.servicescheduling.api.acesso.dominio.PapelUsuarioAdministrativo;
import com.servicescheduling.api.acesso.infraestrutura.seguranca.PrincipalAdministrativo;
import com.servicescheduling.api.agendamentos.aplicacao.CriacaoAgendamento.ComandoNormalizado;
import com.servicescheduling.api.agendamentos.aplicacao.CriacaoAgendamento.ReservaCriada;
import com.servicescheduling.api.agendamentos.dominio.Agendamento;
import com.servicescheduling.api.agendamentos.dominio.EstadoAgendamento;
import com.servicescheduling.api.agendamentos.infraestrutura.persistencia.ConsultaAgendamentosAdministrativos;
import com.servicescheduling.api.agendamentos.infraestrutura.persistencia.DadosRegistroIdempotencia;
import com.servicescheduling.api.agendamentos.infraestrutura.persistencia.RepositorioAgendamento;
import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio;
import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio.Tipo;
import com.servicescheduling.api.compartilhado.infraestrutura.configuracao.PropriedadesNegocio;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ServicoAgendamentosAdministrativos {

    private final CriacaoAgendamento criacao;
    private final RepositorioAgendamento repositorio;
    private final ConsultaAgendamentosAdministrativos consulta;
    private final Clock relogio;
    private final ZoneId fusoHorario;

    public ServicoAgendamentosAdministrativos(
            CriacaoAgendamento criacao,
            RepositorioAgendamento repositorio,
            ConsultaAgendamentosAdministrativos consulta,
            Clock relogio,
            PropriedadesNegocio propriedades) {
        this.criacao = criacao;
        this.repositorio = repositorio;
        this.consulta = consulta;
        this.relogio = relogio;
        this.fusoHorario = propriedades.getFusoHorario();
    }

    public List<DadosAgendamentoAdministrativo> listar(
            LocalDate dataInicial,
            LocalDate dataFinal,
            UUID profissionalSolicitado,
            EstadoAgendamento estado,
            PrincipalAdministrativo principal) {
        if (dataInicial == null || dataFinal == null || dataFinal.isBefore(dataInicial)
                || ChronoUnit.DAYS.between(dataInicial, dataFinal) + 1 > 31) {
            throw new ExcecaoRegraNegocio(
                    Tipo.ENTRADA_INVALIDA,
                    "PERIODO_CONSULTA_INVALIDO",
                    "O periodo deve conter de uma a 31 datas locais inclusivas");
        }
        UUID profissionalId = proprietario(principal) ? profissionalSolicitado : principal.profissionalId();
        return consulta.listar(
                dataInicial.atStartOfDay(fusoHorario).toInstant(),
                dataFinal.plusDays(1).atStartOfDay(fusoHorario).toInstant(),
                profissionalId,
                estado,
                fusoHorario);
    }

    public DadosAgendamentoAdministrativo obter(UUID id, PrincipalAdministrativo principal) {
        return consulta.buscar(id, fusoHorario)
                .filter(dados -> noEscopo(dados.profissional().id(), principal))
                .orElseThrow(this::agendamentoNaoEncontrado);
    }

    @Transactional
    public DadosAgendamentoAdministrativo criar(
            ComandoCriacaoAgendamento comando,
            String chaveIdempotencia,
            PrincipalAdministrativo principal) {
        ComandoNormalizado normalizado = criacao.normalizar(comando, chaveIdempotencia);
        Instant agora = relogio.instant();
        Optional<DadosRegistroIdempotencia> anterior = criacao.reivindicar(
                chaveIdempotencia, new ConteudoAdministrativo(principal.id(), normalizado), agora);
        if (anterior.isPresent()) {
            return obter(anterior.get().agendamentoId(), principal);
        }
        if (normalizado.inicioEm().isBefore(agora)) {
            throw new ExcecaoRegraNegocio(
                    Tipo.ENTRADA_INVALIDA, "INICIO_NO_PASSADO", "O inicio da reserva nao pode estar no passado");
        }

        UUID profissionalPermitido = proprietario(principal) ? null : principal.profissionalId();
        ReservaCriada reserva = criacao.reservar(normalizado, principal.id(), profissionalPermitido, agora);
        UUID agendamentoId = reserva.agendamento().getId();
        criacao.concluirIdempotencia(chaveIdempotencia, new ReferenciaAgendamento(agendamentoId), agendamentoId);
        return obter(agendamentoId, principal);
    }

    @Transactional
    public DadosAgendamentoAdministrativo cancelar(UUID id, String motivo, PrincipalAdministrativo principal) {
        String motivoNormalizado = motivo == null ? "" : motivo.strip();
        if (motivoNormalizado.isEmpty() || motivoNormalizado.length() > 500) {
            throw new ExcecaoRegraNegocio(
                    Tipo.ENTRADA_INVALIDA, "FALHA_VALIDACAO", "O motivo deve possuir de 1 a 500 caracteres");
        }
        Instant agora = relogio.instant();
        Agendamento agendamento = bloquear(id, principal);
        if (!agendamento.podeCancelar(agora)) {
            throw transicaoInvalida();
        }
        agendamento.cancelar(motivoNormalizado, principal.id(), agora);
        repositorio.saveAndFlush(agendamento);
        return obter(id, principal);
    }

    @Transactional
    public DadosAgendamentoAdministrativo encerrar(
            UUID id, EstadoAgendamento destino, PrincipalAdministrativo principal) {
        Instant agora = relogio.instant();
        Agendamento agendamento = bloquear(id, principal);
        if (!agendamento.podeEncerrar(agora)) {
            throw transicaoInvalida();
        }
        agendamento.encerrar(destino, principal.id(), agora);
        repositorio.saveAndFlush(agendamento);
        return obter(id, principal);
    }

    private Agendamento bloquear(UUID id, PrincipalAdministrativo principal) {
        return repositorio.bloquearPorId(id)
                .filter(agendamento -> noEscopo(agendamento.getProfissionalId(), principal))
                .orElseThrow(this::agendamentoNaoEncontrado);
    }

    private boolean noEscopo(UUID profissionalId, PrincipalAdministrativo principal) {
        return proprietario(principal) || profissionalId.equals(principal.profissionalId());
    }

    private boolean proprietario(PrincipalAdministrativo principal) {
        return principal.papel() == PapelUsuarioAdministrativo.PROPRIETARIO;
    }

    private ExcecaoRegraNegocio agendamentoNaoEncontrado() {
        return new ExcecaoRegraNegocio(
                Tipo.NAO_ENCONTRADO, "AGENDAMENTO_NAO_ENCONTRADO", "Agendamento nao encontrado");
    }

    private ExcecaoRegraNegocio transicaoInvalida() {
        return new ExcecaoRegraNegocio(
                Tipo.CONFLITO,
                "TRANSICAO_AGENDAMENTO_INVALIDA",
                "A operacao nao e permitida no estado ou horario atual da reserva");
    }

    private record ConteudoAdministrativo(UUID autorId, ComandoNormalizado comando) {
    }

    private record ReferenciaAgendamento(UUID agendamentoId) {
    }
}
