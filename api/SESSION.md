# SESSION.md — Estado atual para continuidade

Este arquivo é um snapshot mutável, não um histórico. Detalhes de alterações
concluídas pertencem ao Git; requisitos e decisões pertencem ao `sdd_kit/`.

## Baseline

- Este é o projeto Revitalle, derivado por `git clone` do produto-base
  `service-scheduling-api` (D-034). A API fica em `api/`; os frontends em
  `../web-cliente/` e `../web-admin/`. O commit de origem está no `README.md` da raiz.
- A API V1 do produto-base está funcional e documentada para o frontend. A
  linguagem ubíqua do domínio, do contrato e do banco é português brasileiro.
- A fundação usa Java 21 e Spring Boot 4.1.1, com PostgreSQL, Flyway, segurança
  de negação padrão, RFC 9457, OpenAPI, Testcontainers e Docker Compose.

## Trabalho ativo

- C1 em andamento: estrutura do repositório criada; configuração de ambientes,
  hospedagem e validação de herança pendentes (ver `TASKS.md`).
- Início do frontend: landing e agendamento em `web-cliente/`, painel em
  `web-admin/`. A stack do painel e da página de agendamento ainda não foi definida.

## Continuidade

- Preencher `web-cliente/briefing.md` e as referências antes de gerar a landing.
- Os testes de integração usam Testcontainers e exigem o Docker em execução.

## Manutenção deste snapshot

- Substituir, fundir ou remover informações superadas; não acrescentar registros
  cronológicos.
- Manter apenas baseline, trabalho ativo, bloqueios e fatos necessários para a
  próxima retomada.
- Não incluir datas, números de sessão, comandos executados, diffs, hashes ou listas
  de arquivos de tarefas encerradas.
- Não duplicar requisitos do `sdd_kit/`, backlog de `TASKS.md` ou histórico do Git.
- Manter preferencialmente abaixo de 100 linhas.
