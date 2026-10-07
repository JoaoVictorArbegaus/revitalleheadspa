package com.servicescheduling.api;

import com.servicescheduling.api.acesso.infraestrutura.recuperacao.ResultadoRecuperacao;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class AplicacaoAgendamentoServicos {

    public static void main(String[] args) {
        boolean recuperacao = "recuperacao".equalsIgnoreCase(System.getenv("MODO_APLICACAO"));
        SpringApplication aplicacao = new SpringApplication(AplicacaoAgendamentoServicos.class);
        aplicacao.setAddCommandLineProperties(false);
        Map<String, Object> propriedadesOperacionais = new HashMap<>();
        propriedadesOperacionais.put("aplicacao.modo", recuperacao ? "recuperacao" : "normal");
        if (recuperacao) {
            aplicacao.setWebApplicationType(WebApplicationType.NONE);
            propriedadesOperacionais.put(
                    "aplicacao.acesso.recuperacao.email",
                    System.getenv().getOrDefault("RECUPERACAO_PROPRIETARIO_EMAIL", ""));
            propriedadesOperacionais.put(
                    "aplicacao.acesso.recuperacao.nova-senha",
                    System.getenv().getOrDefault("RECUPERACAO_PROPRIETARIO_NOVA_SENHA", ""));
        }
        aplicacao.addInitializers(contexto -> contexto.getEnvironment().getPropertySources()
                .addFirst(new MapPropertySource("modo-operacional", propriedadesOperacionais)));
        ConfigurableApplicationContext contexto = aplicacao.run(args);
        if (recuperacao) {
            int codigo = contexto.getBean(ResultadoRecuperacao.class).getCodigoSaida();
            contexto.close();
            System.exit(codigo);
        }
    }
}
