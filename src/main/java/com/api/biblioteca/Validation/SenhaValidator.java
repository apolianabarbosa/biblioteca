package com.api.biblioteca.Validation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SenhaValidator implements ConstraintValidator<SenhaValida, String>{
    
    private static final String REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$";

    @Override
    public boolean isValid(String senha, ConstraintValidatorContext context){
        if(senha == null) return false;
        return senha.matches(REGEX);
    }
    
}