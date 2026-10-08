# Roadmap

O roadmap descreve resultados. A execução detalhada e o estado ficam em `TASKS.md`.

## B1 — Catálogo simples

Resultado: administração autenticada mantém profissionais, serviços e recursos simples com capacidade e quantidade fixa por serviço.

## B2 — Agenda

Resultado: administração configura expediente recorrente e bloqueios do profissional, e a API calcula disponibilidade segura considerando a capacidade dos recursos exigidos.

## B3 — Reserva auto-confirmada

Resultado: cliente consulta disponibilidade e cria sem login uma reserva que nasce `CONFIRMADO`, informando somente nome e celular e registrando ciência do aviso de privacidade.

Não há confirmação manual, estado `PENDENTE`, expiração de solicitação ou acesso público posterior à criação.

## B4 — Operação administrativa e anonimização manual

Resultado: usuários autenticados operam o ciclo das reservas dentro do próprio escopo, e `PROPRIETARIO` pode anonimizar manualmente um cliente elegível.

## R1 — Documentação para consumidores

Resultado: contrato OpenAPI, catálogo de erros, configuração e exemplos executáveis permitem consumir e operar a API sem ler sua implementação.

## R2 — Release da API V1

Resultado: a API reduzida está validada, documentada e publicada como versão estável apta a originar projetos de clientes.

## C1 — Derivação

Resultado: um projeto separado é derivado da release estável e pode receber frontend, identidade visual e regras aprovadas para o cliente.

## Possibilidades futuras

Notificações não são tarefa ativa nem parte da V1. Uma integração de comunicação só pode entrar em roadmap futuro depois que existir necessidade, canal e provedor concretos, com nova decisão e escopo próprios. Não se antecipa outbox, worker, porta ou adaptador.

## Critério de avanço

A ordem da V1 é exatamente B1, B2, B3, B4, R1, R2 e C1. Um item só avança quando suas dependências, critérios de aceite e decisões aplicáveis estiverem satisfeitos.
