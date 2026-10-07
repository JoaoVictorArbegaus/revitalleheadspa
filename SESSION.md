# Contexto atual

## Objetivo em andamento

Construir uma API genérica de agendamento até uma V1 funcional e publicável no GitHub.

## Estado

- Repositório iniciado somente com documentação de produto e engenharia.
- Produto-base nomeado `service-scheduling-api`; Revitalle permanece como primeiro projeto derivado.
- Repositório Git local iniciado na branch `main` e vinculado ao remoto oficial `https://github.com/JoaoVictorArbegaus/service-scheduling-api.git`.
- Commits e pushes para `main` acompanham tarefas coesas e validadas; release e tag `v1.0.0` permanecem reservadas para R2.
- O núcleo foi definido como agenda de serviços para uma única empresa por instalação.
- Este repositório é o produto-base da API e não deve receber personalização da Revitalle antes da V1.
- A Revitalle será o primeiro projeto de cliente derivado de uma versão estável da API.
- Clientes futuros também terão projetos próprios derivados da versão estável mais adequada.
- Fundação definida: Java 21, Spring Boot 4.1.1, Maven, PostgreSQL, Flyway, OpenAPI, Testcontainers e Docker.
- Projetos de clientes serão cópias independentes de uma release; não haverá atualização automática pelo produto-base.
- Perfil de escala: instalação pequena ou média, com referência de quatro profissionais e cerca de quarenta serviços ativos, sem alto fluxo.
- Autenticação definida: email/senha, Spring Security, sessão HTTP em memória, CSRF, BCrypt 12, bootstrap inicial e negação padrão.
- Recuperação do `PROPRIETARIO` definida como comando operacional de uso único, sem rota pública.
- Erros definidos por RFC 9457 com `ProblemDetail`, códigos estáveis e separação entre `400`, `422` e `409`.
- Confirmação manual cria `PENDENTE`, bloqueia o horário e expira entre 15 minutos e 24 horas, com padrão de 12 horas; expiração usa `EXPIRADO`.
- Cliente é identificado pelo celular brasileiro normalizado e único; nome e celular são obrigatórios, email e CPF seguem a configuração.
- Recursos predefinidos possuem quantidade padrão e mínima; mínimo zero permite remoção e mínimo positivo é obrigatório.
- Limites padrão: reserva com 1 hora de antecedência, horizonte de 90 dias, alteração pública até 24 horas antes e consulta de até 31 dias; exceções podem abrir ou bloquear horários.
- Autoatendimento usa referência e token de 256 bits enviado em cabeçalho; somente o hash é persistido e celular isolado não autoriza acesso.
- Escritas públicas usam `Chave-Idempotencia` por 24 horas; retentativas repetem o resultado sem duplicar efeitos ou eventos.
- Privacidade exige configuração por instalação, CPF cifrado, aviso versionado, marketing separado e operações de exportação, correção e anonimização pelo `PROPRIETARIO`.
- A outbox registra cinco eventos operacionais, tenta entrega até oito vezes e usa adaptador desabilitado no produto-base; provedor e textos pertencem ao projeto do cliente.
- Entrega segue o padrão do KiProdutos: imagem Docker, Compose local, PostgreSQL/Flyway e configuração externa; hospedagem concreta pertence a cada projeto de cliente.
- Papéis administrativos reduzidos a `PROPRIETARIO` e `FUNCIONARIO`; não haverá `GERENTE` na V1.
- `FUNCIONARIO` é vinculado a um profissional e opera somente seus serviços, agenda, reservas e dados de clientes necessários aos próprios atendimentos.
- Linguagem ubíqua definida em português para domínio, código, JSON, rotas e banco; o pacote raiz é `com.servicescheduling.api`.

## Próximo passo

Executar F1 de `TASKS.md` conforme `sdd_kit/18-technical-foundation.md`.

## Restrições ativas

- Não implementar multitenancy.
- Não implementar frontend ou identidade da Revitalle durante a construção da API V1.
- Cliente final sem login na API V1.
- Nome e celular serão persistidos; email e documentos seguem a política configurada pela instalação.
- WhatsApp será tratado como integração posterior e não deve bloquear a reserva.
