package com.api.biblioteca.Validation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = EmailValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)

public @interface EmailValido {
    String message() default "E-mail inválido. Deve possuir o seguinte formato: XXXXX@gmail.com";
    Class<?>[] groups() default{};
    Class<? extends Payload>[] payload() default {};
}

