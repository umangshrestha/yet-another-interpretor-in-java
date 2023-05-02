package com.example.interpreter;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CustomClass implements CustomCallable {
    private final String name;
    private final CustomClass superclass;
    private final Map<String, CustomFunction> methods;

    public static final String constructor = "constructor";

    public CustomFunction findMethod(CustomInstance instance, String name) {
        if (methods.containsKey(name))
            return methods.get(name).bind(instance);
        if (superclass != null)
            return superclass.findMethod(instance, name);
        return null;
    }

    @Override
    public int arity() {
        CustomFunction initializer = methods.get(constructor);
        return (initializer == null) ? 0 : initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        CustomInstance instance = new CustomInstance(this);
        CustomFunction initializer = methods.get(constructor);
        if (initializer != null)
            initializer.bind(instance).call(interpreter, arguments);
        return instance;
    }

}
