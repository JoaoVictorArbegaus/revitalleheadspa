# Imagem "web": landing (/) e painel administrativo (/admin/) num único nginx.
# Contexto do build: raiz do repositório (ver compose.yml).

# Etapa 1: landing com pré-renderização
FROM node:24-alpine AS cliente
WORKDIR /app
COPY web-cliente/package.json web-cliente/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY web-cliente/index.html web-cliente/tsconfig.json web-cliente/vite.config.ts ./
COPY web-cliente/scripts scripts
COPY web-cliente/src src
COPY web-cliente/public public
RUN npm run build

# Etapa 2: painel administrativo (SPA servida em /admin/)
FROM node:24-alpine AS admin
WORKDIR /app
COPY web-admin/package.json web-admin/package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY web-admin/index.html web-admin/tsconfig.json web-admin/vite.config.ts web-admin/components.json ./
COPY web-admin/src src
RUN npm run build

# Etapa 3: nginx sem root servindo os dois e encaminhando /api para a API
FROM nginxinc/nginx-unprivileged:1.28-alpine
# endereço da API dentro da rede (sobrescrevível no deploy)
ENV API_UPSTREAM=http://api:8080
COPY docker/web/security-headers.conf /etc/nginx/snippets/security-headers.conf
# templates/*.template passam por envsubst na subida (só variáveis definidas, como API_UPSTREAM)
COPY docker/web/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=cliente /app/dist /usr/share/nginx/html
COPY --from=admin /app/dist /usr/share/nginx/html/admin
EXPOSE 8080
