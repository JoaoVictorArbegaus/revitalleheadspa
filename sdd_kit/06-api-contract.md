# Contrato inicial da API

Este documento define recursos, nomes públicos e comportamento. Os schemas detalhados serão materializados e validados no OpenAPI durante a implementação, preservando a linguagem ubíqua aqui definida.

Prefixo definitivo: `/api/v1`.

## Convenções

- JSON em requisições e respostas, salvo endpoints operacionais.
- Identificadores públicos não sequenciais.
- Datas locais em `YYYY-MM-DD`.
- Horas locais em `HH:mm` quando não representam um instante.
- Instantes em ISO 8601 com offset ou `Z`.
- Valores monetários como decimal e código de moeda explícito no contrato.
- Durações e intervalos como quantidades inteiras de minutos.
- Paginação obrigatória em coleções administrativas potencialmente grandes.
- Cabeçalho `Chave-Idempotencia` obrigatório em criação, cancelamento e reagendamento públicos.

## Recursos públicos

### `GET /api/v1/publico/servicos`

Lista serviços ativos com o profissional proprietário. Pode aceitar filtro por profissional.

### `GET /api/v1/publico/servicos/{servicoId}/disponibilidade?dataInicial=YYYY-MM-DD&dataFinal=YYYY-MM-DD`

Retorna `servicoId`, fuso, período consultado e dias com opções de `inicioEm` e `fimServicoEm`. Horários usam ISO 8601 com offset do negócio. Cada chamada cobre no máximo 31 dias e respeita `antecedenciaMinimaReservaMinutos` e `horizonteReservaDias`.

### `POST /api/v1/publico/agendamentos`

Cria uma reserva sem autenticação de cliente final.

Entrada conceitual:

- `servicoId`;
- início selecionado com representação temporal definida;
- `cliente.nome`;
- `cliente.celular`;
- `cliente.email`, quando informado;
- `cliente.cpf`, quando permitido ou exigido pela instalação;
- `versaoAvisoPrivacidade` e confirmação de ciência;
- `consentimentoMarketing`, opcional e falso por padrão;
- chave de idempotência no cabeçalho.

Saída conceitual:

- `referencia`;
- estado;
- fotografia pública do serviço;
- data, hora e fuso;
- orientação de confirmação.

A resposta não devolve documento completo nem identificador interno do cliente.

Nome e celular são sempre obrigatórios. O frontend não envia país separadamente; a API aceita formatos brasileiros documentados e normaliza para `+55`. Email e CPF seguem `emailClienteObrigatorio` e `politicaCpf`. O contrato público não revela se esses dados já estavam cadastrados.

A versão enviada deve ser a versão vigente; versão ausente ou antiga retorna validação sem criar a reserva. Recusar marketing não altera a capacidade de reservar.

O cliente envia apenas o `inicioEm` retornado pela disponibilidade. A API calcula os términos, revalida o horário sob bloqueio transacional e responde `409 CONFLITO_AGENDAMENTO` quando outra reserva o ocupou.

### Gestão pública de reserva

- `GET /api/v1/publico/agendamentos/{referencia}`;
- `POST /api/v1/publico/agendamentos/{referencia}/cancelar`;
- `POST /api/v1/publico/agendamentos/{referencia}/reagendar`.

As três operações exigem `X-Token-Agendamento`. O token puro aparece somente na resposta de criação, é associado exclusivamente à referência e nunca é aceito em path ou query string. Token ausente, inválido, revogado ou usado com outra referência recebe resposta neutra equivalente a recurso não encontrado.

Cancelamento e reagendamento também exigem `Chave-Idempotencia`. A chave possui de 16 a 128 caracteres do conjunto documentado, vale por 24 horas e é escopada pelo tipo de operação. Retentativa idêntica devolve o resultado original; conteúdo divergente retorna `409 CONFLITO_IDEMPOTENCIA`.

Reagendamento recebe somente o novo `inicioEm`, mantém serviço e profissional e aplica as mesmas regras de disponibilidade e concorrência da criação. A resposta informa o estado resultante e o novo intervalo. Telefone não participa da autorização.

## Recursos administrativos

- `GET /api/v1/administracao/autenticacao/csrf`;
- `POST /api/v1/administracao/autenticacao/entrar`;
- `GET /api/v1/administracao/autenticacao/eu`;
- `POST /api/v1/administracao/autenticacao/sair`;
- `PUT /api/v1/administracao/autenticacao/senha`;
- `GET|POST /api/v1/administracao/usuarios`;
- `GET|PUT|PATCH /api/v1/administracao/usuarios/{id}`;
- `POST /api/v1/administracao/usuarios/{id}/senha-temporaria`;
- `POST /api/v1/administracao/usuarios/{id}/ativar`;
- `POST /api/v1/administracao/usuarios/{id}/desativar`;
- `GET|POST /api/v1/administracao/profissionais`;
- `GET|PUT|PATCH /api/v1/administracao/profissionais/{id}`;
- `GET|PUT /api/v1/administracao/clientes/{id}`;
- `GET /api/v1/administracao/clientes/{id}/exportar`;
- `POST /api/v1/administracao/clientes/{id}/anonimizar`;
- `GET|POST /api/v1/administracao/recursos`;
- `GET|PUT|PATCH /api/v1/administracao/recursos/{id}`;
- `GET|POST /api/v1/administracao/servicos`;
- `GET|PUT|PATCH /api/v1/administracao/servicos/{id}`;
- `GET|PUT /api/v1/administracao/agenda/semanal`;
- `GET|POST /api/v1/administracao/agenda/excecoes`;
- `DELETE /api/v1/administracao/agenda/excecoes/{id}`;
- `GET|POST /api/v1/administracao/agendamentos`;
- `GET /api/v1/administracao/agendamentos/{id}`;
- `POST /api/v1/administracao/agendamentos/{id}/confirmar`;
- `POST /api/v1/administracao/agendamentos/{id}/rejeitar`;
- `POST /api/v1/administracao/agendamentos/{id}/cancelar`;
- `POST /api/v1/administracao/agendamentos/{id}/concluir`;
- `POST /api/v1/administracao/agendamentos/{id}/nao-compareceu`;
- `GET /api/v1/administracao/eventos-notificacao`;
- `GET /api/v1/administracao/eventos-notificacao/{id}`;
- `POST /api/v1/administracao/eventos-notificacao/{id}/tentar-novamente`.

Recursos administrativos expõem capacidade. Serviços aceitam uma coleção de predefinições com `recursoId`, `quantidadePadrao` e `quantidadeMinima`. Criação e edição administrativa de reservas aceitam a coleção efetiva de alocações com `recursoId` e `quantidade`, respeitando os mínimos vigentes do serviço.

A configuração administrativa expõe `intervaloGradeMinutos`, `antecedenciaMinimaReservaMinutos`, `horizonteReservaDias` e `antecedenciaAlteracaoClienteMinutos`. O contrato de serviço usa `duracaoMinutos` e `intervaloLimpezaMinutos`; não expõe quantidade persistida de unidades de grade. A disponibilidade retorna início e fim reais de cada opção.

Exceções administrativas informam `tipo` como `ABERTURA` ou `BLOQUEIO`. A API rejeita criação ou reagendamento no passado. Operações administrativas fora dos limites públicos continuam sujeitas a expediente, exceções, conflito e capacidade.

A preferência entre comandos explícitos e atualização genérica de estado é por comandos explícitos, pois eles tornam permissões e transições mais claras.

Em confirmação manual, a resposta da criação pública contém estado `PENDENTE` e `expiraEm`. Confirmar ou rejeitar exige `PROPRIETARIO` ou `FUNCIONARIO` vinculado ao profissional. Rejeição recebe motivo obrigatório. Reservas administrativas são criadas diretamente como `CONFIRMADO`.

Exportação, correção integral e anonimização de cliente exigem `PROPRIETARIO`. A resposta de exportação é protegida contra cache, e anonimização recebe motivo obrigatório e falha se houver reserva futura vigente.

Eventos de notificação são visíveis somente ao `PROPRIETARIO`, paginados e filtráveis por estado e período. Apenas evento `FALHA_DEFINITIVA` aceita nova tentativa; payload não é editável e respostas não expõem contato nem conteúdo pessoal.

## Sessão administrativa

- Login recebe email e senha e cria uma sessão HTTP.
- O identificador da sessão usa cookie `HttpOnly`, `SameSite=Lax`, caminho `/api` e `Secure` em produção.
- A sessão expira após 30 minutos de inatividade e não sobrevive ao reinício da aplicação.
- O frontend obtém o token CSRF por endpoint dedicado e o devolve no cabeçalho definido pelo contrato.
- Logout e mudança de senha exigem autenticação e CSRF.
- A mudança de senha exige a senha atual, invalida todas as sessões do usuário e exige novo login.
- Usuário com `deveAlterarSenha` só pode consultar sua identidade, trocar a senha ou encerrar a sessão.
- Gestão de usuários exige papel `PROPRIETARIO`; `FUNCIONARIO` não acessa esses endpoints.
- Endpoints de profissionais, serviços, agendas e reservas aplicam o escopo do profissional vinculado quando o papel é `FUNCIONARIO`.
- `FUNCIONARIO` possui leitura dos recursos operacionais necessários à alocação, mas criação, edição, capacidade, ativação e desativação exigem `PROPRIETARIO`.
- A API não aceita `profissionalId` arbitrário para ampliar o escopo do funcionário; o backend deriva o profissional da sessão.
- Recurso fora do escopo do funcionário responde como não encontrado, sem confirmar sua existência.
- CORS permanece desabilitado na V1 porque o frontend é servido na mesma origem.
- Rotas públicas são autorizadas por método e caminho; `/api/v1/administracao/**` exige autenticação; qualquer rota restante é negada.
- Recuperação de `PROPRIETARIO` não possui endpoint HTTP; é um procedimento operacional executado com a aplicação web parada.

## Modelo de erro

Toda falha usa `Content-Type: application/problem+json`, conforme RFC 9457, e deve conter:

- `type`: URI relativa estável no formato `/problemas/{identificador}`;
- `title`: resumo legível;
- `status`: status HTTP;
- `detail`: explicação segura;
- `instance`: URI `urn:rastreio:{idRastreio}` que identifica a ocorrência;
- `codigo`: código estável em `UPPER_SNAKE_CASE` para tratamento pelo consumidor;
- `idRastreio`: identificador de correlação para suporte;
- `errosCampos`: lista opcional para validação.

Cada item de `errosCampos` contém:

- `campo`: caminho do campo no payload, como `cliente.celular` ou `alocacoes[0].quantidade`;
- `codigo`: regra de validação estável;
- `mensagem`: descrição segura e legível.

O valor rejeitado não é devolvido, porque pode conter dados pessoais ou segredos. A ordem de `errosCampos` não faz parte do contrato.

Status e códigos iniciais:

| Situação | Status | `codigo` |
|---|---:|---|
| corpo ou parâmetro ilegível | `400` | `SOLICITACAO_MALFORMADA` |
| autenticação ausente ou credencial inválida | `401` | `AUTENTICACAO_NECESSARIA` ou `CREDENCIAIS_INVALIDAS` |
| permissão ou CSRF inválido | `403` | `ACESSO_NEGADO` ou `FALHA_VALIDACAO_CSRF` |
| recurso inexistente ou ocultado pelo escopo | `404` | `RECURSO_NAO_ENCONTRADO` |
| método não aceito | `405` | `METODO_NAO_PERMITIDO` |
| conflito de horário ou capacidade | `409` | `CONFLITO_AGENDAMENTO` |
| chave de idempotência reutilizada com conteúdo diferente | `409` | `CONFLITO_IDEMPOTENCIA` |
| celular e CPF apontam para clientes diferentes | `409` | `CONFLITO_IDENTIDADE_CLIENTE` |
| tipo de conteúdo não aceito | `415` | `TIPO_MIDIA_NAO_SUPORTADO` |
| validação de campos | `422` | `FALHA_VALIDACAO` |
| regra de domínio inválida sem conflito de estado | `422` | `VIOLACAO_REGRA_NEGOCIO` |
| limite de requisições excedido | `429` | `LIMITE_REQUISICOES_EXCEDIDO` |
| falha inesperada | `500` | `ERRO_INTERNO` |

Mensagens podem evoluir. `status`, `codigo` e a estrutura documentada são o contrato estável. Os campos `type`, `title`, `status`, `detail` e `instance` preservam os nomes definidos pela RFC 9457; as extensões próprias usam português. Respostas nunca expõem stack trace, mensagem SQL, nomes de tabelas ou detalhes de exceções internas.

## Contrato executável

A especificação OpenAPI gerada ou mantida no repositório será a referência executável do frontend. Alterações incompatíveis exigem versionamento ou plano explícito de migração.
