# Regras de negócio

## Serviços

1. Nome, duração e preço são obrigatórios.
2. Cada serviço pertence exatamente a um profissional.
3. Profissionais diferentes não compartilham o mesmo registro de serviço.
4. Serviço inativo permanece no histórico, mas não aparece para novas reservas.
5. Alterar um serviço não altera reservas já criadas.
6. Duração ocupada é `duracaoMinutos + intervaloLimpezaMinutos`, quando o intervalo estiver configurado.
7. Um serviço pode pré-selecionar recursos específicos com quantidades padrão positivas e quantidades mínimas entre zero e o padrão.
8. Duração e intervalo são armazenados em minutos; quantidade de unidades de grade nunca é persistida no serviço.
9. A duração ocupada deve ser múltipla de `intervaloGradeMinutos`.

## Grade de horários

- Cada instalação define uma única granularidade em minutos.
- Horários oferecidos começam somente em posições alinhadas à grade no fuso da instalação.
- A ocupação é um intervalo contínuo calculado por `duracaoMinutos + intervaloLimpezaMinutos`.
- A quantidade de unidades exibida pelo frontend é derivada da ocupação dividida pela granularidade.
- A API rejeita serviço cuja ocupação não seja múltipla da grade; não arredonda duração silenciosamente.
- Alterar a granularidade é permitido somente quando todos os serviços existentes permanecem compatíveis.

## Disponibilidade

Um horário é reservável somente quando todas as condições forem verdadeiras:

1. o serviço está ativo;
2. o profissional proprietário está ativo;
3. o início e todo o período ocupado estão dentro da agenda do profissional;
4. quando a reserva aloca recursos operacionais, o período também está dentro das agendas desses recursos;
5. não existe bloqueio ou exceção conflitante no profissional ou nos recursos;
6. o profissional não possui reserva vigente sobreposta;
7. para cada recurso, a soma das quantidades em reservas sobrepostas mais a quantidade solicitada não excede sua capacidade;
8. o início está alinhado à granularidade definida;
9. o horário respeita antecedência mínima, limite futuro e fuso do negócio.

Para o fluxo público, a antecedência padrão é 60 minutos e o horizonte padrão é 90 dias. Ambos são configuráveis conforme D-019. Cada consulta pública cobre no máximo 31 dias.

Exceção `ABERTURA` adiciona disponibilidade fora do expediente recorrente; `BLOQUEIO` remove disponibilidade e prevalece em qualquer sobreposição. A abertura extraordinária não ignora reservas, capacidade ou outros bloqueios.

A consulta de disponibilidade é informativa. A criação da reserva deve repetir as validações dentro da proteção transacional, pois outro cliente pode ocupar o horário entre consulta e confirmação.

## Sobreposição

Dois intervalos se sobrepõem quando:

`inicioA < fimB` e `inicioB < fimA`.

Somente reservas `CONFIRMADO` e `PENDENTE` ainda não vencidas bloqueiam agenda. `CANCELADO`, `EXPIRADO`, `CONCLUIDO` e `NAO_COMPARECEU` não bloqueiam novos horários.

## Concorrência

- O banco é a autoridade final sobre ocupação.
- A implementação deve garantir que duas requisições concorrentes não confirmem intervalos conflitantes.
- A garantia abrange o profissional e cada sala ou equipamento alocado.
- Profissional e recursos são bloqueados atomicamente; a reserva inteira falha se qualquer participante estiver indisponível.
- Para recursos com capacidade maior que um, o conflito ocorre quando a soma das quantidades sobrepostas ultrapassa a capacidade.
- A transação bloqueia primeiro o profissional e depois os recursos por identificador crescente, encerra pendências vencidas e revalida todas as ocupações antes de gravar.
- A mesma ordem de bloqueio se aplica à troca de alocações, considerando a união dos recursos antigos e novos.
- Um teste de integração concorrente é obrigatório.
- Cache nunca decide sozinho se um horário pode ser reservado.

## Criação de cliente e reserva

1. Exigir nome e celular, normalizar o celular brasileiro com `+55` e validar email e CPF quando informados ou exigidos.
2. Localizar o cliente pelo celular normalizado ou criá-lo quando não existir.
3. Reutilizar o cliente encontrado e atualizar nome e email canônicos, preservando fotografia desses dados na reserva.
4. Preencher CPF ausente quando válido; rejeitar de forma neutra divergência com CPF existente ou CPF já associado a outro celular.
5. Exigir ciência da versão vigente do aviso; marketing permanece independente, opcional e falso por padrão.
6. Revalidar serviço, profissional e todos os recursos exigidos sob controle transacional.
7. Criar `Agendamento` com fotografias do serviço, dos dados de contato e da versão do aviso.
8. Aplicar `PENDENTE` ou `CONFIRMADO` conforme a política da instalação.
9. Criar `EventoOutbox` na mesma transação.
10. Confirmar a transação.
11. Processar notificações fora da requisição principal.

Reservas `PENDENTE` bloqueiam imediatamente o profissional e os recursos alocados. Seu `expiraEm` é o menor instante entre o início do atendimento e `criadoEm + prazoAprovacaoPendenteMinutos`. O prazo configurável varia de 15 a 1440 minutos e usa 720 minutos como padrão.

Um processo interno idempotente altera pendências vencidas para `EXPIRADO` e libera suas alocações. Consulta de disponibilidade e tentativa de ocupação também revalidam e encerram pendências vencidas relevantes antes de decidir conflito. Assim, correção não depende apenas da periodicidade do processo.

Somente `PROPRIETARIO` ou `FUNCIONARIO` vinculado ao profissional pode confirmar ou rejeitar uma pendência. Confirmação após `expiraEm` é proibida. Rejeição usa `CANCELADO`, exige motivo e registra autor; expiração usa `EXPIRADO` e registra origem `SISTEMA`.

Se o horário tiver sido ocupado, retornar conflito de negócio e permitir ao frontend atualizar as opções.

## Alteração de recursos

- Toda reserva pública usa exatamente as quantidades padrão vigentes do serviço, tanto em confirmação automática quanto manual.
- Criação administrativa e análise de reserva `PENDENTE` podem ajustar recursos e quantidades antes da confirmação, respeitando os mínimos do serviço.
- Uma reserva `CONFIRMADO` pode ter suas alocações alteradas por usuário autorizado.
- Uma realocação nunca pode deixar um recurso predefinido abaixo de `quantidadeMinima`; recursos adicionais possuem mínimo zero.
- A alteração confirmada deve garantir as novas alocações antes de liberar as antigas, dentro da mesma transação.
- Toda alteração registra usuário, instante, valores anteriores, valores novos e motivo quando exigido.
- Reduzir a capacidade de um recurso abaixo da quantidade já alocada em reservas vigentes futuras é proibido.
- `quantidadePadrao` não pode exceder a capacidade atual do recurso.
- Desativar um recurso impede novas alocações, mas preserva reservas e histórico existentes.

## Idempotência

Criação, cancelamento e reagendamento públicos exigem `Chave-Idempotencia` com 16 a 128 caracteres seguros. Cada intenção do usuário recebe uma chave nova; retentativas usam a mesma.

- o escopo de unicidade combina a operação e o hash da chave;
- a mesma chave, operação e conteúdo devolvem por 24 horas o resultado de sucesso original;
- a mesma chave e operação com conteúdo diferente retornam `409 CONFLITO_IDEMPOTENCIA`;
- somente comandos concluídos com sucesso consomem a chave;
- validação, indisponibilidade e erro inesperado permitem nova tentativa;
- chamadas simultâneas com a mesma chave produzem no máximo um efeito e um conjunto de eventos;
- registros vencidos são removidos por processamento interno idempotente.

O token público é derivado com HMAC-SHA-256 da referência e de `SEGREDO_TOKEN_AGENDAMENTO`, permitindo reproduzir a resposta de criação sem persistir o segredo retornado.

## Cancelamento

- Reservas `PENDENTE` ou `CONFIRMADO` podem ser canceladas na API V1 conforme a autorização e o prazo configurado. A rejeição administrativa de uma pendência é um cancelamento com motivo obrigatório.
- Cliente só pode cancelar ou reagendar até `antecedenciaAlteracaoClienteMinutos` antes do início, usando 1440 minutos como padrão.
- Usuário administrativo no escopo pode cancelar ou reagendar uma reserva futura depois do limite público, com motivo obrigatório.
- Depois de `inicioEm`, não é permitido cancelar ou reagendar; a V1 permite concluir ou marcar ausência e não reabre estados terminais.
- Cancelamento registra autor/origem, instante e motivo quando informado.
- O intervalo volta a ficar disponível imediatamente após a confirmação da transação.
- Notificação de cancelamento é consequência assíncrona e não condição para sucesso.

Cancelamento ou reagendamento público exige a referência e o token secreto emitido na criação. A API compara o hash em tempo constante, não aceita token em URL e responde de forma neutra a referência ou token inválido. Informar apenas o número de celular nunca é autorização suficiente.

O reagendamento público mantém serviço, profissional, referência e token. A API garante o novo intervalo antes de liberar o anterior na mesma transação. Reserva `CONFIRMADO` permanece confirmada; `PENDENTE` permanece pendente e recebe novo `expiraEm`. Estado terminal ou chegada de `inicioEm` revoga o token. Trocar de serviço exige cancelar e criar uma nova reserva.

## Eventos de notificação

- Criar pendência, confirmar, reagendar, cancelar e expirar gravam o evento correspondente na mesma transação.
- Concluir, marcar ausência ou alterar somente recursos não cria evento de notificação na V1.
- Cada mudança de domínio produz no máximo um evento de cada tipo e versão, inclusive sob retentativa idempotente.
- Processamento, retentativa e ausência de provedor não alteram o resultado da reserva.
- Conteúdo, canal, lembretes e promoções permanecem fora do núcleo.

## Datas e fuso

- Cada instalação define um fuso IANA. O projeto de cliente da Revitalle poderá usar `America/Sao_Paulo`, sem tornar esse valor fixo no produto-base.
- Instantes persistidos usam UTC quando a tecnologia oferece instantes absolutos.
- Regras de expediente usam data, dia da semana e hora local do negócio.
- O contrato deve diferenciar explicitamente data local, hora local e instante com offset.

## Retenção e direitos do titular

- Produção exige `versaoAvisoPrivacidade`, `contatoTitular` e `retencaoDadosPessoaisDias` válidos.
- Somente `PROPRIETARIO` exporta, corrige ou anonimiza integralmente um cliente, após verificação operacional do solicitante.
- Anonimização é rejeitada enquanto existir reserva futura `PENDENTE` ou `CONFIRMADO`.
- A ação limpa nome, celular, email, CPF e fotografias pessoais, libera índices de identidade e mantém dados operacionais sem identificação.
- O processo interno aplica a mesma anonimização quando o prazo configurado passa desde o último atendimento terminal e não existe reserva futura.
- Auditoria registra autor, instante, motivo e identificadores internos, sem copiar dados pessoais.
- O registro de anonimizações deve ser reaplicado depois de restauração de backup anterior à ação.
