package com.example.interpreter;

import java.util.HashMap;
import java.util.Map;

import com.example.environment.RuntimeError;
import com.example.token.Token;

public class CustomInstance {
    private CustomClass customClass;
    private final Map<String, Object> fields = new HashMap<>();

    CustomInstance(CustomClass customClass) {
        this.customClass = customClass;
    }

    Object get(Token name) {
        if (fields.containsKey(name.getLiteral())) {
            return fields.get(name.getLiteral());
        }
        CustomFunction method = customClass.findMethod(this, (String) name.getLiteral());
        if (method != null)
            return method.bind(this);
        throw new RuntimeError(
                String.format("Undefined property '%s'", name.getLiteral()),
                name);
    }

    void set(Token name, Object value) {
        fields.put((String) name.getLiteral(), value);
    }

    @Override
    public String toString() {
        return String.format("<instance:\"%s>", customClass);
    }
}
