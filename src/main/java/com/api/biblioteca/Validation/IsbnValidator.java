package com.api.biblioteca.Validation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsbnValidator implements ConstraintValidator<IsbnValido, String>{
    
    public static boolean isValidISBN(String isbn) {
        // Remove traços e espaços
        isbn = isbn.replaceAll("[^0-9Xx]", "");

        if (isbn.length() == 10) {
            return isValidISBN10(isbn);
        } else if (isbn.length() == 13) {
            return isValidISBN13(isbn);
        }
        return false;
    }

    private static boolean isValidISBN10(String isbn) {
        int soma = 0;
        for (int i = 0; i < 9; i++) {
            if (!Character.isDigit(isbn.charAt(i))) return false;
            soma += (isbn.charAt(i) - '0') * (10 - i);
        }

        char ultimo = isbn.charAt(9);
        soma += (ultimo == 'X' || ultimo == 'x') ? 10 : (ultimo - '0');

        return soma % 11 == 0;
    }

    private static boolean isValidISBN13(String isbn) {
        int soma = 0;
        for (int i = 0; i < 12; i++) {
            int num = isbn.charAt(i) - '0';
            soma += (i % 2 == 0) ? num : num * 3;
        }
        int digitoVerificador = (10 - (soma % 10)) % 10;

        return digitoVerificador == (isbn.charAt(12) - '0');
    }

    @Override
    public boolean isValid(String isbn, ConstraintValidatorContext context){
        if(isbn == null || isbn.trim().isEmpty()){
            return true;
        }
        return isValidISBN(isbn);
    }

}
