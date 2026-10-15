import type { FieldValues, Path, UseFormSetError } from 'react-hook-form';
import { toast } from 'sonner';
import { ApiError } from '@/api/client';

// Mensagens por código (api/docs/catalogo-erros.md). Decide-se pelo código, nunca pelo texto da API.
const MENSAGENS: Record<string, string> = {
  AUTENTICACAO_NECESSARIA: 'Sua sessão expirou. Entre novamente.',
  ACESSO_NEGADO: 'Você não tem permissão para esta ação.',
  LIMITE_TENTATIVAS_LOGIN: 'Muitas tentativas de login. Aguarde um pouco e tente de novo.',
  SENHA_FORA_DA_POLITICA: 'A senha deve ter entre 12 e 72 caracteres.',
  SENHA_ATUAL_INVALIDA: 'A senha atual está incorreta.',
  FALHA_VALIDACAO: 'Revise os campos destacados.',
  CONFLITO_DADOS: 'Esses dados já existem. Atualize a tela e confira.',
  PROFISSIONAL_OBRIGATORIO: 'Escolha um profissional.',
  PROFISSIONAL_INVALIDO: 'O profissional escolhido está inativo ou não existe.',
  PROFISSIONAL_NAO_ENCONTRADO: 'Profissional não encontrado. Atualize a tela.',
  RECURSO_NAO_ENCONTRADO: 'Recurso não encontrado. Atualize a tela.',
  SERVICO_NAO_ENCONTRADO: 'Serviço não encontrado. Atualize a tela.',
  DURACAO_INCOMPATIVEL_COM_GRADE: 'Duração mais intervalo de limpeza precisa ser múltiplo de 15 minutos.',
  RECURSO_REPETIDO_NO_SERVICO: 'O mesmo recurso foi adicionado duas vezes.',
  RECURSO_INVALIDO: 'Um dos recursos escolhidos não existe mais.',
  QUANTIDADE_SUPERIOR_A_CAPACIDADE: 'A quantidade pedida é maior que a capacidade do recurso.',
  CAPACIDADE_INFERIOR_AO_REQUISITO: 'Algum serviço exige mais unidades deste recurso. Ajuste os serviços antes.',
  CAPACIDADE_INFERIOR_AS_RESERVAS: 'Há reservas futuras usando essa capacidade. Não é possível reduzir agora.',
  PERIODO_CONSULTA_INVALIDO: 'Período inválido (máximo de 31 dias).',
  INTERVALO_EXPEDIENTE_INVALIDO: 'Em cada janela, o início deve ser antes do fim.',
  EXPEDIENTE_SOBREPOSTO: 'Há janelas sobrepostas no mesmo dia.',
  INTERVALO_BLOQUEIO_INVALIDO: 'O início do bloqueio deve ser antes do fim.',
  BLOQUEIO_SOBREPOSTO: 'Esse bloqueio se sobrepõe a outro já cadastrado.',
  BLOQUEIO_NAO_ENCONTRADO: 'Bloqueio não encontrado. Atualize a tela.',
  AVISO_PRIVACIDADE_DESATUALIZADO: 'O aviso de privacidade mudou. Confira o novo texto com o cliente.',
  CONFLITO_AGENDAMENTO: 'Esse horário não está livre (ocupado, bloqueado, fora do expediente ou da grade de 15 minutos). Escolha outro.',
  CONFLITO_IDEMPOTENCIA: 'Essa reserva já foi enviada com outros dados. Tente de novo.',
  INICIO_NO_PASSADO: 'Não é possível agendar no passado.',
  AGENDAMENTO_NAO_ENCONTRADO: 'Agendamento não encontrado. Atualize a tela.',
  TRANSICAO_AGENDAMENTO_INVALIDA: 'Essa ação não é permitida para o estado ou o horário atual da reserva.',
  EMAIL_JA_UTILIZADO: 'Esse email já pertence a outra conta.',
  ULTIMO_PROPRIETARIO_ATIVO: 'Não é possível remover o último proprietário ativo.',
};

export function mensagemErro(e: unknown) {
  if (e instanceof ApiError) {
    const base = (e.codigo && MENSAGENS[e.codigo]) || 'Algo deu errado. Tente novamente.';
    // falha inesperada: mostra o idRastreio para o suporte
    if (e.status >= 500 && e.problema?.idRastreio) return `${base} (código ${e.problema.idRastreio.slice(0, 8)})`;
    return base;
  }
  return 'Não foi possível falar com o servidor. Verifique a conexão.';
}

export function avisarErro(e: unknown) {
  toast.error(mensagemErro(e));
}

/** Leva `errosCampos` da API para os campos do formulário; devolve true se marcou algum. */
export function marcarErrosCampos<T extends FieldValues>(e: unknown, setError: UseFormSetError<T>, campos: readonly string[]) {
  if (!(e instanceof ApiError) || !e.problema?.errosCampos?.length) return false;
  let marcou = false;
  for (const ec of e.problema.errosCampos) {
    if (ec.campo && campos.includes(ec.campo)) {
      setError(ec.campo as Path<T>, { message: ec.mensagem ?? 'Valor inválido' });
      marcou = true;
    }
  }
  return marcou;
}
