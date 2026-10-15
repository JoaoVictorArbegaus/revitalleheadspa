# Etapa 1: build com pré-renderização (gera HTML estático em dist/)
FROM node:24-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund
COPY index.html tsconfig.json vite.config.ts ./
COPY scripts scripts
COPY src src
COPY public public
RUN npm run build

# Etapa 2: nginx sem root servindo só o estático e encaminhando /api para a API
FROM nginxinc/nginx-unprivileged:1.28-alpine
# endereço da API dentro da rede (sobrescrevível no deploy)
ENV API_UPSTREAM=http://api:8080
COPY docker/security-headers.conf /etc/nginx/snippets/security-headers.conf
# templates/*.template passam por envsubst na subida (só variáveis definidas, como API_UPSTREAM)
COPY docker/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 8080
