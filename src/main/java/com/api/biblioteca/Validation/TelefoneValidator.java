package com.api.biblioteca.Validation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TelefoneValidator implements ConstraintValidator<TelefoneValido, String>{
    
    private static final String REGEX = "\\d{11}";

    @Override
    public boolean isValid(String telefone, ConstraintValidatorContext context){
        if(telefone == null || telefone.trim().isEmpty()){
            return true;
        }

        String telefoneNumerico = telefone.replaceAll("\\D", "");

        return telefoneNumerico.matches(REGEX);
    }
}
