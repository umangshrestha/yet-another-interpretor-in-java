package com.example.interpreter;

import java.util.List;

import com.example.ast.Stmt;
import com.example.environment.Environment;

public class CustomFunction implements CustomCallable {
    Environment closure;
    Boolean isInitializer = false;
    Stmt.Function declaration;

    CustomFunction(Stmt.Function declaration, Environment closure, Boolean isInitializer) {
        this.closure = closure;
        this.isInitializer = isInitializer;
        this.declaration = declaration;
    }

    CustomFunction bind(CustomInstance instance) {
        Environment environment = new Environment(closure);
        environment.define("this", instance);
        return new CustomFunction(declaration, environment, isInitializer);
    }

    @Override
    public int arity() {
        return declaration.getParams().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.getParams().size(); i++) {
            environment.declare(
                    declaration.getParams().get(i),
                    arguments.get(i));
        }
        try {
            interpreter.executeBlock(
                    declaration.getBody().getStmts(),
                    environment);
        } catch (CustomError.Return returnValue) {
            if (isInitializer) {
                return closure.getAt(0, "this");
            }
            return returnValue.getValue();
        }
        if (isInitializer) {
            return closure.getAt(0, "this");
        }
        return null;
    }

}
