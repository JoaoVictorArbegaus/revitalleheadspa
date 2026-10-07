# Fundação técnica da API

## Identidade do produto-base

- nome do produto e do repositório: `service-scheduling-api`;
- `artifactId`: `service-scheduling-api`;
- classe principal: `AplicacaoAgendamentoServicos`;
- versão inicial durante o desenvolvimento: `0.1.0-SNAPSHOT`;
- primeira versão pública estável pretendida: `1.0.0`;
- `groupId`: `com.servicescheduling`;
- pacote Java raiz: `com.servicescheduling.api`.

`Revitalle` não aparece em coordenadas Maven, pacotes, variáveis, banco, rotas ou imagens do produto-base. Esse nome pertence ao primeiro projeto derivado depois da V1.

## Baseline da fundação

A tarefa F1 deve iniciar com:

- Java 21;
- Spring Boot 4.1.1;
- Maven Wrapper e `spring-boot-starter-parent`;
- aplicação Servlet com Spring MVC;
- PostgreSQL como único banco suportado;
- migrações Flyway executadas na inicialização;
- JPA/Hibernate para persistência;
- Spring Security, Bean Validation e Actuator;
- springdoc-openapi 3.1.1 para OpenAPI 3 e interface local de exploração;
- JUnit 5, módulos de teste do Spring Boot e Testcontainers PostgreSQL;
- Lombok apenas para reduzir código mecânico, sem `@Data` em entidades JPA nem geração implícita de regras de igualdade;
- imagem Docker da aplicação e Docker Compose para API e PostgreSQL no ambiente local.

Versões gerenciadas pelo Spring Boot não são sobrescritas sem incompatibilidade comprovada. Antes de iniciar F1, uma versão corretiva estável da mesma linha `4.1.x` pode substituir `4.1.1`; mudança de linha exige registrar a decisão e executar a suíte completa.

Dependências específicas de outro produto, inclusive AWS SDK, armazenamento de arquivos, email, pagamentos e provedor de WhatsApp, não entram na fundação. Uma dependência nova precisa atender uma tarefa atual.

## Linguagem ubíqua

Conceitos do negócio são nomeados em português brasileiro em documentação, Java, JSON, OpenAPI e PostgreSQL. Identificadores técnicos não usam acentos nem cedilha. Estados e eventos usam `UPPER_SNAKE_CASE` em português.

Nomes impostos por Java, Spring, HTTP, RFCs ou bibliotecas não são traduzidos. Tipos criados pelo projeto usam português também nas funções técnicas: `ControladorAgendamento`, `RepositorioAgendamento` e `CalculadoraDisponibilidade`. Anotações e interfaces externas, como `RestController` e `JpaRepository`, preservam o nome oficial. Não criar uma classe chamada `Appointment`, um campo `customerId` ou uma rota `/appointments`.

## Estrutura do código

O código usa pacote por funcionalidade dentro de `com.servicescheduling.api`:

```text
com.servicescheduling.api
├── AplicacaoAgendamentoServicos
├── acesso
├── profissionais
├── servicos
├── recursos
├── agenda
├── clientes
├── agendamentos
├── notificacoes
└── compartilhado
```

Responsabilidades:

| Pacote | Responsabilidade |
|---|---|
| `acesso` | contas administrativas, autenticação, sessão, papéis e recuperação operacional |
| `profissionais` | cadastro do profissional e vínculo opcional com `FUNCIONARIO` |
| `servicos` | serviços próprios de cada profissional, preços, durações e predefinições de recursos |
| `recursos` | salas, equipamentos, capacidades e agendas reserváveis |
| `agenda` | expediente, exceções e cálculo de disponibilidade |
| `clientes` | identidade operacional, contatos e direitos sobre dados pessoais |
| `agendamentos` | reservas, estados, alocações, idempotência e concorrência |
| `notificacoes` | outbox, worker e porta de entrega |
| `compartilhado` | erros HTTP, relógio, configuração técnica e utilidades realmente compartilhadas |

Cada funcionalidade cria subpacotes como `api`, `aplicacao`, `dominio` e `infraestrutura` somente quando houver classes reais para eles. Não criar camadas vazias, um projeto Maven por módulo, interfaces que apenas repetem uma classe ou um pacote `comum` genérico.

Controladores dependem de casos de uso da própria funcionalidade. Repositórios JPA e detalhes de integração permanecem internos. Uma funcionalidade não acessa diretamente o repositório de outra; a coordenação acontece por um caso de uso explícito. `compartilhado` não contém regras de negócio.

## Contrato HTTP

- prefixo funcional definitivo: `/api/v1`;
- rotas públicas: `/api/v1/publico/**`;
- rotas administrativas: `/api/v1/administracao/**`;
- saúde operacional: `/actuator/health/liveness` e `/actuator/health/readiness`;
- descrição OpenAPI: `/api-docs`;
- interface de exploração local: `/swagger-ui.html`;
- JSON como formato de sucesso e `application/problem+json` para falhas;
- nomes JSON em `camelCase`;
- nomes de domínio no JSON e nos recursos HTTP em português sem acentos;
- instantes em ISO 8601 com offset ou `Z`;
- IDs internos nunca substituem a referência pública da reserva em fluxos sem autenticação.

Swagger UI e a descrição OpenAPI ficam habilitados em desenvolvimento e teste. Em produção, sua exposição é desabilitada por padrão e pode ser habilitada conscientemente pela instalação.

O contrato fonte é gerado e validado junto da aplicação. Antes da V1, R1 também grava no repositório uma representação OpenAPI consumível e exemplos executáveis, para que outro frontend possa ser criado sem ler o código Java.

## Persistência e tipos

- chaves internas usam UUID gerado pela aplicação;
- instantes absolutos usam `Instant` no domínio e `timestamptz` no PostgreSQL;
- datas e horas recorrentes de expediente usam tipos locais acompanhados do fuso IANA da instalação;
- dinheiro usa decimal, nunca `double` ou `float`, e sempre trafega com moeda explícita;
- durações e intervalos usam minutos inteiros;
- enums persistem nomes textuais estáveis, nunca posições ordinais;
- nomes de tabela e coluna usam `snake_case` em português sem acentos;
- toda alteração de esquema entra em uma nova migração Flyway; migração aplicada não é reescrita;
- `spring.jpa.hibernate.ddl-auto=validate`; Hibernate não cria nem altera o esquema;
- consultas de histórico são paginadas e possuem ordenação determinística.

Entidades JPA não são expostas diretamente pela API. Requisições e respostas usam DTOs específicos, e snapshots históricos não são remontados a partir do cadastro atual.

## Configuração

Usar `application.yml` para valores seguros e padrões locais, e variáveis de ambiente para configuração da instalação e segredos. Configurações relacionadas são ligadas por `@ConfigurationProperties` com validação no início da aplicação.

Perfis previstos:

| Perfil | Uso |
|---|---|
| padrão | execução local com valores não secretos e dependências explícitas |
| `test` | testes automatizados; integração recebe PostgreSQL do Testcontainers |
| `prod` | produção; exige segredos e configurações obrigatórias e usa cookies seguros |

Não criar um perfil por cliente. As diferenças simples entram por configuração; extensões próprias vivem no repositório derivado.

## Observabilidade

- logs estruturados incluem `idRastreio`, método, rota, estado e duração, sem corpo de requisição por padrão;
- CPF, celular, email, senha, cookie, token e chave de idempotência nunca são registrados;
- um filtro aceita ou cria identificador de correlação e o devolve na resposta;
- Actuator expõe somente os endpoints necessários;
- liveness verifica o processo; readiness verifica a capacidade de atender, inclusive banco;
- métricas e logs do worker distinguem `PENDENTE`, `ENTREGUE`, `IGNORADO` e `FALHA_DEFINITIVA` sem copiar conteúdo ou destinatário.

## Qualidade e verificação

O comando de referência é `./mvnw clean verify` no Linux/macOS e `.\mvnw.cmd clean verify` no Windows. Ele deve compilar, executar testes e validar as regras automatizadas configuradas no projeto.

Aplicar esta proporção:

- testes unitários para regras de domínio puras;
- testes de controller para contrato, validação, autenticação, autorização e CSRF;
- testes de integração com PostgreSQL real para JPA, Flyway, locks, capacidade, idempotência e outbox;
- poucos testes de jornada cobrindo os fluxos público e administrativo mais importantes;
- teste de inicialização com banco vazio e validação automática do contrato OpenAPI.

H2 e outros bancos em memória não validam comportamento de persistência. Mocks não substituem os testes concorrentes definidos em D-022.

## Critério de conclusão de F1

F1 termina somente quando o esqueleto usa a identidade aprovada, o build limpo passa, o Compose inicia PostgreSQL e API, Flyway cria um banco vazio, readiness responde, o formato RFC 9457 possui teste e a documentação permite repetir a execução local. Funcionalidades de negócio começam nas tarefas seguintes.
