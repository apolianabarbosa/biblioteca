package com.api.biblioteca.Validation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = SenhaValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)

public @interface SenhaValida {

    String message() default "A senha deve ter no mínimo 8 caracteres, com letra maiúscula, minúscula, número e caractere especial.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
}