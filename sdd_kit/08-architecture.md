# Arquitetura

## Direção arquitetural

Começar com um monólito modular e uma API HTTP. O volume e o domínio iniciais não justificam serviços distribuídos. Módulos internos preservam a capacidade de evolução sem multiplicar operação, rede e consistência distribuída. Fronteiras externas só são criadas quando uma integração concreta for aprovada.

## Contexto de implantação

```text
Frontend público ----\
                      >---- API da instalação ---- Banco da instalação
Frontend admin ------/
```

Este repositório entrega a API. Os frontends aparecem apenas nos projetos derivados. Cada cliente recebe sua própria composição, código derivado, configuração, banco e implantação.

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

Profissionais, salas, equipamentos e suas relações com a oferta. O profissional é sempre alocado; salas e equipamentos são opcionais, não possuem agenda própria, têm capacidade simples e usam a quantidade fixa configurada no serviço.

### Agenda

Expediente recorrente e bloqueios do profissional, intervalos e cálculo de disponibilidade.

### Clientes

Identidade operacional mínima do cliente final, formada por nome e celular, aviso de privacidade e anonimização manual.

### Agendamentos

Reserva auto-confirmada, estados operacionais, concorrência, histórico e operações administrativas.

## Dependências

- `Agendamentos` usa regras públicas de `Catálogo de serviços`, `Agenda` e `Clientes`.
- Frontends consomem apenas contratos da API.
- Código específico da Revitalle não entra na API V1 genérica.
- A V1 não possui módulo de notificações, outbox, worker, porta ou adaptador de comunicação.

## Persistência e consistência

- banco relacional como fonte de verdade;
- transações locais para cliente, reserva e alocações;
- migrações versionadas;
- restrições no banco para invariantes críticas sempre que possível;
- alocação atômica do profissional e de todos os recursos operacionais da reserva;
- cache opcional apenas para leitura reconstruível;
- paginação para históricos e índices relacionais orientados às consultas reais de agenda;
- sem cache distribuído, broker ou mecanismo externo de busca na V1.

## Repositório-base e projetos de clientes

O repositório-base publica versões estáveis da API. Um novo cliente começa com uma cópia de uma tag estável, em um projeto e repositório próprios. Ele nunca deve ser copiado do projeto personalizado de outro cliente.

Dentro do projeto do cliente, preferir:

1. configuração para marca, conteúdo e políticas simples;
2. frontend próprio consumindo o contrato da API;
3. módulos explícitos para regras realmente exclusivas;
4. integração externa somente quando existir necessidade e provedor concretos.

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

Notificações não pertencem à V1. Qualquer trabalho futuro de comunicação depende de nova decisão, tarefa própria e provedor concreto; nenhum componente preparatório é criado no produto-base.

Flyway executa migrações forward-only na inicialização da instância única. Uma atualização exige backup anterior, troca da imagem e verificação de saúde. Rollback do código usa a imagem anterior quando o esquema permanece compatível; migração incompatível exige restauração do backup ou migração corretiva explícita.

## Pendência anterior à fundação

Não há decisão bloqueante aberta para F1. A identidade, o namespace e a linguagem ubíqua estão registrados em D-024 a D-026; as demais decisões da fundação estão registradas nos documentos especializados.
