package com.example.lexer;

import java.util.HashMap;

import com.example.App;
import com.example.token.Token;
import com.example.token.TokenType;

public class Lexer {
    private char[] input;
    private int line = 1;
    private int lineStart = 0;
    private int pos = 0;

    private static HashMap<String, TokenType> keywords = new HashMap<String, TokenType>();
    static {
        keywords.put("fn", TokenType.Function);
        keywords.put("let", TokenType.Let);
        keywords.put("if", TokenType.If);
        keywords.put("else", TokenType.Else);
        keywords.put("return", TokenType.Return);
        keywords.put("while", TokenType.While);
        keywords.put("for", TokenType.For);
        keywords.put("class", TokenType.Class);
        keywords.put("true", TokenType.True);
        keywords.put("false", TokenType.False);
        keywords.put("null", TokenType.Null);
        keywords.put("this", TokenType.This);
        keywords.put("super", TokenType.Super);
        keywords.put("break", TokenType.Break);
        keywords.put("continue", TokenType.Continue);
    }

    public Lexer(String input, String filename) {
        this.input = input.toCharArray();
    }

    public Lexer(String input) {
        this(input, "<stdin>");
    }

    public Token nextToken() {
        while (true) {
            try {
                return next();
            } catch (LexerError e) {
                App.setHasError();
                System.out.println(e.getMessage());
            }
        }
    }

    public Token next() throws LexerError {
        TokenType type = null;
        char c = advance();
        int col = getColumn();
        switch (c) {
            case 0:
                type = TokenType.Eof;
                break;
            case '\n':
            case ' ':
            case '\t':
            case '\r':
                removeWhitespace();
                return next();
            case '"':
                return multiLineString();
            case '\'':
                return string();
            case '+':
                type = match('=') ? TokenType.PlusAssign : TokenType.Plus;
                break;
            case '-':
                type = match('=') ? TokenType.MinusAssign : TokenType.Minus;
                break;
            case '*':
                type = match('=') ? TokenType.MultiplyAssign : TokenType.Multiply;
                break;
            case '/':
                if (match('/')) {
                    removeComment();
                    return next();
                } else if (match('*')) {
                    removeMultilineComment();
                    return next();
                }
                type = match('=') ? TokenType.DivideAssign : TokenType.Divide;
                break;
            case '%':
                type = match('=') ? TokenType.ModuloAssign : TokenType.Modulo;
                break;
            case '^':
                type = match('=') ? TokenType.ExponentAssign : TokenType.Exponent;
                break;
            case '&':
                type = match('&') ? TokenType.And : match('=') ? TokenType.BitwiseAndAssign : TokenType.BitwiseAnd;
                break;
            case '|':
                type = match('|') ? TokenType.Or : match('=') ? TokenType.BitwiseOrAssign : TokenType.BitwiseOr;
                break;
            case '~':
                type = TokenType.BitwiseNot;
                break;
            case '<':
                type = match('=') ? TokenType.LessThanOrEqual : match('<') ? TokenType.LeftShift : TokenType.LessThan;
                break;
            case '>':
                type = match('=') ? TokenType.GreaterThanOrEqual
                        : match('>') ? TokenType.RightShift : TokenType.GreaterThan;
                break;
            case '!':
                type = match('=') ? TokenType.NotEqual : TokenType.Not;
                break;
            case '=':
                type = match('=') ? TokenType.Equal : TokenType.Assign;
                break;
            case '(':
                type = TokenType.LeftParen;
                break;
            case ')':
                type = TokenType.RightParen;
                break;
            case '{':
                type = TokenType.LeftBrace;
                break;
            case '}':
                type = TokenType.RightBrace;
                break;
            case '[':
                type = TokenType.LeftBracket;
                break;
            case ']':
                type = TokenType.RightBracket;
                break;
            case ',':
                type = TokenType.Comma;
                break;
            case '.':
                type = TokenType.Dot;
                break;
            case ':':
                type = TokenType.Colon;
                break;
            case ';':
                type = TokenType.Semicolon;
                break;
            default:
                if (isDigit(c))
                    return number();
                else if (isAlpha(c))
                    return identifier();
                else
                    throw new LexerError(
                            String.format("unepected characted '%c'", c),
                            line,
                            col,
                            getCurrentLine());
        }
        return new Token(type, line, col);
    }

    public String getCurrentLine() {
        int start = lineStart;
        int end = pos;
        while (end < input.length && input[end] != '\n')
            end++;
        return new String(input, start, end - start);
    }

    private boolean isEof() {
        return pos >= input.length;
    }

    private char peek() {
        return isEof() ? 0 : input[pos];
    }

    private Character advance() {
        if (isEof()) {
            return 0;
        }
        char c = input[pos++];
        if (c == '\n') {
            line++;
            lineStart = pos;
        }
        return c;
    }

    private boolean match(char c) {
        if (peek() == c) {
            advance();
            return true;
        }
        return false;
    }

    private void removeWhitespace() {
        while (isWhitespace(peek()))
            advance();
    }

    private boolean isWhitespace(char c) {
        return c == ' ' || c == '\t' || c == '\r' || c == '\n';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isFloat(char c) {
        return c == '.' || isDigit(c);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private int getColumn() {
        return pos - lineStart;
    }

    private Token identifier() {
        int col = getColumn();
        int start = pos - 1;
        while (isAlphaNumeric(peek()))
            advance();
        String text = new String(input, start, pos - start);
        TokenType type = keywords.get(text);
        if (type == null)
            return new Token(TokenType.Identifier, line, col, text);
        return new Token(type, line, col);
    }

    private void removeComment() {
        while (peek() != '\n' && !isEof())
            advance();
    }

    private void removeMultilineComment() throws LexerError {
        while (true) {
            if (match('*')) {
                if (match('/'))
                    break;
            }
            if (isEof())
                throw new LexerError("Unterminated multiline comment", line, pos - lineStart, getCurrentLine());
            advance();
        }
    }

    private Token multiLineString() throws LexerError {
        int start = pos - 1;
        int line = this.line;
        int col = getColumn();
        while (peek() != '"' && !isEof()) {
            if (peek() == '\n') {
                line++;
            }
            if (!match('\\') || !match('\"')) {
                advance();
            }
        }
        if (isEof())
            throw new LexerError("Unterminated string", line, pos - start, getCurrentLine());
        String sb = new String(input, start + 1, pos - start - 1);
        advance();
        return new Token(TokenType.String, line, col, sb.toString());
    }

    private Token string() throws LexerError {
        int start = pos - 1;
        int line = this.line;
        int col = getColumn();
        while (peek() != '\'' && !isEof()) {
            if (peek() == '\n') {
                line++;
            }
            if (match('\\') && peek() == '\'') {
                advance();
            }
            advance();
        }
        if (isEof())
            throw new LexerError("Unterminated string", line, pos - start, getCurrentLine());

        String str = new String(input, start + 1, pos - start - 1);
        advance();
        return new Token(TokenType.String, line, col, str);
    }

    private Token number() throws LexerError {
        int col = getColumn();
        int start = pos - 1;
        while (isAlphaNumeric(peek())) {
            advance();
        }
        if (peek() == '.' && isFloat(peek())) {
            advance();
            while (isFloat(peek())) {
                advance();
            }
        }
        String text = new String(input, start, pos - start);
        try {
            Double f = Double.parseDouble(text);
            return new Token(TokenType.Number, line, col, f);
        } catch (NumberFormatException e) {
            throw new LexerError("Invalid number", line, col, getCurrentLine());
        }
    }
}
