# Fronteiras da API V1

## Resultado esperado

Entregar uma API de agendamento funcional, instalável e reutilizável como código-base para negócios de serviços. A V1 atende uma única empresa por instalação e não depende de frontend, marca ou provedor de comunicação concreto.

## Capacidades incluídas

- autenticação e autorização administrativas simples;
- profissionais com serviços próprios, preço, duração e agenda;
- salas e equipamentos opcionais com capacidade;
- expediente recorrente, exceções e bloqueios;
- disponibilidade por grade configurável;
- reserva pública sem conta;
- confirmação automática ou manual;
- gestão administrativa do ciclo da reserva;
- cancelamento e reagendamento públicos com verificação segura;
- concorrência e idempotência;
- eventos de outbox processados pelo próprio aplicativo;
- OpenAPI, migrações, testes e imagem Docker.

## Fora da V1

- multitenancy;
- frontend;
- aplicativo móvel;
- pagamentos;
- prontuário e dados clínicos;
- marketplace;
- múltiplas unidades físicas;
- serviços coletivos ou turmas;
- pacotes, assinaturas, fidelidade e cupons;
- lista de espera;
- sincronização com calendários externos;
- relatórios analíticos avançados;
- motor genérico de regras ou plugins;
- atualização automática dos projetos de clientes;
- provedor concreto de WhatsApp, salvo decisão posterior de escopo;
- JWT, OAuth, login social e segundo fator;

## Limites técnicos

- monólito modular em um único aplicativo Spring Boot;
- um PostgreSQL por instalação;
- processador de outbox no mesmo aplicativo;
- comunicação síncrona entre módulos por chamadas internas explícitas;
- sem broker, cache distribuído ou busca externa;
- sem infraestrutura de orquestração além do necessário para executar o container e o banco;
- otimização orientada pelo perfil de escala D-020 e por evidência medida.

## Perfil de escala

- instalação pequena ou média;
- referência de até quatro profissionais e aproximadamente quarenta serviços ativos;
- poucos usuários administrativos;
- baixo volume e baixa concorrência de acesso;
- uma instância da aplicação e um banco PostgreSQL;
- números usados para dimensionamento, não como validações rígidas do domínio.

Não fazem parte da V1 testes de carga massiva, alta disponibilidade multi-instância, particionamento de banco, filas externas, réplicas de leitura ou cache distribuído.

## Regra de simplicidade

Uma solução pertence à V1 quando é necessária para um fluxo incluído, protege uma invariante atual ou permite operar e validar a API. Possibilidades futuras ficam em decisão ou backlog, sem código preparatório.

Entre duas soluções corretas, escolher a que possui menos componentes, menos estados e menor custo operacional, desde que preserve segurança, integridade, privacidade e clareza do domínio.

## Comportamento obrigatório da LLM

1. Trabalhar em uma tarefa coesa de `TASKS.md` por vez.
2. Ler somente o contexto indicado por `AGENTS.md` e pelas referências da tarefa.
3. Identificar decisões abertas antes de codificar a parte dependente.
4. Implementar comportamento completo, incluindo validação, migração, teste e contrato aplicáveis.
5. Não criar funcionalidades, extensões ou abstrações para cenários ausentes da V1.
6. Não modificar módulos sem relação com a tarefa salvo dependência demonstrável.
7. Registrar uma expansão aprovada antes de implementá-la.
8. Separar claramente validação executada, validação pendente e limitação conhecida.
9. Manter `SESSION.md` curto e substituir seu estado quando o foco mudar.
10. Não declarar tarefa, etapa ou V1 concluída sem cumprir seus critérios de aceite.

## Critério para abstrações

Uma abstração é justificada quando representa uma regra do domínio, protege uma fronteira externa real, elimina duplicação existente relevante ou permite testar uma dependência inevitável. Uma implementação futura imaginada não é justificativa suficiente.

## Alteração destas fronteiras

Qualquer item novo exige decisão registrada, impacto no roadmap e tarefa com critérios de aceite. A LLM não interpreta uma ideia futura como autorização para expandir a V1.
