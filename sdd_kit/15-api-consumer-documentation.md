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

A referência explica que produção exige a versão vigente do aviso de privacidade definida pela instalação. A política jurídica de retenção é responsabilidade operacional da instalação e não aciona anonimização automática na V1.

### Autenticação administrativa

- obtenção do token CSRF;
- login administrativo por email e cookie de sessão;
- consulta da identidade atual;
- envio de CSRF em operações mutáveis;
- troca de senha;
- logout;
- bootstrap e recuperação operacional;
- respostas de sessão expirada, acesso negado e rate limit.

Todos os guias usam o modelo RFC 9457 definido em D-006. Exemplos de falha devem mostrar `codigo`, `idRastreio` e `errosCampos`, sem valores rejeitados ou detalhes internos. O catálogo de códigos publicado é mantido junto do OpenAPI.

O guia de recuperação deve informar como parar a aplicação, fornecer as variáveis temporárias, executar o modo sem servidor web, interpretar o código de saída, remover os segredos e reiniciar a instalação.

### Recursos da API

A superfície pública compreende a consulta de catálogo e disponibilidade e a criação de uma reserva sempre `CONFIRMADO`. Não existe consulta, cancelamento ou outra operação pública após a criação, nem referência ou token público de reserva para autoatendimento. Consulta e mudanças posteriores usam sessão administrativa e CSRF quando mutáveis. Reagendamento não existe na V1, inclusive para usuários administrativos.

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
5. criação de serviço e requisitos fixos de recursos;
6. configuração da agenda do profissional;
7. consulta pública de disponibilidade;
8. criação pública idempotente de reserva confirmada;
9. criação administrativa de reserva confirmada;
10. consulta administrativa e mudança de estado, incluindo cancelamento;
11. anonimização manual de cliente pelo `PROPRIETARIO`;
12. logout.

Os exemplos de criação não retornam credencial, token ou referência destinada a autorizar operações públicas posteriores. O consumidor público informa que alterações futuras dependem de contato com a administração.

Os exemplos geram uma `Chave-Idempotencia` por intenção de criação, repetem a mesma chave somente em retentativas e demonstram `409 CONFLITO_IDEMPOTENCIA`.

O guia de privacidade documenta a ciência do aviso e a anonimização manual iniciada por `PROPRIETARIO`. Também descreve o procedimento operacional para reaplicar manualmente anonimizações posteriores ao ponto de restauração de um backup. O texto não escolhe base legal ou prazo em nome da instalação.

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
- nenhum endpoint ou exemplo oferece autoatendimento público após a criação da reserva;
- nenhuma configuração ou operação depende de notificação ou outbox;
- incompatibilidades entre documentação, OpenAPI e comportamento falham na validação da entrega.

## Regra contra duplicação

Não manter manualmente duas descrições completas do mesmo schema. OpenAPI define a estrutura; Markdown explica intenção, sequência, regras e exemplos. Quando possível, tabelas e clientes derivados do contrato devem ser gerados ou validados automaticamente.
