# Catálogo de erros

Toda falha da API usa `Content-Type: application/problem+json` (RFC 9457, D-006).
O consumidor decide o comportamento por `status` e `codigo`; o texto de `detail` e
`title` pode mudar sem aviso. Os códigos desta página são estáveis; qualquer mudança
é feita junto com este catálogo e fica explícita no histórico do Git.

```json
{
  "type": "/problemas/falha-validacao",
  "title": "Falha de validacao",
  "status": 422,
  "detail": "A solicitacao possui campos invalidos",
  "instance": "urn:rastreio:550e8400-e29b-41d4-a716-446655440000",
  "codigo": "FALHA_VALIDACAO",
  "idRastreio": "550e8400-e29b-41d4-a716-446655440000",
  "errosCampos": [
    { "campo": "cliente.nome", "codigo": "NAO_PODE_ESTAR_EM_BRANCO", "mensagem": "nao deve estar em branco" }
  ]
}
```

`idRastreio` também volta no cabeçalho `X-Id-Rastreio` e deve ser informado ao
suporte. O valor rejeitado nunca é devolvido. A ordem de `errosCampos` não faz parte
do contrato.

Ações recorrentes citadas abaixo:

- **corrigir a entrada**: destacar os campos ou o parâmetro e permitir nova tentativa;
- **atualizar a tela**: recarregar os dados exibidos, pois o estado mudou no servidor;
- **autenticar**: obter CSRF, entrar e repetir a operação;
- **não repetir**: tratar como erro de programação do consumidor ou de permissão.

## Gerais

| `codigo` | Status | Significado e condição | Ação do consumidor |
|---|---:|---|---|
| `SOLICITACAO_MALFORMADA` | 400 | JSON ilegível, tipo incompatível ou parâmetro com formato inválido, como UUID ou data | corrigir a entrada |
| `AUTENTICACAO_NECESSARIA` | 401 | sessão ausente ou expirada, credenciais incorretas no login, ou rota desconhecida chamada sem sessão | autenticar; no login, informar que email ou senha estão incorretos |
| `ACESSO_NEGADO` | 403 | o papel da sessão não permite a operação, ou a conta ainda precisa trocar a senha temporária | não repetir; ocultar a ação para o papel ou levar à troca de senha |
| `FALHA_VALIDACAO_CSRF` | 403 | operação mutável sem o cabeçalho `X-CSRF-TOKEN` válido | obter novo token em `/autenticacao/csrf` e repetir |
| `ROTA_NAO_ENCONTRADA` | 404 | caminho inexistente chamado com sessão válida | não repetir |
| `METODO_NAO_PERMITIDO` | 405 | método HTTP não suportado no caminho | não repetir |
| `TIPO_MIDIA_NAO_SUPORTADO` | 415 | corpo enviado sem `Content-Type: application/json` | corrigir o cabeçalho |
| `SOLICITACAO_INVALIDA` | 4xx | outra falha HTTP do framework não listada aqui, como `406` | não repetir |
| `FALHA_VALIDACAO` | 422 | campos, cabeçalhos ou parâmetros violam as regras de entrada; `errosCampos` aparece quando a falha é de campo | corrigir a entrada |
| `CONFLITO_DADOS` | 409 | restrição de unicidade violada sem código específico | atualizar a tela |
| `ERRO_INTERNO` | 500 | falha inesperada; nada é garantido sobre o efeito | repetir com cautela; em criação de reserva, reutilizar a mesma `Chave-Idempotencia` |

### Códigos de `errosCampos`

| `codigo` | Condição |
|---|---|
| `OBRIGATORIO` | campo ausente ou `null` |
| `NAO_PODE_ESTAR_EM_BRANCO` | texto ausente, vazio ou só com espaços |
| `NAO_PODE_ESTAR_VAZIO` | coleção ou texto vazio |
| `TAMANHO_INVALIDO` | texto ou coleção fora do tamanho permitido |
| `FORMATO_EMAIL_INVALIDO` | email fora do formato |
| `FORMATO_INVALIDO` | valor fora do padrão exigido, como `Chave-Idempotencia` |
| `VALOR_ABAIXO_DO_MINIMO` | número menor que o mínimo ou não positivo |
| `VALOR_ACIMA_DO_MAXIMO` | número maior que o máximo |
| `CAMPO_INVALIDO` | outra regra de campo, como `cienteAvisoPrivacidade` diferente de `true` |

## Autenticação e usuários

| `codigo` | Status | Significado e condição | Ação do consumidor |
|---|---:|---|---|
| `LIMITE_TENTATIVAS_LOGIN` | 429 | falhas de login da mesma origem excederam o limite; `Retry-After` informa os segundos | aguardar `Retry-After` |
| `SENHA_FORA_DA_POLITICA` | 422 | senha fora de 12 a 72 bytes UTF-8 | corrigir a entrada |
| `SENHA_ATUAL_INVALIDA` | 422 | senha atual incorreta na troca de senha | corrigir a entrada |
| `SENHA_TEMPORARIA_APENAS_FUNCIONARIO` | 422 | redefinição de senha temporária aplicada a `PROPRIETARIO` | não repetir |
| `DADOS_USUARIO_INVALIDOS` | 422 | nome ou email vazio após normalização | corrigir a entrada |
| `USUARIO_NAO_ENCONTRADO` | 404 | usuário inexistente | atualizar a tela |
| `EMAIL_JA_UTILIZADO` | 409 | email já pertence a outra conta | corrigir a entrada |
| `PROFISSIONAL_OBRIGATORIO` | 422 | `FUNCIONARIO` sem profissional, ou `PROPRIETARIO` omitindo `profissionalId` onde ele é obrigatório | corrigir a entrada |
| `PROFISSIONAL_INVALIDO` | 422 | profissional inexistente ou inativo no vínculo do usuário ou no serviço | corrigir a entrada |
| `PROFISSIONAL_JA_VINCULADO` | 409 | profissional já possui outro usuário | escolher outro profissional |
| `ULTIMO_PROPRIETARIO_ATIVO` | 409 | tentativa de desativar ou rebaixar o último `PROPRIETARIO` ativo | não repetir |

## Catálogo, recursos e agenda

| `codigo` | Status | Significado e condição | Ação do consumidor |
|---|---:|---|---|
| `PROFISSIONAL_NAO_ENCONTRADO` | 404 | profissional inexistente | atualizar a tela |
| `RECURSO_NAO_ENCONTRADO` | 404 | recurso operacional inexistente | atualizar a tela |
| `SERVICO_NAO_ENCONTRADO` | 404 | serviço inexistente, inativo nas rotas públicas ou fora do escopo do `FUNCIONARIO` | atualizar a tela |
| `DURACAO_INCOMPATIVEL_COM_GRADE` | 422 | duração mais limpeza não é múltipla da grade da instalação | corrigir a entrada |
| `RECURSO_REPETIDO_NO_SERVICO` | 422 | mesmo recurso informado duas vezes nos requisitos | corrigir a entrada |
| `RECURSO_INVALIDO` | 422 | requisito aponta para recurso inexistente | corrigir a entrada |
| `QUANTIDADE_SUPERIOR_A_CAPACIDADE` | 422 | quantidade exigida maior que a capacidade do recurso | corrigir a entrada |
| `CAPACIDADE_INFERIOR_AO_REQUISITO` | 409 | nova capacidade menor que a quantidade exigida por algum serviço | ajustar os serviços antes |
| `CAPACIDADE_INFERIOR_AS_RESERVAS` | 409 | nova capacidade menor que o consumo simultâneo de reservas futuras confirmadas | não reduzir agora |
| `PERIODO_CONSULTA_INVALIDO` | 422 | período ausente, invertido ou com mais de 31 datas | corrigir a entrada |
| `EXPEDIENTE_INVALIDO` | 422 | lista de janelas ausente no `PUT` do expediente | corrigir a entrada |
| `INTERVALO_EXPEDIENTE_INVALIDO` | 422 | janela sem dia, com segundos ou com início não anterior ao fim | corrigir a entrada |
| `EXPEDIENTE_SOBREPOSTO` | 422 | janelas do mesmo dia se sobrepõem | corrigir a entrada |
| `INTERVALO_BLOQUEIO_INVALIDO` | 422 | bloqueio com início não anterior ao fim | corrigir a entrada |
| `BLOQUEIO_SOBREPOSTO` | 422 | bloqueio se sobrepõe a outro do mesmo profissional | corrigir a entrada |
| `BLOQUEIO_NAO_ENCONTRADO` | 404 | bloqueio inexistente ou fora do escopo | atualizar a tela |

## Reservas e clientes

| `codigo` | Status | Significado e condição | Ação do consumidor |
|---|---:|---|---|
| `LIMITE_REQUISICOES_PUBLICAS` | 429 | limite compartilhado das rotas públicas por origem; `Retry-After` informa os segundos | aguardar `Retry-After` |
| `AVISO_PRIVACIDADE_DESATUALIZADO` | 409 | `versaoAvisoPrivacidade` difere da vigente | buscar o aviso de novo, reapresentar e pedir nova ciência |
| `CONFLITO_AGENDAMENTO` | 409 | horário ocupado, fora do expediente, bloqueado, sem capacidade ou fora da grade no momento da gravação | atualizar a disponibilidade e escolher outro horário com nova chave |
| `CONFLITO_IDEMPOTENCIA` | 409 | `Chave-Idempotencia` vigente já usada com outro conteúdo ou outro autor | gerar nova chave para a nova intenção |
| `INICIO_NO_PASSADO` | 422 | criação administrativa com início anterior ao instante atual | corrigir a entrada |
| `AGENDAMENTO_NAO_ENCONTRADO` | 404 | reserva inexistente ou de outro profissional para `FUNCIONARIO` | atualizar a tela |
| `TRANSICAO_AGENDAMENTO_INVALIDA` | 409 | reserva não está `CONFIRMADO`, cancelamento a partir do início, ou conclusão e ausência antes do início | atualizar a tela |
| `CLIENTE_NAO_ENCONTRADO` | 404 | cliente inexistente | atualizar a tela |
| `CLIENTE_POSSUI_RESERVA_FUTURA` | 409 | anonimização com reserva `CONFIRMADO` de início futuro | cancelar ou aguardar as reservas antes |
| `CLIENTE_JA_ANONIMIZADO` | 409 | cliente já anonimizado | atualizar a tela |
