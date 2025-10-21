package com.api.biblioteca.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError; // Importar
import org.springframework.web.bind.MethodArgumentNotValidException; // Importar
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;


@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- ADICIONADO: Handler de Validação ---
    // Captura erros de @Valid (CPF, Telefone, Senha, NotEmpty, etc)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationExceptions(MethodArgumentNotValidException ex) {
        
        // 1. Cria o objeto "errors" que o seu frontend espera
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage);
        });

        // 2. Cria o ProblemDetail e adiciona o mapa de erros
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "A requisição contém dados inválidos.");
        errorDetail.setTitle("Erro de Validação");
        
        // 3. Adiciona a propriedade "errors" que o frontend vai ler
        errorDetail.setProperty("errors", fieldErrors); 
        
        return errorDetail;
    }

    // --- Seus Handlers de Segurança (Mantidos) ---

    // Credenciais inválidas (login incorreto)
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(BadCredentialsException ex) {
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.getMessage());
        errorDetail.setTitle("Credenciais Inválidas");
        errorDetail.setProperty("description", "O e-mail ou senha está incorreto.");
        return errorDetail;
    }

    // Conta bloqueada, desativada ou expirada
    @ExceptionHandler(AccountStatusException.class)
    public ProblemDetail handleAccountStatus(AccountStatusException ex) {
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        errorDetail.setTitle("Acesso Negado");
        errorDetail.setProperty("description", "A conta está bloqueada.");
        return errorDetail;
    }

    // Sem permissão para acessar o recurso
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        errorDetail.setTitle("Acesso Negado");
        errorDetail.setProperty("description", "Você não tem permissão para acessar este recurso.");
        return errorDetail;
    }

    // Token JWT inválido (assinatura corrompida)
    @ExceptionHandler(SignatureException.class)
    public ProblemDetail handleInvalidToken(SignatureException ex) {
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        errorDetail.setTitle("Token Inválido");
        errorDetail.setProperty("description", "A assinatura do token é inválida.");
        return errorDetail;
    }

    // Token expirado
    @ExceptionHandler(ExpiredJwtException.class)
    public ProblemDetail handleExpiredToken(ExpiredJwtException ex) {
        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        errorDetail.setTitle("Token Expirado");
        errorDetail.setProperty("description", "Sua sessão expirou. Por favor, faça login novamente.");
        return errorDetail;
    }

    // Qualquer outro erro não tratado
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        ex.printStackTrace(); // loga para debug

        ProblemDetail errorDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        errorDetail.setTitle("Erro Interno do Servidor");
        errorDetail.setProperty("description", "Um erro inesperado ocorreu. A equipe de desenvolvimento foi notificada.");
        return errorDetail;
    }
}