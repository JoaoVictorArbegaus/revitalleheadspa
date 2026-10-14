# Estratégia de qualidade

## Pirâmide de validação

### Testes unitários

Cobrem regras puras: sobreposição, transições, normalização, duração ocupada, janelas e valores do domínio.

### Testes de integração

Executam com o banco real escolhido e cobrem repositórios, migrações, transações, restrições, autenticação, concorrência e idempotência da criação pública.

### Testes de contrato/API

Validam payloads, estados HTTP, erros, autorização, paginação, idempotência e compatibilidade com OpenAPI.

### Testes de jornada

Cobrem poucos fluxos críticos completos:

- configurar serviço, recurso e agenda;
- concluir uma reserva pública auto-confirmada;
- disputar o mesmo horário em paralelo;
- cancelar administrativamente e tornar o intervalo disponível novamente;
- anonimizar manualmente um cliente elegível.

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
- reserva usando exatamente a quantidade fixa de cada recurso configurada no serviço;
- rejeição de redução de capacidade abaixo das alocações futuras vigentes;
- repetição idempotente da mesma criação pública;
- chave de idempotência ausente, curta, longa ou com caractere fora de `[A-Za-z0-9._~-]` rejeitada;
- chave idempotente reutilizada com conteúdo diferente;
- nome com 1 e 120 caracteres após `strip` aceito, e nome vazio ou acima do limite rejeitado;
- formatos comuns equivalentes do celular, com e sem `+55`, produzindo o mesmo valor canônico;
- fingerprint idempotente tratando nome, celular e instante normalizados como conteúdo equivalente;
- criações concorrentes com a mesma chave produzindo um único efeito;
- chave não sendo consumida por validação, indisponibilidade ou falha inesperada;
- resultado idempotente válido imediatamente antes de `expiraEm`, inválido no limite
  exclusivo e registro vencido removido oportunisticamente sem job;
- serviço desativado entre consulta e confirmação;
- cancelamento administrativo concorrente com outra ação;
- logs, métricas e erros sem dados pessoais, credenciais, tokens ou segredos;
- alterações de preço e duração sem modificar histórico;
- mudança de dia e regras de fuso aplicáveis;
- limites exatos de antecedência e horizonte usando relógio controlado;
- consulta pública acima de 31 dias rejeitada;
- bloqueio removendo disponibilidade do profissional no intervalo configurado;
- criação administrativa sem conseguir ignorar expediente, bloqueio, conflito ou capacidade;
- cancelamento administrativo rejeitado depois de `inicioEm`;
- toda reserva pública ou administrativa nascendo `CONFIRMADO`;
- bootstrap cria um único `PROPRIETARIO` e não redefine senha existente;
- login troca o identificador da sessão;
- escrita administrativa sem CSRF é rejeitada;
- sessão expirada, usuário inativo e rota desconhecida são rejeitados;
- mudança de senha invalida todas as sessões;
- rate limit bloqueia excesso de tentativas sem registrar credenciais;
- senha temporária bloqueia operações até ser trocada;
- `FUNCIONARIO` não acessa gestão de usuários;
- último `PROPRIETARIO` ativo não pode ser desativado ou rebaixado;
- desativação e redefinição de senha invalidam as sessões afetadas;
- criação de `FUNCIONARIO` sem profissional ou com profissional já vinculado é rejeitada;
- `FUNCIONARIO` mantém somente serviços do profissional vinculado e não lê nem altera serviço, agenda ou reserva de outro profissional;
- identificador de profissional no payload não amplia o escopo derivado da sessão;
- `FUNCIONARIO` não vê histórico do cliente com outros profissionais;
- `FUNCIONARIO` lista recursos e associa quantidades válidas aos próprios serviços, mas não altera cadastro, capacidade ou estado de recursos nem alocações de reservas;
- filtro ou payload não amplia o escopo profissional derivado da sessão;
- profissionais, recursos e serviços aceitam alteração completa por `PUT`, mas não `PATCH`;
- catálogo público retorna somente serviços e profissionais ativos;
- preços são armazenados e expostos com duas casas, e moeda e grade assumem `BRL` e `15` quando as variáveis da instalação não são informadas;
- recuperação não inicia servidor web e rejeita conta inexistente, inativa ou que não seja `PROPRIETARIO`;
- recuperação não expõe senha em logs e retorna código de saída coerente;
- senha recuperada autentica após reinício e a senha anterior deixa de funcionar;
- formatos brasileiros equivalentes do mesmo celular reutilizam um único cliente;
- dois clientes não podem persistir com o mesmo celular normalizado;
- contrato, persistência e respostas de cliente não possuem CPF, email, marketing ou campos adicionais;
- endpoint público devolve exatamente a versão e o texto configurados do aviso;
- produção recusa inicialização sem versão ou texto vigente do aviso de privacidade;
- criação pública e administrativa persistem exatamente a versão apresentada e o instante da ciência;
- criação pública retorna `201` sem `Location`, identificadores internos, referência
  ou token e com estado, snapshots públicos, intervalo, fuso, preço e moeda;
- snapshot de agendamento preserva preço e `moedaContratada` após mudança da configuração;
- criação com versão desatualizada do aviso é rejeitada antes de reservar o horário;
- anonimização manual é proibida para `FUNCIONARIO`;
- anonimização é rejeitada com reserva futura e bem-sucedida depois do fim da relação operacional;
- anonimização remove nome e celular do cadastro e das cópias pessoais das reservas;
- auditoria da anonimização não reproduz os valores removidos;
- não existem tabela, endpoint, job ou módulo de notificação na V1;
- todos os erros usam `application/problem+json` e preservam o schema de D-006;
- JSON ilegível retorna `400`, validação semântica retorna `422` e conflito de agenda retorna `409`;
- erros de campos não devolvem o valor rejeitado nem detalhes internos;
- códigos de erro documentados permanecem compatíveis com o contrato OpenAPI.

## Dados de teste

Para concluir B4, a suíte acrescenta proporcionalmente ao baseline de B3: consulta
administrativa com escopo, criação administrativa fora dos limites públicos e
idempotente, transições com janelas de horário e concorrência, auditoria das
transições, cliente restrito ao `PROPRIETARIO` e anonimização com sua auditoria.

- não usar celulares, nomes ou emails administrativos de pessoas reais;
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
