package com.example.parser;

import com.example.lexer.LexerError;

public class ParserError extends LexerError {
    public ParserError(String message, int line, int col, String lineText) {
        super(message, line, col, lineText);
    }
}
