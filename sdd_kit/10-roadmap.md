# Roadmap

O roadmap descreve resultados. A execução detalhada e o estado ficam em `TASKS.md`.

## Etapa 0 — Decisões e fundação

Resultado: base Java/Spring Boot executável, PostgreSQL migrável, contrato básico e rotina de qualidade.

Inclui F1 e início de F2.

## Etapa 1 — Catálogo e agenda pela API

Resultado: responsável autenticado consegue cadastrar serviços e construir a agenda real do negócio.

Inclui F2, B1 e B2, validados pelo contrato e por chamadas de API.

## Etapa 2 — Reserva completa pela API

Resultado: cliente consulta disponibilidade e conclui uma reserva sem login; responsável acompanha a agenda.

Inclui B3 e B4, com exemplos de consumo e testes da jornada completa sem depender de frontend.

## Etapa 3 — Contrato, comunicação e release V1

Resultado: a API possui contrato completo, eventos confiáveis, documentação reproduzível e uma versão estável publicada no GitHub.

Inclui I1, R1 e R2. O adaptador concreto de WhatsApp pode ser entregue posteriormente.

## Etapa 4 — Projeto Revitalle

Resultado: um projeto separado é derivado da API V1 e recebe frontends, identidade visual, integrações e regras aprovadas para a clínica.

Inclui C1, I2 quando priorizado, O1, conteúdo final, homologação e treinamento.

## Etapa 5 — Segunda implantação

Resultado: iniciar um novo projeto diretamente de uma release estável da API, sem herdar personalizações da Revitalle.

Antes de generalizar novas configurações, comparar necessidades reais da segunda implantação com a Revitalle. O que for comum pode evoluir para o núcleo; o que for exclusivo permanece periférico.

## Critério de avanço

Uma etapa pode começar parcialmente antes da anterior terminar quando não houver dependência, mas nenhum marco deve ser anunciado como entregue sem sua jornada principal validada e suas decisões bloqueantes resolvidas.
