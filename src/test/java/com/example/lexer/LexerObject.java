package com.example.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.example.token.Token;
import com.example.token.TokenType;

@RunWith(Parameterized.class)
public class LexerObject {
    private Token token;
    private String input;

    @Parameterized.Parameters
    public static List<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "1", new Token(TokenType.Number, 1, 1, 1.0) },
                { "1.0", new Token(TokenType.Number, 1, 1, 1.0) },
                { "true", new Token(TokenType.True, 1, 1, null) },
                { "false", new Token(TokenType.False, 1, 1, null) },
                { "null", new Token(TokenType.Null, 1, 1, null) },
        });
    }

    public LexerObject(String input, Token token) {
        this.input = input;
        this.token = token;
    }

    @Test
    public void testObject() {
        Lexer lexer = new Lexer(input);
        try {
            Token t = lexer.next();
            assertEquals(token.toString(), t.toString());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

}
