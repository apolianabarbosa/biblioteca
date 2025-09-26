package com.api.biblioteca.Validation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = IsbnValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)

public @interface IsbnValido {
    
    String message() default "ISBN inválido. Deve conter 10 ou 13 dígitos.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
}
