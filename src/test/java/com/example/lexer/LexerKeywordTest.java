package com.example.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.example.token.Token;
import com.example.token.TokenType;

@RunWith(Parameterized.class)
public class LexerKeywordTest {
    private String input;
    private TokenType expectedType;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "true", TokenType.True },
                { "false", TokenType.False },
                { "null", TokenType.Null },
                { "for", TokenType.For },
                { "while", TokenType.While },
                { "this", TokenType.This },
                { "super", TokenType.Super },
                { "return", TokenType.Return },
                { "fn", TokenType.Function },

        });
    }

    public LexerKeywordTest(String input, TokenType expectedType) {
        this.input = input;
        this.expectedType = expectedType;
    }

    @Test
    public void testSingleToken() {
        Token expected = new Token(expectedType, 1, 1, input);
        Lexer l = new Lexer(input);
        try {
            Token tok = l.next();
            assertEquals(tok, expected);

            tok = new Token(TokenType.Eof, 1, input.length() + 1);
        } catch (LexerError e) {
            fail(e.getMessage());
        }
    }
}