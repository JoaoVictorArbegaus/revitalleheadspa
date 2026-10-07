# Documentação para consumidores da API

## Objetivo

Permitir que um desenvolvedor inicie um frontend ou uma nova instalação usando uma release da API sem precisar ler sua implementação. A documentação de consumo faz parte da entrega da V1 e deve acompanhar o comportamento executável.

## Fonte de verdade do contrato

A especificação OpenAPI versionada ou gerada de forma reproduzível é a fonte executável para caminhos, métodos, parâmetros, payloads, respostas e segurança. Os documentos narrativos explicam comportamento e decisões, mas não podem contradizer a OpenAPI.

Alterações de endpoint exigem, na mesma tarefa:

- atualização do contrato;
- teste de contrato ou integração;
- atualização dos exemplos afetados;
- atualização do catálogo de erros e configuração quando aplicável.

## Entregáveis obrigatórios

### Início rápido

- dependências necessárias;
- configuração do PostgreSQL;
- variáveis mínimas;
- execução de migrações;
- bootstrap do primeiro `PROPRIETARIO`;
- inicialização da API;
- verificação de liveness e readiness.

### Referência de configuração

Cada variável deve informar:

- nome;
- finalidade;
- obrigatoriedade;
- valor padrão seguro, quando existir;
- formato e exemplo fictício;
- classificação como segredo ou configuração comum;
- ambiente em que se aplica.

A referência marca `SEGREDO_TOKEN_AGENDAMENTO`, `PII_ENCRYPTION_KEY` e `PII_LOOKUP_KEY` como segredos distintos e documenta os requisitos de geração e estabilidade. Também explica que produção exige versão do aviso, canal do titular e prazo de retenção definidos pela instalação.

### Autenticação administrativa

- obtenção do token CSRF;
- login e cookie de sessão;
- consulta da identidade atual;
- envio de CSRF em operações mutáveis;
- troca de senha;
- logout;
- bootstrap e recuperação operacional;
- respostas de sessão expirada, acesso negado e rate limit.

Todos os guias usam o modelo RFC 9457 definido em D-006. Exemplos de falha devem mostrar `codigo`, `idRastreio` e `errosCampos`, sem valores rejeitados ou detalhes internos. O catálogo de códigos publicado é mantido junto do OpenAPI.

O guia de recuperação deve informar como parar a aplicação, fornecer as variáveis temporárias, executar o modo sem servidor web, interpretar o código de saída, remover os segredos e reiniciar a instalação.

### Recursos da API

Para cada endpoint:

- objetivo e permissão necessária;
- parâmetros e campos obrigatórios;
- validações relevantes;
- resposta de sucesso;
- códigos de erro possíveis;
- efeitos no domínio;
- implicações de concorrência e idempotência quando existirem.

### Exemplos executáveis

O repositório deve oferecer uma coleção executável pelo IntelliJ HTTP Client ou formato equivalente, sem credenciais reais, cobrindo:

1. saúde da aplicação;
2. CSRF e login;
3. criação de profissional;
4. criação de recurso e capacidade;
5. criação de serviço e pré-seleções;
6. configuração de agenda;
7. consulta pública de disponibilidade;
8. criação idempotente de reserva;
9. consulta administrativa e mudança de estado;
10. cancelamento ou reagendamento público autorizado;
11. exportação e anonimização de cliente pelo `PROPRIETARIO`;
12. inspeção e nova tentativa de evento `FALHA_DEFINITIVA`;
13. logout.

A documentação da outbox lista eventos, payloads versionados, estados, sequência de retentativas, métricas e comportamento do adaptador desabilitado. Não inclui template ou credencial de provedor.

Exemplos públicos usam apenas tokens fictícios. O guia explica como manter o token no fragmento do link, enviá-lo por `X-Token-Agendamento`, evitar logs e tratar a perda do segredo sem tentar localizar reservas por celular.

Os exemplos geram uma `Chave-Idempotencia` por intenção, repetem a mesma chave somente em retentativas e demonstram `409 CONFLITO_IDEMPOTENCIA`. A referência de configuração marca `SEGREDO_TOKEN_AGENDAMENTO` como segredo obrigatório e explica seu requisito de estabilidade.

O guia de privacidade diferencia ciência do aviso e consentimento de marketing, documenta as operações do titular, a anonimização automática e o procedimento de reaplicação após restauração de backup. O texto não escolhe base legal ou prazo em nome da instalação.

### Catálogo de erros

Cada código estável informa status HTTP, significado, condição de ocorrência e ação esperada do consumidor. Mensagens humanas podem evoluir; códigos não mudam silenciosamente dentro da mesma versão principal.

### Banco e evolução

- estratégia de migrações Flyway;
- criação de banco vazio;
- atualização a partir da versão suportada anterior;
- proibição de editar migração já publicada;
- backup recomendado antes de atualização;
- notas sobre mudanças incompatíveis.

### Container e implantação

- construção e execução da imagem Docker;
- uso do Compose somente no ambiente local;
- conexão com PostgreSQL externo em produção;
- endpoints de liveness e readiness;
- configuração externa e segredos fora da imagem;
- backup antes de migração;
- condições para rollback da imagem e restauração do banco;
- exemplo de proxy no mesmo domínio encaminhando `/api`, sem impor provedor de nuvem.

### Releases

Cada release informa:

- versão e commit;
- requisitos de execução;
- migrações incluídas;
- endpoints adicionados, alterados ou removidos;
- novos campos de configuração;
- correções de segurança ou integridade relevantes;
- passos manuais de atualização.

## Critérios de validação

A documentação está pronta quando:

- OpenAPI representa todos os endpoints da release;
- exemplos usam somente dados fictícios e executam contra ambiente limpo;
- uma pessoa consegue instalar, inicializar, autenticar e concluir uma reserva seguindo apenas a documentação;
- campos, permissões, estados e erros não dependem da leitura do código;
- links internos e exemplos são verificados;
- nenhuma credencial, token ou dado pessoal real está presente;
- incompatibilidades entre documentação, OpenAPI e comportamento falham na validação da entrega.

## Regra contra duplicação

Não manter manualmente duas descrições completas do mesmo schema. OpenAPI define a estrutura; Markdown explica intenção, sequência, regras e exemplos. Quando possível, tabelas e clientes derivados do contrato devem ser gerados ou validados automaticamente.
