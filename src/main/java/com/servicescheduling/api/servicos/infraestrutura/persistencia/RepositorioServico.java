package com.servicescheduling.api.servicos.infraestrutura.persistencia;

import com.servicescheduling.api.servicos.dominio.Servico;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RepositorioServico extends JpaRepository<Servico, UUID> {

    List<Servico> findAllByOrderByOrdemExibicaoAscNomeAscIdAsc();

    List<Servico> findAllByProfissionalIdOrderByOrdemExibicaoAscNomeAscIdAsc(UUID profissionalId);

    @Query("""
            select s from Servico s, Profissional p
             where s.profissionalId = p.id and s.ativo = true and p.ativo = true
             order by s.ordemExibicao, s.nome, s.id
            """)
    List<Servico> listarPublicos();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Servico s where s.id = :id")
    Optional<Servico> bloquearPorId(@Param("id") UUID id);
}
