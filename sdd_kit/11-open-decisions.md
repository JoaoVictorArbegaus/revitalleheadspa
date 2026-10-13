# Decisões abertas

Não invente respostas durante a implementação. Quando uma decisão for tomada, registre contexto, escolha, consequências e documentos afetados. Depois atualize o estado aqui.

Estados: `OPEN`, `DECIDED`, `SUPERSEDED`.

## Bloqueantes da fundação

### D-001 — Linguagem e framework da API — `DECIDED`

Decisão: Java 21, Spring Boot em monólito modular e Maven Wrapper, seguindo a direção já adotada no KiProdutos.

Consequências: o domínio permanece em módulos internos; não serão introduzidos microserviços ou mensageria distribuída sem necessidade concreta.

### D-002 — Banco de dados e ferramentas de base — `DECIDED`

Decisão: PostgreSQL, Flyway para migrações, Testcontainers para integração, OpenAPI para contrato, Docker Compose para desenvolvimento e imagem Docker como artefato executável.

Consequências: testes críticos de concorrência e restrições usam PostgreSQL real; não serão usados bancos em memória como prova de comportamento de persistência.

### D-024 — Identidade do produto-base — `DECIDED`

Contexto: o repositório genérico precisa de identidade própria antes da fundação e não pode carregar o nome do primeiro cliente.

Decisão: o produto, repositório, imagem e `artifactId` usam `service-scheduling-api`. O repositório oficial é `https://github.com/JoaoVictorArbegaus/service-scheduling-api`. A classe principal será `AplicacaoAgendamentoServicos`. Revitalle será apenas o nome do primeiro projeto derivado depois da V1.

Consequências: nenhum pacote, rota, tabela, variável ou artefato da API-base usa o nome Revitalle; projetos futuros partem de uma tag estável de `service-scheduling-api`.

Documentos afetados: `README.md`, `SESSION.md`, `sdd_kit/01-product-vision.md`, `sdd_kit/08-architecture.md`, `sdd_kit/11-open-decisions.md`, `sdd_kit/13-release-and-client-derivation.md` e `sdd_kit/18-technical-foundation.md`.

### D-025 — Namespace Java e coordenada Maven — `DECIDED`

Contexto: o `groupId` e o pacote raiz afetam todo o código e devem ser definidos antes de F1. Não foi informado um domínio de internet ou namespace GitHub de propriedade do projeto.

Decisão: usar `groupId` `com.servicescheduling` e pacote raiz `com.servicescheduling.api`, mantendo o nome genérico e curto.

Consequências: o nome do produto permanece reconhecível nas coordenadas; módulos e conceitos internos seguem a linguagem ubíqua em português definida em D-026.

Documentos afetados: `SESSION.md`, `TASKS.md`, `sdd_kit/08-architecture.md`, `sdd_kit/11-open-decisions.md` e `sdd_kit/18-technical-foundation.md`.

### D-026 — Linguagem ubíqua e nomenclatura — `DECIDED`

Contexto: o negócio será especificado e desenvolvido em português; misturar nomes de domínio em inglês enfraquece a linguagem comum entre documentação, código e operação.

Decisão: usar português brasileiro, sem acentos nos identificadores técnicos, para conceitos do domínio, pacotes funcionais, classes, métodos, campos, estados, eventos, recursos HTTP, propriedades JSON e nomes do banco. Exemplos centrais: `Agendamento`, `Cliente`, `Profissional`, `Servico`, `RecursoOperacional`, `CONFIRMADO`, `/agendamentos` e `/servicos`.

O nome de distribuição `service-scheduling-api`, o `groupId` `com.servicescheduling`, o pacote técnico raiz `com.servicescheduling.api`, nomes próprios de tecnologias e elementos fixados por padrões externos permanecem em sua forma original. Tipos criados pelo projeto também usam português: `ControladorAgendamento`, `RepositorioAgendamento` e `CalculadoraDisponibilidade`; anotações e interfaces pertencentes ao Spring ou a bibliotecas preservam seus nomes oficiais.

Consequências: o mesmo termo português deve representar o mesmo conceito em requisitos, código, OpenAPI, JSON e banco; sinônimos concorrentes são evitados; toda alteração de vocabulário atualiza o glossário e os contratos afetados.

Documentos afetados: `AGENTS.md`, `SESSION.md`, `TASKS.md` e todo o `sdd_kit/`, especialmente `03-domain-model.md`, `06-api-contract.md`, `08-architecture.md`, `12-glossary.md` e `18-technical-foundation.md`.

### D-004 — Autenticação administrativa — `DECIDED`

Decisão: seguir o modelo do KiProdutos, adaptado para email. Spring Security com email e senha, BCrypt custo 12, sessão HTTP em memória por 30 minutos, cookie de sessão `HttpOnly`, `SameSite=Lax`, caminho `/api` e `Secure` em produção. CSRF usa cookie e cabeçalho próprios. CORS fica desabilitado para frontend na mesma origem. A senha possui entre 12 e 72 bytes em UTF-8, sem regra obrigatória de composição e sem truncamento.

O primeiro `PROPRIETARIO` é criado uma única vez por variáveis de ambiente quando não existe usuário. Bootstrap repetido não duplica conta nem redefine senha. Login previne session fixation; mudança de senha exige a atual, invalida todas as sessões e força novo login. Tentativas de login são limitadas em memória. Rotas públicas são allowlist explícita, rotas administrativas exigem autenticação e as restantes são negadas.

Consequências: reinício encerra sessões; não há JWT, OAuth, login social ou segundo fator na V1; recuperação de acesso fica separada em D-021.

### D-021 — Recuperação de acesso administrativo — `DECIDED`

Decisão: recuperar um `PROPRIETARIO` por modo operacional de linha de comando do mesmo artefato, executado com a aplicação web parada. Email e nova senha vêm de variáveis de ambiente; não existe endpoint público. O comando só redefine `PROPRIETARIO` existente e ativo, aplica a política normal, grava BCrypt 12, audita sem segredo e encerra com código verificável.

Consequências: o reinício posterior elimina sessões em memória; a senha não aparece em argumentos ou logs; o procedimento precisa estar documentado para quem opera cada instalação.

### D-005 — Estrutura de repositórios e derivação — `DECIDED`

Contexto: a API precisa servir como ponto de partida reutilizável sem criar uma plataforma multitenant.

Decisão: este projeto será o repositório-base da API no GitHub. A API publicará versões estáveis. Revitalle e clientes futuros receberão uma cópia integral de uma release, em projetos próprios, e seguirão de forma independente.

Consequências: não haverá vínculo automático, dependência de biblioteca ou obrigação de atualização. Correções precisam ser levadas manualmente às cópias interessadas. Nenhum cliente novo parte do código personalizado de outro.

Documentos afetados: `README.md`, `SESSION.md`, `TASKS.md`, `sdd_kit/01-product-vision.md`, `sdd_kit/08-architecture.md`, `sdd_kit/10-roadmap.md` e `sdd_kit/13-release-and-client-derivation.md`.

### D-027 — Versionamento durante o desenvolvimento — `DECIDED`

Contexto: impedir qualquer push até a V1 deixaria o trabalho sem histórico remoto incremental e confundiria publicação do código em desenvolvimento com publicação de uma versão estável.

Decisão: tarefas coesas e validadas podem gerar commits e pushes para a branch `main` durante toda a implementação. Cada commit mantém juntos código, testes, contrato e documentação afetados. A presença de código em `main` não declara estabilidade. GitHub Release e tag `v1.0.0` permanecem bloqueadas até a conclusão de R2.

Consequências: o GitHub acompanha a evolução e serve como histórico remoto; estados incompletos continuam identificados em `TASKS.md`; projetos de clientes só derivam de tags estáveis, nunca de `main`.

Documentos afetados: `AGENTS.md`, `README.md`, `SESSION.md`, `TASKS.md`, `sdd_kit/11-open-decisions.md` e `sdd_kit/13-release-and-client-derivation.md`.

## Bloqueantes de contrato e domínio

### D-006 — Padrão HTTP de validação — `DECIDED`

Contexto: todos os frontends derivados precisam interpretar falhas da mesma forma, sem depender de mensagens humanas ou de detalhes internos do Spring.

Decisão: adotar RFC 9457 com `Content-Type: application/problem+json` e o `ProblemDetail` nativo do Spring. O corpo usa os campos padronizados `type`, `title`, `status`, `detail` e `instance`, acrescidos de `codigo` estável para tratamento pelo frontend, `idRastreio` para suporte e `errosCampos` quando houver erros de campos.

Usar `400 Bad Request` quando a requisição não puder ser interpretada, por exemplo JSON malformado, tipo incompatível ou parâmetro com formato inválido. Usar `422 Unprocessable Content` quando a requisição estiver sintaticamente correta, mas violar validações de entrada ou regras de domínio que não representem conflito de estado. Conflitos como horário indisponível, sobreposição e reutilização divergente da chave de idempotência usam `409 Conflict`.

Cada item de `errosCampos` contém `campo`, `codigo` e `mensagem`; o valor rejeitado não é devolvido. O corpo nunca expõe stack trace, mensagem SQL, nome de tabela ou detalhes de exceções internas.

Consequências: o frontend decide comportamento por `status` e `codigo`, nunca pelo texto de `detail`; toda resposta de erro da API segue o mesmo media type; códigos publicados tornam-se parte compatível do contrato.

Documentos afetados: `SESSION.md`, `TASKS.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/08-architecture.md`, `sdd_kit/09-quality-strategy.md`, `sdd_kit/11-open-decisions.md` e `sdd_kit/15-api-consumer-documentation.md`.

### D-007 — Grade e duração dos serviços — `DECIDED`

Decisão: cada instalação define a granularidade de início pela variável `NEGOCIO_INTERVALO_GRADE_MINUTOS`, com padrão `15`. Serviços armazenam `duracaoMinutos` e `intervaloLimpezaMinutos`, sem persistir quantidade de unidades de grade. O calendário oferece inícios alinhados à grade e bloqueia o intervalo contínuo completo no profissional e nos recursos.

Consequências: a duração ocupada deve ser múltipla da granularidade; valores incompatíveis são rejeitados sem arredondamento; a grade só pode mudar se os serviços existentes continuarem compatíveis.

### D-019 — Limites temporais da agenda — `DECIDED`

Contexto: consultas e comandos precisam aplicar limites previsíveis sem impedir que a administração resolva exceções reais do negócio.

Decisão: configurar `fusoHorario` por `NEGOCIO_FUSO_HORARIO`, com padrão
`America/Sao_Paulo`; `antecedenciaMinimaReservaMinutos` por
`NEGOCIO_ANTECEDENCIA_MINIMA_RESERVA_MINUTOS`, entre 0 e 43200 e padrão 60; e
`horizonteReservaDias` por `NEGOCIO_HORIZONTE_RESERVA_DIAS`, entre 1 e 365 e padrão
90. Consultas públicas aceitam no máximo 31 datas locais inclusivas. A antecedência
usa instante absoluto; com horizonte `N`, hoje e os `N - 1` dias locais seguintes são
elegíveis.

Esses limites se aplicam à criação pelo cliente final. A criação administrativa também respeita expediente, bloqueios, conflitos e capacidade. A V1 não oferece abertura extraordinária.

A V1 não oferece cancelamento ou reagendamento público nem reagendamento administrativo. Usuário administrativo no escopo pode cancelar uma reserva futura, com motivo auditável. Não se cria reserva para o passado; depois de `inicioEm`, a V1 permite apenas concluir ou marcar ausência e não reabre estados terminais.

Consequências: `BloqueioAgenda` remove disponibilidade do profissional; limites públicos são calculados pelo relógio do servidor e fuso da instalação; expediente, bloqueios, conflitos e capacidade nunca podem ser ignorados; operação posterior à criação exige sessão administrativa.

Documentos afetados: `SESSION.md`, `TASKS.md`, `sdd_kit/03-domain-model.md`, `sdd_kit/04-business-rules.md`, `sdd_kit/05-user-flows.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/09-quality-strategy.md`, `sdd_kit/11-open-decisions.md` e `sdd_kit/16-scheduling-and-availability.md`.

### D-008 — Identidade do cliente — `DECIDED`

Contexto: o cliente final não cria conta, e a V1 precisa identificá-lo operacionalmente com a menor coleta possível.

Decisão: nome e celular são os únicos dados pessoais solicitados ao cliente final. O celular é normalizado para o formato canônico brasileiro com `+55`, sem campo de país no formulário, e é único na instalação. Uma nova reserva com o mesmo celular reutiliza o `Cliente`; por consequência, duas pessoas diferentes não podem compartilhar o mesmo celular na V1.

CPF, email de cliente, documento, consentimento de marketing e campos adicionais ficam fora da V1. O email permanece somente nas contas administrativas conforme D-004.

Ao reutilizar um cliente, o nome canônico recebe o valor mais recente. A reserva mantém somente as cópias pessoais necessárias à operação e todas elas são alcançadas pela anonimização manual. A API pública nunca informa se o celular já existia.

Consequências: troca de celular não é deduzida automaticamente; a V1 não oferece correção integral do cadastro; o índice de unicidade usa o celular normalizado; somente a administração autorizada acessa o cliente no contexto operacional.

Documentos afetados: `SESSION.md`, `TASKS.md`, `sdd_kit/03-domain-model.md`, `sdd_kit/04-business-rules.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/07-data-privacy-security.md`, `sdd_kit/09-quality-strategy.md` e `sdd_kit/11-open-decisions.md`.

### D-009 — Capacidade operacional — `DECIDED`

Decisão: haverá vários profissionais, e cada profissional define seus próprios serviços, preços e durações. Toda reserva bloqueia obrigatoriamente o profissional. Salas e equipamentos são recursos opcionais com capacidade positiva; cada serviço define uma quantidade fixa dos recursos que exige, e todos são bloqueados junto do profissional na mesma transação.

Consequências: a disponibilidade usa a agenda do profissional e a capacidade simultânea dos recursos exigidos; recursos não possuem agenda própria; insuficiência de qualquer recurso impede a reserva inteira.

### D-010 — Privacidade básica e anonimização manual — `DECIDED`

Contexto: o produto-base precisa oferecer privacidade básica verificável sem antecipar um sistema completo de direitos do titular ou retenção automática.

Decisão: cada instalação informa antes de produção a versão vigente do aviso de privacidade. A reserva registra essa versão e o instante da ciência. O cliente final fornece somente nome e celular; CPF, email de cliente, marketing e campos adicionais não são coletados. Logs, métricas e erros não reproduzem dados pessoais, credenciais ou segredos.

Nome e celular permanecem operacionais sob controle de acesso, TLS e criptografia do volume e dos backups. O email administrativo continua protegido pelo mesmo modelo de segurança. Nenhum dado clínico ou de saúde entra na V1.

Somente `PROPRIETARIO` pode anonimizar manualmente um cliente, após verificação operacional do pedido. A ação exige motivo, remove nome, celular e suas cópias pessoais das reservas, preserva valores operacionais sem identificação e é rejeitada enquanto houver reserva futura `CONFIRMADO`. Toda ação é auditada sem copiar os dados pessoais para o log.

A V1 não oferece exportação completa, correção integral, retenção configurável ou anonimização automática. A instalação continua responsável por definir finalidade, base legal, texto e práticas operacionais apropriadas. Depois de restaurar um backup, a operação verifica e reaplica manualmente anonimizações posteriores ao ponto restaurado.

Consequências: a API oferece somente anonimização manual; demais solicitações são tratadas operacionalmente pelo controlador. Funcionalidades completas de titular ou automação de retenção ficam fora da V1 e dependem de nova decisão antes de qualquer implementação.

Documentos afetados: `SESSION.md`, `TASKS.md`, `sdd_kit/03-domain-model.md`, `sdd_kit/04-business-rules.md`, `sdd_kit/05-user-flows.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/07-data-privacy-security.md`, `sdd_kit/09-quality-strategy.md`, `sdd_kit/11-open-decisions.md` e `sdd_kit/15-api-consumer-documentation.md`.

## Integrações

### D-011 — Comunicação externa fora da V1 — `DECIDED`

Contexto: não existe provedor concreto nem necessidade de comunicação aprovada para a API-base V1.

Decisão: notificações, outbox, worker, porta e adaptador de comunicação não entram na V1. Nenhum componente preparatório é criado. Uma integração futura exige necessidade, canal e provedor concretos, nova decisão e tarefa própria.

Consequências: a reserva não depende de comunicação externa; não existe configuração de adaptador desabilitado; uma possibilidade futura não cria tabela, evento, pacote, dependência ou endpoint na V1.

Documentos afetados: `SESSION.md`, `TASKS.md`, `sdd_kit/08-architecture.md`, `sdd_kit/10-roadmap.md`, `sdd_kit/11-open-decisions.md`, `sdd_kit/14-v1-boundaries.md` e `sdd_kit/17-notifications-outbox.md`.

### D-012 — Gatilhos de comunicação futura — `DECIDED`

Contexto: gatilhos, conteúdo e entrega não podem ser definidos corretamente antes de existir uma integração concreta.

Decisão: a V1 não registra eventos de notificação. Tipos de evento, payload, canal, conteúdo, retentativa, idempotência de entrega e observabilidade ficam fora da V1 e só podem ser definidos por nova decisão ligada a um provedor concreto.

Consequências: não existem eventos, estados de entrega, retentativas ou operação administrativa de comunicação na V1. A funcionalidade adiada não autoriza abstrações ou schema antecipados.

Documentos afetados: `AGENTS.md`, `SESSION.md`, `TASKS.md`, `sdd_kit/00-index.md`, `sdd_kit/03-domain-model.md`, `sdd_kit/04-business-rules.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/08-architecture.md`, `sdd_kit/09-quality-strategy.md`, `sdd_kit/11-open-decisions.md`, `sdd_kit/15-api-consumer-documentation.md` e `sdd_kit/17-notifications-outbox.md`.

### D-014 — Usuários, profissionais e permissões — `DECIDED`

Decisão: haverá somente `PROPRIETARIO` e `FUNCIONARIO`; não existe `GERENTE` na V1. O primeiro `PROPRIETARIO` vem do bootstrap. Somente ele cria, altera, ativa, desativa ou redefine a senha temporária de funcionários. O último `PROPRIETARIO` ativo não pode ser desativado ou rebaixado.

Todo `FUNCIONARIO` é vinculado obrigatoriamente a um único profissional. Pode listar recursos e criar, listar, buscar, alterar, ativar e desativar somente os serviços desse profissional, associando recursos existentes e quantidades. Também opera somente a agenda e as reservas do profissional vinculado. Não administra profissionais nem cria ou altera cadastro, capacidade ou estado de recursos. Dados do cliente são visíveis apenas no contexto desses atendimentos. O backend deriva o escopo da sessão, ignora filtros que tentem ampliá-lo e rejeita identificador divergente em comandos.

Consequências: vínculo de profissional é único por conta; acesso cruzado é ocultado; profissional pode existir sem usuário; `PROPRIETARIO` tem visão global e pode opcionalmente estar vinculado a um profissional.

Sequenciamento: F2 cria a tabela mínima de profissional, limitada aos campos já
aprovados `id`, `nome` e `ativo`, apenas para garantir a integridade do vínculo de
acesso. Endpoints e casos de uso de cadastro continuam pertencendo a B1.

### D-028 — Contrato e configuração do catálogo — `DECIDED`

Contexto: B1 precisa fechar a superfície HTTP e a representação de preço antes da implementação do catálogo.

Decisão: B1 inclui `GET /api/v1/publico/servicos`. Profissionais, recursos e serviços usam `PUT` para alteração completa, incluindo o estado ativo, e não possuem `PATCH` nem comandos separados de ativação. `PROPRIETARIO` gerencia todo o catálogo. Preços possuem duas casas decimais e usam a moeda definida pela variável `NEGOCIO_MOEDA`, cujo padrão é `BRL`.

Consequências: o catálogo público mostra somente serviços e profissionais ativos; preço nunca usa ponto flutuante binário; moeda e grade são configuração da instalação, não campos mutáveis do catálogo; clientes HTTP não precisam conciliar semântica parcial de atualização.

Documentos afetados: `README.md`, `TASKS.md`, `sdd_kit/02-scope-and-requirements.md`, `sdd_kit/03-domain-model.md`, `sdd_kit/04-business-rules.md`, `sdd_kit/05-user-flows.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/09-quality-strategy.md`, `sdd_kit/14-v1-boundaries.md` e `sdd_kit/18-technical-foundation.md`.

### D-029 — Escopo da documentação para frontend — `DECIDED`

Contexto: R1 deve entregar o material necessário para desenvolver o frontend, sem repetir como critério a documentação operacional já entregue pela fundação.

Decisão: R1 é “Contrato da API para desenvolvimento do frontend” e entrega OpenAPI fiel, guia curto de autenticação, CSRF, permissões, estados, erros e idempotência e exemplos executáveis `.http`.

Consequências: instalação, banco, Flyway, Docker, backup, rollback e documentação para terceiros não são critérios de R1. A documentação operacional existente de F1 e F2 permanece válida e não é removida.

Documentos afetados: `README.md`, `TASKS.md`, `sdd_kit/00-index.md`, `sdd_kit/10-roadmap.md`, `sdd_kit/11-open-decisions.md`, `sdd_kit/15-api-consumer-documentation.md` e `sdd_kit/18-technical-foundation.md`.

### D-030 — Contrato administrativo da agenda e recorte de B2 — `DECIDED`

Contexto: B2 precisa definir agenda e disponibilidade sem antecipar a persistência de reservas de B3.

Decisão: expediente semanal é substituído integralmente por `PUT`; cada janela fica
no mesmo dia local, aceita adjacência e rejeita sobreposição. Bloqueios possuem
somente profissional, início e fim, aceitam adjacência e rejeitam sobreposição.
Leituras recebem o profissional por query e escritas no corpo; o backend sempre
aplica o escopo derivado da sessão. Listagens temporais cobrem no máximo 31 datas
locais inclusivas. B2 calcula disponibilidade com catálogo, recursos ativos, grade,
expediente, bloqueios e limites temporais, sem criar reserva, alocação ou abstração
preparatória de ocupações.

Consequências: B3 incorpora reservas confirmadas e capacidade consumida ao cálculo
sem alterar o contrato público. Lista vazia limpa o expediente; períodos válidos sem
horários retornam sucesso com lista vazia.

Documentos afetados: `TASKS.md`, `SESSION.md`, `README.md`, `sdd_kit/03-domain-model.md`, `sdd_kit/04-business-rules.md`, `sdd_kit/05-user-flows.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/09-quality-strategy.md`, `sdd_kit/11-open-decisions.md`, `sdd_kit/16-scheduling-and-availability.md` e `sdd_kit/18-technical-foundation.md`.

### D-031 — Limite compartilhado das rotas públicas — `DECIDED`

Contexto: catálogo e disponibilidade são públicos e precisam atender ao requisito de proteção contra abuso sem introduzir infraestrutura incompatível com o perfil da V1.

Decisão: todas as rotas em `/api/v1/publico/**` compartilham um limite em memória
por origem. `PUBLICO_REQUISICOES_MAXIMO` define a quantidade por janela, padrão 120,
e `PUBLICO_REQUISICOES_JANELA` define a janela, padrão um minuto. Excesso retorna
`429 LIMITE_REQUISICOES_PUBLICAS` e `Retry-After`.

Consequências: o mecanismo é local à única instância prevista para a V1, limpa
janelas vencidas sem job e não usa Redis ou armazenamento distribuído. Instalações
atrás de proxy seguem a política de cabeçalhos encaminhados já definida.

Documentos afetados: `README.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/07-data-privacy-security.md`, `sdd_kit/09-quality-strategy.md`, `sdd_kit/11-open-decisions.md` e `sdd_kit/18-technical-foundation.md`.

### D-032 — Contrato operacional da reserva pública — `DECIDED`

Contexto: B3 precisa fechar aviso de privacidade, validação, resposta e ciclo da
idempotência antes de persistir reservas, sem antecipar a operação administrativa de
B4 nem criar credencial pública pós-reserva.

Decisão: `GET /api/v1/publico/aviso-privacidade` devolve `versao` e `texto` vigentes,
configurados pela instalação. `POST /api/v1/publico/agendamentos` exige o cabeçalho
`Chave-Idempotencia` no formato `[A-Za-z0-9._~-]{16,128}`. O nome possui de 1 a 120
caracteres após `strip`. O celular brasileiro aceita formatação comum, com `+55`
opcional, e é normalizado para `+55` seguido de 10 ou 11 dígitos.

A criação bem-sucedida retorna `201`, sem `Location`, `agendamentoId`, `clienteId`,
referência ou token. O corpo contém estado, fotografias públicas contratadas de
serviço e profissional, `inicioEm`, `fimServicoEm`, fuso, preço e moeda. O snapshot
de `Agendamento` persiste `moedaContratada` junto de `precoContratado`.

A impressão digital idempotente usa o conteúdo relevante depois das normalizações.
O resultado permanece válido somente enquanto `agora < expiraEm`; no instante
`expiraEm` a chave pode iniciar novo comando. Registros vencidos são removidos
oportunisticamente pelo fluxo de criação, sem job. O perfil `prod` exige versão e
texto não vazios do aviso. B3 implementa somente a criação pública e seu efeito na
disponibilidade; criação e operações administrativas de reservas pertencem a B4.

Consequências: retentativas semanticamente iguais compartilham o resultado mesmo
quando nome ou celular chegam em formatos equivalentes; a confirmação inicial não
expõe identificadores internos nem meio de acesso posterior; preço e moeda históricos
não dependem da configuração atual; a instalação não entra em produção sem o aviso.

Documentos afetados: `TASKS.md`, `SESSION.md`, `sdd_kit/03-domain-model.md`,
`sdd_kit/04-business-rules.md`, `sdd_kit/05-user-flows.md`,
`sdd_kit/06-api-contract.md`, `sdd_kit/07-data-privacy-security.md`,
`sdd_kit/09-quality-strategy.md`, `sdd_kit/11-open-decisions.md`,
`sdd_kit/16-scheduling-and-availability.md` e `sdd_kit/18-technical-foundation.md`.

### D-033 — Contrato operacional da administração de reservas e anonimização — `DECIDED`

Contexto: B4 precisa fechar representação, filtros, transições, auditoria e
anonimização antes de alterar o banco, sem criar infraestrutura genérica de eventos.

Decisão: a administração usa uma única representação de agendamento em listagem,
detalhe, criação e transições, com identificadores, estado, origem, fotografias do
serviço, do profissional e do cliente, intervalos, fuso, preço, moeda, aviso,
recursos alocados e campos de auditoria. A listagem exige período de até 31 datas
locais pelo início, aceita `profissionalId` e `estado` opcionais e ordena por
`inicioEm` e `id`; para `FUNCIONARIO` o profissional é sempre o vinculado.

A criação administrativa recebe o mesmo corpo da criação pública, exige
`Chave-Idempotencia` e CSRF, retorna `201` com `Location` e a representação
administrativa e registra origem `ADMINISTRACAO` e autor. Ela ignora antecedência e
horizonte públicos, rejeita início anterior ao instante atual e mantém grade,
expediente, bloqueios, conflito, capacidade e concorrência. A impressão digital
inclui o autor; a retentativa válida devolve `201` com a representação atual da
reserva produzida, de modo que o registro idempotente não guarda dados pessoais.

Cancelar exige motivo de 1 a 500 caracteres após `strip` e só é aceito antes de
`inicioEm`; concluir e registrar ausência não recebem corpo e só são aceitos a partir
de `inicioEm`. Toda transição parte de `CONFIRMADO`, bloqueia a reserva, grava
`estadoAlteradoEm` e `estadoAlteradoPor` e retorna `200`. Transição fora do estado
ou da janela retorna `409 TRANSICAO_AGENDAMENTO_INVALIDA`.

A anonimização é exclusiva de `PROPRIETARIO`, exige motivo de 1 a 500 caracteres,
bloqueia o cliente antes de verificar reservas `CONFIRMADO` com início futuro e
retorna `204`. Ela limpa nome e celular do cliente e das fotografias de todas as suas
reservas, grava `anonimizadoEm` e registra em `anonimizacao_cliente` cliente,
autor, instante e motivo. `GET /api/v1/administracao/clientes/{id}` também é exclusivo
de `PROPRIETARIO`; o identificador do cliente é obtido pela representação
administrativa da reserva, sem busca por celular.

Consequências: autoria e instantes ficam consultáveis no próprio banco sem tabela
genérica de auditoria; um cliente anonimizado deixa de ocupar o índice de celular,
e uma nova reserva com o mesmo número cria outro cliente; o motivo é texto
operacional e não deve conter dados pessoais.

Documentos afetados: `TASKS.md`, `SESSION.md`, `sdd_kit/03-domain-model.md`,
`sdd_kit/04-business-rules.md`, `sdd_kit/06-api-contract.md`,
`sdd_kit/07-data-privacy-security.md`, `sdd_kit/09-quality-strategy.md` e
`sdd_kit/11-open-decisions.md`.

### D-015 — Confirmação automática — `DECIDED`

Contexto: a V1 precisa de um único fluxo de reserva correto e simples, sem processo periódico ou estados transitórios de aprovação.

Decisão: toda reserva pública ou administrativa criada com sucesso nasce `CONFIRMADO`. Não existe configuração de modo de confirmação.

Consequências: `PENDENTE`, `EXPIRADO`, `expiraEm`, prazo de aprovação, confirmação e rejeição ficam fora da V1. Aprovação manual futura depende de nova decisão e não autoriza campos, jobs ou endpoints preparatórios.

Documentos afetados: `SESSION.md`, `TASKS.md`, `sdd_kit/02-scope-and-requirements.md`, `sdd_kit/03-domain-model.md`, `sdd_kit/04-business-rules.md`, `sdd_kit/05-user-flows.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/09-quality-strategy.md` e `sdd_kit/11-open-decisions.md`.

### D-016 — Ausência de acesso público pós-reserva — `DECIDED`

Contexto: oferecer operação pública posterior exigiria credencial, recuperação e superfície de segurança adicionais que não são necessárias à V1 reduzida.

Decisão: a criação devolve somente a confirmação necessária ao fluxo atual, sem referência pública ou token. A API não expõe rota pública de operação posterior.

Consulta e cancelamento depois da criação exigem sessão administrativa e escopo autorizado. Celular nunca autoriza acesso, e reagendamento não pertence à V1.

Consequências: não existem referência pública, hash de token, segredo de token, recuperação pública ou endpoint público pós-reserva. Autoatendimento e reagendamento futuros dependem de novas decisões. Mudar horário ou serviço exige cancelar e criar outra reserva.

Documentos afetados: `SESSION.md`, `TASKS.md`, `sdd_kit/02-scope-and-requirements.md`, `sdd_kit/03-domain-model.md`, `sdd_kit/04-business-rules.md`, `sdd_kit/05-user-flows.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/07-data-privacy-security.md`, `sdd_kit/09-quality-strategy.md`, `sdd_kit/11-open-decisions.md` e `sdd_kit/15-api-consumer-documentation.md`.

### D-017 — Seleção e capacidade de recursos operacionais — `DECIDED`

Decisão: recursos são específicos, como `Sala 1` ou `Aparelho X`, e possuem capacidade positiva. Cada serviço associa zero ou mais recursos com uma quantidade fixa positiva. A reserva copia essas associações e bloqueia exatamente essas quantidades junto do profissional.

Consequências: capacidade 1 representa um item único; capacidade maior representa unidades equivalentes; disponibilidade considera a soma das alocações sobrepostas. A V1 não permite escolher, remover, substituir ou ajustar quantidades por reserva.

### D-018 — Quantidade fixa dos recursos do serviço — `DECIDED`

Contexto: o catálogo simples precisa representar os recursos exigidos sem mínimos, padrões ou edição por reserva.

Decisão: cada associação entre serviço e recurso possui somente uma `quantidade` positiva, limitada pela capacidade atual do recurso. Toda reserva do serviço usa exatamente essa configuração.

Consequências: `0 < quantidade <= capacidade`; não existem `quantidadePadrao`, `quantidadeMinima` ou recursos adicionais por reserva. Reservas antigas preservam suas alocações históricas quando a configuração do serviço muda.

Documentos afetados: `SESSION.md`, `TASKS.md`, `sdd_kit/02-scope-and-requirements.md`, `sdd_kit/03-domain-model.md`, `sdd_kit/04-business-rules.md`, `sdd_kit/05-user-flows.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/09-quality-strategy.md`, `sdd_kit/11-open-decisions.md` e `sdd_kit/16-scheduling-and-availability.md`.

### D-020 — Perfil de escala da V1 — `DECIDED`

Decisão: a V1 atende instalações pequenas ou médias e de baixo fluxo. A referência de maior porte realista é uma barbearia com quatro profissionais e aproximadamente dez serviços por profissional.

Consequências: uma instância Spring Boot e um PostgreSQL são suficientes; não haverá escala horizontal, cache distribuído, broker ou busca externa; o histórico será paginado; concorrência será testada para correção, sem engenharia para carga massiva. Os números são referência de dimensionamento, não limites rígidos do domínio.

### D-022 — Garantia concorrente da agenda — `DECIDED`

Contexto: a disponibilidade consultada pelo frontend pode ficar desatualizada antes da criação, e recursos com capacidade maior que um exigem somar alocações concorrentes.

Decisão: o PostgreSQL é a autoridade. A criação bloqueia, na mesma transação, a linha do profissional e as linhas dos recursos envolvidos em ordem determinística; depois revalida sobreposições e capacidades e grava cliente, reserva e alocações atomicamente. O frontend não cria bloqueio ao selecionar um horário.

Consequências: requisições sem participantes comuns continuam paralelas; disputas são serializadas no banco; conflito retorna `409 CONFLITO_AGENDAMENTO`; testes com PostgreSQL real comprovam profissional, recurso e capacidade. Não haverá lock distribuído, Redis ou tabela de unidades de grade na V1.

Documentos afetados: `AGENTS.md`, `sdd_kit/00-index.md`, `sdd_kit/04-business-rules.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/09-quality-strategy.md`, `sdd_kit/11-open-decisions.md` e `sdd_kit/16-scheduling-and-availability.md`.

### D-023 — Idempotência das operações públicas — `DECIDED`

Contexto: repetição da criação pública causada por duplo clique, timeout ou perda da resposta não pode duplicar reservas.

Decisão: exigir `Chave-Idempotencia` na criação pública. A chave possui de 16 a 128 caracteres seguros, é tratada como opaca e seu hash é mantido por 24 horas junto da impressão digital do payload e do resultado interno.

Repetir a criação com a mesma chave e conteúdo devolve o resultado original sem repetir a reserva. Reutilizar a chave com conteúdo diferente retorna `409 CONFLITO_IDEMPOTENCIA`. Uma restrição única e bloqueio transacional coordenam repetições simultâneas.

Somente comandos concluídos com sucesso são mantidos como resultado idempotente. Validação, conflito de disponibilidade e falha inesperada não consomem a chave; uma tentativa simultânea aguarda o comando dono da chave e então recebe seu resultado. A limpeza de registros vencidos é idempotente e executada pelo próprio aplicativo.

Consequências: o frontend cria uma chave por intenção de reserva e a reutiliza somente nas retentativas daquela intenção; não existe segredo de token de agendamento na V1.

Documentos afetados: `SESSION.md`, `TASKS.md`, `sdd_kit/03-domain-model.md`, `sdd_kit/04-business-rules.md`, `sdd_kit/05-user-flows.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/07-data-privacy-security.md`, `sdd_kit/09-quality-strategy.md`, `sdd_kit/11-open-decisions.md`, `sdd_kit/15-api-consumer-documentation.md` e `sdd_kit/16-scheduling-and-availability.md`.

## Projetos de clientes

### D-003 — Tecnologia dos frontends dos clientes — `DECIDED`

Contexto: o repositório-base precisa permanecer consumível por diferentes interfaces sem antecipar escolhas visuais ou técnicas da Revitalle.

Decisão: a API V1 não escolhe nem contém tecnologia de frontend. A escolha acontece no projeto derivado da Revitalle e pode ser diferente em clientes futuros, desde que consuma o contrato HTTP versionado.

Consequências: nenhum framework web entra nas dependências ou decisões do produto-base; OpenAPI e exemplos executáveis são suficientes para desenvolver um frontend posteriormente.

Documentos afetados: `README.md`, `SESSION.md`, `TASKS.md`, `sdd_kit/01-product-vision.md`, `sdd_kit/02-scope-and-requirements.md`, `sdd_kit/11-open-decisions.md`, `sdd_kit/13-release-and-client-derivation.md` e `sdd_kit/14-v1-boundaries.md`.

### D-013 — Hospedagem e implantação por cliente — `DECIDED`

Contexto: o produto-base precisa ser executável e reproduzível sem acoplar todos os clientes ao mesmo provedor, seguindo a direção já usada no KiProdutos.

Decisão: o produto-base entrega imagem Docker da API, Docker Compose para desenvolvimento local e documentação de execução com PostgreSQL externo. Não escolhe nuvem, domínio ou banco gerenciado.

Cada projeto de cliente mantém ambientes, segredos, banco, backups e domínio próprios. Em produção, usa uma instância do container da API e um PostgreSQL; o frontend estático fica no mesmo domínio por proxy reverso e encaminha `/api` para a aplicação. Segredos entram por variáveis protegidas ou gerenciador da plataforma, nunca na imagem ou no repositório.

Flyway aplica migrações forward-only na inicialização da única instância. Atualização troca a imagem após backup; rollback do código usa a imagem anterior, e incompatibilidade de banco exige restauração do backup ou migração corretiva documentada. Hospedagem, proxy, certificados, backup e monitoramento concretos são fechados no projeto do cliente antes de produção.

Consequências: cada cliente mantém dados, segredos e operação isolados; o mesmo artefato roda em ambientes diferentes por configuração; Compose local não é apresentado como topologia de produção; não há Kubernetes, banco compartilhado ou atualização automática das cópias.

Documentos afetados: `README.md`, `SESSION.md`, `TASKS.md`, `sdd_kit/08-architecture.md`, `sdd_kit/11-open-decisions.md`, `sdd_kit/13-release-and-client-derivation.md` e `sdd_kit/15-api-consumer-documentation.md`.

## Modelo para registrar uma decisão

```md
### D-XXX — Título — `DECIDED`

Contexto: por que a escolha é necessária.

Decisão: o que foi escolhido.

Consequências: benefícios, custos e restrições aceitos.

Documentos afetados: lista de arquivos atualizados.
```
