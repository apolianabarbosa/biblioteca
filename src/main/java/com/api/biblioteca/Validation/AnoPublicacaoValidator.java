package com.api.biblioteca.Validation;

import java.time.Year;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AnoPublicacaoValidator implements ConstraintValidator<AnoPublicacaoValido, Year> {
    @Override
    public boolean isValid(Year ano, ConstraintValidatorContext context){
        if(ano == null){
            return false;
        } 
            int anoAtual = Year.now().getValue();
            int anoValor = ano.getValue();

            return anoValor >= 1500 && anoValor <= anoAtual;
    }
}
