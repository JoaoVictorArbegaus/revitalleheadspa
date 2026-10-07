# Glossário

| Termo | Significado no projeto |
|---|---|
| instalação | sistema e banco isolados destinados a um único cliente contratante |
| negócio | empresa que oferece os serviços e opera a agenda |
| administrador | usuário autenticado que gerencia a instalação |
| profissional | pessoa que possui e executa seus próprios serviços; sempre é bloqueada pela reserva |
| recurso operacional | sala ou equipamento opcional que também é bloqueado quando exigido pelo serviço |
| capacidade do recurso | total de unidades intercambiáveis que podem ser alocadas simultaneamente |
| pré-seleção de recurso | recurso e quantidade usados como padrão ao criar uma reserva de determinado serviço |
| alocação de recurso | recurso e quantidade efetivamente bloqueados por uma reserva |
| usuário administrativo | conta autenticada; pode ou não estar vinculada a um profissional |
| `PROPRIETARIO` | proprietário com acesso global e gestão de usuários |
| `FUNCIONARIO` | funcionário vinculado a um profissional e limitado aos próprios serviços, agenda, reservas e contexto de clientes |
| cliente final | pessoa que agenda ou recebe o serviço, sem conta na API V1 |
| serviço | oferta reservável com nome, duração e preço |
| expediente | janelas recorrentes em que o negócio normalmente atende |
| exceção | alteração pontual no expediente de uma data ou período |
| bloqueio | intervalo em que novas reservas não podem ser criadas |
| disponibilidade | conjunto calculado de horários que podem ser reservados naquele momento |
| agendamento | compromisso de prestar um serviço a um cliente em um intervalo; termo canônico do domínio para a reserva |
| período ocupado | duração do serviço somada ao intervalo operacional posterior, quando houver |
| fotografia | cópia histórica de nome, duração e preço gravada no agendamento |
| referência pública | código aleatório usado externamente sem revelar o identificador interno |
| outbox | registros transacionais de eventos que serão processados por integrações |
| núcleo | módulos reutilizáveis de serviços, agenda, clientes e reservas |
| personalização | marca, conteúdo, integração ou regra periférica específica de uma instalação |
| produto-base | repositório genérico que produz as versões estáveis da API |
| projeto de cliente | código independente derivado de uma versão do produto-base e personalizado para um contratante |
| release de origem | versão estável da API usada para iniciar a cópia independente de um projeto de cliente |
