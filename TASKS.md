# Plano de execução

Estados permitidos: `TODO`, `IN_PROGRESS`, `REVIEW`, `DONE`, `BLOCKED`.

Uma tarefa só passa para `DONE` quando seus critérios de aceite e validações estiverem atendidos. Decisões de produto devem ser registradas em `sdd_kit/`, não apenas neste arquivo.

## Visão das frentes

| Frente | Responsabilidade |
|---|---|
| Produto e domínio | regras, jornadas, linguagem e decisões |
| API | serviços, agenda, reservas, segurança e persistência |
| Contrato administrativo | operação de serviços, agenda e reservas pela API |
| Contrato público | consulta e agendamento sem login pela API |
| Release e operação | versionamento, documentação, observabilidade e entrega |

## Fundação

### F1 — Criar a base executável — `TODO`

Dependência: D-025 e D-026 decididas e `sdd_kit/18-technical-foundation.md` aplicado.

Escopo:

- criar backend, migrações, configuração local e testes básicos;
- aplicar a identidade `service-scheduling-api` e as coordenadas aprovadas;
- estabelecer convenções de módulos e tratamento de erros;
- criar verificação automatizada de build, testes e estilo;
- documentar como executar o projeto localmente.

Critérios de aceite:

- aplicação e banco iniciam em ambiente local;
- migrações são aplicadas em banco vazio;
- endpoint de saúde responde;
- suíte de testes executa por um único comando;
- erros da API seguem D-006 e estão representados no OpenAPI;
- nenhuma regra de negócio depende do frontend.

### F2 — Autenticação e autorização administrativa — `TODO`

Escopo:

- autenticar o responsável pelo negócio;
- criar o primeiro `PROPRIETARIO` por bootstrap quando ainda não existir usuário;
- permitir que `PROPRIETARIO` crie, altere e desative contas `FUNCIONARIO`;
- exigir troca da senha temporária no primeiro login do funcionário;
- proteger toda rota administrativa;
- proteger escritas administrativas com CSRF;
- limitar tentativas de login em memória;
- permitir consulta da identidade atual, logout e mudança da própria senha;
- invalidar todas as sessões do usuário após mudança de senha;
- impedir que dados pessoais apareçam em logs e respostas indevidas;
- restringir exportação, correção integral e anonimização de clientes ao `PROPRIETARIO`;
- implementar o modo operacional de recuperação de `PROPRIETARIO`, sem iniciar servidor web;

Critérios de aceite:

- usuário não autenticado não acessa operações administrativas;
- senhas são armazenadas com BCrypt custo 12;
- sessão expira após 30 minutos de inatividade e é perdida ao reiniciar a aplicação;
- bootstrap repetido não duplica usuário nem redefine senha persistida;
- `FUNCIONARIO` não cria usuários, altera papéis ou eleva as próprias permissões;
- `FUNCIONARIO` acessa somente o profissional vinculado, seus serviços, agenda e reservas;
- dados de cliente para `FUNCIONARIO` são limitados ao contexto de seus atendimentos;
- escopo é aplicado nas consultas do backend e testado contra acesso cruzado;
- o último `PROPRIETARIO` ativo não pode ser desativado ou rebaixado;
- rotas não declaradas são negadas por padrão;
- rotas públicas de disponibilidade e reserva continuam acessíveis sem login.
- recuperação aceita somente `PROPRIETARIO` existente, não registra segredo e termina com código de saída verificável.

## Núcleo da API

### B1 — Catálogo de serviços — `TODO`

Escopo:

- criar, editar, ativar, desativar e listar serviços;
- associar cada serviço exclusivamente ao profissional que o oferece;
- manter nome, descrição, duração, preço e intervalo opcional entre atendimentos;
- permitir que profissionais diferentes tenham serviços de mesmo nome com preço e duração próprios;
- pré-selecionar recursos operacionais com quantidades padrão e mínimas;
- validar duração e preço;
- validar compatibilidade entre duração ocupada e grade da instalação;
- definir ordenação pública.

Critérios de aceite:

- serviço inativo não aceita novas reservas;
- alterações não modificam o histórico de reservas existentes;
- contrato administrativo e contrato público são separados.

### B2 — Regras de agenda e bloqueios — `TODO`

Escopo:

- definir fuso horário do negócio;
- configurar granularidade dos horários de início;
- cadastrar horários recorrentes por recurso reservável;
- cadastrar exceções, folgas e bloqueios;
- permitir exceções `ABERTURA` e `BLOQUEIO`, com bloqueio prevalecendo;
- calcular disponibilidade pela interseção da agenda do profissional com as agendas dos recursos opcionais exigidos;
- aplicar antecedência, horizonte e limite de 31 dias por consulta pública.

Critérios de aceite:

- disponibilidade respeita horário, duração, intervalo, bloqueios e reservas existentes;
- profissional e recursos alocados ficam indisponíveis durante todo o período ocupado;
- recursos com capacidade maior que um permanecem disponíveis enquanto a soma das alocações sobrepostas não esgotar sua quantidade;
- dias sem expediente não produzem horários;
- datas e horários atravessam a API com regra de fuso documentada.
- serviços armazenam minutos e o calendário deriva as unidades visuais da grade.
- limites temporais são testados com relógio controlado.

### B3 — Reserva pública sem login — `TODO`

Escopo:

- receber serviço, horário, nome, celular e os contatos/documentos configurados;
- normalizar e validar os identificadores informados;
- criar ou localizar o cliente conforme a regra de identidade decidida;
- preservar fotografia de nome e contatos em cada reserva;
- registrar ciência do aviso vigente e separar consentimento de marketing;
- cifrar CPF e indexar sua busca com HMAC separado;
- salvar fotografia do serviço contratado;
- impedir reserva duplicada e conflito concorrente;
- implementar idempotência de 24 horas para todas as escritas públicas;
- bloquear horário durante aprovação manual e expirar pendências vencidas;
- devolver um identificador público seguro da reserva.
- gerar token público de 256 bits, persistir somente seu hash e devolvê-lo apenas na criação;

Critérios de aceite:

- duas requisições simultâneas não confirmam o mesmo horário;
- duas reservas simultâneas não alocam o mesmo profissional, sala ou equipamento;
- resposta não expõe identificadores internos ou dados pessoais desnecessários;
- preço, duração e nome do serviço permanecem históricos;
- alteração posterior do cliente não modifica os dados históricos da reserva;
- rotina de retenção anonimiza dados vencidos sem remover o histórico operacional.
- falha de integração externa não desfaz silenciosamente uma reserva válida.
- reserva pendente informa `expiraEm` e não pode ser confirmada após esse instante.

### B4 — Gestão administrativa de reservas — `TODO`

Escopo:

- listar e consultar reservas por período e estado;
- confirmar, rejeitar, cancelar, concluir e marcar ausência conforme transições permitidas;
- criar reserva manual pela administração;
- permitir ajustar recursos e quantidades na criação manual e durante a análise de uma reserva pendente;
- impedir que ajustes reduzam predefinições abaixo das quantidades mínimas do serviço;
- permitir trocar alocações de uma reserva confirmada de forma atômica e auditável;
- permitir cancelamento e reagendamento públicos somente após verificação segura do cliente;
- exigir referência e `X-Token-Agendamento` nas operações públicas da reserva;
- exigir e respeitar `Chave-Idempotencia` no cancelamento e reagendamento públicos;
- aplicar o prazo público de alteração e impedir mudança depois do início;
- registrar motivo e origem de alterações relevantes.

Critérios de aceite:

- transições inválidas são rejeitadas;
- somente usuário no escopo pode decidir uma reserva pendente;
- cancelamento libera a disponibilidade quando aplicável;
- dados pessoais são exibidos apenas no contexto administrativo autorizado.
- token inválido não revela a existência da reserva e token perdido não é recuperado pela API V1.

## Fechamento da API V1

### R1 — Contrato e documentação para consumidores — `TODO`

Escopo:

- consolidar e validar o contrato OpenAPI;
- fornecer exemplos de autenticação, configuração, disponibilidade e reserva;
- documentar erros, paginação, idempotência e versionamento;
- criar exemplos executáveis para IntelliJ HTTP Client ou formato equivalente mantido no repositório;
- documentar todas as variáveis de ambiente, valores padrão e quais são secretas;
- documentar bootstrap, sessão, CSRF, migrações e atualização de uma instalação;
- manter catálogo de códigos de erro estáveis;
- permitir testar a API localmente sem frontend próprio.

Critérios de aceite:

- todos os recursos da V1 aparecem no contrato;
- exemplos executáveis cobrem a jornada administrativa e a jornada pública;
- exemplos e OpenAPI são validados automaticamente contra a aplicação;
- um consumidor identifica campos obrigatórios, estados, erros e permissões sem consultar o código-fonte;
- documentação não contém senha, token ou dados pessoais reais;
- uma nova instalação pode ser iniciada seguindo somente a documentação do repositório.

### R2 — Publicar a release da API V1 — `TODO`

Escopo:

- concluir segurança, privacidade, migrações e validações da V1;
- definir versionamento semântico e política de compatibilidade;
- preparar configuração de exemplo sem segredos;
- construir e validar a imagem Docker da API;
- manter Docker Compose reproduzível para desenvolvimento local;
- consolidar em `main` todo o trabalho validado da V1;
- publicar a GitHub Release da API V1;
- criar a tag estável `v1.0.0` somente após aprovação dos critérios.

Critérios de aceite:

- suíte completa e testes concorrentes aprovados;
- banco vazio chega à versão atual por migrações;
- contrato OpenAPI validado;
- documentação de instalação reproduzida em ambiente limpo;
- container conecta a PostgreSQL externo e expõe liveness e readiness;
- nenhuma referência funcional ou visual específica da Revitalle no núcleo;
- origem, commit, GitHub Release e tag da V1 registrados.

## Integrações e produção

### I1 — Caixa de saída de notificações — `TODO`

Escopo:

- registrar eventos de pendência, confirmação, reagendamento, cancelamento e expiração;
- processar notificações de forma assíncrona e idempotente;
- aplicar a sequência de retentativas e marcar `FALHA_DEFINITIVA` após a oitava falha;
- permitir inspeção e retentativa de falhas somente ao `PROPRIETARIO`;
- usar adaptador desabilitado que conclui eventos como `IGNORADO` quando não há provedor;
- manter o provedor desacoplado do domínio.

Critérios de aceite:

- indisponibilidade do provedor não causa perda da reserva;
- o mesmo evento não envia mensagens duplicadas;
- dados sensíveis não aparecem em logs.
- ausência de provedor não impede nenhuma jornada da API V1.

### I2 — Adaptador de WhatsApp — `TODO`

Dependência: decisão do projeto do cliente sobre provedor e conteúdo das mensagens.

Escopo:

- enviar confirmação e mudanças da reserva;
- registrar identificador e resultado do envio;
- respeitar consentimento, finalidade e política do provedor.

O adaptador concreto pode ser entregue depois da API V1. A V1 deve manter a porta de integração e o processamento confiável definidos em I1.

### O1 — Prontidão para produção — `TODO`

Escopo:

- definir ambientes, segredos, backup e restauração;
- adicionar métricas, logs estruturados, rastreabilidade e alertas;
- revisar limites, abuso das rotas públicas e proteção de dados;
- testar recuperação e migrações.

Critérios de aceite:

- procedimento de implantação e rollback documentado;
- backup restaurado em teste;
- alertas cobrem falha da API, banco e processamento de notificações;
- checklist de privacidade e segurança revisado.

## Projetos de clientes — após a API V1

### C1 — Derivar o projeto Revitalle da API V1 — `TODO`

Dependência: R2 concluída.

Escopo:

- criar um projeto separado a partir da tag estável da API;
- configurar ambiente, banco e segredos próprios;
- construir os frontends administrativo e público;
- aplicar identidade visual e conteúdo da Revitalle;
- implementar somente as extensões de negócio aprovadas para a clínica.

Critérios de aceite:

- projeto não compartilha banco ou segredos com o repositório-base;
- versão de origem da API está documentada;
- projeto pode evoluir de forma independente;
- correções genéricas podem ser reaplicadas manualmente ao produto-base, sem vínculo automático.
