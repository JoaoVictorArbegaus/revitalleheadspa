CREATE TABLE profissional (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    ativo BOOLEAN NOT NULL
);

CREATE TABLE usuario_administrativo (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL,
    hash_senha VARCHAR(60) NOT NULL,
    papel VARCHAR(20) NOT NULL,
    profissional_id UUID,
    ativo BOOLEAN NOT NULL,
    deve_alterar_senha BOOLEAN NOT NULL,
    versao BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uq_usuario_administrativo_email UNIQUE (email),
    CONSTRAINT fk_usuario_profissional FOREIGN KEY (profissional_id) REFERENCES profissional (id),
    CONSTRAINT ck_usuario_email_normalizado CHECK (email = lower(btrim(email))),
    CONSTRAINT ck_usuario_papel CHECK (papel IN ('PROPRIETARIO', 'FUNCIONARIO')),
    CONSTRAINT ck_usuario_vinculo CHECK (papel <> 'FUNCIONARIO' OR profissional_id IS NOT NULL)
);

CREATE UNIQUE INDEX uq_usuario_profissional
    ON usuario_administrativo (profissional_id)
    WHERE profissional_id IS NOT NULL;

CREATE INDEX ix_usuario_papel_ativo ON usuario_administrativo (papel, ativo);
