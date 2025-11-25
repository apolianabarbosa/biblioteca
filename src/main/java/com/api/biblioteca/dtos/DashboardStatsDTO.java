package com.api.biblioteca.dtos;

public class DashboardStatsDTO {
    private long totalLivros;
    private long totalUsuarios;
    private long emprestimosAtivos;
    private long reservasAtivas;

    public DashboardStatsDTO(long totalLivros, long totalUsuarios, long emprestimosAtivos, long reservasAtivas) {
        this.totalLivros = totalLivros;
        this.totalUsuarios = totalUsuarios;
        this.emprestimosAtivos = emprestimosAtivos;
        this.reservasAtivas = reservasAtivas;
    }

    public long getTotalLivros() {
        return totalLivros;
    }

    public void setTotalLivros(long totalLivros) {
        this.totalLivros = totalLivros;
    }

    public long getTotalUsuarios() {
        return totalUsuarios;
    }

    public void setTotalUsuarios(long totalUsuarios) {
        this.totalUsuarios = totalUsuarios;
    }

    public long getEmprestimosAtivos() {
        return emprestimosAtivos;
    }

    public void setEmprestimosAtivos(long emprestimosAtivos) {
        this.emprestimosAtivos = emprestimosAtivos;
    }

    public long getReservasAtivas() {
        return reservasAtivas;
    }

    public void setReservasAtivas(long reservasAtivas) {
        this.reservasAtivas = reservasAtivas;
    }

    
}
