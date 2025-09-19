package com.api.biblioteca.Validation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = DataNascimentoValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)

public @interface DataNascimentoValida {

    String message() default "Data de nascimento inválida. Use o seguinte formato: dd/MM/yyyy";
    Class<?>[] groups() default{};
    Class<? extends Payload>[] payload() default {};

}
