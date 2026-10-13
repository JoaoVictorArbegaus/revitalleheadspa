# Regras de negócio

## Serviços

1. Nome, duração e preço são obrigatórios.
2. Cada serviço pertence exatamente a um profissional.
3. Profissionais diferentes não compartilham o mesmo registro de serviço.
4. Serviço inativo permanece no histórico, mas não aparece para novas reservas.
5. Alterar um serviço não altera reservas já criadas.
6. Duração ocupada é `duracaoMinutos + intervaloLimpezaMinutos`, quando o intervalo estiver configurado.
7. Um serviço pode exigir recursos específicos, cada um com uma quantidade fixa positiva.
8. Duração e intervalo são armazenados em minutos; quantidade de unidades de grade nunca é persistida no serviço.
9. A duração ocupada deve ser múltipla de `intervaloGradeMinutos`.
10. `PROPRIETARIO` administra todos os profissionais, serviços, recursos e requisitos de recursos.
11. `FUNCIONARIO` cria, lista, busca, altera, ativa e desativa somente serviços do profissional vinculado e pode associar recursos existentes com quantidades válidas.
12. `FUNCIONARIO` pode listar recursos, mas não administra profissionais nem cria ou altera cadastro, capacidade ou estado de recursos.
13. O preço possui duas casas decimais e usa a moeda configurada para a instalação.

## Grade de horários

- Cada instalação define uma única granularidade em minutos por variável de ambiente, com padrão `15`.
- Horários oferecidos começam somente em posições alinhadas à grade no fuso da instalação.
- A ocupação é um intervalo contínuo calculado por `duracaoMinutos + intervaloLimpezaMinutos`.
- A quantidade de unidades exibida pelo frontend é derivada da ocupação dividida pela granularidade.
- A API rejeita serviço cuja ocupação não seja múltipla da grade; não arredonda duração silenciosamente.
- Alterar a granularidade é permitido somente quando todos os serviços existentes permanecem compatíveis.

## Disponibilidade

Um horário é reservável somente quando todas as condições forem verdadeiras:

1. o serviço está ativo;
2. o profissional proprietário está ativo;
3. o início e todo o período ocupado estão dentro do expediente recorrente do profissional;
4. não existe bloqueio conflitante do profissional;
5. o profissional não possui reserva `CONFIRMADO` sobreposta;
6. todos os recursos exigidos pelo serviço estão ativos;
7. para cada recurso, a soma das quantidades em reservas `CONFIRMADO` sobrepostas mais a quantidade fixa exigida não excede sua capacidade;
8. o início está alinhado à granularidade definida;
9. o horário respeita antecedência mínima, limite futuro e fuso do negócio.

Para o fluxo público, a antecedência padrão é 60 minutos e o horizonte padrão é 90 dias. Ambos são configuráveis conforme D-019. Cada consulta pública cobre no máximo 31 dias.

`dataInicial` e `dataFinal` são datas locais inclusivas. O período é válido quando
`dataInicial <= dataFinal` e contém no máximo 31 datas. A antecedência usa instante
absoluto: `inicioEm >= agora + antecedenciaMinimaReservaMinutos`. O horizonte usa o
calendário local: com valor `N`, hoje e os `N - 1` dias seguintes são elegíveis.
Consultas válidas podem incluir datas fora desses limites, mas não recebem horários
fora deles.

Um bloqueio remove disponibilidade e prevalece sobre o expediente recorrente. Recursos operacionais não possuem expediente ou bloqueios próprios, e a API V1 não oferece abertura extraordinária.

O `PUT` do expediente substitui atomicamente todas as janelas semanais do
profissional. Janelas omitidas são removidas e lista vazia limpa a agenda. Cada
janela fica no mesmo dia local; adjacência é permitida e sobreposição é rejeitada.
Bloqueios são intervalos semiabertos sem motivo; podem ser adjacentes, mas não
sobrepostos para o mesmo profissional.

A consulta de disponibilidade é informativa. A criação da reserva deve repetir as validações dentro da proteção transacional, pois outro cliente pode ocupar o horário entre consulta e confirmação.

## Sobreposição

Dois intervalos se sobrepõem quando:

`inicioA < fimB` e `inicioB < fimA`.

Somente reservas `CONFIRMADO` bloqueiam agenda e capacidade. `CANCELADO`, `CONCLUIDO` e `NAO_COMPARECEU` não bloqueiam novos horários.

## Concorrência

- O banco é a autoridade final sobre ocupação.
- A implementação deve garantir que duas requisições concorrentes não confirmem intervalos conflitantes.
- A garantia abrange o profissional e cada recurso exigido pelo serviço.
- Profissional e recursos são bloqueados atomicamente; a reserva inteira falha se qualquer participante estiver indisponível.
- Para recursos com capacidade maior que um, o conflito ocorre quando a soma das quantidades sobrepostas ultrapassa a capacidade.
- A transação bloqueia primeiro o profissional e depois os recursos por identificador crescente e revalida todas as ocupações antes de gravar.
- Um teste de integração concorrente é obrigatório.
- Cache nunca decide sozinho se um horário pode ser reservado.

## Criação de cliente e reserva

1. Exigir nome, celular, versão apresentada e confirmação da ciência do aviso de privacidade.
2. Aplicar `strip` ao nome e exigir de 1 a 120 caracteres no resultado.
3. Rejeitar a versão desatualizada e normalizar o celular brasileiro, recebido com
   formatação comum e `+55` opcional, para `+55` seguido de 10 ou 11 dígitos.
4. Localizar o cliente pelo celular normalizado ou criá-lo quando não existir.
5. Reutilizar o cliente encontrado e atualizar seu nome canônico, preservando a fotografia dos dados na reserva.
6. Revalidar serviço, profissional e todos os recursos exigidos sob controle transacional.
7. Criar `Agendamento` diretamente em `CONFIRMADO`, com fotografias do serviço, do
   profissional, do preço e da moeda contratados, do nome e celular informados, da
   versão do aviso e do instante da ciência.
8. Criar automaticamente as alocações com as quantidades fixas vigentes nos requisitos do serviço.
9. Confirmar a transação.

Se o horário tiver sido ocupado, retornar conflito de negócio e permitir ao frontend atualizar as opções.

## Recursos das reservas

- Toda reserva pública ou administrativa usa exatamente os recursos e quantidades fixas exigidos pelo serviço.
- A API V1 não aceita ajuste, remoção, substituição, adição ou realocação de recursos em uma reserva.
- Alterar requisitos do serviço não modifica as alocações de reservas existentes.
- Reduzir a capacidade de um recurso abaixo da quantidade já alocada em reservas futuras `CONFIRMADO` é proibido.
- A quantidade exigida não pode exceder a capacidade atual do recurso.
- Desativar um recurso impede novas reservas dos serviços que o exigem, mas preserva reservas e histórico existentes.

## Idempotência

A criação pública exige `Chave-Idempotencia` no formato
`[A-Za-z0-9._~-]{16,128}`. Cada intenção do usuário recebe uma chave nova;
retentativas usam a mesma.

- o hash da chave é único;
- a impressão digital usa o conteúdo relevante após normalizar nome, celular e valores temporais;
- a mesma chave e o mesmo conteúdo devolvem o resultado original enquanto `agora < expiraEm`;
- a mesma chave com conteúdo diferente retorna `409 CONFLITO_IDEMPOTENCIA`;
- somente comandos concluídos com sucesso consomem a chave;
- validação, indisponibilidade e erro inesperado permitem nova tentativa;
- chamadas simultâneas com a mesma chave produzem no máximo uma reserva;
- no instante `expiraEm` o registro deixa de valer e a chave pode iniciar novo comando;
- registros vencidos são removidos oportunisticamente pelo fluxo de criação, sem job.

## Operação administrativa de reservas

- Reservas públicas e administrativas nascem em `CONFIRMADO`.
- `PROPRIETARIO` pode criar e operar reservas de qualquer profissional.
- `FUNCIONARIO` pode criar, cancelar, concluir ou registrar ausência somente nas reservas do profissional vinculado.
- A criação administrativa exige somente nome e celular do cliente e aplica as mesmas regras de grade, expediente, bloqueio, capacidade e concorrência da criação pública.
- A criação administrativa não aplica antecedência mínima nem horizonte públicos, mas rejeita início anterior ao instante atual com `422 INICIO_NO_PASSADO`.
- A criação administrativa exige `Chave-Idempotencia` com as mesmas regras da pública; a impressão digital inclui o autor e o registro guarda somente a referência da reserva produzida.
- Uma reserva `CONFIRMADO` pode ser cancelada antes de `inicioEm` por usuário autorizado; o cancelamento registra autor, instante e motivo de 1 a 500 caracteres após `strip`.
- A partir de `inicioEm`, a reserva `CONFIRMADO` pode ser concluída ou marcada como `NAO_COMPARECEU` por usuário autorizado, registrando autor e instante.
- Cada transição bloqueia a reserva antes de verificar estado e horário; transição fora do estado ou da janela retorna `409 TRANSICAO_AGENDAMENTO_INVALIDA`.
- O intervalo volta a ficar disponível imediatamente após a confirmação da transação de cancelamento.
- Não existe confirmação, rejeição ou reagendamento público ou administrativo na API V1.

## Datas e fuso

- Cada instalação define o fuso IANA por `NEGOCIO_FUSO_HORARIO`, com padrão `America/Sao_Paulo`.
- Instantes persistidos usam UTC quando a tecnologia oferece instantes absolutos.
- Regras de expediente usam data, dia da semana e hora local do negócio.
- O contrato deve diferenciar explicitamente data local, hora local e instante com offset.

## Anonimização manual

- Somente `PROPRIETARIO` anonimiza integralmente um cliente, após verificação operacional do solicitante.
- A operação bloqueia o cliente e depois verifica reservas; é rejeitada com `409 CLIENTE_POSSUI_RESERVA_FUTURA` enquanto existir reserva `CONFIRMADO` com início posterior ao instante atual.
- Cliente já anonimizado retorna `409 CLIENTE_JA_ANONIMIZADO`.
- A ação limpa nome, celular e fotografias pessoais das reservas, libera índices de identidade e mantém dados operacionais sem identificação.
- A operação registra em `AnonimizacaoCliente` autor, instante, motivo e identificadores internos, sem copiar dados pessoais.
- Uma nova reserva com o celular removido cria um novo cliente.
- Exportação, correção e anonimização automática permanecem fora da API V1.
