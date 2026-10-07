# Notificações e outbox

## Objetivo

Registrar fatos notificáveis de forma atômica e processá-los sem acoplar reservas a WhatsApp, email ou outro provedor. A API-base V1 não envia mensagens reais quando nenhum adaptador está configurado.

## Eventos da V1

| Evento | Gatilho |
|---|---|
| `AGENDAMENTO_PENDENTE` | reserva pública criada em confirmação manual |
| `AGENDAMENTO_CONFIRMADO` | reserva criada automaticamente, criada pela administração ou pendência confirmada |
| `AGENDAMENTO_REAGENDADO` | intervalo da reserva alterado com sucesso |
| `AGENDAMENTO_CANCELADO` | cancelamento público, administrativo ou rejeição de pendência |
| `AGENDAMENTO_EXPIRADO` | pendência encerrada por prazo |

`CONCLUIDO`, `NAO_COMPARECEU` e alterações somente de recursos não geram evento de notificação na V1. Lembretes, promoções, horários silenciosos e textos de mensagem não pertencem ao produto-base.

## Persistência

O evento é inserido na mesma transação da mudança da reserva. Se a transação da reserva falhar, o evento não existe; se ela confirmar, o evento não pode ser perdido.

Campos conceituais:

| Campo | Regra |
|---|---|
| id | identificador aleatório e chave idempotente de entrega |
| tipoAgregado / agregadoId | `AGENDAMENTO` e identificador interno da reserva |
| tipoEvento | um dos cinco eventos da V1 |
| versaoAgregado | ordem da mudança dentro da reserva |
| versaoPayload | começa em 1 e só muda com evolução explícita |
| payload | fotografia mínima do serviço, estado e horários necessários ao adaptador |
| estado | `PENDENTE`, `PROCESSANDO`, `ENTREGUE`, `IGNORADO` ou `FALHA_DEFINITIVA` |
| tentativas | quantidade de tentativas iniciadas |
| disponivelEm | instante da próxima tentativa |
| bloqueadoEm | proteção contra item abandonado em processamento |
| mensagemProvedorId | identificador externo opcional após sucesso |
| codigoUltimoErro | código técnico seguro, sem conteúdo pessoal |
| criadoEm / processadoEm | auditoria temporal |

O payload não contém CPF, token público puro ou texto final da mensagem. Dados de contato são carregados sob autorização no momento da entrega e nunca registrados em log.

## Processamento

Um worker no mesmo aplicativo executa periodicamente:

1. seleciona até 50 eventos elegíveis por `disponivelEm`, ordenados por criação;
2. usa bloqueio de banco com `SKIP LOCKED` para impedir processamento duplicado;
3. marca o item `PROCESSANDO` e incrementa `tentativas`;
4. chama a porta de notificação usando `evento.id` como chave idempotente externa;
5. marca `ENTREGUE` com o identificador do provedor quando houver sucesso;
6. agenda nova tentativa após 1 minuto, 5 minutos, 15 minutos, 1 hora, 3 horas, 6 horas e 12 horas;
7. após a oitava falha, marca `FALHA_DEFINITIVA`.

Um item preso em `PROCESSANDO` além do timeout configurado volta a ser elegível sem incrementar artificialmente a quantidade. O provedor deve receber a mesma chave idempotente em toda tentativa.

## Provedor desabilitado

Sem adaptador concreto, a porta usa `AdaptadorNotificacaoDesabilitado`. O evento termina como `IGNORADO`, com `codigoUltimoErro = PROVEDOR_DESABILITADO`, e não é tentado novamente automaticamente. Isso permite operar e testar a API sem simular entrega real.

## Operação administrativa

Somente `PROPRIETARIO` pode:

- listar eventos por estado e período, sempre com paginação;
- consultar metadados e erro técnico seguro;
- solicitar nova tentativa de um evento `FALHA_DEFINITIVA`.

A nova tentativa altera `FALHA_DEFINITIVA` para `PENDENTE`, limpa o erro técnico e mantém o mesmo `id`. Payload não é editável. Eventos `IGNORADO` por provedor desabilitado não são reenviados retroativamente na V1.

## Fronteira do adaptador

O adaptador recebe um comando com evento, fotografia e contato estritamente necessário. Cabe ao projeto de cliente:

- escolher canal e provedor;
- cadastrar templates e idioma;
- aplicar horários permitidos;
- mapear erros permanentes e temporários;
- proteger credenciais;
- cumprir regras do provedor e da instalação.

Comunicação operacional da reserva e marketing são fluxos separados. A recusa de marketing não suprime os eventos operacionais da tabela acima e nunca autoriza campanhas.

## Observabilidade e segurança

- logs usam `eventoId`, `tipoEvento`, tentativa, duração e código do resultado;
- telefone, nome, CPF, conteúdo e tokens não aparecem em log ou métrica;
- métricas contam pendentes, entregues, ignorados, falhas definitivas e idade do evento mais antigo;
- falha do adaptador nunca altera o resultado da transação da reserva;
- testes usam adaptador falso controlável, sem rede externa.
