# Fluxos de usuário

Os fluxos descrevem as jornadas que a API deve suportar. A implementação visual pertence a cada projeto de cliente e começa depois da API V1.

## Fluxo público de agendamento

1. Cliente acessa a página de agendamento.
2. Sistema apresenta profissionais e seus serviços ativos, com descrição, duração e preço próprios.
3. Cliente seleciona um serviço.
4. Sistema apresenta dias e horários disponíveis dentro do horizonte permitido.
5. Cliente seleciona um horário.
6. Cliente informa nome, celular e os contatos/documentos exigidos pela instalação.
7. Sistema apresenta resumo, aviso de privacidade e consentimentos aplicáveis.
8. Cliente confirma.
9. Frontend envia uma nova `Chave-Idempotencia`; em retentativas da mesma confirmação, reutiliza essa chave.
10. API valida novamente o horário e cria a reserva uma única vez.
11. Interface exibe referência, serviço, data, hora, valor e orientação seguinte.
12. Evento de confirmação é processado pela integração configurada.

Quando a instalação usa aprovação manual, o passo 9 cria uma reserva `PENDENTE`, bloqueia o horário e informa no resumo que ela aguarda decisão até `expiraEm`. Se não houver decisão no prazo, o estado passa para `EXPIRADO` e o horário é liberado.

### Exceções

- Horário ocupado durante o preenchimento: informar conflito, preservar dados não sensíveis necessários ao formulário e solicitar novo horário.
- Dados inválidos: indicar os campos sem expor regra interna desnecessária.
- Falha temporária da API: não afirmar que a reserva foi criada; oferecer tentativa segura usando a mesma chave de idempotência.
- Falha no WhatsApp: manter a reserva e sinalizar a falha para operação.

A consulta pública cobre no máximo 31 dias por chamada, respeita a antecedência e nunca ultrapassa o horizonte da instalação.

A ciência do aviso vigente é obrigatória para a reserva. Marketing aparece separadamente, nunca vem pré-marcado e sua recusa não impede o agendamento.

## Atendimento a direitos do titular

1. O controlador recebe a solicitação pelo canal configurado e verifica a identidade fora da API pública.
2. `PROPRIETARIO` localiza o cliente sem expor a busca publicamente.
3. Pode exportar os dados, corrigir o cadastro ou solicitar anonimização integral.
4. A API rejeita anonimização quando existe reserva futura vigente.
5. Em caso de sucesso, dados pessoais são removidos, o histórico operacional é preservado sem identificação e a ação é auditada.

## Gestão de serviços

1. Administrador autenticado abre o catálogo.
2. Seleciona o profissional proprietário e cria ou edita nome, descrição, duração, intervalo, preço, estado e ordem.
3. Sistema valida e salva.
4. Alterações passam a valer para novas consultas e reservas.
5. Reservas antigas mantêm sua fotografia histórica.

## Gestão de usuários

1. `PROPRIETARIO` autenticado cria uma conta `FUNCIONARIO` com nome, email, senha temporária e vínculo obrigatório com um profissional ainda sem usuário.
2. Funcionário entra com a credencial temporária.
3. Sistema exige a troca da senha antes de liberar as demais operações administrativas.
4. `PROPRIETARIO` pode alterar o vínculo profissional, redefinir uma senha temporária ou desativar a conta.
5. Contas desativadas permanecem no histórico de auditoria.

`FUNCIONARIO` não cria usuários, altera papéis ou eleva as próprias permissões.

## Operação do funcionário

1. `FUNCIONARIO` acessa somente o profissional ao qual sua conta está vinculada.
2. Pode manter os próprios serviços, preços, durações e pré-seleções de recursos.
3. Pode manter sua agenda, exceções e bloqueios.
4. Pode consultar e operar apenas suas reservas.
5. Pode consultar os dados de contato necessários ao atendimento, sem acessar reservas do cliente com outros profissionais.
6. Pode consultar recursos compartilhados e alocá-los nos próprios serviços ou reservas, mas não altera cadastro ou capacidade desses recursos.

## Configuração da agenda

1. Administrador seleciona um recurso e define seu expediente semanal.
2. Adiciona intervalos separados quando houver pausa.
3. Registra folgas, indisponibilidades ou outros bloqueios.
4. Sistema valida sobreposições e mostra uma prévia da disponibilidade resultante.

## Autoatendimento da reserva

1. Após criar, o frontend recebe a referência e o token uma única vez e monta um link com o segredo no fragmento da URL.
2. Ao abrir o link, o frontend lê o fragmento localmente e envia o token pelo cabeçalho `X-Token-Agendamento`.
3. A API valida referência, hash, estado e prazo e devolve somente o resumo daquela reserva.
4. Cliente seleciona cancelar ou reagendar.
5. Frontend cria uma `Chave-Idempotencia` para a ação e a reutiliza em eventual retentativa.
6. No reagendamento, a API garante o novo horário antes de liberar o anterior, mantendo o mesmo serviço e profissional.
7. Sistema registra a alteração e emite o evento de notificação correspondente uma única vez.

Telefone isolado não localiza nem autoriza reservas. Se o token for perdido, a V1 orienta contato com o estabelecimento.

Se o limite de alteração pública já tiver passado, a API mantém a reserva e orienta o cliente a contatar o estabelecimento. A administração pode atuar enquanto o início ainda estiver no futuro, registrando motivo.

## Operação diária

1. Administrador consulta reservas por dia ou período.
2. Abre detalhes de uma reserva.
3. Executa uma transição permitida.
4. Sistema registra a mudança e gera evento de notificação quando aplicável.

Na análise de uma reserva `PENDENTE`, `PROPRIETARIO` ou o `FUNCIONARIO` responsável pode confirmar ou rejeitar. Rejeição exige motivo; pendência vencida não pode ser confirmada.

## Reserva administrativa

1. Administrador seleciona serviço e horário.
2. Sistema carrega os recursos e quantidades pré-selecionados pelo serviço.
3. Administrador mantém ou ajusta as alocações sem reduzir os mínimos definidos pelo serviço.
4. Localiza cliente existente ou informa seus dados.
5. Confirma o resumo.
6. API aplica as mesmas regras de conflito e capacidade, cria a reserva como `CONFIRMADO` e registra origem `ADMINISTRACAO`.

## Análise ou edição administrativa

1. Usuário autorizado abre uma reserva pendente ou confirmada.
2. Sistema mostra profissional, recursos, quantidades e disponibilidade atual.
3. Usuário altera as alocações, respeita os mínimos do serviço e informa motivo quando exigido.
4. API tenta garantir o novo conjunto de forma atômica.
5. Em caso de conflito, nenhuma alocação antiga é perdida.
6. Em caso de sucesso, a alteração é auditada e pode gerar notificação.
