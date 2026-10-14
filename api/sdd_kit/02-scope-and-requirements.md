# Escopo e requisitos da API V1

Este documento descreve comportamentos oferecidos pela API. Interfaces visuais serão criadas nos projetos de clientes após a publicação da V1.

## Escopo da API V1

### Administração

- autenticar o responsável pelo negócio;
- administrar contas com os papéis `PROPRIETARIO` e `FUNCIONARIO`;
- restringir cada `FUNCIONARIO` ao profissional vinculado, incluindo seus serviços, agenda, reservas e dados necessários aos atendimentos;
- permitir ao `PROPRIETARIO` cadastrar e manter profissionais e todo o catálogo;
- permitir ao `FUNCIONARIO` criar e manter somente serviços do profissional vinculado, usando recursos existentes;
- cadastrar recursos operacionais opcionais com nome, estado ativo e capacidade;
- definir por serviço os recursos exigidos e a quantidade fixa de cada um;
- configurar expediente recorrente e bloqueios do profissional;
- consultar agenda e detalhes de reservas;
- criar, cancelar, concluir e registrar ausência em reservas, conforme autorização;
- anonimizar manualmente clientes por ação de `PROPRIETARIO`.

### Cliente final

- consultar serviços ativos;
- consultar datas e horários disponíveis por profissional, considerando a capacidade dos recursos exigidos;
- escolher um serviço de um profissional;
- reservar sem login, informando somente nome e celular;
- visualizar a confirmação da reserva.

### Plataforma

- persistir clientes, serviços, agenda e reservas;
- manter histórico do que foi contratado e dos recursos alocados automaticamente;
- proteger dados pessoais;
- impedir conflitos e duplicidade sob requisições simultâneas;
- criar reservas públicas e administrativas diretamente em `CONFIRMADO`.

## Requisitos não funcionais

- API independente do frontend e documentada por contrato versionado.
- Moeda e grade de horários definidas por variáveis da instalação, com padrões `BRL` e `15` minutos.
- Preços com representação decimal de duas casas.
- Datas e horários tratados com um fuso explícito por instalação.
- Operações públicas protegidas contra abuso e enumeração de dados.
- Logs sem telefone completo ou segredos.
- Alterações de esquema feitas por migrações reproduzíveis.
- Erros retornados em formato consistente e rastreável.
- A criação pública deve ser idempotente.
- Operação dimensionada para uma instalação pequena ou média, executada em uma única instância da aplicação.
- Coleções administrativas de histórico potencialmente crescente devem ser paginadas.
- O perfil de escala não cria limites rígidos de quatro profissionais ou quarenta serviços no domínio.

## Fora da API V1

- multitenancy;
- conta e login do cliente final;
- pagamentos online;
- marketplace de profissionais;
- fidelidade, pacotes, assinaturas e cupons;
- prontuário, anamnese e dados clínicos;
- sincronização com calendários externos;
- lista de espera;
- aplicativo móvel nativo;
- frontend administrativo e frontend público neste repositório;
- marca, conteúdo ou regra exclusiva da Revitalle;
- automação que copie personalizações de um cliente para outro;
- estados `PENDENTE` e `EXPIRADO`, confirmação manual, rejeição e expiração de reservas;
- consulta, cancelamento e reagendamento públicos, inclusive referência pública, token de acesso e HMAC;
- reagendamento administrativo;
- notificações, outbox e endpoints administrativos de eventos;
- CPF, email, marketing e outros campos adicionais do cliente;
- exportação, correção e anonimização automática de dados pessoais;
- expediente ou bloqueios próprios de recursos operacionais;
- quantidades mínima e padrão, ajuste ou realocação de recursos em uma reserva.

Itens fora da API V1 podem ser priorizados depois. Eles não devem ser antecipados no modelo.

## Premissas da V1

- cada profissional possui seus próprios serviços, preços e durações;
- o backend deriva da sessão o profissional do `FUNCIONARIO` e não aceita ampliação desse escopo pelo cliente;
- a instalação pode operar com vários profissionais, salas ou equipamentos;
- toda reserva ocupa obrigatoriamente o profissional do serviço e, quando aplicável, a quantidade fixa dos recursos operacionais exigidos;
- cada serviço ocupa um período contínuo;
- a cobrança acontece fora do sistema;
- telefone sem verificação não autoriza consulta ou alteração pública de reservas;
- projetos de clientes são cópias independentes de um commit da API-base.

Premissas alteradas devem gerar atualização de domínio, regras, contrato e backlog.
