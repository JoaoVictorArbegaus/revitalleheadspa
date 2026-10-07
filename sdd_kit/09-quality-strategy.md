# Estratégia de qualidade

## Pirâmide de validação

### Testes unitários

Cobrem regras puras: sobreposição, transições, normalização, duração ocupada, janelas e valores do domínio.

### Testes de integração

Executam com o banco real escolhido e cobrem repositórios, migrações, transações, restrições, autenticação e outbox.

### Testes de contrato/API

Validam payloads, estados HTTP, erros, autorização, paginação, idempotência e compatibilidade com OpenAPI.

### Testes de jornada

Cobrem poucos fluxos críticos completos:

- configurar serviço e agenda;
- concluir uma reserva pública;
- disputar o mesmo horário em paralelo;
- cancelar e tornar o intervalo disponível novamente;
- registrar e processar confirmação sem duplicidade.

## Casos críticos obrigatórios

- início ou fim fora do expediente;
- serviço que não cabe no restante da janela;
- rejeição de duração ocupada incompatível com a grade;
- horários de início alinhados à grade configurada;
- alteração de granularidade rejeitada quando invalida serviço existente;
- bloqueio parcial e integral;
- intervalos adjacentes sem falso conflito;
- reserva concorrente do mesmo horário;
- profissionais diferentes disputando a mesma sala ou equipamento;
- consumo parcial e total de recurso com capacidade maior que um;
- duas reservas concorrentes tentando consumir a última unidade disponível;
- duas criações concorrentes que consultaram previamente o mesmo horário resultam em somente uma ocupação válida;
- falha integral quando apenas um dos recursos exigidos está indisponível;
- serviço sem recurso operacional dependendo somente da agenda do profissional;
- reserva pública usando exatamente as quantidades padrão do serviço;
- remoção administrativa permitida quando o mínimo é zero e rejeitada quando viola mínimo positivo;
- recurso adicional sem predefinição podendo ser removido posteriormente;
- troca atômica de recursos sem perder as alocações antigas quando a nova falha;
- rejeição de redução de capacidade abaixo das alocações futuras vigentes;
- repetição idempotente da mesma solicitação;
- chave idempotente reutilizada com conteúdo diferente;
- criação, cancelamento e reagendamento simultâneos com a mesma chave produzindo um único efeito e eventos únicos;
- chave não sendo consumida por validação, indisponibilidade ou falha inesperada;
- expiração e limpeza do registro após 24 horas;
- retentativa da criação reproduzindo o mesmo token sem persistência em texto puro;
- serviço desativado entre consulta e confirmação;
- cancelamento concorrente com outra ação;
- falha e retentativa do provedor de notificação;
- criação atômica da reserva e de exatamente um evento por mudança;
- oito falhas levando a `FALHA_DEFINITIVA` conforme a sequência de espera;
- nova tentativa administrativa preservando `eventoId` e impedindo entrega duplicada;
- adaptador desabilitado produzindo `IGNORADO` sem falhar a reserva;
- `FUNCIONARIO` sem acesso à inspeção ou retentativa global da outbox;
- payload, logs e métricas sem CPF, token ou contato completo;
- alterações de preço e duração sem modificar histórico;
- mudança de dia e regras de fuso aplicáveis.
- limites exatos de antecedência, horizonte e alteração pública usando relógio controlado;
- consulta pública acima de 31 dias rejeitada;
- exceção `ABERTURA` criando disponibilidade extraordinária e `BLOQUEIO` prevalecendo na sobreposição;
- administração ultrapassando limites públicos sem conseguir ignorar expediente, conflito ou capacidade;
- cancelamento e reagendamento rejeitados depois de `inicioEm`;
- referência sem token, token inválido e token de outra reserva produzindo a mesma resposta neutra;
- token não aparecendo em persistência pura, URL, log ou resposta posterior à criação;
- reagendamento garantindo o novo intervalo antes de liberar o anterior;
- reagendamento preservando serviço, profissional, referência e estado conforme D-016;
- estado terminal e chegada de `inicioEm` revogando o acesso público;
- bootstrap cria um único `PROPRIETARIO` e não redefine senha existente;
- login troca o identificador da sessão;
- escrita administrativa sem CSRF é rejeitada;
- sessão expirada, usuário inativo e rota desconhecida são rejeitados;
- mudança de senha invalida todas as sessões;
- rate limit bloqueia excesso de tentativas sem registrar credenciais.
- senha temporária bloqueia operações até ser trocada;
- `FUNCIONARIO` não acessa gestão de usuários;
- último `PROPRIETARIO` ativo não pode ser desativado ou rebaixado;
- desativação e redefinição de senha invalidam as sessões afetadas.
- criação de `FUNCIONARIO` sem profissional ou com profissional já vinculado é rejeitada;
- `FUNCIONARIO` não lê nem altera serviço, agenda ou reserva de outro profissional;
- identificador de profissional no payload não amplia o escopo derivado da sessão;
- `FUNCIONARIO` não vê histórico do cliente com outros profissionais;
- `FUNCIONARIO` pode consultar e alocar recurso compartilhado, mas não alterar sua capacidade.
- recuperação não inicia servidor web e rejeita conta inexistente, inativa ou que não seja `PROPRIETARIO`;
- recuperação não expõe senha em logs e retorna código de saída coerente;
- senha recuperada autentica após reinício e a senha anterior deixa de funcionar.
- pendência bloqueia profissional e todos os recursos até confirmação, rejeição ou expiração;
- expiração concorrente é idempotente e libera as alocações uma única vez;
- pendência vencida não pode ser confirmada, mesmo antes da próxima execução periódica;
- `FUNCIONARIO` não confirma nem rejeita pendência de outro profissional;
- criação administrativa nasce confirmada nos dois modos de confirmação;
- formatos brasileiros equivalentes do mesmo celular reutilizam um único cliente;
- dois clientes não podem persistir com o mesmo celular normalizado;
- divergência entre celular e CPF retorna conflito neutro sem revelar cadastro existente;
- atualização do cadastro não altera as fotografias de reservas anteriores;
- CPF persistido cifrado, localizado por HMAC e ausente de logs e respostas indevidas;
- produção recusando inicialização sem aviso, canal e retenção configurados;
- recusa de marketing não impedindo reserva e nunca sendo convertida em aceite implícito;
- exportação, correção integral e anonimização proibidas para `FUNCIONARIO`;
- anonimização rejeitada com reserva futura e bem-sucedida após o fim da relação operacional;
- restauração de backup reaplicando anonimizações posteriores;
- todos os erros usam `application/problem+json` e preservam o schema de D-006;
- JSON ilegível retorna `400`, validação semântica retorna `422` e conflito de agenda retorna `409`;
- erros de campos não devolvem o valor rejeitado nem detalhes internos;
- códigos de erro documentados permanecem compatíveis com o contrato OpenAPI.

## Dados de teste

- não usar CPFs, celulares ou nomes de pessoas reais;
- manter geradores de dados válidos e claramente fictícios;
- limpar ou isolar dados por teste;
- testar migrações desde banco vazio e, antes de produção, a partir da versão anterior suportada.

## Portão de conclusão

Uma tarefa funcional exige:

- critérios de aceite satisfeitos;
- testes relevantes automatizados e aprovados;
- análise estática e build aprovados;
- contrato e documentação atualizados;
- nenhuma credencial ou dado pessoal em código, fixtures inadequadas ou logs;
- limitações restantes registradas em `TASKS.md` ou `sdd_kit/11-open-decisions.md`.

## Desempenho proporcional

- O perfil de referência possui quatro profissionais e aproximadamente quarenta serviços ativos.
- Testes comprovam concorrência e integridade nos pontos críticos, sem suíte de carga distribuída.
- Consultas de agenda devem evitar padrões N+1 evidentes e usar índices coerentes com filtros por profissional, recurso, estado e período.
- Histórico de reservas usa paginação; catálogo pequeno pode usar listagem simples quando o contrato delimitar o volume.
- Otimização adicional exige medição ou requisito concreto.

## Validação manual

Interfaces exigem verificação real em tamanhos de tela representativos, teclado e mensagens de erro. Um teste automatizado aprovado não prova sozinho legibilidade, fluxo ou aparência.
