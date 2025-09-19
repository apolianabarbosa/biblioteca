package com.api.biblioteca.Validation;
import java.util.regex.Pattern;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EmailValidator implements ConstraintValidator<EmailValido, String>{
    
    private static final String REGEX = "^[\\w.-]+@[\\w.-]+\\.\\w{2,}$";

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context){
        return email != null && Pattern.matches(REGEX, email);
    }
    
}
