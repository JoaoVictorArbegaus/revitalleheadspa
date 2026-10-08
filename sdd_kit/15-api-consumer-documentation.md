# Contrato da API para desenvolvimento do frontend

## Objetivo

Permitir que um desenvolvedor implemente o frontend da V1 sem precisar ler o código da API. Instalação e operação continuam documentadas pelos entregáveis próprios de F1 e F2, mas não são critérios de R1.

## Fonte de verdade do contrato

A especificação OpenAPI versionada ou gerada de forma reproduzível é a fonte executável para caminhos, métodos, parâmetros, payloads, respostas e segurança. Os documentos narrativos explicam comportamento e decisões, mas não podem contradizer a OpenAPI.

Alterações de endpoint exigem, na mesma tarefa:

- atualização do contrato;
- teste de contrato ou integração;
- atualização dos exemplos afetados;
- atualização do catálogo de erros quando aplicável.

## Entregáveis obrigatórios

### OpenAPI fiel

O contrato descreve todos os endpoints públicos e administrativos necessários ao
frontend, incluindo schemas, parâmetros, respostas, segurança, permissões e códigos
de erro estáveis.

### Guia curto do frontend

- obtenção do token CSRF;
- login administrativo por email e cookie de sessão;
- consulta da identidade atual;
- envio de CSRF em operações mutáveis;
- troca de senha e logout;
- permissões de `PROPRIETARIO` e `FUNCIONARIO`, inclusive escopo derivado da sessão;
- estados e transições de agendamento;
- modelo RFC 9457 e códigos de erro;
- criação pública idempotente e tratamento seguro de retentativas.

Todos os guias usam o modelo RFC 9457 definido em D-006. Exemplos de falha devem mostrar `codigo`, `idRastreio` e `errosCampos`, sem valores rejeitados ou detalhes internos. O catálogo de códigos publicado é mantido junto do OpenAPI.

O guia explica que a superfície pública compreende catálogo, disponibilidade e
criação de reserva sempre `CONFIRMADO`. Não existe operação pública posterior à
criação nem reagendamento na V1.

### Exemplos executáveis

O repositório deve oferecer exemplos `.http` executáveis pelo IntelliJ HTTP Client,
sem credenciais reais, cobrindo:

1. CSRF, login, identidade atual e logout;
2. operações administrativas de profissionais, recursos e serviços conforme as permissões;
3. configuração da agenda do profissional;
4. consulta pública de serviços e disponibilidade;
5. criação pública idempotente de reserva confirmada, incluindo retentativa e conflito de chave;
6. criação e operação administrativa de reserva confirmada;
7. anonimização manual de cliente pelo `PROPRIETARIO`;
8. erros representativos de autenticação, autorização, validação e conflito.

Os exemplos de criação não retornam credencial, token ou referência destinada a autorizar operações públicas posteriores. O consumidor público informa que alterações futuras dependem de contato com a administração.

Os exemplos geram uma `Chave-Idempotencia` por intenção de criação, repetem a mesma chave somente em retentativas e demonstram `409 CONFLITO_IDEMPOTENCIA`.

O guia documenta a ciência do aviso e a anonimização manual iniciada por `PROPRIETARIO`, sem escolher base legal ou prazo em nome da instalação.

### Catálogo de erros

Cada código estável informa status HTTP, significado, condição de ocorrência e ação esperada do consumidor. Mensagens humanas podem evoluir; códigos não mudam silenciosamente dentro da mesma versão principal.

## Critérios de validação

A documentação está pronta quando:

- OpenAPI representa todos os endpoints da V1;
- exemplos `.http` usam somente dados fictícios e executam contra uma API disponível;
- uma pessoa consegue implementar os fluxos público e administrativo do frontend seguindo o OpenAPI, o guia curto e os exemplos;
- autenticação, CSRF, campos, permissões, estados, erros e idempotência não dependem da leitura do código;
- links internos e exemplos são verificados;
- nenhuma credencial, token ou dado pessoal real está presente;
- nenhum endpoint ou exemplo oferece autoatendimento público após a criação da reserva;
- incompatibilidades entre documentação, OpenAPI e comportamento falham na validação da entrega.

## Regra contra duplicação

Não manter manualmente duas descrições completas do mesmo schema. OpenAPI define a estrutura; Markdown explica intenção, sequência, regras e exemplos. Quando possível, tabelas e clientes derivados do contrato devem ser gerados ou validados automaticamente.
