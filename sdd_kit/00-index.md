# Índice da especificação

Leia este arquivo primeiro e abra apenas os documentos necessários para a tarefa.

| Documento | Quando consultar |
|---|---|
| [01-product-vision.md](01-product-vision.md) | objetivo, público, modelo de implantação e limites estratégicos |
| [02-scope-and-requirements.md](02-scope-and-requirements.md) | escopo da API V1, requisitos e itens adiados |
| [03-domain-model.md](03-domain-model.md) | entidades, valores, estados e relações |
| [04-business-rules.md](04-business-rules.md) | disponibilidade, reserva, cancelamento e concorrência |
| [05-user-flows.md](05-user-flows.md) | jornadas pública e administrativa |
| [06-api-contract.md](06-api-contract.md) | recursos HTTP, contratos, erros e idempotência |
| [07-data-privacy-security.md](07-data-privacy-security.md) | CPF, celular, LGPD, acesso, logs e retenção |
| [08-architecture.md](08-architecture.md) | módulos, fronteiras, implantação e integrações |
| [09-quality-strategy.md](09-quality-strategy.md) | testes, validação e critérios de qualidade |
| [10-roadmap.md](10-roadmap.md) | sequência de entregas e evolução do produto |
| [11-open-decisions.md](11-open-decisions.md) | escolhas pendentes e registro de decisões |
| [12-glossary.md](12-glossary.md) | linguagem comum do projeto |
| [13-release-and-client-derivation.md](13-release-and-client-derivation.md) | publicação da API V1 e criação de projetos por cliente |
| [14-v1-boundaries.md](14-v1-boundaries.md) | limites funcionais, técnicos e de atuação da LLM na V1 |
| [15-api-consumer-documentation.md](15-api-consumer-documentation.md) | entregáveis e critérios da documentação para consumidores da API |
| [16-scheduling-and-availability.md](16-scheduling-and-availability.md) | persistência da agenda, cálculo de horários, concorrência e integração do calendário |
| [17-notifications-outbox.md](17-notifications-outbox.md) | eventos notificáveis, worker, retentativas e fronteira de provedores |
| [18-technical-foundation.md](18-technical-foundation.md) | identidade do produto-base, stack, pacotes, convenções e configuração inicial |

## Ordem sugerida para uma nova tarefa

1. `SESSION.md`.
2. Item correspondente em `TASKS.md`.
3. Este índice.
4. Documentos indicados pelo item e pelos links encontrados.

## Regra de atualização

Uma alteração funcional deve atualizar, quando aplicável, requisitos, domínio, regras, contrato, testes e tarefa. Não deixe decisões importantes somente em conversa, código ou mensagem de commit.
