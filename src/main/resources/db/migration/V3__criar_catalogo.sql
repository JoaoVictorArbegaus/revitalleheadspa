ALTER TABLE profissional
    ADD CONSTRAINT ck_profissional_nome CHECK (char_length(btrim(nome)) BETWEEN 1 AND 120);

CREATE TABLE recurso_operacional (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    capacidade INTEGER NOT NULL,
    ativo BOOLEAN NOT NULL,
    CONSTRAINT ck_recurso_nome CHECK (char_length(btrim(nome)) BETWEEN 1 AND 120),
    CONSTRAINT ck_recurso_capacidade CHECK (capacidade BETWEEN 1 AND 999)
);

CREATE TABLE servico (
    id UUID PRIMARY KEY,
    profissional_id UUID NOT NULL,
    nome VARCHAR(120) NOT NULL,
    descricao VARCHAR(1000),
    duracao_minutos INTEGER NOT NULL,
    intervalo_limpeza_minutos INTEGER NOT NULL,
    preco NUMERIC(11, 2) NOT NULL,
    ativo BOOLEAN NOT NULL,
    ordem_exibicao INTEGER NOT NULL,
    CONSTRAINT fk_servico_profissional FOREIGN KEY (profissional_id) REFERENCES profissional (id),
    CONSTRAINT ck_servico_nome CHECK (char_length(btrim(nome)) BETWEEN 1 AND 120),
    CONSTRAINT ck_servico_descricao CHECK (descricao IS NULL OR char_length(descricao) <= 1000),
    CONSTRAINT ck_servico_duracao CHECK (duracao_minutos BETWEEN 1 AND 1440),
    CONSTRAINT ck_servico_limpeza CHECK (intervalo_limpeza_minutos BETWEEN 0 AND 1440),
    CONSTRAINT ck_servico_preco CHECK (preco BETWEEN 0 AND 999999999.99),
    CONSTRAINT ck_servico_ordem CHECK (ordem_exibicao BETWEEN 0 AND 9999)
);

CREATE TABLE requisito_recurso_servico (
    servico_id UUID NOT NULL,
    recurso_operacional_id UUID NOT NULL,
    quantidade INTEGER NOT NULL,
    PRIMARY KEY (servico_id, recurso_operacional_id),
    CONSTRAINT fk_requisito_servico FOREIGN KEY (servico_id) REFERENCES servico (id) ON DELETE CASCADE,
    CONSTRAINT fk_requisito_recurso FOREIGN KEY (recurso_operacional_id) REFERENCES recurso_operacional (id),
    CONSTRAINT ck_requisito_quantidade CHECK (quantidade BETWEEN 1 AND 999)
);

CREATE INDEX ix_servico_profissional_ordem
    ON servico (profissional_id, ativo, ordem_exibicao, nome, id);
CREATE INDEX ix_servico_publico
    ON servico (ativo, ordem_exibicao, nome, id);
CREATE INDEX ix_requisito_recurso
    ON requisito_recurso_servico (recurso_operacional_id);
