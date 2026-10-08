# Notificações e outbox fora da V1

## Decisão vigente

Notificações e outbox não fazem parte da API V1. A aplicação não registra eventos notificáveis, não mantém tabela ou worker de outbox, não oferece adaptador de comunicação e não expõe configuração, métrica ou operação administrativa relacionada a entrega de mensagens.

A criação e as mudanças administrativas de uma reserva são concluídas sem depender de email, WhatsApp ou outro canal externo. A ausência de comunicação não altera o estado nem o resultado da operação.

## Retomada futura

Uma retomada exige necessidade demonstrada por um projeto de cliente, provedor concreto definido, nova decisão registrada e tarefa própria com critérios de aceite. Essa decisão deve definir eventos, conteúdo, segurança, persistência, retentativas, observabilidade e operação antes de qualquer código, migration, endpoint, porta ou abstração preparatória.

O Git preserva o desenho anteriormente considerado; ele não constitui requisito vigente nem autorização para implementação.
