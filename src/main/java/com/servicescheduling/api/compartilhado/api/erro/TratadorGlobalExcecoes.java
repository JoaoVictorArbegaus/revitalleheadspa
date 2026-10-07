package com.servicescheduling.api.compartilhado.api.erro;

import com.servicescheduling.api.compartilhado.aplicacao.ExcecaoRegraNegocio;
import com.servicescheduling.api.compartilhado.infraestrutura.web.FiltroRastreio;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Comparator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class TratadorGlobalExcecoes extends ResponseEntityExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(TratadorGlobalExcecoes.class);

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException excecao,
            HttpHeaders cabecalhos,
            HttpStatusCode estado,
            WebRequest requisicao) {
        List<ErroCampo> errosCampos = excecao.getBindingResult().getFieldErrors().stream()
                .map(erro -> new ErroCampo(
                        erro.getField(),
                        codigoValidacao(erro.getCode()),
                        erro.getDefaultMessage() == null ? "campo invalido" : erro.getDefaultMessage()))
                .sorted(Comparator.comparing(ErroCampo::campo).thenComparing(ErroCampo::codigo))
                .toList();

        ProblemDetail problema = criarProblema(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "falha-validacao",
                "Falha de validacao",
                "A solicitacao possui campos invalidos",
                "FALHA_VALIDACAO",
                requisicao);
        problema.setProperty("errosCampos", errosCampos);
        return resposta(problema, cabecalhos);
    }

    @Override
    protected ResponseEntity<Object> handleHandlerMethodValidationException(
            HandlerMethodValidationException excecao,
            HttpHeaders cabecalhos,
            HttpStatusCode estado,
            WebRequest requisicao) {
        List<ErroCampo> errosCampos = excecao.getParameterValidationResults().stream()
                .flatMap(resultado -> resultado.getResolvableErrors().stream()
                        .map(erro -> new ErroCampo(
                                resultado.getMethodParameter().getParameterName() == null
                                        ? "parametro"
                                        : resultado.getMethodParameter().getParameterName(),
                                codigoValidacao(primeiroCodigo(erro.getCodes())),
                                erro.getDefaultMessage() == null ? "campo invalido" : erro.getDefaultMessage())))
                .sorted(Comparator.comparing(ErroCampo::campo).thenComparing(ErroCampo::codigo))
                .toList();

        ProblemDetail problema = criarProblema(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "falha-validacao",
                "Falha de validacao",
                "A solicitacao possui campos invalidos",
                "FALHA_VALIDACAO",
                requisicao);
        problema.setProperty("errosCampos", errosCampos);
        return resposta(problema, cabecalhos);
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<Object> tratarAutenticacao(AuthenticationException excecao, WebRequest requisicao) {
        ProblemDetail problema = criarProblema(
                HttpStatus.UNAUTHORIZED,
                "autenticacao-necessaria",
                "Autenticacao necessaria",
                "E necessario autenticar-se para acessar este recurso",
                "AUTENTICACAO_NECESSARIA",
                requisicao);
        return resposta(problema, new HttpHeaders());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Object> tratarAcessoNegado(AccessDeniedException excecao, WebRequest requisicao) {
        ProblemDetail problema = criarProblema(
                HttpStatus.FORBIDDEN,
                "acesso-negado",
                "Acesso negado",
                "A operacao solicitada nao e permitida",
                "ACESSO_NEGADO",
                requisicao);
        return resposta(problema, new HttpHeaders());
    }

    @ExceptionHandler(CsrfException.class)
    ResponseEntity<Object> tratarCsrf(CsrfException excecao, WebRequest requisicao) {
        ProblemDetail problema = criarProblema(
                HttpStatus.FORBIDDEN,
                "falha-validacao-csrf",
                "Falha de validacao CSRF",
                "O token CSRF esta ausente ou e invalido",
                "FALHA_VALIDACAO_CSRF",
                requisicao);
        return resposta(problema, new HttpHeaders());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Object> tratarErroInesperado(Exception excecao, WebRequest requisicao) {
        LOG.error("falha inesperada ao processar requisicao tipo={}", excecao.getClass().getSimpleName());
        ProblemDetail problema = criarProblema(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "erro-interno",
                "Erro interno",
                "Ocorreu uma falha inesperada",
                "ERRO_INTERNO",
                requisicao);
        return resposta(problema, new HttpHeaders());
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception excecao,
            Object corpo,
            HttpHeaders cabecalhos,
            HttpStatusCode estado,
            WebRequest requisicao) {
        HttpStatus status = HttpStatus.resolve(estado.value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String codigo = switch (status) {
            case BAD_REQUEST -> "SOLICITACAO_MALFORMADA";
            case METHOD_NOT_ALLOWED -> "METODO_NAO_PERMITIDO";
            case UNSUPPORTED_MEDIA_TYPE -> "TIPO_MIDIA_NAO_SUPORTADO";
            case NOT_FOUND -> "RECURSO_NAO_ENCONTRADO";
            default -> status.is5xxServerError() ? "ERRO_INTERNO" : "SOLICITACAO_INVALIDA";
        };
        ProblemDetail problema = criarProblema(
                status,
                codigo.toLowerCase().replace('_', '-'),
                status.getReasonPhrase(),
                status.is5xxServerError() ? "Ocorreu uma falha inesperada" : "A solicitacao nao pode ser processada",
                codigo,
                requisicao);
        return resposta(problema, cabecalhos);
    }

    private ProblemDetail criarProblema(
            HttpStatus status,
            String identificador,
            String titulo,
            String detalhe,
            String codigo,
            WebRequest requisicao) {
        String idRastreio = idRastreio(requisicao);
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, detalhe);
        problema.setType(URI.create("/problemas/" + identificador));
        problema.setTitle(titulo);
        problema.setInstance(URI.create("urn:rastreio:" + idRastreio));
        problema.setProperty("codigo", codigo);
        problema.setProperty("idRastreio", idRastreio);
        return problema;
    }

    private ResponseEntity<Object> resposta(ProblemDetail problema, HttpHeaders cabecalhosOriginais) {
        HttpHeaders cabecalhos = new HttpHeaders();
        cabecalhos.putAll(cabecalhosOriginais);
        cabecalhos.setContentType(MediaType.APPLICATION_PROBLEM_JSON);
        return new ResponseEntity<>(problema, cabecalhos, HttpStatusCode.valueOf(problema.getStatus()));
    }

    private String idRastreio(WebRequest requisicao) {
        if (requisicao instanceof ServletWebRequest requisicaoServlet) {
            HttpServletRequest http = requisicaoServlet.getRequest();
            Object valor = http.getAttribute(FiltroRastreio.ATRIBUTO_ID_RASTREIO);
            if (valor instanceof String idRastreio) {
                return idRastreio;
            }
        }
        return "indisponivel";
    }

    private String codigoValidacao(String codigoOriginal) {
        String codigo = codigoOriginal == null ? "" : codigoOriginal;
        if (codigo.contains("NotBlank")) {
            return "NAO_PODE_ESTAR_EM_BRANCO";
        }
        if (codigo.contains("NotEmpty")) {
            return "NAO_PODE_ESTAR_VAZIO";
        }
        if (codigo.contains("NotNull")) {
            return "OBRIGATORIO";
        }
        if (codigo.contains("Size")) {
            return "TAMANHO_INVALIDO";
        }
        if (codigo.contains("Email")) {
            return "FORMATO_EMAIL_INVALIDO";
        }
        if (codigo.contains("Pattern")) {
            return "FORMATO_INVALIDO";
        }
        if (codigo.contains("Min") || codigo.contains("Positive")) {
            return "VALOR_ABAIXO_DO_MINIMO";
        }
        if (codigo.contains("Max") || codigo.contains("Negative")) {
            return "VALOR_ACIMA_DO_MAXIMO";
        }
        return "CAMPO_INVALIDO";
    }

    private String primeiroCodigo(String[] codigos) {
        return codigos == null || codigos.length == 0 ? "" : codigos[0];
    }

    @ExceptionHandler(ExcecaoRegraNegocio.class)
    ResponseEntity<Object> tratarRegraNegocio(ExcecaoRegraNegocio excecao, WebRequest requisicao) {
        String identificador = excecao.getCodigo().toLowerCase().replace('_', '-');
        HttpStatus status = switch (excecao.getTipo()) {
            case ENTRADA_INVALIDA -> HttpStatus.UNPROCESSABLE_CONTENT;
            case CONFLITO -> HttpStatus.CONFLICT;
            case NAO_ENCONTRADO -> HttpStatus.NOT_FOUND;
            case LIMITE_EXCEDIDO -> HttpStatus.TOO_MANY_REQUESTS;
        };
        ProblemDetail problema = criarProblema(
                status, identificador, "Operacao nao permitida",
                excecao.getMessage(), excecao.getCodigo(), requisicao);
        HttpHeaders cabecalhos = new HttpHeaders();
        if (status == HttpStatus.TOO_MANY_REQUESTS) {
            cabecalhos.set(HttpHeaders.RETRY_AFTER, String.valueOf(excecao.getRetryAfterSegundos()));
        }
        return resposta(problema, cabecalhos);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<Object> tratarConflitoIntegridade(DataIntegrityViolationException excecao, WebRequest requisicao) {
        String mensagemInterna = excecao.getMostSpecificCause().getMessage();
        String codigo = "CONFLITO_DADOS";
        String identificador = "conflito-dados";
        String detalhe = "A operacao conflita com dados existentes";
        if (mensagemInterna != null && mensagemInterna.contains("uq_usuario_administrativo_email")) {
            codigo = "EMAIL_JA_UTILIZADO";
            identificador = "email-ja-utilizado";
            detalhe = "O email ja esta em uso";
        } else if (mensagemInterna != null && mensagemInterna.contains("uq_usuario_profissional")) {
            codigo = "PROFISSIONAL_JA_VINCULADO";
            identificador = "profissional-ja-vinculado";
            detalhe = "O profissional ja possui usuario vinculado";
        }
        ProblemDetail problema = criarProblema(
                HttpStatus.CONFLICT,
                identificador,
                "Conflito de dados",
                detalhe,
                codigo,
                requisicao);
        return resposta(problema, new HttpHeaders());
    }
}
