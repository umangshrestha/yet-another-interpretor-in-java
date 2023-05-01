package com.example.environment;

import java.util.HashMap;
import java.util.Map;

import com.example.token.Token;

public class Environment {
    private final Map<String, Object> values = new HashMap<>();
    private final Environment enclosing;

    public Environment() {
        enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public Environment getEnclosing() {
        return enclosing;
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public Object get(Token name) {
        if (values.containsKey(name.getLiteral())) {
            return values.get(name.getLiteral());
        }
        throw new RuntimeError(
                String.format("Undefined variable '%s'", name.getLiteral()),
                name);
    }

    public void declare(Token name, Object value) {
        if (values.containsKey(name.getLiteral())) {
            throw new RuntimeError(
                    String.format("Variable '%s' already declared",
                            name.getLiteral()),
                    name);
        }
        values.put((String) name.getLiteral(), value);
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.getLiteral())) {
            values.put((String) name.getLiteral(), value);
            return;
        }
        throw new RuntimeError(
                String.format("Undefined variable '%s' cannot be updated", name.getLiteral()),
                name);
    }

    public Object getAt(int distance, String name) {
        return ancestor(distance).values.get(name);
    }

    public void assignAt(int distance, Token name, Object value) {
        ancestor(distance).values.put((String) name.getLiteral(), value);
    }

    private Environment ancestor(int distance) {
        Environment environment = this;
        for (int i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }
        return environment;
    }

    public Object deleteAt(int distance, String name) {
        Environment environment = ancestor(distance);
        if (environment.values.containsKey(name)) {
            return environment.values.remove(name);
        }
        throw new RuntimeError(
                String.format("Undefined variable '%s' cannot be deleted", name),
                null);
    }
}
