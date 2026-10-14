# Instruções para LLMs

Estas regras são obrigatórias para qualquer alteração neste repositório.

## Leitura progressiva

1. Leia `SESSION.md` e `TASKS.md` antes de iniciar uma tarefa.
2. Leia `sdd_kit/14-v1-boundaries.md` antes de alterar código.
3. Leia `sdd_kit/00-index.md` e apenas as especificações relacionadas à tarefa.
4. Consulte documentos adicionais quando uma referência ou dúvida concreta exigir.
5. Não carregue toda a documentação por padrão.

Ao executar F1 ou alterar fundação, dependências, pacotes, configuração ou build, leia também `sdd_kit/18-technical-foundation.md`.

## Fontes de verdade

- Requisitos e decisões duráveis: `sdd_kit/`.
- Trabalho planejado e estado das entregas: `TASKS.md`.
- Continuidade da tarefa atual: `SESSION.md`.
- Histórico de alterações: Git.
- O código e os testes demonstram o comportamento implementado, mas não substituem decisões de produto registradas.

Se documentos e implementação divergirem, interrompa a expansão da tarefa, identifique a divergência e atualize a fonte correta como parte da mesma mudança.

## Regras de execução

- Execute uma tarefa coesa de `TASKS.md` por vez.
- Antes de implementar, confirme critérios de aceite, documentos aplicáveis e decisões pendentes.
- Não invente uma decisão marcada como pendente. Registre a necessidade em `sdd_kit/11-open-decisions.md`.
- Implemente a solução completa mais simples que satisfaça os requisitos atuais.
- Não crie infraestrutura, extensão, abstração ou configuração para uma necessidade apenas hipotética.
- Não expanda o escopo da tarefa com refatorações oportunistas ou funcionalidades futuras.
- Se uma expansão for necessária, registre-a primeiro em `TASKS.md` e na especificação correspondente.
- Não introduza multitenancy, `tenant_id` ou compartilhamento de banco entre clientes.
- Neste projeto derivado, frontends ficam em `../web-cliente/` e `../web-admin/`; `api/` não contém código de interface.
- Mantenha o núcleo de agendamento independente de marca, clínica e provedores externos.
- Preserve a linguagem ubíqua de D-026: conceitos de negócio, identificadores, estados, rotas, JSON e banco usam português sem acentos nos identificadores técnicos.
- Trate telefone, email administrativo e demais dados pessoais como dados protegidos.
- Qualquer operação de reserva deve ser segura contra concorrência e duplicidade.
- Preserve o modelo de segurança: sessão HTTP, CSRF ativo, allowlist pública explícita e negação padrão.
- Não introduza JWT, OAuth, login social ou segundo fator na V1.
- Não crie endpoint público de recuperação administrativa; preserve o procedimento operacional definido em D-021.
- Prefira mudanças pequenas o bastante para revisão, mas completas o bastante para entregar comportamento verificável.
- Atualize testes, contrato da API e documentação afetada na mesma tarefa.
- Toda mudança de contrato deve atualizar OpenAPI, exemplos executáveis e o catálogo de erros ou configuração aplicável.
- Não declare uma tarefa concluída sem executar a validação aplicável.
- Siga `sdd_kit/13-release-and-client-derivation.md` ao iniciar um projeto de cliente.
- Faça commits coesos e pushes para `main` conforme o trabalho validado avança, mantendo documentação, testes e implementação correspondentes no mesmo conjunto.
- Não crie tags nem GitHub Releases; projetos de clientes partem de um clone de `main` (D-034).
- Siga `sdd_kit/15-api-consumer-documentation.md` ao criar ou alterar qualquer endpoint.
- Siga `sdd_kit/16-scheduling-and-availability.md` ao alterar agenda, disponibilidade, reservas ou calendário.
- Siga `sdd_kit/18-technical-foundation.md` ao alterar stack, pacotes, persistência, configuração, observabilidade ou build.

## Restrições contra overengineering

- Preserve o monólito modular e o PostgreSQL como base da V1.
- Não introduza microserviços, Redis, Kafka, RabbitMQ, Kubernetes, service mesh, CQRS, event sourcing, GraphQL, Elasticsearch ou cache distribuído sem uma decisão posterior aprovada.
- Não crie mecanismo genérico de plugins, motor de regras, DSL, barramento interno complexo ou framework próprio.
- Não introduza notificações, outbox, worker, porta ou adaptador de comunicação na V1; integração futura exige necessidade e provedor concretos, nova decisão e tarefa própria.
- Uma interface é aceitável quando protege uma fronteira externa real ou uma fronteira de domínio, não apenas para espelhar uma única classe.
- Uma nova dependência precisa resolver uma necessidade da tarefa atual e possuir uso imediato.
- Não crie campos, endpoints, tabelas ou eventos reservados para uso futuro.
- Não otimize consultas sem evidência de problema ou requisito mensurável; garanta primeiro correção e índices óbvios.

## Estado dos arquivos de acompanhamento

- `TASKS.md` é o quadro operacional atual: contém somente trabalho ativo ou realmente aguardando execução, com estado, dependências, fonte do SDD e reservas de migration.
- Detalhe apenas a tarefa liberada ou em andamento. Remova tarefas concluídas após integração; o Git preserva o histórico e o SDD preserva requisitos.
- Não use `DONE` como arquivo permanente, não replique toda a especificação no quadro e não transforme intenção futura em tarefa ativa.
- `SESSION.md` é um snapshot curto e substituível para continuidade, não um histórico.
- Mantenha em `SESSION.md` apenas baseline, trabalho ativo, decisões abertas, bloqueios e próximo ponto de continuidade; prefira menos de 100 linhas.
- Ao mudar o foco, substitua, funda ou remova informações superadas. Não acrescente diário, datas, números de sessão, comandos, diffs, hashes ou relatos de tarefas encerradas.
- Não duplique no `SESSION.md` requisitos do `sdd_kit/`, backlog de `TASKS.md` ou histórico preservado pelo Git.
- Decisões aceitas devem sair de `SESSION.md` e ser registradas em `sdd_kit/`.

## Encerramento de tarefa

Ao concluir uma tarefa:

1. registre o resultado e a validação em `TASKS.md`;
2. atualize as especificações afetadas;
3. atualize `SESSION.md` somente quando o estado de continuidade mudar e deixe nele apenas o próximo contexto necessário;
4. informe limitações ou validações não executadas de forma explícita.
