package com.api.biblioteca.Validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<CpfValido, String>{
    public static boolean isValidCPF(String cpf) {
        // Remove caracteres não numéricos
        cpf = cpf.replaceAll("\\D", "");

        // Verifica se tem 11 dígitos
        if (!cpf.matches("\\d{11}")) return false;

        // Elimina CPFs com todos dígitos iguais (ex: 11111111111)
        if (cpf.chars().distinct().count() == 1) return false;

        try {
            // Cálculo do 1º dígito verificador
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += (cpf.charAt(i) - '0') * (10 - i);
            }
            int dig1 = 11 - (soma % 11);
            dig1 = (dig1 > 9) ? 0 : dig1;

            // Cálculo do 2º dígito verificador
            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += (cpf.charAt(i) - '0') * (11 - i);
            }
            int dig2 = 11 - (soma % 11);
            dig2 = (dig2 > 9) ? 0 : dig2;

            // Verifica os dígitos
            return dig1 == (cpf.charAt(9) - '0') &&
                   dig2 == (cpf.charAt(10) - '0');

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context){
        if(cpf == null) return false;
        return CpfValidator.isValidCPF(cpf);
    }
}
