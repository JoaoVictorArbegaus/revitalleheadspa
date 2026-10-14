package com.servicescheduling.api.compartilhado.infraestrutura.configuracao;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConfiguracaoTempo {

    @Bean
    Clock relogio() {
        return Clock.systemUTC();
    }
}
