package com.servicescheduling.api.agenda.aplicacao;

import com.servicescheduling.api.agenda.dominio.BloqueioAgenda;
import com.servicescheduling.api.agenda.dominio.DiaSemana;
import com.servicescheduling.api.agenda.dominio.ExpedienteSemanal;
import com.servicescheduling.api.agenda.infraestrutura.persistencia.RepositorioBloqueioAgenda;
import com.servicescheduling.api.agenda.infraestrutura.persistencia.RepositorioExpedienteSemanal;
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
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CalculadoraDisponibilidade {

    private final ConsultaServicoDisponibilidade consultaServico;
    private final ConsultaProfissional consultaProfissional;
    private final ConsultaRecursoOperacional consultaRecurso;
    private final RepositorioExpedienteSemanal repositorioExpediente;
    private final RepositorioBloqueioAgenda repositorioBloqueio;
    private final Clock relogio;
    private final ZoneId fusoHorario;
    private final int intervaloGradeMinutos;
    private final int antecedenciaMinimaMinutos;
    private final int horizonteDias;

    public CalculadoraDisponibilidade(
            ConsultaServicoDisponibilidade consultaServico,
            ConsultaProfissional consultaProfissional,
            ConsultaRecursoOperacional consultaRecurso,
            RepositorioExpedienteSemanal repositorioExpediente,
            RepositorioBloqueioAgenda repositorioBloqueio,
            Clock relogio,
            PropriedadesNegocio propriedades) {
        this.consultaServico = consultaServico;
        this.consultaProfissional = consultaProfissional;
        this.consultaRecurso = consultaRecurso;
        this.repositorioExpediente = repositorioExpediente;
        this.repositorioBloqueio = repositorioBloqueio;
        this.relogio = relogio;
        this.fusoHorario = propriedades.getFusoHorario();
        this.intervaloGradeMinutos = propriedades.getIntervaloGradeMinutos();
        this.antecedenciaMinimaMinutos = propriedades.getAntecedenciaMinimaReservaMinutos();
        this.horizonteDias = propriedades.getHorizonteReservaDias();
    }

    public DadosDisponibilidade calcular(UUID servicoId, LocalDate dataInicial, LocalDate dataFinal) {
        validarPeriodo(dataInicial, dataFinal);
        DadosServicoDisponibilidade servico = consultaServico.obter(servicoId);
        Profissional profissional = servico == null ? null : consultaProfissional.obter(servico.profissionalId());
        if (servico == null || !servico.ativo() || profissional == null || !profissional.isAtivo()) {
            throw new ExcecaoRegraNegocio(
                    Tipo.NAO_ENCONTRADO, "SERVICO_NAO_ENCONTRADO", "Servico nao encontrado");
        }

        if (!recursosDisponiveis(servico.requisitos())) {
            return respostaVazia(servicoId, dataInicial, dataFinal);
        }

        List<ExpedienteSemanal> expediente = repositorioExpediente.findAllByProfissionalId(servico.profissionalId());
        if (expediente.isEmpty()) {
            return respostaVazia(servicoId, dataInicial, dataFinal);
        }

        Instant inicioPeriodo = dataInicial.atStartOfDay(fusoHorario).toInstant();
        Instant fimPeriodo = dataFinal.plusDays(1).atStartOfDay(fusoHorario).toInstant();
        List<BloqueioAgenda> bloqueios = repositorioBloqueio
                .findAllByProfissionalIdAndInicioEmLessThanAndFimEmGreaterThanOrderByInicioEmAsc(
                        servico.profissionalId(), fimPeriodo, inicioPeriodo);

        Map<DiaSemana, List<ExpedienteSemanal>> expedientePorDia = new HashMap<>();
        for (ExpedienteSemanal janela : expediente) {
            expedientePorDia.computeIfAbsent(janela.getDiaSemana(), chave -> new ArrayList<>()).add(janela);
        }
        expedientePorDia.values().forEach(janelas -> janelas.sort(Comparator.comparing(ExpedienteSemanal::getInicio)));

        Instant primeiroInicio = relogio.instant().plus(antecedenciaMinimaMinutos, ChronoUnit.MINUTES);
        LocalDate hoje = LocalDate.now(relogio.withZone(fusoHorario));
        LocalDate ultimaData = hoje.plusDays(horizonteDias - 1L);
        int ocupacaoMinutos = servico.duracaoMinutos() + servico.intervaloLimpezaMinutos();
        List<DadosDiaDisponivel> dias = new ArrayList<>();

        for (LocalDate data = dataInicial; !data.isAfter(dataFinal); data = data.plusDays(1)) {
            if (data.isBefore(hoje) || data.isAfter(ultimaData)) {
                continue;
            }
            List<DadosHorarioDisponivel> horarios = calcularDia(
                    data,
                    expedientePorDia.getOrDefault(DiaSemana.de(data.getDayOfWeek()), List.of()),
                    bloqueios,
                    servico.duracaoMinutos(),
                    ocupacaoMinutos,
                    primeiroInicio);
            if (!horarios.isEmpty()) {
                dias.add(new DadosDiaDisponivel(data, horarios));
            }
        }

        return new DadosDisponibilidade(
                servicoId, fusoHorario.getId(), dataInicial, dataFinal, List.copyOf(dias));
    }

    private List<DadosHorarioDisponivel> calcularDia(
            LocalDate data,
            List<ExpedienteSemanal> janelas,
            List<BloqueioAgenda> bloqueios,
            int duracaoServicoMinutos,
            int ocupacaoMinutos,
            Instant primeiroInicio) {
        List<DadosHorarioDisponivel> horarios = new ArrayList<>();
        for (ExpedienteSemanal janela : janelas) {
            LocalTime inicioAlinhado = alinhar(janela.getInicio());
            if (inicioAlinhado == null) {
                continue;
            }
            Instant inicioJanela = resolverLimite(data.atTime(janela.getInicio()), false);
            Instant fimJanela = resolverLimite(data.atTime(janela.getFim()), true);
            for (LocalDateTime candidatoLocal = data.atTime(inicioAlinhado);
                    candidatoLocal.toLocalTime().isBefore(janela.getFim()) && candidatoLocal.toLocalDate().equals(data);
                    candidatoLocal = candidatoLocal.plusMinutes(intervaloGradeMinutos)) {
                List<ZoneOffset> offsets = fusoHorario.getRules().getValidOffsets(candidatoLocal);
                for (ZoneOffset offset : offsets) {
                    ZonedDateTime candidato = ZonedDateTime.ofLocal(candidatoLocal, fusoHorario, offset);
                    Instant inicio = candidato.toInstant();
                    Instant ocupadoAte = inicio.plus(ocupacaoMinutos, ChronoUnit.MINUTES);
                    if (inicio.isBefore(inicioJanela) || ocupadoAte.isAfter(fimJanela)
                            || inicio.isBefore(primeiroInicio) || sobrepoeBloqueio(inicio, ocupadoAte, bloqueios)) {
                        continue;
                    }
                    horarios.add(new DadosHorarioDisponivel(
                            candidato.toOffsetDateTime(),
                            inicio.plus(duracaoServicoMinutos, ChronoUnit.MINUTES)
                                    .atZone(fusoHorario)
                                    .toOffsetDateTime()));
                }
            }
        }
        horarios.sort(Comparator.comparing(DadosHorarioDisponivel::inicioEm));
        return List.copyOf(horarios);
    }

    private Instant resolverLimite(LocalDateTime horarioLocal, boolean usarMaisTardio) {
        List<ZoneOffset> offsets = fusoHorario.getRules().getValidOffsets(horarioLocal);
        if (offsets.isEmpty()) {
            return horarioLocal.atZone(fusoHorario).toInstant();
        }
        return offsets.stream()
                .map(offset -> ZonedDateTime.ofLocal(horarioLocal, fusoHorario, offset).toInstant())
                .reduce(usarMaisTardio
                        ? (primeiro, segundo) -> primeiro.isAfter(segundo) ? primeiro : segundo
                        : (primeiro, segundo) -> primeiro.isBefore(segundo) ? primeiro : segundo)
                .orElseThrow();
    }

    private LocalTime alinhar(LocalTime inicio) {
        int minutoDoDia = inicio.getHour() * 60 + inicio.getMinute();
        int resto = minutoDoDia % intervaloGradeMinutos;
        int alinhado = resto == 0 ? minutoDoDia : minutoDoDia + intervaloGradeMinutos - resto;
        if (alinhado >= 24 * 60) {
            return null;
        }
        return LocalTime.of(alinhado / 60, alinhado % 60);
    }

    private boolean sobrepoeBloqueio(Instant inicio, Instant fim, List<BloqueioAgenda> bloqueios) {
        return bloqueios.stream()
                .anyMatch(bloqueio -> inicio.isBefore(bloqueio.getFimEm()) && bloqueio.getInicioEm().isBefore(fim));
    }

    private boolean recursosDisponiveis(List<DadosRequisitoDisponibilidade> requisitos) {
        if (requisitos.isEmpty()) {
            return true;
        }
        Map<UUID, RecursoOperacional> recursos = new HashMap<>();
        for (RecursoOperacional recurso : consultaRecurso.obterTodos(
                requisitos.stream().map(DadosRequisitoDisponibilidade::recursoId).toList())) {
            recursos.put(recurso.getId(), recurso);
        }
        return requisitos.stream().allMatch(requisito -> {
            RecursoOperacional recurso = recursos.get(requisito.recursoId());
            return recurso != null && recurso.isAtivo() && recurso.getCapacidade() >= requisito.quantidade();
        });
    }

    private void validarPeriodo(LocalDate dataInicial, LocalDate dataFinal) {
        if (dataInicial == null || dataFinal == null || dataFinal.isBefore(dataInicial)
                || ChronoUnit.DAYS.between(dataInicial, dataFinal) + 1 > 31) {
            throw new ExcecaoRegraNegocio(
                    Tipo.ENTRADA_INVALIDA,
                    "PERIODO_CONSULTA_INVALIDO",
                    "O periodo deve conter de uma a 31 datas locais inclusivas");
        }
    }

    private DadosDisponibilidade respostaVazia(UUID servicoId, LocalDate dataInicial, LocalDate dataFinal) {
        return new DadosDisponibilidade(servicoId, fusoHorario.getId(), dataInicial, dataFinal, List.of());
    }
}
