# Arquitetura

## Direção arquitetural

Começar com um monólito modular e uma API HTTP. O volume e o domínio iniciais não justificam serviços distribuídos. Módulos internos e portas de integração preservam a capacidade de evolução sem multiplicar operação, rede e consistência distribuída.

## Contexto de implantação

```text
Frontend público ----\
                      >---- API da instalação ---- Banco da instalação
Frontend admin ------/             |
                                   +---- Worker de notificações ---- Provedor externo
```

Este repositório entrega primeiro a API e seu worker. Os frontends aparecem apenas nos projetos derivados. Cada cliente recebe sua própria composição, código derivado, configuração, banco e implantação.

A V1 opera com uma única instância da aplicação e um PostgreSQL. Reinício, implantação e falha afetam apenas a instalação daquele cliente. Escala horizontal não faz parte do perfil atual.

O artefato de entrega é uma imagem Docker da API. Docker Compose inicia API e PostgreSQL apenas para desenvolvimento e validação local. Em produção, o banco é externo ao container e persistido pela plataforma escolhida no projeto do cliente.

## Módulos

Cada módulo funcional é organizado internamente em `api`, `aplicacao`, `dominio` e
`infraestrutura`, criados conforme existam entradas, casos de uso, regras ou
adaptadores reais. A separação preserva as dependências em direção ao domínio sem
transformar o monólito em múltiplos projetos ou criar camadas vazias.

### Acesso e identidade

Autenticação administrativa por Spring Security, sessão HTTP em memória, CSRF, bootstrap inicial e autorização por papéis. Não representa o cliente final.

### Catálogo de serviços

Cadastro, apresentação e ciclo de vida dos serviços próprios de cada profissional.

### Recursos

Profissionais, salas, equipamentos e suas relações com oferta e disponibilidade. O profissional é sempre alocado; salas e equipamentos são opcionais e alocados apenas quando exigidos pelo serviço.

### Agenda

Expediente, exceções, intervalos e cálculo de disponibilidade.

### Clientes

Identidade operacional e dados de contato do cliente final.

### Agendamentos

Reserva, estados, concorrência, histórico e operações administrativas.

### Notificações

Outbox, processamento e porta para adaptadores de provedores. Templates e adaptador concreto pertencem ao projeto do cliente.

## Dependências

- `Agendamentos` usa regras públicas de `Catálogo de serviços`, `Agenda` e `Clientes`.
- `Notificações` consome eventos; o domínio de reserva não chama diretamente WhatsApp.
- Frontends consomem apenas contratos da API.
- Código específico da Revitalle não entra na API V1 genérica.

## Persistência e consistência

- banco relacional como fonte de verdade;
- transações locais para reserva e outbox;
- migrações versionadas;
- restrições no banco para invariantes críticas sempre que possível;
- alocação atômica do profissional e de todos os recursos operacionais da reserva;
- cache opcional apenas para leitura reconstruível;
- processamento assíncrono com idempotência e retentativa controlada.
- paginação para históricos e índices relacionais orientados às consultas reais de agenda;
- sem cache distribuído, broker ou mecanismo externo de busca na V1.

## Repositório-base e projetos de clientes

O repositório-base publica versões estáveis da API. Um novo cliente começa com uma cópia de uma tag estável, em um projeto e repositório próprios. Ele nunca deve ser copiado do projeto personalizado de outro cliente.

Dentro do projeto do cliente, preferir:

1. configuração para marca, conteúdo e políticas simples;
2. frontend próprio consumindo o contrato da API;
3. adaptadores para integrações do cliente;
4. módulos explícitos para regras realmente exclusivas.

Depois da cópia, o projeto do cliente evolui independentemente. Não existe dependência de pacote, atualização automática ou obrigação de acompanhar novas versões. Uma correção genérica pode ser aplicada manualmente no produto-base e nas cópias interessadas. O processo detalhado está em `13-release-and-client-derivation.md`.

## Stack e convenções

- produto-base `service-scheduling-api`;
- Java 21;
- Spring Boot 4.1.1 em monólito modular;
- Maven Wrapper;
- PostgreSQL;
- Flyway;
- OpenAPI;
- Testcontainers com PostgreSQL real nos testes de integração;
- Docker Compose no desenvolvimento;
- imagem Docker como unidade executável da API.

A estrutura concreta de pacotes, dependências, configuração, tipos e verificação está em `18-technical-foundation.md`.

Recuperação de acesso segue D-021, a matriz de permissões segue D-014 e o contrato uniforme de erros segue D-006.

Disponibilidade e criação de reservas seguem D-022 e `16-scheduling-and-availability.md`: o PostgreSQL coordena disputas por bloqueio transacional do profissional e dos recursos, sem tabela de unidades de grade ou lock distribuído.

Notificações seguem D-011, D-012 e `17-notifications-outbox.md`: o worker vive no mesmo aplicativo, usa o PostgreSQL como fila transacional e opera com adaptador desabilitado quando nenhum canal foi instalado.

Flyway executa migrações forward-only na inicialização da instância única. Uma atualização exige backup anterior, troca da imagem e verificação de saúde. Rollback do código usa a imagem anterior quando o esquema permanece compatível; migração incompatível exige restauração do backup ou migração corretiva explícita.

## Pendência anterior à fundação

Não há decisão bloqueante aberta para F1. A identidade, o namespace e a linguagem ubíqua estão registrados em D-024 a D-026; as demais decisões da fundação estão registradas nos documentos especializados.
