package com.example.interpreter;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class builtIn {

    private final static String nativeFn = "<native fn>";

    public static class Clock implements CustomCallable {
        @Override
        public int arity() {
            return 0;
        }

        @Override
        public String toString() {
            return nativeFn;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            return (double) System.currentTimeMillis() / 1000.0;
        }
    }

    public static class Print implements CustomCallable {
        @Override
        public int arity() {
            return -1;
        }

        @Override
        public String toString() {
            return nativeFn;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            StringBuilder sb = new StringBuilder();
            for (Object obj : arguments) {
                if (obj == null) {
                    sb.append("null");
                    sb.append(" ");
                    continue;
                }
                sb.append(obj.toString());
                sb.append(" ");
            }
            System.out.println(sb.toString());
            return null;
        }
    }

    public static class TypeOf implements CustomCallable {
        @Override
        public int arity() {
            return 1;
        }

        @Override
        public String toString() {
            return nativeFn;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object obj = arguments.get(0);
            if (obj == null) {
                return "null";
            }
            return obj.getClass().getSimpleName();
        }

    }

    public static class Exit implements CustomCallable {
        @Override
        public int arity() {
            return 0;
        }

        @Override
        public String toString() {
            return nativeFn;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            System.exit(0);
            return null;
        }
    }

    public static class Input implements CustomCallable {
        @Override
        public int arity() {
            return 0;
        }

        @Override
        public String toString() {
            return nativeFn;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            String input = "";
            try (Scanner scanner = new Scanner(System.in)) {
                input = scanner.nextLine();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            return input;
        }
    }

    public static class Bool implements CustomCallable {
        @Override
        public int arity() {
            return 1;
        }

        @Override
        public String toString() {
            return nativeFn;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object obj = arguments.get(0);
            return interpreter.bool(obj);
        }
    }

    public static class Length implements CustomCallable {
        @Override
        public int arity() {
            return 1;
        }

        @Override
        public String toString() {
            return nativeFn;
        }

        @Override
        public Object call(Interpreter interpreter, List<Object> arguments) {
            Object obj = arguments.get(0);
            if (obj instanceof String) {
                return (double) ((String) obj).length();
            }
            if (obj instanceof List) {
                return (double) ((List) obj).size();
            }

            if (obj instanceof Map) {
                return (double) ((Map) obj).size();
            }
            return null;
        }
    }
}
