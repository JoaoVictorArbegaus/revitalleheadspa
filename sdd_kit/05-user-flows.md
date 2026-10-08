# Fluxos de usuário

Os fluxos descrevem as jornadas que a API deve suportar. A implementação visual pertence a cada projeto de cliente e começa depois da API V1.

## Fluxo público de agendamento

1. Cliente acessa a página de agendamento.
2. Sistema apresenta profissionais e seus serviços ativos, com descrição, duração e preço próprios.
3. Cliente seleciona um serviço.
4. Sistema apresenta dias e horários disponíveis dentro do horizonte permitido, considerando a agenda do profissional e a capacidade dos recursos exigidos.
5. Cliente seleciona um horário.
6. Cliente informa somente nome e celular.
7. Sistema apresenta o resumo, o aviso de privacidade vigente e sua versão.
8. Cliente confirma a ciência dessa versão e a reserva.
9. Frontend envia uma nova `Chave-Idempotencia`; em retentativas da mesma confirmação, reutiliza essa chave.
10. API valida novamente o horário, registra a versão do aviso e o instante da ciência e cria a reserva uma única vez, diretamente em `CONFIRMADO`.
11. Interface exibe confirmação, serviço, profissional, data, hora e valor.

### Exceções

- Horário ocupado durante o preenchimento: informar conflito, preservar dados não sensíveis necessários ao formulário e solicitar novo horário.
- Dados inválidos: indicar os campos sem expor regra interna desnecessária.
- Falha temporária da API: não afirmar que a reserva foi criada; oferecer tentativa segura usando a mesma chave de idempotência.

A consulta pública cobre no máximo 31 dias por chamada, respeita a antecedência e nunca ultrapassa o horizonte da instalação.

## Anonimização manual

1. O controlador recebe a solicitação pelo canal operacional e verifica a identidade fora da API pública.
2. `PROPRIETARIO` localiza o cliente sem expor a busca publicamente.
3. Solicita a anonimização integral com motivo.
4. A API rejeita a operação quando existe reserva futura `CONFIRMADO`.
5. Em caso de sucesso, nome, celular e fotografias pessoais são removidos, o histórico operacional é preservado sem identificação e a ação é auditada.

Exportação, correção e anonimização automática não fazem parte da API V1.

## Gestão de serviços

1. Usuário autenticado abre o catálogo dentro de sua permissão.
2. `PROPRIETARIO` seleciona qualquer profissional; `FUNCIONARIO` usa obrigatoriamente o profissional derivado da sessão.
3. Cria ou altera por `PUT` nome, descrição, duração, intervalo, preço, ordem e requisitos fixos de recursos, associando recursos existentes.
4. A substituição completa por `PUT` inclui o estado ativo; não existe alteração por `PATCH`.
5. Sistema valida e salva.
6. Alterações passam a valer para novas consultas e reservas.
7. Reservas antigas mantêm sua fotografia histórica.

## Gestão de usuários

1. `PROPRIETARIO` autenticado cria uma conta `FUNCIONARIO` com nome, email, senha temporária e vínculo obrigatório com um profissional ainda sem usuário.
2. Funcionário entra com a credencial temporária.
3. Sistema exige a troca da senha antes de liberar as demais operações administrativas.
4. `PROPRIETARIO` pode alterar o vínculo profissional, redefinir uma senha temporária ou desativar a conta.
5. Contas desativadas permanecem no histórico de auditoria.

`FUNCIONARIO` não cria usuários, altera papéis, eleva as próprias permissões,
administra profissionais ou altera cadastro, capacidade e estado de recursos. Pode
listar recursos e manter somente os serviços do profissional vinculado.

## Operação do funcionário

1. `FUNCIONARIO` acessa somente a agenda e as reservas do profissional ao qual sua conta está vinculada.
2. Pode listar recursos e criar, listar, buscar, alterar, ativar e desativar serviços desse profissional, incluindo seus requisitos de recursos.
3. Pode manter o expediente recorrente e os bloqueios desse profissional.
4. Pode consultar e criar reservas somente para esse profissional.
5. Pode cancelar, concluir ou registrar ausência somente nessas reservas.
6. Pode consultar os dados de contato necessários ao atendimento, sem acessar reservas do cliente com outros profissionais.
7. Pode visualizar os recursos alocados automaticamente nas próprias reservas, mas não altera essas alocações nem o cadastro dos recursos.

## Configuração da agenda

1. Usuário autorizado seleciona um profissional dentro do próprio escopo e define seu expediente semanal.
2. Adiciona intervalos separados quando houver pausa.
3. Registra folgas, indisponibilidades ou outros bloqueios.
4. Sistema valida sobreposições e mostra uma prévia da disponibilidade resultante.

Recursos operacionais não possuem expediente ou bloqueios próprios, e não existe abertura extraordinária na API V1.

## Operação diária

1. Usuário autorizado consulta reservas por dia ou período dentro do próprio escopo.
2. Abre os detalhes de uma reserva.
3. Cria, cancela, conclui ou registra ausência conforme o estado e o horário permitirem.
4. Sistema registra a operação e o autor.

Não existe confirmação, rejeição ou reagendamento na API V1.

## Reserva administrativa

1. Usuário autorizado seleciona um serviço dentro do próprio escopo e um horário.
2. Sistema determina automaticamente os recursos e quantidades fixas exigidos pelo serviço.
3. Usuário informa somente nome e celular do cliente.
4. Sistema apresenta o aviso vigente, e o usuário confirma que o cliente teve ciência dessa versão.
5. Usuário confirma o resumo.
6. API rejeita versão desatualizada e aplica as mesmas regras de expediente, bloqueio, conflito, capacidade e concorrência do fluxo público.
7. API cria a reserva em `CONFIRMADO`, registra origem `ADMINISTRACAO`, versão e instante da ciência e preserva a fotografia automática das alocações.

As alocações não podem ser ajustadas ou realocadas na reserva.
