package tarefas02.api.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class DataBindingViolationException extends RuntimeException {

    public DataBindingViolationException(String message) {
        super(message);
    }
}