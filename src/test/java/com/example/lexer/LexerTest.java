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
public class LexerTest {
    private String input;
    private TokenType expectedType;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "+", TokenType.Plus },
                { "-", TokenType.Minus },
                { "/", TokenType.Divide },
                { "*", TokenType.Multiply },
                { "^", TokenType.Exponent },
                { "!", TokenType.Not },
                { "[", TokenType.LeftBracket },
                { "]", TokenType.RightBracket },
                { "{", TokenType.LeftBrace },
                { "}", TokenType.RightBrace },
                { "(", TokenType.LeftParen },
                { ")", TokenType.RightParen },
                { "&", TokenType.BitwiseAnd },
                { "|", TokenType.BitwiseOr },
                { "&&", TokenType.And },
                { "||", TokenType.Or },
                { "=", TokenType.Assign },
                { "==", TokenType.Equal },
                { "+=", TokenType.PlusAssign },
                { "-=", TokenType.MinusAssign },
                { ">>", TokenType.RightShift },
                { "<<", TokenType.LeftShift },
                { ">", TokenType.GreaterThan },
                { ">=", TokenType.GreaterThanOrEqual },
                { "<", TokenType.LessThan },
                { "<=", TokenType.LessThanOrEqual },
                { "/=", TokenType.DivideAssign },
                { "*=", TokenType.MultiplyAssign },
                { "^=", TokenType.ExponentAssign },
                { "!=", TokenType.NotEqual },
        });
    }

    public LexerTest(String input, TokenType expectedType) {
        this.input = input;
        this.expectedType = expectedType;
    }

    @Test
    public void testSingleToken() {
        Token expected = new Token(expectedType, 1, 1);
        Lexer l = new Lexer(input);
        try {
            Token tok = l.next();
            assertEquals(tok, expected);

            tok = new Token(TokenType.Eof, 1, input.length() + 1);
        } catch (LexerError e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSingleTokenWithComment() {
        Token expected = new Token(expectedType, 2, 16);
        Lexer l = new Lexer(String.format("\n/*1234567890*/ %s // comment", input));
        try {
            Token tok = l.next();
            assertEquals(tok, expected);
        } catch (LexerError e) {
            fail(e.getMessage());
        }
    }
}
