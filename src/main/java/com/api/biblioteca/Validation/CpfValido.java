package com.api.biblioteca.Validation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = CpfValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfValido {
    String message() default "CPF inválido. Deve conter exatamente 11 dígitos numéricos.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
}
