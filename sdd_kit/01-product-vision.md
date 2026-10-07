# Visão do produto

## Problema

Negócios que vendem serviços precisam publicar seus horários, reduzir o trabalho manual de marcação e manter uma agenda confiável. Muitas soluções tratam todos os negócios como iguais ou exigem uma plataforma multitenant, o que dificulta personalizações profundas por cliente.

## Proposta

Construir primeiro uma API reutilizável e funcional de agendamento de serviços. O produto-base se chama `service-scheduling-api` e será versionado no GitHub, independente de frontend, marca ou cliente específico.

Depois da V1, cada cliente receberá um projeto próprio derivado de uma versão estável da API. Esse projeto poderá receber frontend, marca, integrações e regras específicas. A Revitalle, uma clínica de estética, será a primeira implantação e começará somente depois da V1 da API.

## Públicos

### Responsável pelo negócio

Precisa configurar a oferta, controlar a agenda e acompanhar reservas com segurança.

### Cliente final

Precisa encontrar um serviço e concluir a reserva com pouco atrito, recebendo uma confirmação confiável.

### Equipe de desenvolvimento

Precisa baixar uma versão estável da API, iniciar um projeto independente e personalizá-lo sem carregar identidade visual ou regras exclusivas de outro cliente.

## Modelo de implantação

- uma instalação por cliente contratante;
- banco de dados isolado por instalação;
- configuração e segredos isolados;
- frontend substituível ou personalizável;
- evolução do núcleo por versões controladas;
- extensões específicas implementadas por adaptadores ou módulos claros.
- projeto de código independente derivado de uma release conhecida da API.

Não haverá `tenant_id`, seleção de organização ou dados de clientes diferentes na mesma instância.

Projetos derivados não acompanham automaticamente a API-base. Depois da cópia, cada projeto pode evoluir de forma independente e incorporar correções de outras linhas apenas por decisão explícita.

## Objetivos da API V1

- administrar serviços e agenda;
- publicar horários realmente disponíveis;
- reservar sem conta de cliente final;
- preservar dados e histórico necessários à operação;
- impedir sobreposição de reservas;
- preparar notificações sem acoplar o domínio ao WhatsApp.
- oferecer contrato OpenAPI completo e exemplos para consumidores;
- ser instalável e testável sem um frontend;
- publicar uma release estável que possa originar projetos de clientes.

## Fora da construção da API V1

- frontend administrativo;
- frontend público;
- identidade visual e conteúdo da Revitalle;
- regras exclusivas de uma clínica ou cliente;
- adaptação de um projeto já derivado para outro cliente.

## Indicadores da API V1

- conflitos de reserva confirmados: zero;
- instalação reproduzível a partir da documentação;
- contrato OpenAPI completo e validado;
- jornada administrativa e jornada pública cobertas por testes de API;
- concorrência, idempotência e migrações validadas com o banco real;
- nenhuma dependência de frontend, marca ou regra da Revitalle.

Métricas de conversão, usabilidade e operação serão definidas nos projetos de clientes, onde existirão interfaces e uso real.
