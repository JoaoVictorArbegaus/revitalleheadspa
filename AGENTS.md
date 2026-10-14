# Instruções para LLMs — Revitalle

- `api/` segue integralmente `api/AGENTS.md`; leia-o antes de alterar a API.
- `web-cliente/` contém a landing page e a página de agendamento públicas, em
  React com pré-renderização (`src/`). O conteúdo da landing fica em
  `src/content/site.ts`; o design em `design/direcao-visual.md`, a partir de
  `design/briefing.md` e `design/referencias/`.
- Rode `npm run build` em `web-cliente/` antes de concluir uma mudança de frontend:
  ele faz typecheck, build e pré-renderização.
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
