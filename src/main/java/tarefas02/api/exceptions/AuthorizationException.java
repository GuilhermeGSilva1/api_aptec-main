package tarefas02.api.exceptions; // Pacote das exceções customizadas da camada de serviço

import org.springframework.http.HttpStatus; // Import da enumeração de status HTTP do Spring
import org.springframework.web.bind.annotation.ResponseStatus; // Anotação para mapear o status HTTP retornado por padrão

@ResponseStatus(value = HttpStatus.FORBIDDEN) // Mapeia a exceção para retornar o código HTTP 403 (Forbidden) por padrão
public class AuthorizationException extends RuntimeException { // Herda de RuntimeException para ser uma exceção em tempo de execução

    private static final long serialVersionUID = 1L; // Identificador único para a serialização da classe

    // Construtor que recebe uma mensagem descritiva do erro de autorização
    public AuthorizationException(String message) {
        super(message); // Repassa a mensagem para a classe pai RuntimeException
    }

    // Construtor que recebe uma mensagem e a causa da exceção
    public AuthorizationException(String message, Throwable cause) {
        super(message, cause); // Repassa a mensagem e a causa para a classe pai RuntimeException
    }
}