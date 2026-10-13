# TASKS.md — Quadro operacional atual

Este arquivo contém somente trabalho ativo ou aguardando execução. Requisitos e
decisões ficam no `sdd_kit/`, o estado de continuidade em `SESSION.md` e o histórico
de trabalho concluído no Git.

## Estado

- O escopo reduzido da API V1 está consolidado e não possui decisão bloqueante aberta.
- F1, F2, B1, B2, B3 e B4 foram concluídas e são preservadas no histórico do Git. B4
  passou em `clean verify` com 64 testes, incluindo escopo, criação administrativa
  idempotente, transições concorrentes, cancelamento liberando horário e
  anonimização auditada.
- R1 é a próxima tarefa liberada e ainda não foi iniciada.
- Tarefas posteriores permanecem aguardando suas dependências; não devem ser
  iniciadas antecipadamente.

## Ordem recomendada

Um item bloqueado só pode ser ultrapassado quando o próximo estiver independente e
explicitamente liberado.

| Ordem | ID | Tarefa | Estado | Dependência | Fonte principal |
|---:|---|---|---|---|---|
| 1 | R1 | Contrato da API para desenvolvimento do frontend | TODO | liberada | `sdd_kit/15-api-consumer-documentation.md` |
| 2 | R2 | Publicar a release da API V1 | TODO | R1 | `sdd_kit/13-release-and-client-derivation.md` |
| 3 | C1 | Derivar o projeto Revitalle | TODO | R2 | `sdd_kit/13-release-and-client-derivation.md` |

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
