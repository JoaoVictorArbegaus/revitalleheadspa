# Autenticação administrativa

Este guia descreve a autenticação e a gestão de contas implementadas em F2. A API
usa sessão HTTP e CSRF. Não usa JWT, OAuth ou recuperação por endpoint HTTP.

## Configuração

### Primeiro proprietário

Em um banco sem usuários administrativos, as três variáveis abaixo são
obrigatórias. O bootstrap cria exatamente um `PROPRIETARIO`. Se já existir qualquer
usuário, ele não cria outra conta nem altera credenciais.

| Variável | Finalidade | Obrigatoriedade e formato | Classificação |
|---|---|---|---|
| `BOOTSTRAP_PROPRIETARIO_NOME` | nome do primeiro proprietário | obrigatória somente no banco sem usuários; texto não vazio | configuração com dado pessoal |
| `BOOTSTRAP_PROPRIETARIO_EMAIL` | identificador de login inicial | obrigatória somente no banco sem usuários; email fictício de exemplo: `proprietario@example.test` | configuração com dado pessoal |
| `BOOTSTRAP_PROPRIETARIO_SENHA` | senha inicial | obrigatória somente no banco sem usuários; 12 a 72 bytes UTF-8 | segredo |

As variáveis não possuem padrão. A senha é armazenada somente como BCrypt com
custo 12. Remova ou troque as variáveis de bootstrap depois da primeira
inicialização para não manter a credencial no ambiente.

### Limite de login

| Variável | Finalidade | Padrão | Exemplo | Classificação |
|---|---|---|---|---|
| `LOGIN_TENTATIVAS_MAXIMO` | máximo de falhas por origem dentro da janela | `5` | `5` | configuração comum |
| `LOGIN_TENTATIVAS_JANELA` | duração da janela em memória | `15m` | `15m` | configuração comum |
| `FORWARD_HEADERS_STRATEGY` | tratamento de cabeçalhos do proxy | `NONE` | `NATIVE` somente atrás de proxy confiável | configuração comum |

Uma tentativa posterior ao limite recebe `429` e
`LIMITE_TENTATIVAS_LOGIN`. Um login bem-sucedido limpa as falhas da origem. O
controle fica em memória e é reiniciado com a aplicação.
Atrás de proxy reverso, configure a plataforma para remover cabeçalhos encaminhados
recebidos do cliente e defina `FORWARD_HEADERS_STRATEGY=NATIVE`; sem isso, todas as
tentativas serão atribuídas ao endereço do proxy.

## Sessão e CSRF

O cookie de autenticação é `JSESSIONID`, com `HttpOnly`, `SameSite=Lax` e caminho
`/api`. No perfil `prod`, ele também possui `Secure` e só deve trafegar por HTTPS.
A sessão expira após 30 minutos sem atividade e é perdida ao reiniciar a aplicação.

Antes de qualquer requisição mutável, inclusive `entrar`, faça uma requisição
`GET /api/v1/administracao/autenticacao/csrf`. A resposta contém o token e grava
o cookie `CSRF-TOKEN`:

```json
{
  "token": "token-opaco",
  "nomeCabecalho": "X-CSRF-TOKEN"
}
```

`CSRF-TOKEN` precisa ser legível pelo frontend, não autentica o usuário e é
distinto de `JSESSIONID`. O cliente deve manter os dois cookies e repetir o token
no cabeçalho `X-CSRF-TOKEN` das operações mutáveis. CORS permanece desabilitado;
o frontend da V1 deve usar a mesma origem.

## Fluxo com curl

Os comandos abaixo requerem `curl` e `jq`, usam somente valores fictícios e
mantêm os cookies em um arquivo temporário. Defina credenciais próprias da sua
instalação em vez de gravá-las no histórico do shell.

```bash
export BASE_URL='http://localhost:8080'
export ADMIN_EMAIL='proprietario@example.test'
read -r -s -p 'Senha administrativa: ' ADMIN_SENHA; echo

TOKEN_CSRF="$(curl -fsS -c cookies.txt \
  "$BASE_URL/api/v1/administracao/autenticacao/csrf" | jq -r '.token')"

curl -i -sS -b cookies.txt -c cookies.txt \
  -H 'Content-Type: application/json' \
  -H "X-CSRF-TOKEN: $TOKEN_CSRF" \
  --data "$(jq -n --arg email "$ADMIN_EMAIL" --arg senha "$ADMIN_SENHA" \
    '{email: $email, senha: $senha}')" \
  "$BASE_URL/api/v1/administracao/autenticacao/entrar"

curl -fsS -b cookies.txt \
  "$BASE_URL/api/v1/administracao/autenticacao/eu" | jq
```

Para trocar a própria senha, informe os valores de forma interativa. O retorno é
`204`; todas as sessões da conta são invalidadas e um novo login é obrigatório.

```bash
read -r -s -p 'Senha atual: ' SENHA_ATUAL; echo
read -r -s -p 'Nova senha: ' NOVA_SENHA; echo

curl -i -sS -X PUT -b cookies.txt -c cookies.txt \
  -H 'Content-Type: application/json' \
  -H "X-CSRF-TOKEN: $TOKEN_CSRF" \
  --data "$(jq -n --arg atual "$SENHA_ATUAL" --arg nova "$NOVA_SENHA" \
    '{senhaAtual: $atual, novaSenha: $nova}')" \
  "$BASE_URL/api/v1/administracao/autenticacao/senha"

rm -f cookies.txt
unset ADMIN_SENHA SENHA_ATUAL NOVA_SENHA TOKEN_CSRF
```

Para sair sem trocar a senha:

```bash
curl -i -sS -X POST -b cookies.txt \
  -H "X-CSRF-TOKEN: $TOKEN_CSRF" \
  "$BASE_URL/api/v1/administracao/autenticacao/sair"
rm -f cookies.txt
```

## Rotas de autenticação

| Método e rota | Acesso | Corpo | Sucesso |
|---|---|---|---|
| `GET /api/v1/administracao/autenticacao/csrf` | público | nenhum | `200`, `token` e `nomeCabecalho` |
| `POST /api/v1/administracao/autenticacao/entrar` | público, com CSRF | `email`, `senha` | `204` e cookie de sessão |
| `GET /api/v1/administracao/autenticacao/eu` | autenticado | nenhum | `200`, usuário atual |
| `PUT /api/v1/administracao/autenticacao/senha` | autenticado, com CSRF | `senhaAtual`, `novaSenha` | `204`, sessões invalidadas |
| `POST /api/v1/administracao/autenticacao/sair` | autenticado, com CSRF | nenhum | `204`, sessão invalidada |

A senha aceita entre 12 e 72 bytes em UTF-8, não caracteres. Não há exigência de
maiúscula, número ou símbolo, e valores acima do limite não são truncados.

## Gestão de usuários

Todas estas rotas exigem `PROPRIETARIO`; operações mutáveis também exigem CSRF.
`FUNCIONARIO` não pode administrar contas.

| Método e rota | Corpo | Sucesso |
|---|---|---|
| `GET /api/v1/administracao/usuarios` | nenhum | `200`, array de usuários |
| `GET /api/v1/administracao/usuarios/{id}` | nenhum | `200`, usuário |
| `POST /api/v1/administracao/usuarios` | criação de funcionário | `201`, usuário e `Location` |
| `PUT /api/v1/administracao/usuarios/{id}` | alteração completa | `200`, usuário |
| `PATCH /api/v1/administracao/usuarios/{id}` | mesmo corpo completo de `PUT` | `200`, usuário |
| `POST /api/v1/administracao/usuarios/{id}/senha-temporaria` | redefinição | `204` |
| `POST /api/v1/administracao/usuarios/{id}/ativar` | nenhum | `204` |
| `POST /api/v1/administracao/usuarios/{id}/desativar` | nenhum | `204` |

Formato retornado:

```json
{
  "id": "64fc5548-c3e2-4b83-9d47-b57be53c0289",
  "nome": "Pessoa Exemplo",
  "email": "pessoa@example.test",
  "papel": "FUNCIONARIO",
  "profissionalId": "968a3652-4f11-40b6-b90e-f49831f36ba1",
  "ativo": true,
  "deveAlterarSenha": true
}
```

Criação de funcionário:

```json
{
  "nome": "Pessoa Exemplo",
  "email": "pessoa@example.test",
  "senhaTemporaria": "valor-ficticio-temporario",
  "profissionalId": "968a3652-4f11-40b6-b90e-f49831f36ba1"
}
```

Alteração por `PUT` ou `PATCH` (ambos exigem todos os campos mostrados):

```json
{
  "nome": "Pessoa Exemplo",
  "email": "pessoa@example.test",
  "papel": "FUNCIONARIO",
  "profissionalId": "968a3652-4f11-40b6-b90e-f49831f36ba1"
}
```

Redefinição de senha:

```json
{
  "senhaTemporaria": "outro-valor-ficticio"
}
```

`nome` aceita até 120 caracteres e `email`, até 254. O email é normalizado para
minúsculas e sem espaços nas extremidades. `profissionalId` é obrigatório e único
para `FUNCIONARIO`; em `PROPRIETARIO`, pode ser `null`. O profissional precisa
existir e estar ativo. A API não permite desativar nem rebaixar o último
proprietário ativo.

Uma senha temporária só pode ser definida para funcionário e deixa
`deveAlterarSenha=true`. Até trocá-la, a sessão acessa apenas `csrf`, `eu`, `senha`
e `sair`. Alteração cadastral, redefinição de senha e desativação invalidam as
sessões da conta afetada.

## Recuperação do proprietário

Não há endpoint de recuperação. O procedimento usa o mesmo JAR da aplicação em
modo sem servidor web e redefine apenas uma conta `PROPRIETARIO` existente e
ativa.

1. Pare a instância normal da aplicação e mantenha o PostgreSQL acessível.
2. Disponibilize temporariamente as variáveis de banco e de recuperação.
3. Execute o mesmo JAR sem passar email ou senha como argumentos.
4. Verifique o código de saída.
5. Remova as variáveis de recuperação e reinicie a aplicação normalmente.

Exemplo para Linux ou macOS, após gerar o JAR da versão instalada:

```bash
export DATABASE_URL='jdbc:postgresql://localhost:5432/service_scheduling'
export DATABASE_USERNAME='service_scheduling'
read -r -s -p 'Senha do banco: ' DATABASE_PASSWORD; echo
export DATABASE_PASSWORD
export MODO_APLICACAO='recuperacao'
export RECUPERACAO_PROPRIETARIO_EMAIL='proprietario@example.test'
read -r -s -p 'Nova senha administrativa: ' RECUPERACAO_PROPRIETARIO_NOVA_SENHA; echo
export RECUPERACAO_PROPRIETARIO_NOVA_SENHA

java -jar target/service-scheduling-api-0.1.0-SNAPSHOT.jar
CODIGO=$?

unset MODO_APLICACAO RECUPERACAO_PROPRIETARIO_EMAIL \
  RECUPERACAO_PROPRIETARIO_NOVA_SENHA DATABASE_PASSWORD
exit "$CODIGO"
```

Exemplo equivalente em PowerShell:

```powershell
$env:DATABASE_URL = 'jdbc:postgresql://localhost:5432/service_scheduling'
$env:DATABASE_USERNAME = 'service_scheduling'
$env:DATABASE_PASSWORD = Read-Host 'Senha do banco'
$env:MODO_APLICACAO = 'recuperacao'
$env:RECUPERACAO_PROPRIETARIO_EMAIL = 'proprietario@example.test'
$env:RECUPERACAO_PROPRIETARIO_NOVA_SENHA = Read-Host 'Nova senha administrativa'

java -jar target/service-scheduling-api-0.1.0-SNAPSHOT.jar
$codigo = $LASTEXITCODE

Remove-Item Env:MODO_APLICACAO, Env:RECUPERACAO_PROPRIETARIO_EMAIL,
  Env:RECUPERACAO_PROPRIETARIO_NOVA_SENHA, Env:DATABASE_PASSWORD
exit $codigo
```

Use o nome real do JAR instalado quando a versão for diferente. Nunca forneça a
nova senha como argumento de `java`, opção `--...`, log ou URL.

| Código de saída | Resultado |
|---:|---|
| `0` | senha redefinida com sucesso |
| `2` | email não corresponde a um proprietário existente e ativo |
| `3` | configuração ausente ou nova senha fora da política |

## Erros

Falhas HTTP usam `Content-Type: application/problem+json` conforme RFC 9457. O
cliente deve tratar `status` e `codigo`; textos humanos podem evoluir.

```json
{
  "type": "/problemas/falha-validacao",
  "title": "Falha de validacao",
  "status": 422,
  "detail": "A solicitacao possui campos invalidos",
  "instance": "urn:rastreio:6504f74f-7473-4f99-a8cf-44a32bcf076f",
  "codigo": "FALHA_VALIDACAO",
  "idRastreio": "6504f74f-7473-4f99-a8cf-44a32bcf076f",
  "errosCampos": [
    {
      "campo": "email",
      "codigo": "FORMATO_EMAIL_INVALIDO",
      "mensagem": "deve ser um endereco de e-mail bem formado"
    }
  ]
}
```

Erros principais do fluxo:

| Status | `codigo` | Tratamento esperado |
|---:|---|---|
| `401` | `AUTENTICACAO_NECESSARIA` | autenticar novamente ou conferir a credencial informada |
| `403` | `FALHA_VALIDACAO_CSRF` | obter novo CSRF e repetir com cookie e cabeçalho correspondentes |
| `403` | `ACESSO_NEGADO` | não repetir sem corrigir papel ou troca obrigatória de senha |
| `404` | `USUARIO_NAO_ENCONTRADO` | conferir o ID sem presumir a existência da conta |
| `409` | `EMAIL_JA_UTILIZADO` | escolher outro email |
| `409` | `PROFISSIONAL_JA_VINCULADO` | usar um profissional sem outra conta vinculada |
| `409` | `ULTIMO_PROPRIETARIO_ATIVO` | manter ou criar outro proprietário ativo antes da alteração |
| `422` | `FALHA_VALIDACAO` | corrigir os itens de `errosCampos` |
| `422` | `SENHA_FORA_DA_POLITICA` | usar de 12 a 72 bytes UTF-8 |
| `422` | `SENHA_ATUAL_INVALIDA` | conferir a senha atual |
| `422` | `PROFISSIONAL_OBRIGATORIO` ou `PROFISSIONAL_INVALIDO` | informar um profissional existente e ativo |
| `422` | `SENHA_TEMPORARIA_APENAS_FUNCIONARIO` | não aplicar senha temporária a proprietário |
| `429` | `LIMITE_TENTATIVAS_LOGIN` | aguardar o fim da janela antes de tentar novamente |

Toda resposta recebe `X-Id-Rastreio`. O cliente pode enviar previamente nesse
cabeçalho um UUID válido para correlação. Respostas nunca devolvem senha, hash ou
valor rejeitado.
