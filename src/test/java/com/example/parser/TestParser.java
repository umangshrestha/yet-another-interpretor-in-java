package com.example.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.example.ast.Program;
import com.example.lexer.Lexer;

@RunWith(Parameterized.class)
public class TestParser {

    private String input;
    private String expected;

    public TestParser(String input, String expected) {
        this.input = input;
        this.expected = expected;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { "[1,2,3];", "[[1.0, 2.0, 3.0]]" },
                // The map is not ordered, so the expected result is not ordered
                // { "{1:2, 3:\"4\", \"5\":6};", "[{5=6.0, 1.0=2.0, 3.0=4}]" },
                { "3;", "[3.0]" },
                { "let a = 3*1/3;", "[let(a, Multiply(3.0, Divide(1.0, 3.0)))]" },
                { "let a = 1*4 + 2*3;", "[let(a, Plus(Multiply(1.0, 4.0), Multiply(2.0, 3.0)))]" },
                { "if (!true) { print(123); }else { b=3; }", "[if(Not(true), [print[123.0]], [Assign(b, 3.0)])]" },
                { "fn square(a) { return a^2;}", "[square[a] [return(Exponent(a, 2.0))])]" },
                { "square(3);", "[square[3.0]]" },
                { "(300 >= 20)||1<20;",
                        "[Or(GreaterThanOrEqual(300.0, 20.0), LessThan(1.0, 20.0))]" },
        });
    }

    @Test
    public void testParser() {
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.getProgram();
        if (program == null) {
            fail("Parser returned null");
        }
        String actual = program.toString();
        assertEquals(input, expected, actual);
    }
}
