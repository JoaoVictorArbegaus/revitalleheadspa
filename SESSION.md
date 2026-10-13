# SESSION.md — Estado atual para continuidade

Este arquivo é um snapshot mutável, não um histórico. Detalhes de alterações
concluídas pertencem ao Git; requisitos e decisões pertencem ao `sdd_kit/`.

## Baseline

- `service-scheduling-api` é o produto-base genérico; Revitalle será o primeiro
  projeto independente derivado de uma release estável.
- O escopo reduzido da V1 está consolidado, sem decisão aberta bloqueante. A linguagem ubíqua
  do domínio, do contrato e do banco é português brasileiro.
- O repositório usa `main` como linha de integração. Commits e pushes registram o
  desenvolvimento; GitHub Release e tag `v1.0.0` pertencem somente a R2.
- A fundação executável usa Java 21 e Spring Boot 4.1.1, com PostgreSQL, Flyway,
  segurança de negação padrão, RFC 9457, OpenAPI, Testcontainers e Docker Compose.
- O monólito é organizado primeiro por domínio funcional e, dentro de cada módulo,
  por `api`, `aplicacao`, `dominio` e `infraestrutura`, somente quando houver classes
  reais.

## Trabalho ativo

- F1, F2, B1, B2 e B3 estão concluídas e validadas; reservas públicas
  auto-confirmadas já ocupam a disponibilidade.
- B4 — operação administrativa e anonimização manual — é a próxima tarefa liberada,
  ainda não iniciada nem detalhada em `TASKS.md`.
- Não há decisão aberta nem bloqueio conhecido.

## Continuidade

- Ao iniciar B4, detalhar seus critérios de aceite em `TASKS.md` a partir de
  `sdd_kit/05-user-flows.md` e `sdd_kit/06-api-contract.md`, confirmar decisões
  pendentes e reservar a próxima migration (`V6`) se houver mudança de banco.
- Os testes de integração usam Testcontainers e exigem o Docker em execução.
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
