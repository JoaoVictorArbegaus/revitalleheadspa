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
npm run dev      # http://localhost:5173 e /agendar/ (a agenda precisa do ambiente Docker de pé)
npm run build    # typecheck, build e pré-renderização em dist/
npm run preview  # serve dist/ em http://localhost:4173
```

A página `/agendar/` é a segunda entrada do mesmo build (`agendar/index.html`,
código em `src/agendar/`), também pré-renderizada. Ela segue o fluxo público do
`api/docs/guia-frontend.md`: tratamento, dia e horário livre, nome e celular,
resumo com ciência do aviso de privacidade e confirmação. Conflito de horário
recarrega a agenda sem perder os dados; falha de rede ou `5xx` repete com a mesma
`Chave-Idempotencia`. Os cartões de serviço da landing abrem a agenda já no
tratamento (`/agendar/?servico=Hair%20Spa`, comparado sem acentos nem caixa).

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

- Landing: `http://localhost:8081`; agendamento: `http://localhost:8081/agendar/`; painel: `http://localhost:8081/admin/`; a API responde
  em `http://localhost:8081/api/...` (a porta da API não é exposta).
- Imagem `web` (`docker/web/Dockerfile`, contexto na raiz): builds da landing
  (pré-renderizada) e do painel, servidos por `nginx-unprivileged` na porta 8080, com
  cache longo em `/assets/`, gzip, cabeçalhos de segurança e `/admin/` como SPA fora
  dos buscadores (`docker/web/`).
- O nginx encaminha `/api/` para `API_UPSTREAM` (padrão `http://api:8080`) e
  **sobrescreve** `X-Forwarded-*` com os dados da conexão; por isso, atrás dele, a API
  pode usar `FORWARD_HEADERS_STRATEGY=NATIVE`.
- O domínio de produção é `https://revitalle.jksoftware.com.br/`; HTTPS e certificados ficam no proxy da plataforma.
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

### Produção

O arquivo `compose.prod.yml` executa PostgreSQL, API e web sem publicar portas no
host. Somente o serviço `web`, pelo alias `revitalle-web:8080`, participa da rede
interna `revitalle_edge` usada pelo proxy reverso da plataforma. Cada release fica
em um diretório imutável apontado por `current`.

```bash
cp .env.prod.example .env.prod
# preencha os valores e proteja o arquivo com chmod 600
docker compose --env-file .env.prod -f compose.prod.yml config --quiet
docker compose --env-file .env.prod -f compose.prod.yml up -d --build
```

O banco usa o volume exclusivo `revitalle_postgres_data`. Nunca remova esse volume
durante uma atualização. Após o primeiro acesso administrativo, esvazie
`BOOTSTRAP_PROPRIETARIO_SENHA` em `.env.prod` e recrie somente o serviço `api`.

## Personalizações locais

- Landing e página de agendamento próprias da Revitalle em `web-cliente/` (React pré-renderizado).
- Componentes adaptados de [kokonutui](https://kokonutui.com) (Card Flip, Carousel Cards, Background Paths, Particle Button, Slide Text Button, Receipt Printer, este como comprovante do agendamento, sem a opção de rasgar) e [React Bits](https://reactbits.dev) (BorderGlow, CardNav), sob licença MIT, reescritos sem Tailwind, GSAP ou dependências novas.

## Migrações próprias

Nenhuma ainda. Migrações do produto-base vão até `V6`.

## Correções transportadas

Nenhuma ainda.
