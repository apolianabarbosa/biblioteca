package com.api.biblioteca.Validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<CpfValido, String>{
    public static boolean isValidCPF(String cpf) {
        if(cpf == null){
            return false;
        }

        String cpfNumerico = cpf.replaceAll("\\D", "");

        return cpfNumerico.length() == 11;
    }

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext context){
        if(cpf == null || cpf.trim().isEmpty()){
            return false;
        } 
        
        return CpfValidator.isValidCPF(cpf);
    }
}
