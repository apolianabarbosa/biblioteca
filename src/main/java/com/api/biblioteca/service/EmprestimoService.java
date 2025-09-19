package com.api.biblioteca.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.api.biblioteca.repository.EmprestimoRepository;

@Service
public class EmprestimoService {
    private final EmprestimoRepository er;

    @Autowired
    public EmprestimoService(EmprestimoRepository er){
        this.er = er;
    }
    
}
