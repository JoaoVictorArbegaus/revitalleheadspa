# SESSION.md — Estado atual para continuidade

Este arquivo é um snapshot mutável, não um histórico. Detalhes de alterações
concluídas pertencem ao Git; requisitos e decisões pertencem ao `sdd_kit/`.

## Baseline

- `service-scheduling-api` é o produto-base genérico; Revitalle será o primeiro
  projeto independente derivado de uma release estável.
- O escopo reduzido da V1 está consolidado, sem decisão aberta bloqueante. A linguagem ubíqua
  do domínio, do contrato e do banco é português brasileiro.
- O repositório local acompanha `origin/main`. Commits e pushes registram o
  desenvolvimento; GitHub Release e tag `v1.0.0` pertencem somente a R2.
- A fundação executável usa Java 21 e Spring Boot 4.1.1, com PostgreSQL, Flyway,
  segurança de negação padrão, RFC 9457, OpenAPI, Testcontainers e Docker Compose.
- O monólito é organizado primeiro por domínio funcional e, dentro de cada módulo,
  por `api`, `aplicacao`, `dominio` e `infraestrutura`, somente quando houver classes
  reais.

## Trabalho ativo

- F1 e F2 estão concluídas e validadas.
- B1 — catálogo simples de profissionais, recursos e serviços — é a próxima tarefa
  liberada, mas ainda não foi iniciada.
- `V1` e `V2` estão ocupadas; a próxima migration deve ser reservada ao iniciar B1.
- Não há decisão aberta nem bloqueio conhecido para B1.

## Continuidade

- Detalhar e iniciar somente B1 antes de avançar para agenda ou agendamentos.
- Aplicar `sdd_kit/03-domain-model.md`, `sdd_kit/04-business-rules.md`, o contrato e
  as regras de segurança citados no item liberado.
- Limitar B1 a profissionais, serviços com preço e duração, recursos opcionais com
  capacidade e quantidade fixa por serviço; não antecipar agenda ou reservas.
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
