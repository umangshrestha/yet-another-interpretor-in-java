package com.example.lexer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.example.token.Token;
import com.example.token.TokenType;

@DisplayName("Lexer DataType Test by @ValueSource")
public class LexerStringTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "\"\"",
            "\"a\"",
            "\" \"",
            "\"Hello, World!\"",
            "\"Hello, \\\"World!\\\"\"",
            "\"Hello, \\\\World!\\\\\"",
            "\"Hello, \\nWorld!\\n\"",
            "\"Hello, \\tWorld!\\t\"",
            "\"Hello, \\bWorld!\\b\"",
            "\"Hello, \\rWorld!\\r\"",
            "\"Hello, \\fWorld!\\f\"",
            "\"Hello, \\u0021World!\\u0021\"",
            "\"Hello, \\u0021World!\\u0021\"",
    })
    public void testMultiLineString(String input) {
        try {
            Lexer lexer = new Lexer(input);
            Token token = lexer.next();
            String expectedString = input.substring(1, input.length() - 1); // remove double quotes
            Token expected = new Token(TokenType.String, 1, 1, expectedString);
            assertEquals(String.format("Input: %s", input), expected, token);
        } catch (LexerError e) {
            fail(e.getMessage());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "'a'",
            "' '",
            "'\\n'",
            "'\\t'",
            "'\\b'",
            "'Hello, World!'",
            "'Hello, \"World!\"'",
            "'Hello, \\\\World!\\\\'",
    })
    public void testString(String input) {
        try {
            Lexer lexer = new Lexer(input);
            Token token = lexer.next();
            String expectedString = input.substring(1, input.length() - 1); // remove double quotes
            Token expected = new Token(TokenType.String, 1, 1, expectedString);
            assertEquals(String.format("Input: %s", input), expected, token);
        } catch (LexerError e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testUnterminatedString() {
        String input = "\"Hello, World!";
        try {
            Lexer lexer = new Lexer(input);
            lexer.next();
            fail("Should throw SyntaxException");
        } catch (LexerError e) {
            LexerError expected = new LexerError("Unterminated string", 1, 14, input);
            assertEquals(e.toString(), expected.toString());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "a",
            "apple",
            "new_variable",
            "newVariable",
            "newVariable123",
            "NewVariable123",
            "new_variable_123",
            "new_variable_123_",
    })
    public void testValidIdentifier(String input) {
        try {
            Lexer lexer = new Lexer(input);
            Token tok = lexer.next();
            Token expected = new Token(TokenType.Identifier, 1, 1, input);
            assertEquals(String.format("Input: %s", input), expected, tok);
        } catch (LexerError e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testInvalidIdentifier() {
        String input = "123abc";
        try {
            Lexer lexer = new Lexer(input);
            lexer.next();
            fail("Should throw SyntaxException");
        } catch (LexerError e) {
            LexerError expected = new LexerError("Invalid number", 1, 1, input);
            assertEquals(e.toString(), expected.toString());
        }
    }

}
