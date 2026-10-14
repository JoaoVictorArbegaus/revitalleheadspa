package com.servicescheduling.api.acesso.infraestrutura.seguranca;

import java.util.UUID;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.stereotype.Component;

@Component
public class GerenciadorSessoesAdministrativas {

    private final SessionRegistry registro;

    public GerenciadorSessoesAdministrativas(SessionRegistry registro) {
        this.registro = registro;
    }

    public void invalidar(UUID usuarioId) {
        for (Object principal : registro.getAllPrincipals()) {
            if (principal instanceof PrincipalAdministrativo administrativo
                    && administrativo.id().equals(usuarioId)) {
                for (SessionInformation sessao : registro.getAllSessions(principal, false)) {
                    sessao.expireNow();
                }
            }
        }
    }
}
