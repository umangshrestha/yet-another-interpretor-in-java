package com.example.lexer;

import com.example.App;

public class LexerError extends Exception {
    private final String message;
    private final int line;
    private final int col;
    private final String lineText;

    public LexerError(String message, int line, int col, String lineText) {
        super();
        this.message = message;
        this.line = line;
        this.col = col;
        this.lineText = lineText;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("File \"%s\", line %d, col %d\n%s\n", App.getfileName(), line, col, lineText));
        for (int i = 0; i < col - 1; i++) {
            sb.append(" ");
        }
        sb.append("↑");
        sb.append("\nSyntaxError:");
        sb.append(message);
        sb.append("\n");
        return sb.toString();
    }
}
