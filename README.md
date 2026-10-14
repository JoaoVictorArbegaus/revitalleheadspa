# Revitalle Head Spa

Projeto do cliente Revitalle, derivado do produto-base
[service-scheduling-api](https://github.com/JoaoVictorArbegaus/service-scheduling-api)
por `git clone` (D-034, `api/sdd_kit/13-release-and-client-derivation.md`).

## Origem

- Produto-base: `JoaoVictorArbegaus/service-scheduling-api`, branch `main`.
- Commit de origem: `0d8b3c31a82488f7b1842d57d2bc0bd52615d403`.
- O remote `upstream` aponta para o produto-base somente para consulta (push
  desabilitado). Correções são transportadas manualmente, por exemplo
  `git cherry-pick -Xsubtree=api <commit>`, com testes completos no destino.

## Estrutura

| Pasta | Conteúdo |
|---|---|
| `api/` | API de agendamento (Java 21, Spring Boot, PostgreSQL). Ver `api/README.md`. |
| `web-cliente/` | Frontend público: landing page e página de agendamento (`agendar/`). |
| `web-admin/` | Frontend administrativo (proprietário e funcionários). |
| `.claude/` | Agentes e skills de frontend usados no desenvolvimento. |

## Stack dos frontends

| Superfície | Stack | Motivo |
|---|---|---|
| Landing (`web-cliente/`) | Vite, React, TypeScript, CSS próprio; pré-renderizada no build | Componentes React com HTML estático na entrega (SEO e carregamento rápido no celular). |
| Agendamento (`web-cliente/agendar/`) | Vite, React, TypeScript (mesmo projeto da landing) | Fluxo com estado (serviço, dia, horário, aviso, dados); reaproveita os componentes de vidro da landing. |
| Admin (`web-admin/`) | Vite, React, TypeScript, Tailwind CSS, shadcn/ui | Muitas telas de CRUD, formulários e interface por papel; componentes acessíveis prontos. |

O build é estático; em desenvolvimento, o proxy do Vite encaminha `/api` para a
API local (`localhost:8080`).

### Rodar o web-cliente

```bash
cd web-cliente
npm install
npm run dev      # desenvolvimento em http://localhost:5173
npm run build    # typecheck, build e pré-renderização em dist/
npm run preview  # serve dist/ em http://localhost:4173
```

Material de design (briefing, direção visual, referências e originais da
cliente) fica em `web-cliente/design/`; as imagens otimizadas servidas pela página
ficam em `web-cliente/public/img/`.

Os frontends são servidos no mesmo domínio da API, com `/api` encaminhado ao
container da API: a sessão administrativa usa cookie e CSRF, e o CORS está
desabilitado (`api/docs/guia-frontend.md`).

## Personalizações locais

Nenhuma ainda.

## Migrações próprias

Nenhuma ainda. Migrações do produto-base vão até `V6`.

## Correções transportadas

Nenhuma ainda.
