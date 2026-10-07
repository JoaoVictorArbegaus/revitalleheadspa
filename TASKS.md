# TASKS.md — Quadro operacional atual

Este arquivo contém somente trabalho ativo ou aguardando execução. Requisitos e
decisões ficam no `sdd_kit/`, o estado de continuidade em `SESSION.md` e o histórico
de trabalho concluído no Git.

## Estado

- O SDD da API V1 está consolidado e não possui decisão bloqueante aberta.
- F1 é a única tarefa liberada para implementação.
- Ainda não existe código executável nem migration criada.
- Tarefas posteriores permanecem aguardando suas dependências; não devem ser
  iniciadas antecipadamente.

## Ordem recomendada

Um item bloqueado só pode ser ultrapassado quando o próximo estiver independente e
explicitamente liberado.

| Ordem | ID | Tarefa | Estado | Dependência | Fonte principal |
|---:|---|---|---|---|---|
| 1 | F1 | Criar a base executável | TODO | liberada | `sdd_kit/18-technical-foundation.md` |
| 2 | F2 | Autenticação e autorização administrativa | TODO | F1 | D-004, D-014, D-021 e `sdd_kit/07-data-privacy-security.md` |
| 3 | B1 | Catálogo de profissionais, recursos e serviços | TODO | F2 | `sdd_kit/03-domain-model.md` e `sdd_kit/04-business-rules.md` |
| 4 | B2 | Agenda, bloqueios e disponibilidade | TODO | B1 | `sdd_kit/16-scheduling-and-availability.md` |
| 5 | B3 | Agendamento público sem login | TODO | B1 e B2 | `sdd_kit/04-business-rules.md`, `sdd_kit/06-api-contract.md` e D-023 |
| 6 | B4 | Gestão administrativa de agendamentos | TODO | B3 | `sdd_kit/05-user-flows.md` e `sdd_kit/06-api-contract.md` |
| 7 | I1 | Caixa de saída de notificações | TODO | B3 e B4 | `sdd_kit/17-notifications-outbox.md` |
| 8 | R1 | Contrato e documentação para consumidores | TODO | F2, B1 a B4 e I1 | `sdd_kit/15-api-consumer-documentation.md` |
| 9 | R2 | Publicar a release da API V1 | TODO | R1 | `sdd_kit/13-release-and-client-derivation.md` |
| 10 | C1 | Derivar o projeto Revitalle | TODO | R2 | `sdd_kit/13-release-and-client-derivation.md` |
| 11 | O1 | Preparar uma instalação para produção | TODO | R2 e projeto de cliente | `sdd_kit/07-data-privacy-security.md` e `sdd_kit/08-architecture.md` |
| 12 | I2 | Implementar adaptador concreto de WhatsApp | BLOCKED | decisão do projeto de cliente | `sdd_kit/17-notifications-outbox.md` |

## Tarefa liberada

### F1 — Criar a base executável

Objetivo: transformar a documentação atual em uma aplicação mínima executável, sem
antecipar autenticação ou regras funcionais das tarefas seguintes.

Escopo:

- criar o projeto Maven com Java 21, Spring Boot e pacote
  `com.servicescheduling.api`;
- aplicar a estrutura funcional e a linguagem ubíqua de
  `sdd_kit/18-technical-foundation.md`;
- configurar PostgreSQL, Flyway, JPA, Validation, Security, Actuator e OpenAPI;
- criar Maven Wrapper, imagem Docker e Docker Compose local;
- estabelecer o tratamento RFC 9457 e a correlação de requisições;
- adicionar testes básicos e documentação de execução local.

Critérios de aceite:

- aplicação e PostgreSQL iniciam pelo procedimento documentado;
- Flyway aplica as migrations em um banco vazio;
- liveness e readiness respondem conforme o contrato;
- erro de validação usa o formato D-006 e aparece no OpenAPI;
- `.\mvnw.cmd clean verify` executa toda a validação automatizada da fundação;
- nenhuma regra de negócio depende de frontend, Revitalle ou provedor externo.

Validação mínima:

- confirmar Java 21 e Docker disponíveis;
- executar a suíte com PostgreSQL do Testcontainers;
- iniciar o Compose em ambiente limpo e verificar aplicação, migration e saúde;
- revisar referências e instruções de execução atualizadas.

F1 não inclui contas administrativas, catálogo, agenda ou agendamentos. Ao cumprir os
critérios, remover F1 deste quadro, liberar F2 e preservar o resultado no Git.

## Migrations

- Nenhuma migration está ocupada.
- `V1` é o próximo número disponível e será reservado por F1 ao iniciar.
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
