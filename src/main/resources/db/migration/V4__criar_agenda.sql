CREATE TABLE expediente_semanal (
    id UUID PRIMARY KEY,
    profissional_id UUID NOT NULL REFERENCES profissional (id),
    dia_semana VARCHAR(20) NOT NULL,
    inicio TIME WITHOUT TIME ZONE NOT NULL,
    fim TIME WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT ck_expediente_dia_semana CHECK (
        dia_semana IN (
            'SEGUNDA_FEIRA', 'TERCA_FEIRA', 'QUARTA_FEIRA', 'QUINTA_FEIRA',
            'SEXTA_FEIRA', 'SABADO', 'DOMINGO'
        )
    ),
    CONSTRAINT ck_expediente_intervalo CHECK (inicio < fim),
    CONSTRAINT uk_expediente_janela UNIQUE (profissional_id, dia_semana, inicio, fim)
);

CREATE INDEX ix_expediente_profissional_dia_inicio
    ON expediente_semanal (profissional_id, dia_semana, inicio);

CREATE TABLE bloqueio_agenda (
    id UUID PRIMARY KEY,
    profissional_id UUID NOT NULL REFERENCES profissional (id),
    inicio_em TIMESTAMPTZ NOT NULL,
    fim_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_bloqueio_agenda_intervalo CHECK (inicio_em < fim_em)
);

CREATE INDEX ix_bloqueio_agenda_profissional_intervalo
    ON bloqueio_agenda (profissional_id, inicio_em, fim_em);
