package com.api.biblioteca.Validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsbnValidator implements ConstraintValidator<IsbnValido, String> {

    public static boolean isValidISBN(String isbn) {
        if (isbn == null) {
            return false;
        }
        
        String isbnLimpo = isbn.replaceAll("[^0-9Xx]", "");

        // Verifica se o comprimento é 10 ou 13
        return isbnLimpo.length() == 10 || isbnLimpo.length() == 13;
    }


    @Override
    public boolean isValid(String isbn, ConstraintValidatorContext context) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return true; // Permite campo vazio se não for obrigatório
        }
        return isValidISBN(isbn);
    }
}
