package com.api.biblioteca.Validation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TelefoneValidator implements ConstraintValidator<TelefoneValido, String>{
    
    private static final String REGEX = "\\(\\d{2}\\)\\d{4,5}-\\d{4}";

    @Override
    public boolean isValid(String telefone, ConstraintValidatorContext context){
        return telefone != null && telefone.matches(REGEX);
    }
}
