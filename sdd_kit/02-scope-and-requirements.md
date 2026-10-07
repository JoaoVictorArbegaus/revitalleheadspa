# Escopo e requisitos da API V1

Este documento descreve comportamentos oferecidos pela API. Interfaces visuais serão criadas nos projetos de clientes após a publicação da V1.

## Escopo da API V1

### Administração

- autenticar o responsável pelo negócio;
- administrar contas de funcionários com os papéis `PROPRIETARIO` e `FUNCIONARIO`;
- restringir cada `FUNCIONARIO` ao profissional vinculado e aos dados necessários aos próprios atendimentos;
- cadastrar e manter serviços;
- cadastrar profissionais e recursos operacionais opcionais, como salas e equipamentos;
- definir a quantidade disponível de cada recurso operacional;
- pré-selecionar recursos, quantidades padrão e quantidades mínimas por serviço;
- configurar expediente recorrente por recurso;
- criar bloqueios e exceções de agenda;
- consultar agenda e detalhes de reservas;
- criar, confirmar, rejeitar, cancelar, concluir e marcar ausência quando permitido.

### Cliente final

- consultar serviços ativos;
- consultar datas e horários disponíveis;
- escolher um serviço de um profissional;
- reservar sem login, informando nome, celular e os dados adicionais exigidos pela instalação;
- visualizar confirmação e referência da reserva.
- consultar, cancelar e reagendar uma reserva com referência e token secreto próprios.

### Plataforma

- persistir clientes, serviços, agenda e reservas;
- manter histórico do que foi contratado;
- proteger dados pessoais;
- impedir conflitos sob requisições simultâneas;
- emitir eventos para notificações posteriores.

## Requisitos não funcionais

- API independente do frontend e documentada por contrato versionado.
- Datas e horários tratados com um fuso explícito por instalação.
- Operações públicas protegidas contra abuso e enumeração de dados.
- Logs sem CPF, telefone completo, tokens ou segredos.
- Alterações de esquema feitas por migrações reproduzíveis.
- Erros retornados em formato consistente e rastreável.
- Processos externos com retentativa devem ser idempotentes.
- Núcleo testável sem WhatsApp ou outro provedor real.
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
- aplicativo móvel nativo.
- frontend administrativo e frontend público neste repositório;
- marca, conteúdo ou regra exclusiva da Revitalle;
- automação que copie personalizações de um cliente para outro.

Itens fora da API V1 podem ser priorizados depois. Eles não devem ser antecipados no modelo salvo quando uma pequena decisão evita um bloqueio conhecido.

## Premissas a validar

- cada profissional possui seus próprios serviços, preços e durações;
- a instalação pode operar com vários profissionais, salas ou equipamentos;
- toda reserva ocupa obrigatoriamente o profissional do serviço e, quando aplicável, os recursos operacionais exigidos;
- a alocação efetiva pode ser ajustada por usuário autorizado, desde que capacidade e concorrência sejam revalidadas;
- cada serviço ocupa um período contínuo;
- a cobrança acontece fora do sistema;
- a instalação escolhe entre confirmação automática e aprovação manual;
- telefone sem verificação não autoriza consulta ou alteração de reservas;
- projetos de clientes são cópias independentes de uma release da API-base.

Premissas alteradas devem gerar atualização de domínio, regras, contrato e backlog.
