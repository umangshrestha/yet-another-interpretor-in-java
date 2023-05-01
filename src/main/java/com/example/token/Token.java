package com.example.token;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class Token {
    private TokenType type;
    private int line;
    private int col;
    private Object literal;

    public Token(TokenType type, int line, int col, Object literal) {
        this.type = type;
        this.line = line;
        this.col = col;
        this.literal = literal;
    }

    public Token(TokenType type, int line, int col) {
        this(type, line, col, null);
    }

    public String toString() {
        return String.format("%s{\"Line\":%d,\"Col\":%d,\"Literal\":\"%s\"}", type, line, col, literal);
    }
}
