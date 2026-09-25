package com.bazaar.usuarios.dto;

public record LoginResponse(String token, String tipo) {
    public LoginResponse(String token){
        this(token, "Bearer");
    }
}
