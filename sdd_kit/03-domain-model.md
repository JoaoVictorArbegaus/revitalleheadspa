# Modelo de domínio

## Entidades principais

### Servico

Representa um serviço próprio de um profissional. Não existe catálogo compartilhado nem reutilização do mesmo registro por profissionais diferentes.

| Campo conceitual | Regra |
|---|---|
| id | identificador interno não previsível |
| profissionalId | profissional proprietário do serviço |
| nome | obrigatório e exibido ao cliente |
| descricao | opcional |
| duracaoMinutos | duração positiva em minutos |
| intervaloLimpezaMinutos | intervalo opcional após o atendimento |
| preco | valor não negativo com moeda definida pela instalação |
| ativo | controla novas reservas |
| ordemExibicao | ordenação pública estável |

Dois profissionais podem oferecer serviços com o mesmo nome, mas cada registro possui preço, duração, descrição e ciclo de vida próprios.

### Profissional

Representa a pessoa que oferece e executa serviços. Todo serviço pertence exatamente a um profissional, que sempre ocupa sua agenda durante a reserva.

| Campo conceitual | Regra |
|---|---|
| id | identificador interno |
| nome | identificação administrativa e pública |
| ativo | controla novas disponibilidades e reservas |

O vínculo de acesso é mantido em `UsuarioAdministrativo.profissionalId`, evitando duas fontes para a mesma relação.

### RecursoOperacional

Representa uma sala ou equipamento que pode ser exigido por um serviço. É opcional: serviços que dependem somente do profissional não alocam recursos operacionais.

| Campo conceitual | Regra |
|---|---|
| id | identificador interno |
| tipo | `SALA` ou `EQUIPAMENTO` |
| nome | identificação administrativa |
| capacidade | quantidade positiva de unidades intercambiáveis disponíveis |
| ativo | controla novas alocações |

Recursos únicos, como uma sala específica, usam capacidade 1. Uma capacidade maior representa várias unidades equivalentes administradas como um único estoque reservável.

### PredefinicaoRecursoServico

Registra a pré-seleção de um recurso específico para um serviço.

| Campo conceitual | Regra |
|---|---|
| servicoId | serviço configurado |
| recursoOperacionalId | sala ou equipamento específico |
| quantidadePadrao | quantidade positiva usada na reserva pública e sugerida na administração |
| quantidadeMinima | quantidade mínima entre zero e `quantidadePadrao` |

A reserva pública usa a quantidade padrão. Operações administrativas podem ajustar as alocações, mas nunca reduzir uma predefinição abaixo de `quantidadeMinima`; zero permite remover ou substituir totalmente o recurso.

### UsuarioAdministrativo

Representa uma conta autenticada da instalação. Conta de acesso e profissional são conceitos diferentes: o proprietário pode não prestar serviços, e um profissional pode existir sem receber acesso ao sistema.

| Campo conceitual | Regra |
|---|---|
| id | identificador interno |
| nome | nome de exibição |
| email | identificador de autenticação normalizado e único |
| hashSenha | BCrypt com custo 12; senha nunca é persistida |
| papel | um dos papéis definidos em D-014 |
| profissionalId | obrigatório e único para `FUNCIONARIO`; opcional para `PROPRIETARIO` |
| ativo | bloqueia novos acessos sem apagar histórico |
| deveAlterarSenha | verdadeiro para credencial temporária criada pelo proprietário |

Papéis existentes na V1:

- `PROPRIETARIO`: proprietário com administração global e gestão de usuários; pode também representar um profissional;
- `FUNCIONARIO`: funcionário obrigatoriamente vinculado a um único profissional, sem gestão de usuários.

Não existe papel `GERENTE` na V1. O último `PROPRIETARIO` ativo não pode ser desativado ou rebaixado.

### ConfiguracaoNegocio

Configuração única da instalação com políticas transversais do agendamento.

| Campo conceitual | Regra |
|---|---|
| fusoHorario | fuso IANA |
| moeda | moeda usada nos preços |
| intervaloGradeMinutos | granularidade positiva dos horários de início |
| modoConfirmacao | `AUTOMATICA` ou `MANUAL` |
| prazoAprovacaoPendenteMinutos | de 15 a 1440; padrão 720 quando o modo é `MANUAL` |
| antecedenciaMinimaReservaMinutos | de 0 a 43200; padrão 60 |
| horizonteReservaDias | de 1 a 365; padrão 90 |
| antecedenciaAlteracaoClienteMinutos | de 0 a 43200; padrão 1440 |
| emailClienteObrigatorio | falso por padrão |
| politicaCpf | `DESABILITADA`, `OPCIONAL` ou `OBRIGATORIA`; padrão `OPCIONAL` |
| versaoAvisoPrivacidade | obrigatório antes de aceitar reservas em produção |
| contatoTitular | canal informado pelo controlador |
| retencaoDadosPessoaisDias | prazo positivo definido pela instalação antes de produção |

Os limites públicos são avaliados pelo relógio do servidor e pelo fuso IANA da instalação.

### Cliente

Representa uma pessoa que realizou ou tentou realizar uma reserva válida.

| Campo conceitual | Regra |
|---|---|
| id | identificador interno |
| nome | nome informado mais recente |
| celular | celular brasileiro normalizado com `+55`, obrigatório e único |
| email | contato mais recente; obrigatório somente quando configurado |
| cpfCifrado / hmacBuscaCpf | CPF cifrado e índice protegido, quando existir |
| anonimizadoEm | instante em que os identificadores foram removidos |
| criadoEm / atualizadoEm | instante auditável |

Cliente não é usuário autenticado na API V1.

CPF é um documento associado ao cliente conforme `politicaCpf`; email é contato, não documento. A V1 não aceita outros tipos de documento. Reservas repetidas com o mesmo celular reutilizam o cliente e atualizam seus dados canônicos, mantendo fotografia dos dados informados em cada reserva.

### ExpedienteSemanal

Define janelas recorrentes de disponibilidade de um `Profissional` ou `RecursoOperacional` por dia da semana. Um dia pode ter mais de uma janela, desde que não haja sobreposição.

### ExcecaoAgenda

Altera a agenda de um `Profissional` ou `RecursoOperacional` em uma data ou intervalo específico. O tipo `ABERTURA` adiciona um horário extraordinário e o tipo `BLOQUEIO` remove disponibilidade. Quando se sobrepõem, `BLOQUEIO` prevalece.

### Agendamento

Representa a reserva de um serviço para um cliente.

| Campo conceitual | Regra |
|---|---|
| id | identificador interno |
| referenciaPublica | referência aleatória adequada para comunicação externa |
| hashTokenAcesso | hash do token público de 256 bits; o token puro não é persistido |
| clienteId | cliente associado |
| servicoId | serviço de origem |
| profissionalId | profissional obrigatório do serviço |
| inicioEm | início do atendimento |
| fimServicoEm | término exibido do atendimento |
| ocupadoAte | término do atendimento mais intervalo de limpeza |
| estado | estado atual |
| nomeServicoContratado | nome contratado |
| duracaoContratadaMinutos | duração contratada |
| precoContratado | preço contratado |
| nomeClienteInformado | nome informado na criação |
| celularClienteInformado | celular normalizado informado na criação |
| emailClienteInformado | email informado na criação, quando houver |
| versaoAvisoPrivacidadeRegistrada | versão apresentada na criação |
| cienciaAvisoPrivacidadeEm | instante da ciência registrada |
| consentimentoMarketing | opcional e falso por padrão; separado da reserva |
| origem | `PUBLICO` ou `ADMINISTRACAO` na API V1 |
| expiraEm | obrigatório apenas enquanto o estado for `PENDENTE` |
| criadoEm / atualizadoEm | instantes auditáveis |
| motivoCancelamento | motivo quando aplicável |

A reserva sempre ocupa o profissional proprietário do serviço.

Referência e token autorizam somente operações públicas daquela reserva. O acesso é revogado em estado terminal ou quando `inicioEm` é alcançado. Reagendamento preserva referência e token, não troca serviço ou profissional e move o intervalo atomicamente.

### AlocacaoRecursoAgendamento

Registra cada sala ou equipamento concreto bloqueado por uma reserva e sua quantidade. Um agendamento pode não possuir nenhuma alocação operacional ou possuir várias. As alocações efetivas podem diferir da pré-seleção do serviço e devem ser gravadas e protegidas contra conflito na mesma transação da reserva.

| Campo conceitual | Regra |
|---|---|
| agendamentoId | reserva proprietária da alocação |
| recursoOperacionalId | recurso específico alocado |
| quantidade | quantidade positiva bloqueada |

As alocações da reserva formam uma fotografia operacional independente de futuras alterações no serviço.

### EventoOutbox

Registra um fato do domínio que precisa ser entregue a um processo externo, como uma confirmação. Deve ser persistido atomicamente com a mudança que o originou.

Possui tipo e versão do evento, identificador e versão da reserva, payload mínimo versionado, estado de processamento, tentativas, próxima tentativa, bloqueio temporário, resultado externo e instantes de auditoria. Os estados são `PENDENTE`, `PROCESSANDO`, `ENTREGUE`, `IGNORADO` e `FALHA_DEFINITIVA`.

### RegistroIdempotencia

Registra por 24 horas o resultado de uma escrita pública concluída com sucesso.

| Campo conceitual | Regra |
|---|---|
| operacao | criação, cancelamento ou reagendamento |
| hashChave | hash da chave opaca; o valor recebido não é persistido |
| hashRequisicao | impressão digital determinística do conteúdo relevante |
| statusResposta | status HTTP original |
| corpoResposta | resposta original sem segredo persistido em texto puro |
| agendamentoId | reserva produzida ou alterada |
| criadoEm / expiraEm | controle da janela de 24 horas |

A combinação `operacao + hashChave` é única. O token de acesso da resposta de criação é reproduzido por HMAC a partir da referência e do segredo da instalação.

## Estados de Agendamento

Estados:

- `PENDENTE`: criada e aguardando aprovação quando a instalação usa confirmação manual;
- `CONFIRMADO`: horário reservado e vigente;
- `CANCELADO`: reserva cancelada;
- `EXPIRADO`: solicitação pendente expirada automaticamente;
- `CONCLUIDO`: atendimento realizado;
- `NAO_COMPARECEU`: cliente não compareceu.

Instalações com confirmação automática criam reservas públicas diretamente em `CONFIRMADO`. No modo manual, a reserva pública nasce `PENDENTE`; reservas administrativas nascem `CONFIRMADO`. O envio de WhatsApp, por si só, não define o estado da reserva.

## Transições de estado

| Origem | Destino permitido |
|---|---|
| criação pública em modo automático | `CONFIRMADO` |
| criação pública em modo manual | `PENDENTE` |
| criação administrativa | `CONFIRMADO` |
| `PENDENTE` | `CONFIRMADO`, `CANCELADO`, `EXPIRADO` |
| `CONFIRMADO` | `CANCELADO`, `CONCLUIDO`, `NAO_COMPARECEU` |
| `CANCELADO` | nenhuma na API V1 |
| `EXPIRADO` | nenhuma na API V1 |
| `CONCLUIDO` | nenhuma na API V1 |
| `NAO_COMPARECEU` | nenhuma na API V1 |

Correções administrativas posteriores devem ser tratadas como decisão explícita de auditoria.

## Valores do domínio

- `DocumentoCliente`: tipo e valor normalizado conforme a política da instalação.
- `NumeroCelular`: país, DDD e número em formato canônico.
- `EnderecoEmail`: contato eletrônico validado e independente de documento.
- `Dinheiro`: valor decimal e moeda; nunca ponto flutuante binário.
- `IntervaloTempo`: início anterior ao fim e comparação de sobreposição.
- `FusoHorarioNegocio`: fuso IANA configurado por instalação.

## Evoluções previstas, ainda fora do modelo

Unidades físicas distintas e serviços coletivos com várias vagas por horário permanecem fora da API V1. A capacidade de estoque dos recursos operacionais faz parte da V1.
