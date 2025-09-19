package com.api.biblioteca.Validation;

import jakarta.validation.Payload;

public  @interface AnoPublicacaoValido {
     
    String message() default "Ano de publicação inválida";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
