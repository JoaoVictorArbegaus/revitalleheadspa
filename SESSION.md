# SESSION.md — Estado atual para continuidade

Este arquivo é um snapshot mutável, não um histórico. Detalhes de alterações
concluídas pertencem ao Git; requisitos e decisões pertencem ao `sdd_kit/`.

## Baseline

- `service-scheduling-api` é o produto-base genérico; Revitalle será o primeiro
  projeto independente derivado de uma release estável.
- O SDD da V1 está consolidado, sem decisão aberta bloqueante. A linguagem ubíqua
  do domínio, do contrato e do banco é português brasileiro.
- A fundação definida usa Java 21, Spring Boot 4.1.1, Maven, PostgreSQL, Flyway,
  OpenAPI, Testcontainers e Docker.
- O repositório local acompanha `origin/main`. Commits e pushes registram o
  desenvolvimento; GitHub Release e tag `v1.0.0` pertencem somente a R2.
- A fundação executável usa Java 21 e Spring Boot 4.1.1, com PostgreSQL, Flyway,
  segurança de negação padrão, RFC 9457, OpenAPI, Testcontainers e Docker Compose.
- O monólito é organizado primeiro por domínio funcional e, dentro de cada módulo,
  por `api`, `aplicacao`, `dominio` e `infraestrutura`, somente quando houver classes
  reais.

## Trabalho ativo

- A fundação F1 está concluída e validada.
- A próxima tarefa liberada é F2 — autenticação e autorização administrativa.
- Não há decisão aberta nem bloqueio conhecido para iniciar F2.

## Continuidade

- Implementar somente F2 antes de avançar para catálogo ou regras de agendamento.
- Antes de criar migration, marcar F2 em andamento e reservar `V2` em `TASKS.md`.
- Aplicar D-004, D-014 e D-021 junto ao contrato e às regras de segurança citados
  no item liberado.
- Preservar o núcleo sem multitenancy, frontend, marca Revitalle ou provedor
  concreto de comunicação.

## Manutenção deste snapshot

- Substituir, fundir ou remover informações superadas; não acrescentar registros
  cronológicos.
- Manter apenas baseline, trabalho ativo, bloqueios e fatos necessários para a
  próxima retomada.
- Não incluir datas, números de sessão, comandos executados, diffs, hashes ou listas
  de arquivos de tarefas encerradas.
- Não duplicar requisitos do `sdd_kit/`, backlog de `TASKS.md` ou histórico do Git.
- Manter preferencialmente abaixo de 100 linhas.
