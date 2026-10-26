package tarefas02.api.exceptions; // Pacote do manipulador de exceções globais da API

import java.io.PrintWriter; // Classe para escrita formatada de rastros de pilha
import java.io.StringWriter; // Classe para conversão do fluxo de escrita em String

import org.apache.commons.lang3.exception.ExceptionUtils; // Utilitário da Apache Commons para extração da stacktrace
import org.springframework.beans.factory.annotation.Value; // Anotação para injeção de propriedades do arquivo application.properties
import org.springframework.dao.DataIntegrityViolationException; // Exceção lançada em violações de integridade no MySQL
import org.springframework.http.HttpStatus; // Enumeração com os códigos de status HTTP
import org.springframework.http.ResponseEntity; // Encapsulador de resposta HTTP com payload e cabeçalhos
import org.springframework.security.access.AccessDeniedException; // Exceção de acesso negado nativa do Spring Security
import org.springframework.security.core.AuthenticationException; // Exceção genérica de falha na autenticação
import org.springframework.web.bind.annotation.ExceptionHandler; // Anotação que vincula exceções a métodos manipuladores
import org.springframework.web.bind.annotation.ResponseStatus; // Define o status HTTP padrão para a resposta
import org.springframework.web.bind.annotation.RestControllerAdvice; // Torna a classe um interceptador global de erros para Controllers
import org.springframework.web.context.request.WebRequest; // Abstração dos detalhes da requisição Web
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler; // Classe base do Spring MVC para erros

import jakarta.validation.ConstraintViolation; // Interface de violação do Bean Validation
import jakarta.validation.ConstraintViolationException; // Exceção lançada quando ocorrem falhas de validação de beans
import tarefas02.api.exceptions.AuthorizationException; // Exceção customizada para erros de autorização
import tarefas02.api.services.exceptions.DataBindingViolationException; // Exceção customizada de violação de dados
import tarefas02.api.services.exceptions.ObjectNotFoundException; // Exceção customizada para recursos não encontrados

// Interceptador global para uniformizar o formato do JSON retornado em erros na API
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // Lê a propriedade que indica se a stacktrace completa deve ser exibida no JSON de erro
    @Value("${server.error.include-exception:false}")
    private boolean printStackTrace;

    // Trata erros de recursos não encontrados (Status 404 - Not Found)
    @ExceptionHandler(ObjectNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<Object> handleObjectNotFoundException(
            ObjectNotFoundException objectNotFoundException,
            WebRequest request) {
        return buildErrorResponse(
                objectNotFoundException,
                objectNotFoundException.getMessage(),
                HttpStatus.NOT_FOUND,
                request
        );
    }

    // Trata conflitos de vínculo de dados (Status 409 - Conflict)
    @ExceptionHandler(DataBindingViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Object> handleDataBindingViolationException(
            DataBindingViolationException dataBindingViolationException,
            WebRequest request) {
        return buildErrorResponse(
                dataBindingViolationException,
                dataBindingViolationException.getMessage(),
                HttpStatus.CONFLICT,
                request
        );
    }

    // Trata violações de chave única ou integridade no banco de dados (Status 409 - Conflict)
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<Object> handleDataIntegrityViolationException(
            DataIntegrityViolationException dataIntegrityViolationException,
            WebRequest request) {
        return buildErrorResponse(
                dataIntegrityViolationException,
                "Violação de integridade dos dados no banco.",
                HttpStatus.CONFLICT,
                request
        );
    }

    // Trata exceções customizadas de autorização negada na regra de negócio (Status 403 - Forbidden)
    @ExceptionHandler(AuthorizationException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Object> handleAuthorizationException(
            AuthorizationException authorizationException,
            WebRequest request) {
        return buildErrorResponse(
                authorizationException,
                authorizationException.getMessage(),
                HttpStatus.FORBIDDEN,
                request
        );
    }

    // Trata exceções nativas de acesso negado do Spring Security (Status 403 - Forbidden)
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ResponseEntity<Object> handleAccessDeniedException(
            AccessDeniedException accessDeniedException,
            WebRequest request) {
        return buildErrorResponse(
                accessDeniedException,
                "Acesso negado. Você não possui permissão para acessar este recurso.",
                HttpStatus.FORBIDDEN,
                request
        );
    }

    // Trata exceções nativas de falha de autenticação do Spring Security (Status 401 - Unauthorized)
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<Object> handleAuthenticationException(
            AuthenticationException authenticationException,
            WebRequest request) {
        return buildErrorResponse(
                authenticationException,
                "Falha na autenticação. Usuário ou senha inválidos.",
                HttpStatus.UNAUTHORIZED,
                request
        );
    }

    // Trata violações de validação de campos anotados com Bean Validation (Status 422 - Unprocessable Entity)
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ResponseEntity<Object> handleConstraintViolationException(
            ConstraintViolationException constraintViolationException,
            WebRequest request) {

        // Instancia o objeto de resposta de erro com código HTTP 422
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Validation error. Check 'errors' field for details."
        );

        // Percorre cada violação identificada no corpo da requisição
        for (ConstraintViolation<?> violation : constraintViolationException.getConstraintViolations()) {
            // Adiciona o nome do campo e a mensagem de erro específica
            errorResponse.addValidationError(
                    violation.getPropertyPath().toString(),
                    violation.getMessage()
            );
        }

        // Retorna a resposta HTTP contendo os detalhes das validações com erro
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse);
    }

    // Trata quaisquer outras exceções não capturadas anteriormente (Status 500 - Internal Server Error)
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleAllUncaughtException(
            Exception exception,
            WebRequest request) {
        return buildErrorResponse(
                exception,
                "Ocorreu um erro interno no servidor.",
                HttpStatus.INTERNAL_SERVER_ERROR,
                request
        );
    }

    // Método utilitário privado para construir a estrutura padronizada de erro HTTP
    private ResponseEntity<Object> buildErrorResponse(
            Exception exception,
            String message,
            HttpStatus httpStatus,
            WebRequest request) {
        // Cria a resposta padrão com o código e mensagem
        ErrorResponse errorResponse = new ErrorResponse(httpStatus.value(), message);

        // Se a propriedade printStackTrace estiver ativada, anexa a pilha do erro
        if (printStackTrace) {
            errorResponse.setStackTrace(ExceptionUtils.getStackTrace(exception));
        }

        // Retorna o objeto de resposta envelopado
        return ResponseEntity.status(httpStatus).body(errorResponse);
    }
}