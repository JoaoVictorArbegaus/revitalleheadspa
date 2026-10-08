# service-scheduling-api

API genérica e reutilizável para agendamento de serviços. Este repositório será desenvolvido até uma V1 funcional e versionada no GitHub, antes de qualquer personalização para um cliente específico.

Repositório oficial: [JoaoVictorArbegaus/service-scheduling-api](https://github.com/JoaoVictorArbegaus/service-scheduling-api).

O desenvolvimento é versionado continuamente na branch `main`. A tag `v1.0.0` e a primeira GitHub Release só serão publicadas quando os critérios da etapa R2 estiverem concluídos.

Depois da V1, cada cliente terá um projeto próprio derivado de uma versão estável desta API, com banco, frontend e personalizações independentes. O produto não é multitenant: não existe compartilhamento de dados entre empresas nem identificação de tenant nas entidades do domínio.

A API-base é distribuída como imagem Docker e usa PostgreSQL com migrações Flyway. Docker Compose atende o desenvolvimento local; hospedagem, domínio, proxy, certificados, backups e monitoramento são definidos separadamente em cada projeto de cliente.

## Objetivo da V1

Entregar uma API completa para que o responsável por um negócio administre profissionais, recursos, serviços, preços, duração e horários, enquanto o cliente final consulta a disponibilidade e agenda sem criar conta, informando somente nome e celular.

A V1 termina com contrato OpenAPI, banco versionado, segurança administrativa, testes, documentação de execução e uma versão estável publicada. Frontends, identidade visual e regras próprias da Revitalle começam depois, em um projeto de cliente derivado da V1.

## Stack definida

- Java 21 e Spring Boot 4.1.1 em monólito modular;
- Maven Wrapper;
- PostgreSQL e Flyway;
- OpenAPI;
- Testcontainers para integração com banco real;
- Docker Compose no desenvolvimento e imagem Docker como artefato executável.
- linguagem ubíqua em português no domínio, código, JSON, rotas e banco.

## Executar localmente

Pré-requisitos:

- JDK 21 disponível em `JAVA_HOME`;
- Docker em execução para os testes de integração e para o ambiente completo.

Para compilar e executar toda a validação automatizada no Windows:

```powershell
.\mvnw.cmd clean verify
```

No Linux ou macOS:

```bash
./mvnw clean verify
```

Para iniciar a API e o PostgreSQL com valores exclusivamente locais:

```bash
export BOOTSTRAP_PROPRIETARIO_NOME='Proprietario Local'
export BOOTSTRAP_PROPRIETARIO_EMAIL='proprietario@example.test'
read -r -s -p 'Senha inicial: ' BOOTSTRAP_PROPRIETARIO_SENHA; echo
export BOOTSTRAP_PROPRIETARIO_SENHA
docker compose up --build
```

Depois da inicialização:

- liveness: `http://localhost:8080/actuator/health/liveness`;
- readiness: `http://localhost:8080/actuator/health/readiness`;
- OpenAPI: `http://localhost:8080/api-docs`;
- Swagger UI: `http://localhost:8080/swagger-ui.html`.

Para encerrar e preservar os dados locais, execute `docker compose down`. Para
recriar também o banco vazio e reaplicar todas as migrations, execute
`docker compose down --volumes` antes de subir o ambiente novamente.

As credenciais de `compose.yml` são apenas padrões locais. Uma instalação externa
deve fornecer `DATABASE_URL`, `DATABASE_USERNAME` e `DATABASE_PASSWORD` por meio
protegido. OpenAPI e Swagger UI ficam desabilitados por padrão no perfil `prod` e
podem ser habilitados conscientemente com `OPENAPI_HABILITADA` e
`SWAGGER_UI_HABILITADA`.

Na primeira inicialização de um banco sem usuários administrativos, também são
obrigatórias `BOOTSTRAP_PROPRIETARIO_NOME`, `BOOTSTRAP_PROPRIETARIO_EMAIL` e
`BOOTSTRAP_PROPRIETARIO_SENHA`. Elas criam uma única conta `PROPRIETARIO`; quando
já existe qualquer usuário, o bootstrap não cria conta nem redefine senha. Remova
a senha do ambiente depois da inicialização. Toda senha administrativa deve ter
entre 12 e 72 bytes em UTF-8.

O limite de login é configurado por `LOGIN_TENTATIVAS_MAXIMO` (padrão `5`) e
`LOGIN_TENTATIVAS_JANELA` (padrão `15m`). A autenticação usa sessão HTTP e CSRF,
não JWT. Consulte o [guia de autenticação administrativa](docs/autenticacao-administrativa.md)
para os exemplos `curl`, a gestão de usuários e o procedimento de recuperação do
proprietário.

`FORWARD_HEADERS_STRATEGY` permanece `NONE` por padrão. Uma instalação atrás de
proxy só deve habilitar cabeçalhos encaminhados depois de configurar a plataforma
para remover valores enviados diretamente pelo cliente e confiar apenas no proxy.
Nesse cenário, use `FORWARD_HEADERS_STRATEGY=NATIVE` para que o limite de login
identifique a origem do cliente em vez do endereço do proxy.

## Organização do código

O sistema é um monólito modular organizado primeiro por domínio funcional. Cada
domínio cria, conforme a necessidade real, os pacotes:

- `api`: controladores HTTP e DTOs de entrada e saída;
- `aplicacao`: coordenação dos casos de uso;
- `dominio`: entidades, valores e regras de negócio;
- `infraestrutura`: persistência, configuração e adaptadores externos.

Uma funcionalidade não acessa diretamente o repositório de outra. A coordenação
ocorre por casos de uso explícitos, mantendo regras de negócio independentes de
HTTP, JPA e provedores externos. A fundação atual possui somente classes reais em
`compartilhado`; os demais módulos serão criados nas tarefas funcionais.

## Como a documentação está organizada

- [AGENTS.md](AGENTS.md): regras obrigatórias para qualquer LLM que trabalhe no projeto.
- [TASKS.md](TASKS.md): plano operacional e estado das entregas.
- [SESSION.md](SESSION.md): fotografia curta do contexto atual.
- [sdd_kit/00-index.md](sdd_kit/00-index.md): índice das especificações duráveis do produto.
- [sdd_kit/13-release-and-client-derivation.md](sdd_kit/13-release-and-client-derivation.md): processo para publicar a API e iniciar projetos de clientes.
- [sdd_kit/15-api-consumer-documentation.md](sdd_kit/15-api-consumer-documentation.md): padrão obrigatório para que outros projetos consumam a API.
- [sdd_kit/18-technical-foundation.md](sdd_kit/18-technical-foundation.md): identidade, stack e convenções para iniciar o código.
- [docs/autenticacao-administrativa.md](docs/autenticacao-administrativa.md): bootstrap, sessão, CSRF, usuários e recuperação administrativa.

Antes de codificar, resolva as decisões bloqueantes registradas em `sdd_kit/11-open-decisions.md` e atualize os documentos afetados.

## Princípios do produto

1. Uma instalação atende um único negócio.
2. O cliente final agenda sem conta e com o mínimo de dados necessário.
3. Uma reserva nunca pode ocupar um horário já reservado.
4. Preço e duração contratados são preservados no agendamento, mesmo que o serviço mude depois.
5. O domínio de agenda não depende de WhatsApp, provedor de pagamentos ou framework de frontend.
6. A API V1 deve ser funcional e utilizável sem frontend próprio.
7. Cada cliente parte de uma versão estável da API, nunca do projeto personalizado de outro cliente.
8. Cada cópia evolui de forma independente; correções podem ser levadas manualmente entre o produto-base e os projetos quando houver interesse.
