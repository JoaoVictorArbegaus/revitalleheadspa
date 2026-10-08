# TASKS.md — Quadro operacional atual

Este arquivo contém somente trabalho ativo ou aguardando execução. Requisitos e
decisões ficam no `sdd_kit/`, o estado de continuidade em `SESSION.md` e o histórico
de trabalho concluído no Git.

## Estado

- O escopo reduzido da API V1 está consolidado e não possui decisão bloqueante aberta.
- F1 e F2 foram concluídas e são preservadas no histórico do Git. A revisão de
  escopo e contrato passou por revisão de qualidade e `clean verify` com 24 testes.
- B1 é a única tarefa liberada para implementação.
- Tarefas posteriores permanecem aguardando suas dependências; não devem ser
  iniciadas antecipadamente.

## Ordem recomendada

Um item bloqueado só pode ser ultrapassado quando o próximo estiver independente e
explicitamente liberado.

| Ordem | ID | Tarefa | Estado | Dependência | Fonte principal |
|---:|---|---|---|---|---|
| 1 | B1 | Catálogo simples de profissionais, recursos e serviços | TODO | liberada | `sdd_kit/03-domain-model.md` e `sdd_kit/04-business-rules.md` |
| 2 | B2 | Agenda, bloqueios e disponibilidade | TODO | B1 | `sdd_kit/16-scheduling-and-availability.md` |
| 3 | B3 | Reserva pública auto-confirmada | TODO | B1 e B2 | `sdd_kit/04-business-rules.md`, `sdd_kit/06-api-contract.md` e D-023 |
| 4 | B4 | Operação administrativa e anonimização manual | TODO | B3 | `sdd_kit/05-user-flows.md` e `sdd_kit/06-api-contract.md` |
| 5 | R1 | Contrato e documentação para consumidores | TODO | F2 e B1 a B4 | `sdd_kit/15-api-consumer-documentation.md` |
| 6 | R2 | Publicar a release da API V1 | TODO | R1 | `sdd_kit/13-release-and-client-derivation.md` |
| 7 | C1 | Derivar o projeto Revitalle | TODO | R2 | `sdd_kit/13-release-and-client-derivation.md` |

## Migrations

- `V1` está ocupada pela fundação criada em F1.
- `V2` está ocupada por F2 com acesso administrativo e a referência mínima de
  profissional.
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
