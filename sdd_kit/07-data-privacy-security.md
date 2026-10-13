# Dados, privacidade e segurança

Este projeto persiste dados pessoais e aplica minimização desde a modelagem. A revisão jurídica final do aviso de privacidade e da operação é responsabilidade do negócio com apoio especializado.

## Classificação inicial

| Dado | Finalidade inicial | Exposição permitida |
|---|---|---|
| nome do cliente | identificar a reserva e prestar o atendimento | administração autorizada e resumo necessário |
| celular do cliente | contato operacional e identificação do cadastro | administração autorizada; mascarado onde possível |
| dados da reserva | prestar e administrar o serviço | cliente na confirmação inicial e administração autorizada |
| dados da conta administrativa | autenticar e autorizar a equipe | titular da conta e administração autorizada |
| eventos técnicos | segurança e diagnóstico | equipe autorizada, sem dados pessoais desnecessários |

O cliente final informa somente nome e celular. CPF, email de cliente, documento, consentimento de marketing e campos adicionais não pertencem à V1. O email permanece como identificador das contas administrativas.

## Princípios obrigatórios

- coletar somente dados necessários à finalidade definida;
- apresentar o aviso de privacidade antes da confirmação e registrar sua versão;
- limitar o acesso administrativo ao necessário para cada papel;
- mascarar dados em listagens quando o valor completo não for necessário;
- não incluir nome, celular completo, email administrativo, senha, cookie, token, chave de idempotência ou segredo em logs, métricas, URLs ou mensagens de erro;
- não registrar corpos de requisição por padrão;
- criptografar trânsito e armazenamento conforme a infraestrutura escolhida;
- manter backups com proteção equivalente ao banco principal.

## Celular

- receber sem campo de país, aceitar formatação comum e `+55` opcional e normalizar
  números brasileiros para `+55` seguido de 10 ou 11 dígitos;
- manter unicidade por instalação e usar o valor normalizado para localizar o cliente;
- mascarar por padrão quando o valor completo não for necessário;
- nunca usar o celular isoladamente para autenticar o cliente ou autorizar acesso a uma reserva.

## Rotas públicas

- aplicar limite de requisições por origem e sinal de abuso;
- não oferecer busca de cliente ou reserva por celular;
- não confirmar publicamente a existência de cadastro anterior;
- validar tamanho e formato de toda entrada;
- devolver somente os dados necessários à confirmação da criação.

Na V1, todas as rotas sob `/api/v1/publico/**` compartilham um limite em memória por
origem. O padrão é 120 requisições por minuto, configurável por
`PUBLICO_REQUISICOES_MAXIMO` e `PUBLICO_REQUISICOES_JANELA`. Excesso responde `429`
com `Retry-After`. O limite é compatível com o perfil de uma instância da V1 e não
introduz armazenamento distribuído.

A V1 não emite referência ou token de acesso e não oferece consulta, cancelamento ou reagendamento público depois da criação. A operação posterior pertence à administração autenticada.

## Autenticação administrativa

- Spring Security com sessão HTTP armazenada em memória.
- Email normalizado como identificador único.
- Senhas armazenadas somente como hash BCrypt com custo 12.
- A política comum aceita senhas entre 12 e 72 bytes em UTF-8, sem exigir classes
  específicas de caracteres; a senha nunca é truncada para caber no BCrypt.
- Cookie de sessão `HttpOnly`, `SameSite=Lax`, limitado a `/api` e `Secure` em produção.
- Timeout de 30 minutos de inatividade; reinício da aplicação encerra as sessões.
- Proteção contra session fixation no login.
- CSRF obrigatório em operações administrativas mutáveis. `POST /api/v1/publico/agendamentos`
  é a única escrita isenta, porque não usa sessão nem autenticação e é protegida por
  idempotência e limite de requisições públicas.
- Limitação em memória para tentativas de login, inicialmente cinco tentativas por origem em quinze minutos, configurável.
- Mudança de senha exige a senha atual, invalida todas as sessões e força novo login.
- Rotas públicas são declaradas explicitamente e todo caminho não reconhecido é negado.
- JWT, OAuth, login social e segundo fator ficam fora da V1.

O cookie que transporta o token CSRF precisa ser legível pelo frontend; ele não é o cookie de sessão e não contém autenticação.

## Bootstrap administrativo

- Quando não existe usuário administrativo, a aplicação exige email e senha inicial fornecidos por variáveis de ambiente.
- O bootstrap cria exatamente um `PROPRIETARIO` e não registra a senha em log.
- Se já existe qualquer usuário, o bootstrap não cria, duplica ou redefine credenciais.
- A senha inicial passa pela mesma política e pelo mesmo encoder das demais senhas.
- As variáveis de bootstrap devem ser removidas ou trocadas após a inicialização da instalação.

## Contas de funcionários

- Somente `PROPRIETARIO` cria, altera, ativa, desativa ou redefine credenciais de `FUNCIONARIO`.
- A senha criada pelo proprietário é temporária e deve ser substituída no primeiro login.
- Até a troca, a sessão só acessa identidade, mudança de senha e logout.
- A redefinição administrativa invalida todas as sessões do funcionário.
- Contas são desativadas, não apagadas, para preservar auditoria.
- O último `PROPRIETARIO` ativo não pode ser desativado ou rebaixado.
- F2 persiste somente a identidade mínima de `Profissional` necessária para a chave
  estrangeira e para a unicidade do vínculo. Cadastro e gestão de profissionais
  permanecem em B1.

## Recuperação do proprietário

- Não existe rota pública de esquecimento ou recuperação de senha na V1.
- A recuperação usa um modo de linha de comando do mesmo artefato da aplicação, sem iniciar servidor web.
- A aplicação normal deve estar parada durante o procedimento; o reinício posterior elimina todas as sessões em memória.
- Email e nova senha são fornecidos por variáveis de ambiente próprias do procedimento.
- A senha não pode ser argumento de linha de comando, log, evento ou mensagem de erro.
- O comando redefine somente uma conta `PROPRIETARIO` existente e ativa; não cria usuário nem altera papel.
- A nova senha passa pela política normal e é gravada com BCrypt custo 12.
- Sucesso e falha geram código de saída verificável; a auditoria registra o identificador do usuário, instante e resultado, sem segredo.
- As variáveis de recuperação devem ser removidas após o uso.

## Escopo do funcionário

- Toda conta `FUNCIONARIO` possui vínculo obrigatório e único com um profissional.
- O backend deriva esse vínculo da sessão e nunca confia em identificador de profissional enviado para ampliar acesso.
- O funcionário lista recursos e mantém somente serviços do profissional vinculado, sem alterar cadastro, capacidade ou estado dos recursos.
- Agenda, reservas e ações administrativas são filtradas pelo profissional vinculado.
- Dados de cliente são exibidos somente no contexto das reservas daquele profissional.
- Histórico de atendimentos do mesmo cliente com outros profissionais não é exposto.
- Alocações automáticas podem ser visualizadas somente no contexto das reservas autorizadas; `FUNCIONARIO` não as altera.
- Tentativa de acessar entidade fora do escopo não confirma sua existência.

## Aviso de privacidade e anonimização manual

- A instalação configura a versão e o texto vigentes do aviso de privacidade; o
  perfil `prod` recusa inicialização quando qualquer um estiver ausente ou vazio.
- `GET /api/v1/publico/aviso-privacidade` expõe somente essa versão e esse texto para
  apresentação antes da confirmação.
- A API registra na reserva a versão apresentada e o instante da ciência.
- Somente `PROPRIETARIO` pode anonimizar manualmente um cliente, após verificação operacional do pedido.
- A operação exige motivo e é rejeitada enquanto existir reserva futura `CONFIRMADO`.
- A anonimização remove nome, celular e suas cópias pessoais nas reservas, libera o índice de identidade e preserva somente o histórico operacional não identificável.
- A auditoria registra identificadores internos, autor, motivo, resultado e instante, sem reproduzir o dado removido.
- A V1 não oferece exportação completa, correção integral, retenção configurável ou anonimização automática.
- Backups seguem a política operacional da instalação. Depois de uma restauração, a operação deve verificar e reaplicar manualmente anonimizações posteriores ao ponto restaurado.

Criptografia do volume e dos backups, TLS e controle de acesso protegem nome e celular do cliente e os dados das contas administrativas. Nenhum dado de saúde, prontuário ou anamnese pertence à V1.

## Referências oficiais

- [Lei nº 13.709/2018 — Lei Geral de Proteção de Dados Pessoais](https://planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13709compilado.htm).
- [Guia Orientativo sobre Segurança da Informação para Agentes de Tratamento de Pequeno Porte — ANPD](https://www.gov.br/anpd/pt-br/centrais-de-conteudo/materiais-educativos-e-publicacoes/guia_seguranca_da_informacao_para_atpps___defeso_eleitoral.pdf).

As referências sustentam minimização, transparência, controle de acesso e segurança. A base legal concreta e o texto apresentado ao titular dependem do fluxo real e devem ser revisados antes da produção.
