# Dados, privacidade e segurança

Este projeto persiste dados pessoais e deve aplicar privacidade desde a modelagem. A revisão jurídica final da política de privacidade, bases legais e prazos é responsabilidade do negócio com apoio especializado.

## Classificação inicial

| Dado | Finalidade inicial | Exposição permitida |
|---|---|---|
| nome | identificar a reserva e atender o cliente | administração autorizada e resumo necessário |
| CPF | distinguir cliente conforme regra adotada | mascarado na interface; nunca em URL ou log |
| celular | contato e confirmação | administração autorizada; mascarado onde possível |
| dados da reserva | prestar e administrar o serviço | cliente na confirmação e administração autorizada |
| eventos técnicos | segurança e diagnóstico | equipe autorizada, sem dados pessoais desnecessários |

## Princípios obrigatórios

- coletar somente dados com finalidade definida;
- informar finalidade antes da confirmação;
- separar aceite de termos e consentimento de mensagens promocionais;
- não condicionar a reserva a marketing;
- limitar acesso administrativo;
- mascarar dados em telas de listagem quando o valor completo não for necessário;
- não incluir CPF, celular, token ou segredo em logs, métricas, URLs ou mensagens de erro;
- criptografar trânsito e armazenamento conforme a infraestrutura escolhida;
- manter backups com proteção equivalente ao banco principal.

## CPF

- normalizar antes de comparar;
- validar dígitos verificadores;
- não usar CPF como identificador público;
- cifrar o valor normalizado com AES-256-GCM e nonce único;
- manter HMAC-SHA-256 separado para busca e unicidade;
- usar chaves distintas de cifragem e busca, com ao menos 32 bytes aleatórios, fora do banco;
- restringir descriptografia ao menor conjunto de operações;
- mascarar como padrão de apresentação.

Hash simples de CPF não é proteção suficiente porque o espaço de valores é previsível.

## Celular

- receber sem campo de país e normalizar números brasileiros para o formato canônico com `+55`;
- manter unicidade por instalação e usar o valor normalizado para localizar o cliente;
- validar sem assumir que o número possui WhatsApp;
- registrar separadamente o estado de entrega de mensagens;
- não reutilizar para marketing sem fundamento e aceite apropriados.

O celular informado isoladamente identifica o cadastro durante a criação, mas não autentica o cliente nem autoriza consulta, cancelamento ou reagendamento.

## Rotas públicas

- aplicar limite de requisições por origem e sinal de abuso;
- evitar endpoints que confirmem a existência de um CPF;
- usar referências públicas aleatórias;
- não permitir consulta de reserva apenas por referência previsível;
- validar tamanho e formato de toda entrada;
- proteger documentação ou detalhes operacionais quando necessário.

## Token público da reserva

- usar como token os 256 bits produzidos por HMAC-SHA-256 sobre a referência pública aleatória e `SEGREDO_TOKEN_AGENDAMENTO`, codificados em formato seguro para transporte;
- persistir somente hash e comparar em tempo constante;
- devolver o valor puro somente na criação da reserva;
- transportar à API apenas no cabeçalho `X-Token-Agendamento`;
- quando o frontend criar um link, manter o token no fragmento da URL para que não alcance servidor, proxy ou log de acesso;
- não registrar cabeçalhos, fragmentos, hashes ou valores do token em telemetria;
- aplicar resposta neutra e limitação de tentativas inválidas;
- revogar o acesso em estado terminal ou ao chegar o início do atendimento.

Perda do token exige contato com o estabelecimento na V1. Recuperação por mensagem depende de integração futura e não autoriza a criação de endpoint baseado apenas em celular.

`SEGREDO_TOKEN_AGENDAMENTO` contém ao menos 32 bytes aleatórios, fica fora do banco e não é reutilizado para outra finalidade. Precisa permanecer estável enquanto houver reservas operáveis; sua troca planejada exige invalidar ou concluir os acessos existentes. Chaves de idempotência também são persistidas somente como hash e nunca entram em logs.

## Autenticação administrativa

- Spring Security com sessão HTTP armazenada em memória.
- Email normalizado como identificador único.
- Senhas armazenadas somente como hash BCrypt com custo 12.
- A política comum aceita senhas entre 12 e 72 bytes em UTF-8, sem exigir classes
  específicas de caracteres; a senha nunca é truncada para caber no BCrypt.
- Cookie de sessão `HttpOnly`, `SameSite=Lax`, limitado a `/api` e `Secure` em produção.
- Timeout de 30 minutos de inatividade; reinício da aplicação encerra as sessões.
- Proteção contra session fixation no login.
- CSRF obrigatório em operações administrativas mutáveis.
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
- Serviços, agenda, reservas e ações administrativas são filtrados pelo profissional vinculado.
- Dados de cliente são exibidos somente no contexto das reservas daquele profissional.
- Histórico de atendimentos do mesmo cliente com outros profissionais não é exposto.
- Recursos operacionais compartilhados podem ser consultados e alocados; somente `PROPRIETARIO` altera cadastro ou capacidade.
- Tentativa de acessar entidade fora do escopo não confirma sua existência.

## Retenção e direitos do titular

- O projeto-base não presume base legal nem prazo universal.
- Antes de iniciar em produção, a instalação exige `versaoAvisoPrivacidade`, `contatoTitular` e `retencaoDadosPessoaisDias`.
- O controlador documenta finalidade, base legal, prazo, operador e canal com revisão adequada ao seu contexto.
- A API registra ciência da versão do aviso; consentimento promocional é separado, opcional e falso por padrão.
- Somente `PROPRIETARIO` exporta, corrige ou anonimiza integralmente, após verificação do solicitante feita pela operação.
- A anonimização remove identificadores canônicos, índices de busca e fotografias pessoais, preservando somente o histórico operacional não identificável.
- Reservas futuras vigentes impedem anonimização; o pedido deve ser tratado pela operação até que possa ser atendido ou fundamentadamente conservado.
- Um processo interno aplica a retenção configurada depois do último atendimento terminal.
- Backups seguem prazo próprio e protegido; restauração reaplica o registro de anonimizações realizadas depois do ponto restaurado.
- Logs de auditoria guardam identificadores internos, autor, motivo, resultado e instante, sem reproduzir o dado pessoal removido.

Criptografia do volume e dos backups, TLS e controle de acesso protegem nome, celular e email operacionais. Nenhum dado de saúde, prontuário ou anamnese pertence à V1.

## Referências oficiais

- [Lei nº 13.709/2018 — Lei Geral de Proteção de Dados Pessoais](https://planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13709compilado.htm).
- [Guia Orientativo sobre Segurança da Informação para Agentes de Tratamento de Pequeno Porte — ANPD](https://www.gov.br/anpd/pt-br/centrais-de-conteudo/materiais-educativos-e-publicacoes/guia_seguranca_da_informacao_para_atpps___defeso_eleitoral.pdf).

As referências sustentam a direção de minimização, transparência, controle de acesso e segurança. A base legal concreta, os textos apresentados ao titular e os prazos de retenção dependem do fluxo real e devem ser revisados antes da produção.
