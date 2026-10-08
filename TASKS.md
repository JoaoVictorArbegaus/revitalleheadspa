# TASKS.md — Quadro operacional atual

Este arquivo contém somente trabalho ativo ou aguardando execução. Requisitos e
decisões ficam no `sdd_kit/`, o estado de continuidade em `SESSION.md` e o histórico
de trabalho concluído no Git.

## Estado

- O escopo reduzido da API V1 está consolidado e não possui decisão bloqueante aberta.
- F1, F2, B1 e B2 foram concluídas e são preservadas no histórico do Git. B2 passou
  pelos gates de código, desempenho e conformidade documental, além de `clean verify`
  com 39 testes.
- Não há tarefa em implementação; B3 é a próxima tarefa liberada.
- Tarefas posteriores permanecem aguardando suas dependências; não devem ser
  iniciadas antecipadamente.

## Ordem recomendada

Um item bloqueado só pode ser ultrapassado quando o próximo estiver independente e
explicitamente liberado.

| Ordem | ID | Tarefa | Estado | Dependência | Fonte principal |
|---:|---|---|---|---|---|
| 1 | B3 | Reserva pública auto-confirmada | TODO | liberada | `sdd_kit/04-business-rules.md`, `sdd_kit/06-api-contract.md` e D-023 |
| 2 | B4 | Operação administrativa e anonimização manual | TODO | B3 | `sdd_kit/05-user-flows.md` e `sdd_kit/06-api-contract.md` |
| 3 | R1 | Contrato da API para desenvolvimento do frontend | TODO | B3 e B4 | `sdd_kit/15-api-consumer-documentation.md` |
| 4 | R2 | Publicar a release da API V1 | TODO | R1 | `sdd_kit/13-release-and-client-derivation.md` |
| 5 | C1 | Derivar o projeto Revitalle | TODO | R2 | `sdd_kit/13-release-and-client-derivation.md` |

## Migrations

- `V1` está ocupada pela fundação criada em F1.
- `V2` está ocupada por F2 com acesso administrativo e a referência mínima de
  profissional.
- `V3` está ocupada pelo catálogo implementado em B1.
- `V4` está ocupada por expediente semanal e bloqueios de B2.
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
