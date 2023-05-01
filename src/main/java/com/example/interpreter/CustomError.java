package com.example.interpreter;

public abstract class CustomError extends RuntimeException {
    private final Object value;

    public CustomError(Object value) {
        super(null, null, false, false);
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public static class Return extends CustomError {
        public Return(Object value) {
            super(value);
        }
    }

    public static class Break extends CustomError {
        public Break(Object value) {
            super(value);
        }
    }

    public static class Continue extends CustomError {
        public Continue(Object value) {
            super(value);
        }
    }

}
