package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;

import com.example.ast.Program;
import com.example.environment.RuntimeError;
import com.example.interpreter.Interpreter;
import com.example.lexer.Lexer;
import com.example.parser.Parser;
import com.example.token.Token;

public class App {
    private static boolean hasError = false;
    private static String fileName = "<stdin>";

    public static String getfileName() {
        return fileName;
    }

    public static boolean hasError() {
        return hasError;
    }

    public static void setHasError() {
        hasError = true;
    }

    private static void runFile(String fileName) {
        Interpreter interpreter = new Interpreter();
        String input;
        try {
            input = Files.readString(Paths.get(fileName));
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }
        Lexer lexer = new Lexer(input);
        Parser parser = new Parser(lexer);
        Program program = parser.getProgram();
        if (hasError()) {
            System.exit(65);
        }
        try {
            interpreter.interpret(program);
        } catch (RuntimeError e) {
            Token token = e.getToken();
            String line = input.split("\n")[token.getLine() - 1];
            System.out.println(getErrorMessage(token.getLine(), token.getCol(), e.getMessage(), line));
        }
    }

    private static void runPrompt() {
        Interpreter interpreter = new Interpreter();
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("> ");
                String input = scanner.nextLine();
                Lexer lexer = new Lexer(input);
                Parser parser = new Parser(lexer);
                if (hasError()) {
                    continue;
                }
                try {
                    interpreter.interpret(parser.getProgram());
                } catch (RuntimeError e) {
                    Token token = e.getToken();
                    System.out.println(getErrorMessage(token.getLine(), token.getCol(), e.getMessage(), input));
                } finally {
                    hasError = false;
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            fileName = args[0];
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    public static String getErrorMessage(int line, int col, String message, String lineText) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("File \"%s\", line %d, col %d\n%s\n", fileName, line, col, lineText));
        for (int i = 0; i < col - 1; i++) {
            sb.append(" ");
        }
        sb.append("↑");
        sb.append("\nRuntimeError:");
        sb.append(message);
        return sb.toString();
    }
}
