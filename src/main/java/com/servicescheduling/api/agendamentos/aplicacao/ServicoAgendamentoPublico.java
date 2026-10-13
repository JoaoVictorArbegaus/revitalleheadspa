package com.servicescheduling.api.agendamentos.aplicacao;

import com.servicescheduling.api.agendamentos.aplicacao.CriacaoAgendamento.ComandoNormalizado;
import com.servicescheduling.api.agendamentos.aplicacao.CriacaoAgendamento.ReservaCriada;
import com.servicescheduling.api.agendamentos.aplicacao.DadosConfirmacaoAgendamento.ProfissionalConfirmado;
import com.servicescheduling.api.agendamentos.aplicacao.DadosConfirmacaoAgendamento.ServicoConfirmado;
import com.servicescheduling.api.agendamentos.dominio.EstadoAgendamento;
import com.servicescheduling.api.agendamentos.infraestrutura.persistencia.DadosRegistroIdempotencia;
import com.servicescheduling.api.compartilhado.infraestrutura.configuracao.PropriedadesNegocio;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ServicoAgendamentoPublico {

    private final CriacaoAgendamento criacao;
    private final PropriedadesNegocio propriedades;
    private final Clock relogio;

    public ServicoAgendamentoPublico(CriacaoAgendamento criacao, PropriedadesNegocio propriedades, Clock relogio) {
        this.criacao = criacao;
        this.propriedades = propriedades;
        this.relogio = relogio;
    }

    @Transactional
    public DadosConfirmacaoAgendamento criar(ComandoCriacaoAgendamento comando, String chaveIdempotencia) {
        ComandoNormalizado normalizado = criacao.normalizar(comando, chaveIdempotencia);
        Instant agora = relogio.instant();
        Optional<DadosRegistroIdempotencia> anterior = criacao.reivindicar(chaveIdempotencia, normalizado, agora);
        if (anterior.isPresent()) {
            return criacao.lerResposta(anterior.get().corpoResposta(), DadosConfirmacaoAgendamento.class);
        }

        ReservaCriada reserva = criacao.reservar(normalizado, null, null, agora);
        ZoneId fuso = propriedades.getFusoHorario();
        DadosConfirmacaoAgendamento resposta = new DadosConfirmacaoAgendamento(
                EstadoAgendamento.CONFIRMADO,
                new ServicoConfirmado(reserva.servico().id(), reserva.servico().nome(),
                        reserva.servico().duracaoMinutos()),
                new ProfissionalConfirmado(reserva.profissional().getId(), reserva.profissional().getNome()),
                normalizado.inicioEm().atZone(fuso).toOffsetDateTime(),
                reserva.fimServico().atZone(fuso).toOffsetDateTime(),
                fuso.getId(),
                reserva.servico().preco(),
                propriedades.getMoeda());
        criacao.concluirIdempotencia(chaveIdempotencia, resposta, reserva.agendamento().getId());
        return resposta;
    }
}
