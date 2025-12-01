package com.api.biblioteca.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.api.biblioteca.dtos.DashboardStatsDTO;
import com.api.biblioteca.dtos.RelatorioItemDTO;
import com.api.biblioteca.service.RelatorioService;

@RestController
@RequestMapping("/relatorios")
public class RelatorioController {

    @Autowired
    private RelatorioService relatorioService;

    // 1. Dashboard com Filtro de Data
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats(
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate) {
        
        // Passa as datas (podem ser null) para o Service
        return ResponseEntity.ok(relatorioService.obterEstatisticasGerais(startDate, endDate));
    }

    // 2. Top Empréstimos com Filtro de Data
    @GetMapping("/top-emprestimos")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<List<RelatorioItemDTO>> getTopEmprestimos(
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate) {
        
        // Passa as datas para o Service
        return ResponseEntity.ok(relatorioService.obterTopLivrosEmprestados(startDate, endDate));
    }

    // 3. Top Reservas com Filtro de Data
    @GetMapping("/top-reservas")
    @PreAuthorize("hasRole('BIBLIOTECARIO')")
    public ResponseEntity<List<RelatorioItemDTO>> getTopReservas(
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate) {
        
        // Passa as datas para o Service
        return ResponseEntity.ok(relatorioService.obterTopLivrosReservados(startDate, endDate));
    }
}