package com.servicescheduling.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.servicescheduling.api.clientes.dominio.NumeroCelular;
import org.junit.jupiter.api.Test;

class NumeroCelularTest {

    @Test
    void normalizaFormatosBrasileirosEquivalentes() {
        assertThat(NumeroCelular.de("(11) 99999-0000").valor()).isEqualTo("+5511999990000");
        assertThat(NumeroCelular.de("+55 11 99999-0000").valor()).isEqualTo("+5511999990000");
        assertThat(NumeroCelular.de("5511999990000").valor()).isEqualTo("+5511999990000");
        assertThat(NumeroCelular.de("11 3333-4444").valor()).isEqualTo("+551133334444");
    }

    @Test
    void rejeitaQuantidadeOuCaracteresInvalidos() {
        assertThatThrownBy(() -> NumeroCelular.de("119999000"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumeroCelular.de("11 99999/0000"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
