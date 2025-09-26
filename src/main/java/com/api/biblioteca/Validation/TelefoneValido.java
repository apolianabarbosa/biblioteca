package com.api.biblioteca.Validation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = TelefoneValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)

public @interface TelefoneValido {

    String message() default "Telefone inválido. Use o formato com 11 dígitos, incluindo DDD (ex: 71999999999)";
    Class<?>[] groups() default{};
    Class<? extends Payload>[] payload() default {};

}