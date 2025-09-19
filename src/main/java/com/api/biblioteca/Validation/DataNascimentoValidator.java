package com.api.biblioteca.Validation;

import java.time.LocalDate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DataNascimentoValidator implements ConstraintValidator<DataNascimentoValida, LocalDate> {
    
    @Override
    public boolean isValid(LocalDate data, ConstraintValidatorContext context){
        if(data == null){
            return false;
        }

        return !data.isAfter(LocalDate.now());
    }
}
