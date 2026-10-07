package com.servicescheduling.api.acesso.infraestrutura.persistencia;

import com.servicescheduling.api.acesso.dominio.PapelUsuarioAdministrativo;
import com.servicescheduling.api.acesso.dominio.UsuarioAdministrativo;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface RepositorioUsuarioAdministrativo extends JpaRepository<UsuarioAdministrativo, UUID> {

    Optional<UsuarioAdministrativo> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByProfissionalId(UUID profissionalId);

    List<UsuarioAdministrativo> findAllByOrderByNomeAscIdAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UsuarioAdministrativo u where u.papel = :papel and u.ativo = true order by u.id")
    List<UsuarioAdministrativo> bloquearAtivosPorPapel(PapelUsuarioAdministrativo papel);
}
