package com.api.biblioteca.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.api.biblioteca.dtos.DashboardStatsDTO;
import com.api.biblioteca.dtos.RelatorioItemDTO;
import com.api.biblioteca.service.RelatorioService;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(relatorioService.obterEstatisticasGerais());
    }

    @GetMapping("/top-emprestimos")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<List<RelatorioItemDTO>> getTopEmprestimos() {
        return ResponseEntity.ok(relatorioService.obterTopLivrosEmprestados());
    }

    @GetMapping("/top-reservas")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<List<RelatorioItemDTO>> getTopReservas() {
        return ResponseEntity.ok(relatorioService.obterTopLivrosReservados());
    }
}