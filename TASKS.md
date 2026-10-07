# TASKS.md — Quadro operacional atual

Este arquivo contém somente trabalho ativo ou aguardando execução. Requisitos e
decisões ficam no `sdd_kit/`, o estado de continuidade em `SESSION.md` e o histórico
de trabalho concluído no Git.

## Estado

- O SDD da API V1 está consolidado e não possui decisão bloqueante aberta.
- F1 foi concluída e será preservada no histórico do Git.
- F2 é a única tarefa liberada para implementação.
- Tarefas posteriores permanecem aguardando suas dependências; não devem ser
  iniciadas antecipadamente.

## Ordem recomendada

Um item bloqueado só pode ser ultrapassado quando o próximo estiver independente e
explicitamente liberado.

| Ordem | ID | Tarefa | Estado | Dependência | Fonte principal |
|---:|---|---|---|---|---|
| 1 | F2 | Autenticação e autorização administrativa | TODO | liberada | D-004, D-014, D-021 e `sdd_kit/07-data-privacy-security.md` |
| 2 | B1 | Catálogo de profissionais, recursos e serviços | TODO | F2 | `sdd_kit/03-domain-model.md` e `sdd_kit/04-business-rules.md` |
| 3 | B2 | Agenda, bloqueios e disponibilidade | TODO | B1 | `sdd_kit/16-scheduling-and-availability.md` |
| 4 | B3 | Agendamento público sem login | TODO | B1 e B2 | `sdd_kit/04-business-rules.md`, `sdd_kit/06-api-contract.md` e D-023 |
| 5 | B4 | Gestão administrativa de agendamentos | TODO | B3 | `sdd_kit/05-user-flows.md` e `sdd_kit/06-api-contract.md` |
| 6 | I1 | Caixa de saída de notificações | TODO | B3 e B4 | `sdd_kit/17-notifications-outbox.md` |
| 7 | R1 | Contrato e documentação para consumidores | TODO | F2, B1 a B4 e I1 | `sdd_kit/15-api-consumer-documentation.md` |
| 8 | R2 | Publicar a release da API V1 | TODO | R1 | `sdd_kit/13-release-and-client-derivation.md` |
| 9 | C1 | Derivar o projeto Revitalle | TODO | R2 | `sdd_kit/13-release-and-client-derivation.md` |
| 10 | O1 | Preparar uma instalação para produção | TODO | R2 e projeto de cliente | `sdd_kit/07-data-privacy-security.md` e `sdd_kit/08-architecture.md` |
| 11 | I2 | Implementar adaptador concreto de WhatsApp | BLOCKED | decisão do projeto de cliente | `sdd_kit/17-notifications-outbox.md` |

## Tarefa liberada

### F2 — Autenticação e autorização administrativa

Objetivo: permitir acesso administrativo seguro por sessão e administrar as contas
da instalação, sem antecipar catálogo, agenda ou agendamentos.

Escopo:

- persistir `UsuarioAdministrativo` com email normalizado, papel, estado, senha
  BCrypt custo 12, vínculo profissional e exigência de troca de senha;
- implementar bootstrap idempotente do primeiro `PROPRIETARIO` por variáveis de
  ambiente;
- implementar CSRF, login, identidade atual, logout e troca da própria senha com
  sessão HTTP em memória;
- implementar gestão de contas por `PROPRIETARIO`, incluindo senha temporária,
  ativação, desativação e as invariantes dos papéis `PROPRIETARIO` e `FUNCIONARIO`;
- aplicar limitação de tentativas de login e invalidação das sessões afetadas;
- implementar a recuperação operacional de `PROPRIETARIO` sem servidor web;
- atualizar contrato OpenAPI, catálogo de erros, testes e documentação operacional.

Critérios de aceite:

- bootstrap cria um único `PROPRIETARIO` e nunca redefine credencial existente;
- login protege contra fixation, aplica limite de tentativas e cria sessão com os
  atributos de cookie definidos em D-004;
- CSRF protege escritas administrativas, e senha temporária restringe a sessão até
  ser substituída;
- gestão de usuários exige `PROPRIETARIO`, preserva o último proprietário ativo e
  exige vínculo único com profissional para `FUNCIONARIO`;
- troca, redefinição e desativação invalidam as sessões correspondentes;
- recuperação funciona somente em modo operacional, sem expor ou receber senha por
  argumento ou endpoint HTTP;
- erros, OpenAPI e documentação representam os contratos implementados.

Validação mínima:

- executar testes de controller para autenticação, autorização, sessão e CSRF;
- executar testes de integração com PostgreSQL para bootstrap, contas e invariantes;
- validar recuperação em modo sem servidor e códigos de saída;
- executar `.\mvnw.cmd clean verify` e revisar contrato e instruções atualizados.

F2 não inclui cadastro funcional de profissionais, serviços ou agenda. O vínculo com
profissional deve respeitar o modelo aprovado sem antecipar a entrega B1.

## Migrations

- `V1` está ocupada pela fundação criada em F1.
- `V2` é o próximo número disponível.
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
