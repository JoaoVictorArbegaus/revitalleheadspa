ALTER TABLE cliente
    ALTER COLUMN nome DROP NOT NULL,
    ALTER COLUMN celular DROP NOT NULL,
    ADD COLUMN anonimizado_em TIMESTAMPTZ,
    ADD CONSTRAINT ck_cliente_anonimizacao CHECK (
        (anonimizado_em IS NULL AND nome IS NOT NULL AND celular IS NOT NULL)
        OR (anonimizado_em IS NOT NULL AND nome IS NULL AND celular IS NULL)
    );

ALTER TABLE agendamento
    ALTER COLUMN nome_cliente_informado DROP NOT NULL,
    ALTER COLUMN celular_cliente_informado DROP NOT NULL,
    ADD COLUMN criado_por UUID REFERENCES usuario_administrativo (id),
    ADD COLUMN estado_alterado_em TIMESTAMPTZ,
    ADD COLUMN estado_alterado_por UUID REFERENCES usuario_administrativo (id),
    ADD COLUMN motivo_cancelamento VARCHAR(500),
    ADD CONSTRAINT ck_agendamento_cliente_informado CHECK (
        (nome_cliente_informado IS NULL) = (celular_cliente_informado IS NULL)
    ),
    ADD CONSTRAINT ck_agendamento_autoria CHECK (
        (origem = 'PUBLICO' AND criado_por IS NULL)
        OR (origem = 'ADMINISTRACAO' AND criado_por IS NOT NULL)
    ),
    ADD CONSTRAINT ck_agendamento_transicao CHECK (
        (estado = 'CONFIRMADO' AND estado_alterado_em IS NULL AND estado_alterado_por IS NULL)
        OR (estado <> 'CONFIRMADO' AND estado_alterado_em IS NOT NULL AND estado_alterado_por IS NOT NULL)
    ),
    ADD CONSTRAINT ck_agendamento_motivo_cancelamento CHECK (
        (estado = 'CANCELADO' AND char_length(btrim(motivo_cancelamento)) BETWEEN 1 AND 500)
        OR (estado <> 'CANCELADO' AND motivo_cancelamento IS NULL)
    );

CREATE INDEX ix_agendamento_inicio
    ON agendamento (inicio_em, id);

CREATE TABLE anonimizacao_cliente (
    id UUID PRIMARY KEY,
    cliente_id UUID NOT NULL REFERENCES cliente (id),
    autor_id UUID NOT NULL REFERENCES usuario_administrativo (id),
    motivo VARCHAR(500) NOT NULL,
    anonimizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_anonimizacao_cliente UNIQUE (cliente_id),
    CONSTRAINT ck_anonimizacao_cliente_motivo CHECK (char_length(btrim(motivo)) BETWEEN 1 AND 500)
);
