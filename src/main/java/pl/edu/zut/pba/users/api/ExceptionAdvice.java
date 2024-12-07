package pl.edu.zut.pba.users.api;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import pl.edu.zut.pba.users.api.model.Error;

@ControllerAdvice
public class ExceptionAdvice
{

    @ExceptionHandler
    ResponseEntity<Error> handle(MethodArgumentNotValidException ex)
    {
        return new ResponseEntity<>(
                new Error().code("400").message(ex.getBindingResult().getFieldError().getField() + ": " + ex.getBindingResult().getFieldError().getDefaultMessage()),
                HttpStatusCode.valueOf(400)
        );
    }

}
