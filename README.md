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
| `web-admin/` | Painel administrativo em `/admin/` (proprietária e funcionários). |
| `.claude/` | Agentes e skills de frontend usados no desenvolvimento. |

## Stack dos frontends

| Superfície | Stack | Motivo |
|---|---|---|
| Landing (`web-cliente/`) | Vite, React, TypeScript, CSS próprio, Motion e Lenis; pré-renderizada no build | Componentes React com HTML estático na entrega (SEO e carregamento rápido no celular); Motion para animações de entrada, rolagem e 3D, Lenis para rolagem suave. |
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

### Rodar o web-admin

```bash
cd web-admin
npm install
npm run dev        # http://localhost:5174/admin/ (precisa do ambiente Docker de pé)
npm run build      # typecheck e build em dist/
npm run api:tipos  # regenera src/api/schema.d.ts a partir de src/api/openapi.json
```

Em desenvolvimento, o Vite encaminha `/api` e `/img` para `http://localhost:8081`
(nginx do ambiente Docker), mantendo a mesma origem exigida pela sessão e pelo CSRF.
Para atualizar o contrato depois de mudar a API:
`docker compose exec -T api wget -qO- http://localhost:8080/api-docs > web-admin/src/api/openapi.json`
e então `npm run api:tipos`.

O painel cobre: login e troca de senha obrigatória, visão geral (faturamento
estimado realizado e previsto, estados das reservas, comparecimento, ocupação,
reservas pelo site, serviços e horários mais procurados), agenda do dia e da semana
(concluir, ausência e cancelamento com motivo), criação de agendamento pelo painel,
serviços com preço, duração, limpeza e recursos, expediente semanal, bloqueios,
recursos e profissionais. Os gráficos são SVG próprio com a paleta validada pela
skill `dataviz` (tokens `--viz-*` e `--seq-*` em `src/index.css`); o cartão de
faturamento e os anéis são adaptados de kokonutui (MIT). Ficam para a próxima entrega: contas de funcionários e
consulta e anonimização de clientes.

### Ambiente completo em Docker

O `compose.yml` da raiz sobe PostgreSQL, API e o `web-cliente` servido por nginx, na
mesma origem, como em produção:

```bash
cp .env.example .env   # preencha nome, email e senha (12 a 72 bytes) do proprietário inicial
docker compose up -d --build
```

- Landing: `http://localhost:8081`; painel: `http://localhost:8081/admin/`; a API responde
  em `http://localhost:8081/api/...` (a porta da API não é exposta).
- Imagem `web` (`docker/web/Dockerfile`, contexto na raiz): builds da landing
  (pré-renderizada) e do painel, servidos por `nginx-unprivileged` na porta 8080, com
  cache longo em `/assets/`, gzip, cabeçalhos de segurança e `/admin/` como SPA fora
  dos buscadores (`docker/web/`).
- O nginx encaminha `/api/` para `API_UPSTREAM` (padrão `http://api:8080`) e
  **sobrescreve** `X-Forwarded-*` com os dados da conexão; por isso, atrás dele, a API
  pode usar `FORWARD_HEADERS_STRATEGY=NATIVE`.
- HTTPS, domínio e certificados ficam na plataforma de hospedagem (pendente na C1).
  O perfil `prod` da API exige cookies seguros, logo HTTPS na frente. Atrás de um
  proxy com TLS, o nginx precisa repassar o `X-Forwarded-Proto` recebido da plataforma
  em vez de `$scheme`.
- `api/compose.yml` continua disponível para trabalhar só na API.

Material de design (briefing, direção visual, referências e originais da
cliente) fica em `web-cliente/design/`; as imagens otimizadas servidas pela página
ficam em `web-cliente/public/img/`.

Os frontends são servidos no mesmo domínio da API, com `/api` encaminhado ao
container da API: a sessão administrativa usa cookie e CSRF, e o CORS está
desabilitado (`api/docs/guia-frontend.md`).

## Personalizações locais

- Landing própria da Revitalle em `web-cliente/` (React pré-renderizado).
- Componentes adaptados de [kokonutui](https://kokonutui.com) (Card Flip, Carousel Cards, Background Paths, Particle Button, Slide Text Button) e [React Bits](https://reactbits.dev) (BorderGlow, CardNav), sob licença MIT, reescritos sem Tailwind, GSAP ou dependências novas.

## Migrações próprias

Nenhuma ainda. Migrações do produto-base vão até `V6`.

## Correções transportadas

Nenhuma ainda.
