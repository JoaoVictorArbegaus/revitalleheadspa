# Releases e projetos de clientes

## Objetivo

Manter `service-scheduling-api` como produto-base genérico no GitHub e criar um projeto independente para cada cliente. Este modelo permite personalizações profundas sem misturar dados, código ou operação entre empresas.

## Ciclo do produto-base

1. Implementar uma tarefa genérica e coesa no repositório da API.
2. Atualizar contrato, migrações, testes e documentação afetados.
3. Executar as validações aplicáveis e registrar o estado em `TASKS.md` e `SESSION.md`.
4. Criar um commit coerente e enviá-lo para `main`.
5. Ao concluir os critérios de R2, publicar uma release seguindo versionamento semântico.
6. Manter notas de versão com mudanças, migrações e incompatibilidades para orientar cópias futuras.

A branch `main` representa desenvolvimento contínuo e pode conter uma V1 incompleta. A primeira versão apta a originar clientes será `v1.0.0`; a GitHub Release e a tag só podem ser criadas quando R2 estiver concluída.

## Criação de um projeto de cliente

1. Selecionar uma release estável da API, nunca uma branch de desenvolvimento.
2. Criar um novo repositório para o cliente a partir dessa versão.
3. Registrar a release de origem no README do projeto.
4. Configurar banco, segredos, domínio e ambientes exclusivos.
5. Escolher hospedagem, proxy reverso, certificados, backup e monitoramento do cliente.
6. Hospedar o frontend estático no mesmo domínio e encaminhar `/api` ao container da API.
7. Criar os frontends e adaptações do cliente no novo projeto.
8. Validar que nenhuma credencial, dado ou identidade de outro cliente foi herdada.

Revitalle será o primeiro projeto criado por esse processo depois da V1.

## Regra de proveniência

Um cliente novo sempre deriva do produto-base. Ele nunca deriva do repositório da Revitalle ou de outro cliente, mesmo que o segmento seja parecido.

## Destino inicial das mudanças

| Tipo de mudança | Onde implementar |
|---|---|
| correção de conflito de agenda | produto-base |
| melhoria genérica do contrato | produto-base |
| novo mecanismo reutilizável de integração | produto-base |
| identidade visual | projeto do cliente |
| texto, campanha ou conteúdo próprio | projeto do cliente |
| regra aceita apenas por um cliente | módulo no projeto do cliente |

Depois de criado, o projeto do cliente é independente. Quando uma necessidade nasce nele, a equipe decide se vale reaplicá-la no produto-base. Essa reaplicação é manual e não cria dependência entre os projetos.

## Manutenção das cópias

Cada projeto registra:

- release de origem;
- correções posteriormente copiadas de outros projetos;
- personalizações locais;
- migrações adicionais próprias;
- validações executadas após cada transplante de código.

O produto-base fornece imagem Docker, Compose local e configuração de exemplo sem segredos. O Compose não define produção. Cada cliente executa uma instância da API com PostgreSQL próprio; segredos vêm de variáveis protegidas ou do gerenciador da plataforma.

Não haverá atualização automática, pacote compartilhado obrigatório ou sincronização contínua com o produto-base. Quando uma correção precisar ser transportada, ela deve ocorrer por commit identificado ou implementação equivalente, com revisão de conflitos e testes completos no destino.

## Versionomento

- `MAJOR`: mudança incompatível no contrato ou comportamento suportado.
- `MINOR`: funcionalidade compatível adicionada.
- `PATCH`: correção compatível.

Antes da V1, não existe garantia de compatibilidade. Depois da V1, toda incompatibilidade exige nova versão principal e guia de migração.

## Limites

Este processo não cria multitenancy, banco compartilhado ou atualização automática de todos os clientes. Releases novas servem principalmente como origem para clientes futuros; projetos existentes seguem sua própria linha de evolução.
