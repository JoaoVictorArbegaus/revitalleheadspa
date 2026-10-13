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

- F1, F2, B1, B2, B3, B4 e R1 estão concluídas e validadas; a API V1 está
  funcional e documentada para o frontend.
- R2 — publicar a release da API V1 — é a próxima tarefa liberada, ainda não
  iniciada. Ela cria a tag `v1.0.0` e a GitHub Release, ações externas que exigem
  confirmação explícita do responsável.
- Não há decisão aberta nem bloqueio conhecido.

## Continuidade

- Ao iniciar R2, seguir `sdd_kit/13-release-and-client-derivation.md` e detalhar
  seus critérios em `TASKS.md` antes de qualquer tag ou publicação.
- Os testes de integração usam Testcontainers e exigem o Docker em execução; os
  exemplos `.http` podem ser executados com a imagem `jetbrains/intellij-http-client`
  contra o Docker Compose.
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
