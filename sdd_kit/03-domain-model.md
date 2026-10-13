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
| preco | valor não negativo com duas casas decimais e moeda definida pela instalação |
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

Representa uma capacidade operacional opcional, como uma sala ou um equipamento, que pode ser exigida por um serviço.

| Campo conceitual | Regra |
|---|---|
| id | identificador interno |
| nome | identificação administrativa |
| capacidade | quantidade positiva de unidades intercambiáveis disponíveis |
| ativo | controla novas reservas que exigem o recurso |

Recursos únicos usam capacidade 1. Uma capacidade maior representa várias unidades equivalentes administradas como um único estoque reservável. O recurso não possui expediente nem bloqueios próprios na API V1.

### RequisitoRecursoServico

Registra um recurso operacional obrigatório para novas reservas de um serviço.

| Campo conceitual | Regra |
|---|---|
| servicoId | serviço configurado |
| recursoOperacionalId | recurso exigido |
| quantidade | quantidade positiva e fixa exigida por reserva |

Um serviço pode não exigir recursos ou exigir vários. A quantidade é aplicada integralmente em toda criação pública ou administrativa e não pode ser ajustada na reserva.

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
- `FUNCIONARIO`: funcionário obrigatoriamente vinculado a um único profissional; mantém os serviços desse profissional e opera sua agenda e reservas, sem gestão de usuários, profissionais ou cadastro, capacidade e estado de recursos.

Não existe papel `GERENTE` na V1. O último `PROPRIETARIO` ativo não pode ser desativado ou rebaixado.

### ConfiguracaoNegocio

Configuração única da instalação com políticas transversais do agendamento.

| Campo conceitual | Regra |
|---|---|
| fusoHorario | fuso IANA recebido por `NEGOCIO_FUSO_HORARIO`; padrão `America/Sao_Paulo` |
| moeda | moeda usada nos preços, recebida por variável da instalação; padrão `BRL` |
| intervaloGradeMinutos | granularidade positiva dos horários de início, recebida por variável da instalação; padrão `15` |
| antecedenciaMinimaReservaMinutos | recebido por `NEGOCIO_ANTECEDENCIA_MINIMA_RESERVA_MINUTOS`; de 0 a 43200; padrão 60 |
| horizonteReservaDias | recebido por `NEGOCIO_HORIZONTE_RESERVA_DIAS`; de 1 a 365; padrão 90 |
| versaoAvisoPrivacidade | versão vigente apresentada antes da reserva |
| textoAvisoPrivacidade | texto vigente apresentado antes da reserva |

Os limites públicos são avaliados pelo relógio do servidor e pelo fuso IANA da instalação.

### Cliente

Representa uma pessoa que realizou uma reserva.

| Campo conceitual | Regra |
|---|---|
| id | identificador interno |
| nome | nome informado mais recente, com 1 a 120 caracteres após `strip`, enquanto o cliente não estiver anonimizado |
| celular | celular brasileiro normalizado como `+55` seguido de 10 ou 11 dígitos, obrigatório e único enquanto não anonimizado |
| anonimizadoEm | instante em que os identificadores foram removidos |
| criadoEm / atualizadoEm | instantes auditáveis |

Cliente não é usuário autenticado na API V1. Reservas repetidas com o mesmo celular reutilizam o cliente e atualizam seu nome canônico, mantendo a fotografia dos dados informados em cada reserva.

### ExpedienteSemanal

Define janelas recorrentes de disponibilidade de um `Profissional` por dia da
semana. Cada janela possui `profissionalId`, `diaSemana`, `inicio` inclusivo e `fim`
exclusivo, com `inicio < fim` no mesmo dia local. Um dia pode ter várias janelas sem
sobreposição; adjacência é permitida. O conjunto inteiro é substituído atomicamente.

### BloqueioAgenda

Remove a disponibilidade de um `Profissional` em um intervalo específico. Possui
`id`, `profissionalId`, `inicioEm` inclusivo e `fimEm` exclusivo, sem motivo,
descrição, categoria ou estado. Bloqueios do mesmo profissional podem ser adjacentes,
mas não sobrepostos. Um bloqueio prevalece sobre o expediente recorrente. A API V1
não oferece abertura extraordinária.

### Agendamento

Representa a reserva de um serviço para um cliente.

| Campo conceitual | Regra |
|---|---|
| id | identificador interno |
| clienteId | cliente associado |
| servicoId | serviço de origem |
| profissionalId | profissional obrigatório do serviço |
| inicioEm | início do atendimento |
| fimServicoEm | término exibido do atendimento |
| ocupadoAte | término do atendimento mais intervalo de limpeza |
| estado | estado atual |
| nomeServicoContratado | nome contratado |
| nomeProfissionalContratado | nome público do profissional na criação |
| duracaoContratadaMinutos | duração contratada |
| precoContratado | preço contratado |
| moedaContratada | código da moeda vigente quando a reserva foi criada |
| nomeClienteInformado | nome informado na criação |
| celularClienteInformado | celular normalizado informado na criação |
| versaoAvisoPrivacidade | versão apresentada ao cliente na criação |
| cienciaAvisoPrivacidadeEm | instante em que o cliente confirmou a ciência |
| origem | `PUBLICO` ou `ADMINISTRACAO` |
| criadoEm / atualizadoEm | instantes auditáveis |
| motivoCancelamento | motivo quando aplicável |

A reserva sempre ocupa o profissional proprietário do serviço e nasce em `CONFIRMADO`, tanto no fluxo público quanto no administrativo.

### AlocacaoRecursoAgendamento

Registra a fotografia dos recursos e quantidades bloqueados automaticamente por uma reserva. Um agendamento pode não possuir alocações ou possuir várias.

| Campo conceitual | Regra |
|---|---|
| agendamentoId | reserva proprietária da alocação |
| recursoOperacionalId | recurso exigido pelo serviço |
| quantidade | quantidade fixa copiada do requisito do serviço |

As alocações são criadas na mesma transação da reserva, não podem ser ajustadas ou realocadas e permanecem independentes de futuras alterações no serviço.

### RegistroIdempotencia

Registra o resultado de uma criação pública concluída com sucesso até `expiraEm`,
com limite exclusivo.

| Campo conceitual | Regra |
|---|---|
| hashChave | hash da chave opaca; o valor recebido não é persistido |
| hashRequisicao | impressão digital determinística do conteúdo relevante |
| statusResposta | status HTTP original |
| corpoResposta | resposta original |
| agendamentoId | reserva produzida |
| criadoEm / expiraEm | início e limite exclusivo da janela de 24 horas |

`hashChave` é único enquanto o registro estiver vigente. `hashRequisicao` representa
o conteúdo relevante depois da normalização de nome, celular e valores temporais.
Não existe token, referência pública ou reprodução por HMAC na API V1.

## Estados de Agendamento

Estados:

- `CONFIRMADO`: horário reservado e vigente;
- `CANCELADO`: reserva cancelada;
- `CONCLUIDO`: atendimento realizado;
- `NAO_COMPARECEU`: cliente não compareceu.

## Transições de estado

| Origem | Destino permitido |
|---|---|
| criação pública ou administrativa | `CONFIRMADO` |
| `CONFIRMADO` | `CANCELADO`, `CONCLUIDO`, `NAO_COMPARECEU` |
| `CANCELADO` | nenhuma na API V1 |
| `CONCLUIDO` | nenhuma na API V1 |
| `NAO_COMPARECEU` | nenhuma na API V1 |

Não há reagendamento nem reabertura de estados terminais na API V1.

## Valores do domínio

- `NumeroCelular`: país, DDD e número em formato canônico.
- `Dinheiro`: valor decimal com duas casas e moeda; nunca ponto flutuante binário.
- `IntervaloTempo`: início anterior ao fim e comparação de sobreposição.
- `FusoHorarioNegocio`: fuso IANA configurado por instalação, com padrão `America/Sao_Paulo`.

## Evoluções previstas, ainda fora do modelo

Unidades físicas distintas, agenda própria de recursos e serviços coletivos com várias vagas por horário permanecem fora da API V1. A capacidade de estoque dos recursos operacionais faz parte da V1.
