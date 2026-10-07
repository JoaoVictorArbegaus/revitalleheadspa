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

Decisão: usar português brasileiro, sem acentos nos identificadores técnicos, para conceitos do domínio, pacotes funcionais, classes, métodos, campos, estados, eventos, recursos HTTP, propriedades JSON e nomes do banco. Exemplos centrais: `Agendamento`, `Cliente`, `Profissional`, `Servico`, `RecursoOperacional`, `PENDENTE`, `CONFIRMADO`, `/agendamentos` e `/servicos`.

O nome de distribuição `service-scheduling-api`, o `groupId` `com.servicescheduling`, o pacote técnico raiz `com.servicescheduling.api`, nomes próprios de tecnologias e elementos fixados por padrões externos permanecem em sua forma original. Tipos criados pelo projeto também usam português: `ControladorAgendamento`, `RepositorioAgendamento` e `CalculadoraDisponibilidade`; anotações e interfaces pertencentes ao Spring ou a bibliotecas preservam seus nomes oficiais.

Consequências: o mesmo termo português deve representar o mesmo conceito em requisitos, código, OpenAPI, JSON e banco; sinônimos concorrentes são evitados; toda alteração de vocabulário atualiza o glossário e os contratos afetados.

Documentos afetados: `AGENTS.md`, `SESSION.md`, `TASKS.md` e todo o `sdd_kit/`, especialmente `03-domain-model.md`, `06-api-contract.md`, `08-architecture.md`, `12-glossary.md` e `18-technical-foundation.md`.

### D-004 — Autenticação administrativa — `DECIDED`

Decisão: seguir o modelo do KiProdutos, adaptado para email. Spring Security com email e senha, BCrypt custo 12, sessão HTTP em memória por 30 minutos, cookie de sessão `HttpOnly`, `SameSite=Lax`, caminho `/api` e `Secure` em produção. CSRF usa cookie e cabeçalho próprios. CORS fica desabilitado para frontend na mesma origem.

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

Decisão: cada instalação define uma granularidade de início em minutos, como 10 ou 15. Serviços armazenam `duracaoMinutos` e `intervaloLimpezaMinutos`, sem persistir quantidade de unidades de grade. O calendário oferece inícios alinhados à grade e bloqueia o intervalo contínuo completo no profissional e nos recursos.

Consequências: a duração ocupada deve ser múltipla da granularidade; valores incompatíveis são rejeitados sem arredondamento; a grade só pode mudar se os serviços existentes continuarem compatíveis.

### D-019 — Limites temporais da agenda — `DECIDED`

Contexto: consultas e comandos precisam aplicar limites previsíveis sem impedir que a administração resolva exceções reais do negócio.

Decisão: configurar por instalação `antecedenciaMinimaReservaMinutos` entre 0 e 43200, padrão 60; `horizonteReservaDias` entre 1 e 365, padrão 90; e `antecedenciaAlteracaoClienteMinutos` entre 0 e 43200, padrão 1440. Consultas públicas aceitam no máximo 31 dias por requisição e nunca retornam opções fora da antecedência ou do horizonte.

Esses limites se aplicam ao cliente final. Usuários administrativos podem criar e reagendar no futuro fora da antecedência e do horizonte público, mas não podem ignorar expediente, bloqueios, conflitos ou capacidade. Para atender fora do expediente recorrente, criam antes uma exceção `ABERTURA`; exceções `BLOQUEIO` removem disponibilidade e prevalecem quando houver sobreposição.

Cancelamento ou reagendamento público é recusado quando faltar menos que `antecedenciaAlteracaoClienteMinutos` para o início. Usuário administrativo no escopo pode cancelar ou reagendar uma reserva futura depois desse limite, com motivo auditável. Não se cria nem reagenda reserva para o passado; depois de `inicioEm`, a V1 permite apenas concluir ou marcar ausência e não reabre estados terminais.

Consequências: `ExcecaoAgenda` possui tipo `ABERTURA` ou `BLOQUEIO`; `BLOQUEIO` prevalece em sobreposição; limites públicos são calculados pelo relógio do servidor e fuso da instalação; conflitos e capacidade nunca podem ser ignorados.

Documentos afetados: `SESSION.md`, `TASKS.md`, `sdd_kit/03-domain-model.md`, `sdd_kit/04-business-rules.md`, `sdd_kit/05-user-flows.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/09-quality-strategy.md`, `sdd_kit/11-open-decisions.md` e `sdd_kit/16-scheduling-and-availability.md`.

### D-008 — Identidade do cliente — `DECIDED`

Contexto: o cliente final não cria conta, mas a instalação precisa reconhecer retornos, manter histórico e evitar duplicação excessiva sem exigir CPF.

Decisão: nome e celular são obrigatórios. O celular é normalizado para o formato canônico brasileiro com `+55`, sem campo de país no formulário, e é único na instalação. Uma nova reserva com o mesmo celular reutiliza o `Cliente`; por consequência, duas pessoas diferentes não podem compartilhar o mesmo celular na V1.

Email é contato e CPF é documento; são campos conceitualmente separados. Email é opcional por padrão e pode ser exigido pela configuração. A política de CPF pode ser `DESABILITADA`, `OPCIONAL` ou `OBRIGATORIA`, com padrão `OPCIONAL`. A V1 aceita apenas CPF como documento de cliente, normaliza e valida seus dígitos.

Cada reserva grava fotografia do nome, celular e email informados. Ao reutilizar um cliente, nome e email canônicos recebem os valores mais recentes sem alterar reservas antigas. Um CPF novo pode preencher um cadastro ainda sem documento; CPF diferente do já associado, ou CPF já vinculado a outro celular, produz conflito neutro e exige correção administrativa. A API pública nunca informa se o celular ou CPF já existia.

Consequências: troca de celular não é deduzida automaticamente; correções preservam histórico; índices de unicidade usam valores normalizados; CPF recebe proteção específica definida no documento de segurança.

Documentos afetados: `SESSION.md`, `TASKS.md`, `sdd_kit/03-domain-model.md`, `sdd_kit/04-business-rules.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/07-data-privacy-security.md`, `sdd_kit/09-quality-strategy.md` e `sdd_kit/11-open-decisions.md`.

### D-009 — Capacidade operacional — `DECIDED`

Decisão: haverá vários profissionais, e cada profissional define seus próprios serviços, preços e durações. Toda reserva bloqueia obrigatoriamente o profissional. Salas e equipamentos são recursos opcionais; quando exigidos pelo serviço, todos são bloqueados junto do profissional e na mesma transação.

Consequências: a disponibilidade resulta da interseção das agendas de todos os participantes; indisponibilidade de qualquer recurso impede a reserva inteira; serviços sem recursos operacionais usam somente a agenda do profissional.

### D-010 — Privacidade e retenção — `DECIDED`

Contexto: o produto-base precisa oferecer controles técnicos consistentes sem escolher, em nome de cada cliente, sua base legal, seus textos ou seu prazo de conservação.

Decisão: o projeto-base não fixa base legal nem prazo universal para todos os clientes. Cada instalação deve informar, antes de produção, versão do aviso de privacidade, canal do titular e `retencaoDadosPessoaisDias`; produção falha ao iniciar sem essa configuração. Ciência do aviso aplicável à reserva é registrada por versão e instante. Consentimento de marketing é separado, opcional e falso por padrão.

CPF é cifrado com AES-256-GCM e possui HMAC-SHA-256 separado para busca e unicidade; chaves ficam fora do banco. Nome, celular e email permanecem operacionais sob controle de acesso, TLS e criptografia do volume e dos backups. Nenhum dado clínico ou de saúde entra na V1.

Somente `PROPRIETARIO` pode exportar, corrigir ou anonimizar integralmente um cliente. Anonimização remove dados canônicos e fotografias pessoais das reservas, preservando valores operacionais sem identificação, e é rejeitada enquanto houver reserva futura `PENDENTE` ou `CONFIRMADO`. Um processo interno aplica a retenção configurada após o último atendimento terminal. Toda ação é auditada sem copiar os dados pessoais para o log.

A instalação continua responsável por definir finalidade, base legal, texto e prazo com revisão apropriada. Backups expiram pela política operacional e, quando restaurados, precisam reaplicar o registro de anonimizações posteriores ao ponto restaurado.

Consequências: a API oferece exportação, correção e anonimização, mas a verificação da identidade do solicitante e a decisão sobre hipóteses legais de conservação pertencem à operação do controlador; o projeto do cliente documenta controlador, operador e canal de atendimento; dados pessoais não são apagados de backups isoladamente, mas deixam de reaparecer após restauração.

Documentos afetados: `SESSION.md`, `TASKS.md`, `sdd_kit/03-domain-model.md`, `sdd_kit/04-business-rules.md`, `sdd_kit/05-user-flows.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/07-data-privacy-security.md`, `sdd_kit/09-quality-strategy.md`, `sdd_kit/11-open-decisions.md` e `sdd_kit/15-api-consumer-documentation.md`.

## Integrações

### D-011 — Provedor e política de WhatsApp — `DECIDED`

Contexto: a API-base precisa ser funcional e testável sem contrato, credencial ou regra comercial de um provedor específico.

Decisão: nenhum provedor concreto, template, custo ou política própria de WhatsApp entra na API-base V1. O núcleo oferece porta de notificação e outbox; cada projeto de cliente escolhe e implementa seu adaptador quando necessário.

Consequências: a configuração padrão usa adaptador desabilitado; indisponibilidade ou ausência de provedor nunca desfaz reserva; homologação do canal, templates e horários de envio ocorre no projeto do cliente.

Documentos afetados: `SESSION.md`, `TASKS.md`, `sdd_kit/08-architecture.md`, `sdd_kit/10-roadmap.md`, `sdd_kit/11-open-decisions.md`, `sdd_kit/14-v1-boundaries.md` e `sdd_kit/17-notifications-outbox.md`.

### D-012 — Conteúdo e gatilhos de notificação — `DECIDED`

Contexto: o domínio deve registrar fatos notificáveis de maneira confiável sem antecipar conteúdo, canal ou provedor de um cliente.

Decisão: a V1 registra na outbox `AGENDAMENTO_PENDENTE`, `AGENDAMENTO_CONFIRMADO`, `AGENDAMENTO_REAGENDADO`, `AGENDAMENTO_CANCELADO` e `AGENDAMENTO_EXPIRADO`. Conclusão, ausência e alterações internas de recursos não geram notificação. Lembretes, horários silenciosos e textos concretos ficam fora da API-base V1.

O evento é gravado na mesma transação da reserva, contém identificadores e fotografia mínima do fato e recebe chave única para entrega idempotente. O worker do mesmo aplicativo busca lotes pequenos, tenta novamente com espera crescente até oito vezes e então marca `FALHA_DEFINITIVA`. `PROPRIETARIO` pode listar falhas e solicitar nova tentativa, sem editar payload.

Sem adaptador configurado, o evento termina como `IGNORADO` com motivo `PROVEDOR_DESABILITADO`; a reserva nunca falha por isso. O adaptador do projeto de cliente transforma o evento em texto e canal. Comunicação transacional e marketing permanecem separados; a recusa de marketing não altera eventos operacionais necessários ao agendamento.

Consequências: payload possui versão e fotografia mínima do fato, sem CPF ou token puro; falha de entrega é visível e repetível; o produto-base não envia lembretes nem mensagens promocionais.

Documentos afetados: `AGENTS.md`, `SESSION.md`, `TASKS.md`, `sdd_kit/00-index.md`, `sdd_kit/03-domain-model.md`, `sdd_kit/04-business-rules.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/08-architecture.md`, `sdd_kit/09-quality-strategy.md`, `sdd_kit/11-open-decisions.md`, `sdd_kit/15-api-consumer-documentation.md` e `sdd_kit/17-notifications-outbox.md`.

### D-014 — Usuários, profissionais e permissões — `DECIDED`

Decisão: haverá somente `PROPRIETARIO` e `FUNCIONARIO`; não existe `GERENTE` na V1. O primeiro `PROPRIETARIO` vem do bootstrap. Somente ele cria, altera, ativa, desativa ou redefine a senha temporária de funcionários. O último `PROPRIETARIO` ativo não pode ser desativado ou rebaixado.

Todo `FUNCIONARIO` é vinculado obrigatoriamente a um único profissional e opera somente seus serviços, agenda e reservas. Dados do cliente são visíveis apenas no contexto desses atendimentos. Recursos compartilhados podem ser consultados e alocados, mas somente `PROPRIETARIO` altera seu cadastro ou capacidade. O backend deriva o escopo da sessão e não confia em identificadores enviados pelo cliente.

Consequências: vínculo de profissional é único por conta; acesso cruzado é ocultado; profissional pode existir sem usuário; `PROPRIETARIO` tem visão global e pode opcionalmente estar vinculado a um profissional.

### D-015 — Confirmação automática ou manual — `DECIDED`

Contexto: a instalação precisa escolher se uma solicitação pública já confirma o atendimento ou se aguarda análise, sem permitir dupla promessa do mesmo horário.

Decisão: em modo `AUTOMATICA`, a reserva pública nasce `CONFIRMADO`. Em modo `MANUAL`, nasce `PENDENTE` e bloqueia imediatamente o profissional e todos os recursos alocados, evitando que a aprovação posterior dispute um horário já prometido a outra pessoa.

Toda `PENDENTE` recebe `expiraEm`, calculado pelo menor instante entre o início do atendimento e `criadoEm + prazoAprovacaoPendenteMinutos`. O prazo é configurável por instalação entre 15 minutos e 24 horas, com padrão de 12 horas. Um processo interno simples expira pendências vencidas e libera as alocações; a expiração também é revalidada antes de consultar ou ocupar disponibilidade, para não depender exclusivamente da execução periódica.

Somente `PROPRIETARIO` ou o `FUNCIONARIO` vinculado ao profissional da reserva pode confirmar ou rejeitar. Rejeição altera o estado para `CANCELADO` e exige motivo; expiração usa o estado explícito `EXPIRADO`. Ambas encerram a reserva sem apagar o histórico e liberam suas alocações. Uma reserva criada por usuário administrativo nasce `CONFIRMADO`, pois já foi validada por alguém autorizado.

Consequências: disponibilidade e proteção concorrente consideram `PENDENTE` e `CONFIRMADO`; o processamento de expiração é idempotente; confirmar uma pendência vencida é proibido; notificações continuam assíncronas e não determinam o estado.

Documentos afetados: `SESSION.md`, `TASKS.md`, `sdd_kit/02-scope-and-requirements.md`, `sdd_kit/03-domain-model.md`, `sdd_kit/04-business-rules.md`, `sdd_kit/05-user-flows.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/09-quality-strategy.md` e `sdd_kit/11-open-decisions.md`.

### D-016 — Acesso público à reserva — `DECIDED`

Contexto: o cliente precisa operar uma reserva sem conta e sem depender do futuro provedor de mensagens, mas celular e referência isolados não são autenticação.

Decisão: cada reserva recebe uma referência pública e um token criptograficamente seguro de 256 bits. A API devolve o token somente na criação e persiste apenas seu hash. O frontend pode montar um link usando fragmento de URL, que não é enviado ao servidor, e apresenta o token à API pelo cabeçalho `X-Token-Agendamento`; token nunca aparece em path, query string ou log.

Referência e token juntos autorizam somente a leitura resumida, o cancelamento e o reagendamento daquela reserva. O token vale enquanto a reserva puder ser operada, é invalidado após cancelamento ou início do atendimento e não permite listar outras reservas do mesmo celular. Tentativas inválidas recebem resposta neutra e rate limit.

Reagendamento mantém serviço e profissional, garante o novo intervalo antes de liberar o anterior e preserva referência e token. Reserva `CONFIRMADO` continua confirmada; `PENDENTE` continua pendente e recebe novo `expiraEm`. Se o token for perdido, a V1 orienta contato com o estabelecimento; recuperação por código enviado ao celular fica para quando houver um provedor concreto.

Consequências: token ausente, inválido ou associado a outra referência produz resposta neutra; estados terminais e o início do atendimento revogam o acesso; mudar de serviço exige cancelar e criar outra reserva; a documentação do frontend deve evitar armazenamento inseguro do token.

Documentos afetados: `SESSION.md`, `TASKS.md`, `sdd_kit/02-scope-and-requirements.md`, `sdd_kit/03-domain-model.md`, `sdd_kit/04-business-rules.md`, `sdd_kit/05-user-flows.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/07-data-privacy-security.md`, `sdd_kit/09-quality-strategy.md`, `sdd_kit/11-open-decisions.md` e `sdd_kit/15-api-consumer-documentation.md`.

### D-017 — Seleção e capacidade de recursos operacionais — `DECIDED`

Decisão: recursos são específicos, como `Sala 1` ou `Aparelho X`, e possuem uma capacidade positiva. O serviço pré-seleciona recursos e quantidades. A reserva grava as alocações efetivas e bloqueia as quantidades junto do profissional.

Consequências: capacidade 1 representa um item único; capacidade maior representa unidades equivalentes; disponibilidade considera a soma das alocações sobrepostas; usuários autorizados podem ajustar alocações em reservas manuais, pendentes ou confirmadas, sempre com revalidação, atomicidade e auditoria.

### D-018 — Obrigatoriedade da pré-seleção de recursos — `DECIDED`

Contexto: recursos pré-selecionados precisam servir tanto como conveniência quanto como requisito operacional, sem criar dois modelos diferentes.

Decisão: cada `PredefinicaoRecursoServico` possui `quantidadePadrao` positiva e `quantidadeMinima` entre zero e a quantidade padrão. A reserva pública usa exatamente `quantidadePadrao`. Na criação administrativa, análise de pendência ou realocação posterior, usuários autorizados podem alterar ou remover recursos, mas a alocação final nunca pode ficar abaixo do mínimo de cada predefinição do serviço.

`quantidadeMinima = 0` torna o recurso totalmente substituível ou removível. Um valor positivo torna obrigatória ao menos aquela quantidade do recurso específico. Recursos adicionais que não fazem parte das predefinições podem ser incluídos administrativamente e possuem mínimo zero para aquela reserva.

Consequências: `0 <= quantidadeMinima <= quantidadePadrao <= capacidade`; toda alteração de alocação revalida os mínimos vigentes do serviço e a capacidade; reservas antigas preservam suas alocações históricas quando a configuração do serviço muda.

Documentos afetados: `SESSION.md`, `TASKS.md`, `sdd_kit/02-scope-and-requirements.md`, `sdd_kit/03-domain-model.md`, `sdd_kit/04-business-rules.md`, `sdd_kit/05-user-flows.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/09-quality-strategy.md`, `sdd_kit/11-open-decisions.md` e `sdd_kit/16-scheduling-and-availability.md`.

### D-020 — Perfil de escala da V1 — `DECIDED`

Decisão: a V1 atende instalações pequenas ou médias e de baixo fluxo. A referência de maior porte realista é uma barbearia com quatro profissionais e aproximadamente dez serviços por profissional.

Consequências: uma instância Spring Boot e um PostgreSQL são suficientes; não haverá escala horizontal, cache distribuído, broker ou busca externa; o histórico será paginado; concorrência será testada para correção, sem engenharia para carga massiva. Os números são referência de dimensionamento, não limites rígidos do domínio.

### D-022 — Garantia concorrente da agenda — `DECIDED`

Contexto: a disponibilidade consultada pelo frontend pode ficar desatualizada antes da criação, e recursos com capacidade maior que um exigem somar alocações concorrentes.

Decisão: o PostgreSQL é a autoridade. A criação ou realocação bloqueia, na mesma transação, a linha do profissional e as linhas dos recursos envolvidos em ordem determinística; depois encerra pendências vencidas, revalida sobreposições e capacidades e grava reserva, alocações e outbox atomicamente. O frontend não cria bloqueio ao selecionar um horário.

Consequências: requisições sem participantes comuns continuam paralelas; disputas são serializadas no banco; conflito retorna `409 CONFLITO_AGENDAMENTO`; testes com PostgreSQL real comprovam profissional, recurso e capacidade. Não haverá lock distribuído, Redis ou tabela de unidades de grade na V1.

Documentos afetados: `AGENTS.md`, `sdd_kit/00-index.md`, `sdd_kit/04-business-rules.md`, `sdd_kit/06-api-contract.md`, `sdd_kit/09-quality-strategy.md`, `sdd_kit/11-open-decisions.md` e `sdd_kit/16-scheduling-and-availability.md`.

### D-023 — Idempotência das operações públicas — `DECIDED`

Contexto: repetição causada por duplo clique, timeout ou perda da resposta não pode duplicar reservas, transições ou eventos.

Decisão: exigir `Chave-Idempotencia` em toda escrita pública de criação, cancelamento e reagendamento. A chave possui de 16 a 128 caracteres seguros, é tratada como opaca e seu hash é mantido por 24 horas junto da operação, impressão digital do payload e referência do resultado.

Repetir a mesma operação com a mesma chave e conteúdo devolve o resultado original sem repetir reserva, transição ou evento. Reutilizar a chave na mesma operação com conteúdo diferente retorna `409 CONFLITO_IDEMPOTENCIA`. Uma restrição única e bloqueio transacional coordenam repetições simultâneas.

Para que a criação repetida devolva o mesmo token sem persisti-lo em texto puro, o token de 256 bits é derivado por HMAC-SHA-256 a partir da referência pública e de um segredo próprio da instalação; somente seu hash permanece no banco. O segredo é obrigatório, não aparece em logs e precisa permanecer estável enquanto existirem reservas operáveis.

Somente comandos concluídos com sucesso são mantidos como resultado idempotente. Validação, conflito de disponibilidade e falha inesperada não consomem a chave; uma tentativa simultânea aguarda o comando dono da chave e então recebe seu resultado. A limpeza de registros vencidos é idempotente e executada pelo próprio aplicativo.

Consequências: o frontend cria uma chave por intenção do usuário e a reutiliza somente nas retentativas daquela intenção; o escopo combina tipo de operação e hash da chave; o segredo `SEGREDO_TOKEN_AGENDAMENTO` é configuração obrigatória com pelo menos 32 bytes aleatórios.

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
