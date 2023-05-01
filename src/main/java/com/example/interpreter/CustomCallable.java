package com.example.interpreter;

import java.util.List;

public interface CustomCallable {
    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);
}
