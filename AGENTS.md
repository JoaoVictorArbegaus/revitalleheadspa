# Instruções para LLMs — Revitalle

- `api/` segue integralmente `api/AGENTS.md`; leia-o antes de alterar a API.
- `web-cliente/` contém a landing page e a página de agendamento públicas. A
  landing é gerada e revisada pelo agente `gerador-landing` a partir de
  `web-cliente/briefing.md` e `web-cliente/referencias/`.
- `web-admin/` contém o painel administrativo.
- A stack de cada frontend está no `README.md` da raiz; não troque nem acrescente
  biblioteca de base sem registrar a decisão ali.
- Os frontends consomem somente o contrato documentado em
  `api/docs/guia-frontend.md` e no OpenAPI. Mudança de contrato acontece em `api/`
  no mesmo commit do frontend que a usa.
- Não há multitenancy: este repositório atende somente a Revitalle.
- Registre no `README.md` da raiz personalizações locais, migrações próprias e
  correções transportadas do produto-base.
- Não faça push para `upstream` (produto-base).
