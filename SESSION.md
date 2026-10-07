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
- Ainda não existe código executável. Não presuma que aplicação, banco, Docker ou
  suíte de testes já foram validados.

## Trabalho ativo

- A próxima tarefa liberada é F1 — criar a base executável conforme `TASKS.md` e
  `sdd_kit/18-technical-foundation.md`.
- Não há decisão de produto aberta nem bloqueio conhecido para iniciar F1.

## Continuidade

- Implementar somente F1 antes de avançar para autenticação ou regras do domínio.
- Validar Java, Maven Wrapper, Docker, PostgreSQL/Flyway, inicialização, saúde,
  formato RFC 9457 e testes básicos definidos nos critérios de F1.
- Preservar o núcleo sem multitenancy, frontend, marca Revitalle ou provedor
  concreto de comunicação.
- Ao concluir F1, atualizar seu estado em `TASKS.md`, substituir este snapshot pelo
  próximo contexto necessário e criar um commit coeso em `main`.

## Manutenção deste snapshot

- Substituir, fundir ou remover informações superadas; não acrescentar registros
  cronológicos.
- Manter apenas baseline, trabalho ativo, bloqueios e fatos necessários para a
  próxima retomada.
- Não incluir datas, números de sessão, comandos executados, diffs, hashes ou listas
  de arquivos de tarefas encerradas.
- Não duplicar requisitos do `sdd_kit/`, backlog de `TASKS.md` ou histórico do Git.
- Manter preferencialmente abaixo de 100 linhas.
