# TASKS.md — Quadro operacional atual

Este arquivo contém somente trabalho ativo ou aguardando execução. Requisitos e
decisões ficam no `sdd_kit/`, o estado de continuidade em `SESSION.md` e o histórico
de trabalho concluído no Git.

## Estado

- O escopo reduzido da API V1 está consolidado e não possui decisão bloqueante aberta.
- F1, F2, B1, B2, B3, B4 e R1 foram concluídas e são preservadas no histórico do Git.
  R1 passou em `clean verify` com 68 testes, incluindo `ContratoDocumentacaoTest`, e
  os exemplos `.http` executaram contra a API em Docker Compose sem falhas.
- Não há tarefa ativa. Tags e releases não são usadas (D-034); C1 é a derivação
  por `git clone`, feita pelo responsável fora deste repositório quando houver uso.
- Tarefas posteriores permanecem aguardando suas dependências; não devem ser
  iniciadas antecipadamente.

## Ordem recomendada

Um item bloqueado só pode ser ultrapassado quando o próximo estiver independente e
explicitamente liberado.

| Ordem | ID | Tarefa | Estado | Dependência | Fonte principal |
|---:|---|---|---|---|---|
| 1 | C1 | Derivar o projeto Revitalle por clone | IN_PROGRESS | liberada | `sdd_kit/13-release-and-client-derivation.md` |

### C1 — escopo operacional

- Feito: clone de `main`, repositório próprio `revitalleheadspa` com a API em
  `api/`, commit de origem no `README.md` da raiz, `upstream` sem push e pastas
  `web-cliente/` e `web-admin/`.
- Pendente: banco, segredos, domínio e ambientes exclusivos; hospedagem, proxy,
  certificados, backup e monitoramento; verificação de que nenhuma credencial ou
  identidade herdada permanece; `clean verify` no novo repositório.

## Migrations

- `V1` está ocupada pela fundação criada em F1.
- `V2` está ocupada por F2 com acesso administrativo e a referência mínima de
  profissional.
- `V3` está ocupada pelo catálogo implementado em B1.
- `V4` está ocupada por expediente semanal e bloqueios de B2.
- `V5` está ocupada por cliente, agendamento, alocações e idempotência de B3.
- `V6` está ocupada pela autoria das transições, anonimização e auditoria de B4.
- Toda reserva ou mudança de número deve aparecer aqui antes da criação do arquivo.

## Regras de manutenção

- Usar apenas os estados `TODO`, `IN_PROGRESS`, `REVIEW` e `BLOCKED`.
- Manter no máximo uma tarefa funcional principal em `IN_PROGRESS`, salvo trabalho
  paralelo explicitamente coordenado.
- Liberar uma tarefa somente quando dependências e decisões necessárias estiverem
  resolvidas no SDD.
- Ao iniciar, detalhar somente o escopo operacional necessário e reservar migrations.
- Implementar cada item como unidade coesa, incluindo documentação, código, testes e
  correções necessárias.
- Remover tarefas concluídas depois do commit e push; não manter itens `DONE`.
- Não usar este arquivo para criar requisitos ou registrar diário de execução.
- Não transformar possibilidade futura em tarefa ativa sem decisão e escopo aprovados.
