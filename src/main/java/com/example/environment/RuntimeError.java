package com.example.environment;

import com.example.token.Token;

public class RuntimeError extends RuntimeException {
    private Token token;

    public RuntimeError(String message, Token token) {
        super(message);
        this.token = token;
    }

    public Token getToken() {
        return token;
    }
}
