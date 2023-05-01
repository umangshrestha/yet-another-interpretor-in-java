package com.example.ast;

public interface StmtVisitor {
    void visitBlockStmt(Stmt.Block stmt);

    void visitClassStmt(Stmt.Class stmt);

    void visitExprStmt(Stmt.ExprStmt stmt);

    void visitFunctionStmt(Stmt.Function stmt);

    void visitIfStmt(Stmt.If stmt);

    void visitReturnStmt(Stmt.Return stmt);

    void visitLetStmt(Stmt.Let stmt);

    void visitWhileStmt(Stmt.While stmt);

    void visitBreakStmt(Stmt.Break expr);

    void visitContinueStmt(Stmt.Continue expr);
}
