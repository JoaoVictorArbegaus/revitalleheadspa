CREATE TABLE cliente (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    celular VARCHAR(14) NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT uk_cliente_celular UNIQUE (celular),
    CONSTRAINT ck_cliente_nome CHECK (char_length(btrim(nome)) BETWEEN 1 AND 120),
    CONSTRAINT ck_cliente_celular CHECK (celular ~ '^\+55[0-9]{10,11}$')
);

CREATE TABLE agendamento (
    id UUID PRIMARY KEY,
    cliente_id UUID NOT NULL REFERENCES cliente (id),
    servico_id UUID NOT NULL REFERENCES servico (id),
    profissional_id UUID NOT NULL REFERENCES profissional (id),
    inicio_em TIMESTAMPTZ NOT NULL,
    fim_servico_em TIMESTAMPTZ NOT NULL,
    ocupado_ate TIMESTAMPTZ NOT NULL,
    estado VARCHAR(30) NOT NULL,
    origem VARCHAR(20) NOT NULL,
    nome_servico_contratado VARCHAR(120) NOT NULL,
    nome_profissional_contratado VARCHAR(120) NOT NULL,
    duracao_contratada_minutos INTEGER NOT NULL,
    preco_contratado NUMERIC(11, 2) NOT NULL,
    moeda_contratada VARCHAR(3) NOT NULL,
    nome_cliente_informado VARCHAR(120) NOT NULL,
    celular_cliente_informado VARCHAR(14) NOT NULL,
    versao_aviso_privacidade VARCHAR(64) NOT NULL,
    ciencia_aviso_privacidade_em TIMESTAMPTZ NOT NULL,
    criado_em TIMESTAMPTZ NOT NULL,
    atualizado_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_agendamento_intervalo CHECK (inicio_em < fim_servico_em AND fim_servico_em <= ocupado_ate),
    CONSTRAINT ck_agendamento_estado CHECK (estado IN ('CONFIRMADO', 'CANCELADO', 'CONCLUIDO', 'NAO_COMPARECEU')),
    CONSTRAINT ck_agendamento_origem CHECK (origem IN ('PUBLICO', 'ADMINISTRACAO')),
    CONSTRAINT ck_agendamento_duracao CHECK (duracao_contratada_minutos BETWEEN 1 AND 1440),
    CONSTRAINT ck_agendamento_preco CHECK (preco_contratado BETWEEN 0 AND 999999999.99),
    CONSTRAINT ck_agendamento_moeda CHECK (moeda_contratada ~ '^[A-Z]{3}$'),
    CONSTRAINT ck_agendamento_nome_servico CHECK (char_length(btrim(nome_servico_contratado)) BETWEEN 1 AND 120),
    CONSTRAINT ck_agendamento_nome_profissional CHECK (char_length(btrim(nome_profissional_contratado)) BETWEEN 1 AND 120),
    CONSTRAINT ck_agendamento_nome_cliente CHECK (char_length(btrim(nome_cliente_informado)) BETWEEN 1 AND 120),
    CONSTRAINT ck_agendamento_celular_cliente CHECK (celular_cliente_informado ~ '^\+55[0-9]{10,11}$'),
    CONSTRAINT ck_agendamento_versao_aviso CHECK (char_length(btrim(versao_aviso_privacidade)) BETWEEN 1 AND 64)
);

CREATE INDEX ix_agendamento_profissional_intervalo
    ON agendamento (profissional_id, estado, inicio_em, ocupado_ate);
CREATE INDEX ix_agendamento_confirmado_intervalo
    ON agendamento (ocupado_ate, inicio_em, id)
    WHERE estado = 'CONFIRMADO';
CREATE INDEX ix_agendamento_cliente_inicio
    ON agendamento (cliente_id, inicio_em DESC, id);

CREATE TABLE alocacao_recurso_agendamento (
    agendamento_id UUID NOT NULL REFERENCES agendamento (id) ON DELETE CASCADE,
    recurso_operacional_id UUID NOT NULL REFERENCES recurso_operacional (id),
    quantidade INTEGER NOT NULL,
    PRIMARY KEY (agendamento_id, recurso_operacional_id),
    CONSTRAINT ck_alocacao_quantidade CHECK (quantidade BETWEEN 1 AND 999)
);

CREATE INDEX ix_alocacao_recurso_agendamento
    ON alocacao_recurso_agendamento (recurso_operacional_id, agendamento_id);

CREATE TABLE registro_idempotencia (
    hash_chave CHAR(64) PRIMARY KEY,
    hash_requisicao CHAR(64) NOT NULL,
    status_resposta SMALLINT,
    corpo_resposta JSONB,
    agendamento_id UUID REFERENCES agendamento (id),
    criado_em TIMESTAMPTZ NOT NULL,
    expira_em TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_registro_idempotencia_hash_chave CHECK (hash_chave ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_registro_idempotencia_hash_requisicao CHECK (hash_requisicao ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_registro_idempotencia_intervalo CHECK (criado_em < expira_em),
    CONSTRAINT ck_registro_idempotencia_resultado CHECK (
        (status_resposta IS NULL AND corpo_resposta IS NULL AND agendamento_id IS NULL)
        OR (status_resposta IS NOT NULL AND corpo_resposta IS NOT NULL AND agendamento_id IS NOT NULL)
    )
);

CREATE INDEX ix_registro_idempotencia_expiracao
    ON registro_idempotencia (expira_em);
