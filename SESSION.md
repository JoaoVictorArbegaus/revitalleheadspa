# SESSION.md — Estado atual para continuidade

Este arquivo é um snapshot mutável, não um histórico. Detalhes de alterações
concluídas pertencem ao Git; requisitos e decisões pertencem ao `sdd_kit/`.

## Baseline

- `service-scheduling-api` é o produto-base genérico; Revitalle será o primeiro
  projeto independente derivado por `git clone` de `main` (D-034).
- O escopo reduzido da V1 está consolidado, sem decisão aberta bloqueante. A linguagem ubíqua
  do domínio, do contrato e do banco é português brasileiro.
- O repositório usa `main` como linha de integração. Commits e pushes registram o
  desenvolvimento; não há tags nem GitHub Releases.
- A fundação executável usa Java 21 e Spring Boot 4.1.1, com PostgreSQL, Flyway,
  segurança de negação padrão, RFC 9457, OpenAPI, Testcontainers e Docker Compose.
- O monólito é organizado primeiro por domínio funcional e, dentro de cada módulo,
  por `api`, `aplicacao`, `dominio` e `infraestrutura`, somente quando houver classes
  reais.

## Trabalho ativo

- F1, F2, B1, B2, B3, B4 e R1 estão concluídas e validadas; a API V1 está
  funcional e documentada para o frontend em `main`.
- Não há tarefa ativa, decisão aberta nem bloqueio conhecido.

## Continuidade

- O próximo passo é C1: o responsável clona `main` para iniciar o projeto
  Revitalle, seguindo `sdd_kit/13-release-and-client-derivation.md`.
- Novas funcionalidades no produto-base exigem decisão e tarefa próprias em
  `TASKS.md` antes da implementação.
- Os testes de integração usam Testcontainers e exigem o Docker em execução; os
  exemplos `.http` podem ser executados com a imagem `jetbrains/intellij-http-client`
  contra o Docker Compose.

## Manutenção deste snapshot

- Substituir, fundir ou remover informações superadas; não acrescentar registros
  cronológicos.
- Manter apenas baseline, trabalho ativo, bloqueios e fatos necessários para a
  próxima retomada.
- Não incluir datas, números de sessão, comandos executados, diffs, hashes ou listas
  de arquivos de tarefas encerradas.
- Não duplicar requisitos do `sdd_kit/`, backlog de `TASKS.md` ou histórico do Git.
- Manter preferencialmente abaixo de 100 linhas.
