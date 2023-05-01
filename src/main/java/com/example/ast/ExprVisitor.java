package com.example.ast;

public interface ExprVisitor {
    Object visitAssignExpr(Expr.Assign expr);

    Object visitBinaryExpr(Expr.Binary expr);

    Object visitCallExpr(Expr.Call expr);

    Object visitGetExpr(Expr.Get expr);

    Object visitSetExpr(Expr.Set expr);

    Object visitGetIndexExpr(Expr.GetIndex expr);

    Object visitSetIndexExpr(Expr.SetIndex expr);

    Object visitGroupingExpr(Expr.Grouping expr);

    Object visitLiteralExpr(Expr.Literal expr);

    Object visitSuperExpr(Expr.Super expr);

    Object visitThisExpr(Expr.This expr);

    Object visitUnaryExpr(Expr.Unary expr);

    Object visitVariableExpr(Expr.Variable expr);

    Object visitArrayExpr(Expr.Array expr);

    Object visitMapExpr(Expr.Map expr);

}