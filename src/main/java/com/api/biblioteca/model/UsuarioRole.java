package com.api.biblioteca.model;

public enum UsuarioRole {

    BIBLIOTECARIO("bibliotecario"),
    LEITOR("leitor");

    private String role;

    UsuarioRole(String role){
        this.role = role;
    }

    public String getRole(){
        return role;
    }
}
