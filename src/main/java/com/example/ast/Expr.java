package com.example.ast;

import java.util.List;

import com.example.token.Token;

import lombok.AllArgsConstructor;
import lombok.Getter;

public abstract class Expr {
    abstract public Object accept(ExprVisitor v);

    @Getter
    @AllArgsConstructor
    public static class Assign extends Expr {
        private final Token name;
        private final Expr value;

        public Object accept(ExprVisitor v) {
            return v.visitAssignExpr(this);
        }

        public String toString() {
            return String.format("Assign(%s, %s)", name.getLiteral(), value);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Binary extends Expr {
        private final Expr left;
        private final Token operator;
        private final Expr right;

        public Object accept(ExprVisitor v) {
            return v.visitBinaryExpr(this);
        }

        public String toString() {
            return String.format("%s(%s, %s)", operator.getType(), left, right);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Call extends Expr {
        private final Expr callee;
        private final Token paren;
        private final List<Expr> arguments;

        public Object accept(ExprVisitor v) {
            return v.visitCallExpr(this);
        }

        public String toString() {
            return String.format("%s%s", callee, arguments);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Get extends Expr {
        private final Expr object;
        private final Token name;
        private final Token operator;

        public Object accept(ExprVisitor v) {
            return v.visitGetExpr(this);
        }

        public String toString() {
            return String.format("%s.%s", object, name);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Grouping extends Expr {
        private final Expr expression;

        public Object accept(ExprVisitor v) {
            return v.visitGroupingExpr(this);
        }

        public String toString() {
            return String.format("%s", expression);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Literal extends Expr {
        private final Object value;

        public Object accept(ExprVisitor v) {
            return v.visitLiteralExpr(this);
        }

        public String toString() {
            return String.format("%s", value);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Set extends Expr {
        private final Expr object;
        private final Token name;
        private final Expr value;
        private final Token operator;

        public Object accept(ExprVisitor v) {
            return v.visitSetExpr(this);
        }

        public String toString() {
            return String.format("set(%s.%s, %s)", object, name, value);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Super extends Expr {
        private final Token keyword;
        private final Token method;

        public Object accept(ExprVisitor v) {
            return v.visitSuperExpr(this);
        }

        public String toString() {
            return String.format("super(%s)", method);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class This extends Expr {
        private final Token keyword;

        public Object accept(ExprVisitor v) {
            return v.visitThisExpr(this);
        }

        public String toString() {
            return String.format("this.%s", keyword.getLiteral());
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Unary extends Expr {
        private final Token operator;
        private final Expr right;

        public Object accept(ExprVisitor v) {
            return v.visitUnaryExpr(this);
        }

        public String toString() {
            return String.format("%s(%s)", operator.getType(), right);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Variable extends Expr {
        private final Token name;

        public Object accept(ExprVisitor v) {
            return v.visitVariableExpr(this);
        }

        public String toString() {
            return String.format("%s", name.getLiteral());
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Array extends Expr {
        private final List<Expr> values;

        public Object accept(ExprVisitor v) {
            return v.visitArrayExpr(this);
        }

        public String toString() {
            return String.format("%s", values);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Map extends Expr {
        private final java.util.Map<Expr, Expr> values;

        public Object accept(ExprVisitor v) {
            return v.visitMapExpr(this);
        }

        public String toString() {
            return String.format("%s", values);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class GetIndex extends Expr {
        private final Expr object;
        private final Expr index;
        private final Token operator;

        public Object accept(ExprVisitor v) {
            return v.visitGetIndexExpr(this);
        }

        public String toString() {
            return String.format("%s[%s]", object, index);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class SetIndex extends Expr {
        private final Expr object;
        private final Expr index;
        private final Expr value;
        private final Token operator;

        public Object accept(ExprVisitor v) {
            return v.visitSetIndexExpr(this);
        }

        public String toString() {
            return String.format("set(%s[%s], %s)", object, index, value);
        }
    }
}