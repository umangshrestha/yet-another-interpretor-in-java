package com.example.ast;

import java.util.List;

import com.example.token.Token;

import lombok.AllArgsConstructor;
import lombok.Getter;

public abstract class Stmt {

    public abstract void accept(StmtVisitor v);

    @Getter
    @AllArgsConstructor
    public static class Block extends Stmt {
        private final List<Stmt> stmts;

        public void accept(StmtVisitor v) {
            v.visitBlockStmt(this);
        }

        public String toString() {
            return String.format("%s", stmts);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Class extends Stmt {
        private final Token name;
        private final Expr superclass;
        private final List<Stmt.Function> methods;

        public void accept(StmtVisitor v) {
            v.visitClassStmt(this);
        }

        public String toString() {
            if (superclass == null)
                return String.format("(class %s %s)", name, methods);
            return String.format("(class %s < %s %s)", name, superclass, methods);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class ExprStmt extends Stmt {
        private final Expr expression;

        public void accept(StmtVisitor v) {
            v.visitExprStmt(this);
        }

        public String toString() {
            return expression.toString();
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Function extends Stmt {
        private final Token name;
        private final List<Token> params;
        private final Stmt.Block body;

        public void accept(StmtVisitor v) {
            v.visitFunctionStmt(this);
        }

        public String toString() {
            List<Object> params = this.params.stream().map(p -> p.getLiteral()).toList();
            return String.format("%s%s %s)", name.getLiteral(), params, body);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class If extends Stmt {
        private final Expr condition;
        private final Stmt thenBranch;
        private final Stmt elseBranch;

        public void accept(StmtVisitor v) {
            v.visitIfStmt(this);
        }

        public String toString() {
            return String.format("if(%s, %s, %s)", condition, thenBranch, elseBranch);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Return extends Stmt {
        private final Token keyword;
        private final Expr value;

        public void accept(StmtVisitor v) {
            v.visitReturnStmt(this);
        }

        public String toString() {
            return String.format("return(%s)", value);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Let extends Stmt {
        private final Token name;
        private final Expr initializer;

        public void accept(StmtVisitor v) {
            v.visitLetStmt(this);
        }

        public String toString() {
            return String.format("let(%s, %s)", name.getLiteral(), initializer);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class While extends Stmt {
        private final Expr condition;
        private final Stmt body;

        public void accept(StmtVisitor v) {
            v.visitWhileStmt(this);
        }

        public String toString() {
            return String.format("(while %s %s)", condition, body);
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Break extends Stmt {
        private final Token keyword;

        public void accept(StmtVisitor v) {
            v.visitBreakStmt(this);
        }

        public String toString() {
            return String.format("break");
        }
    }

    @Getter
    @AllArgsConstructor
    public static class Continue extends Stmt {
        private final Token keyword;

        public void accept(StmtVisitor v) {
            v.visitContinueStmt(this);
        }

        public String toString() {
            return String.format("continue");
        }
    }

}
