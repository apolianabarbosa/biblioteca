package com.api.biblioteca.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.biblioteca.service.EmprestimoService;

@RestController
@RequestMapping("/emprestimos")
public class EmprestimoController {
    
    private final EmprestimoService es;

    public EmprestimoController(EmprestimoService es){
        this.es = es;
    }

    // MÉTODOS
}
