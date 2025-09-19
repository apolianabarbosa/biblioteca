package com.api.biblioteca.exception;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.api.biblioteca.model.RespostaModel;

@RestControllerAdvice
public class ValidacaoHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaModel> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult()
                            .getAllErrors()
                            .stream()
                            .map(err -> err.getDefaultMessage())
                            .collect(Collectors.joining("; "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RespostaModel(mensagem));
    }
}
