package com.example.interpretor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.example.App;
import com.example.ast.Program;
import com.example.interpreter.Interpreter;
import com.example.lexer.Lexer;
import com.example.parser.Parser;

@RunWith(Parameterized.class)
public class InterpretorTest {
    private String input;
    private Object expected;

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "let a = 10 * 20; a += 12;", 212.0 },
                { "let a = (1+2*3);", 7.0 },
                { "let a = 'Hello, ' + 'World!';", "Hello, World!" },
                { "let a = \"Hello, \" + \"World!\";", "Hello, World!" },
                { "let a = true;", true },
                { "let a = bool(1) || false || bool(20);", true },
                { "let a = 100>120;", false },
        });
    }

    public InterpretorTest(String input, Object expected) {
        this.input = input;
        this.expected = expected;
    }

    @Test
    public void testVisitExpression() {
        Interpreter interpreter = new Interpreter();
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.getProgram();
        if (App.hasError()) {
            fail("Parser has error");
        }
        try {
            interpreter.interpret(program);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        Object actual = interpreter.getEnvironment().deleteAt(0, "a");
        assertEquals(input, expected, actual);
    }
}
