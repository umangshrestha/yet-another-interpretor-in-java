package com.example.environment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import com.example.ast.Expr.Array;
import com.example.ast.Expr.Assign;
import com.example.ast.Expr.Binary;
import com.example.ast.Expr.Call;
import com.example.ast.Expr.Get;
import com.example.ast.Expr.GetIndex;
import com.example.ast.Expr.Grouping;
import com.example.ast.Expr.Literal;
import com.example.ast.Expr.Set;
import com.example.ast.Expr.SetIndex;
import com.example.ast.Expr.Super;
import com.example.ast.Expr.This;
import com.example.ast.Expr.Unary;
import com.example.ast.Expr.Variable;
import com.example.ast.Expr;
import com.example.ast.ExprVisitor;
import com.example.ast.Stmt;
import com.example.ast.Stmt.Block;
import com.example.ast.Stmt.Break;
import com.example.ast.Stmt.Class;
import com.example.ast.Stmt.Continue;
import com.example.ast.Stmt.ExprStmt;
import com.example.ast.Stmt.Function;
import com.example.ast.Stmt.If;
import com.example.ast.Stmt.Let;
import com.example.ast.Stmt.Return;
import com.example.ast.Stmt.While;
import com.example.token.Token;
import com.example.ast.StmtVisitor;

public class Resolver implements ExprVisitor, StmtVisitor {
    private final Stack<Map<String, Boolean>> scopes = new Stack<>();
    private final Map<Expr, Integer> locals = new HashMap<>();

    private enum FunctionType {
        NONE, FUNCTION, METHOD, INITIALIZER
    }

    private FunctionType currentFunction = FunctionType.NONE;

    private enum ClassType {
        NONE, CLASS, SUBCLASS
    }

    private ClassType currentClass = ClassType.NONE;

    private LoopType currentLoop = LoopType.NONE;

    private enum LoopType {
        NONE, LOOP
    }

    public void resolve(List<Stmt> stmts) {
        for (Stmt stmt : stmts)
            stmt.accept(this);
    }

    public int getDepth(Expr expr) {
        if (!locals.containsKey(expr))
            return -1;
        return locals.get(expr);
    }

    @Override
    public void visitBlockStmt(Block stmt) {
        beginScope();
        resolve(stmt.getStmts());
        endScope();
    }

    @Override
    public void visitClassStmt(Class stmt) {
        declareVariable(stmt.getName());
        defineVariable(stmt.getName());

        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        if (stmt.getSuperclass() != null) {
            currentClass = ClassType.SUBCLASS;
            stmt.getSuperclass().accept(this);
            beginScope();
            scopes.peek().put("super", true);
        }

        beginScope();
        scopes.peek().put("this", true);

        for (Function method : stmt.getMethods()) {
            FunctionType declaration = FunctionType.METHOD;
            if (method.getName().getLiteral().equals("init"))
                declaration = FunctionType.INITIALIZER;
            resolveFunction(method, declaration);
        }
        if (stmt.getSuperclass() != null)
            endScope();

        currentClass = enclosingClass;
    }

    void resolveFunction(Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (Token param : function.getParams()) {
            declareVariable(param);
            defineVariable(param);
        }
        function.getBody().accept(this);
        endScope();
        currentFunction = enclosingFunction;
    }

    @Override
    public void visitFunctionStmt(Stmt.Function stmt) {
        declareVariable(stmt.getName());
        defineVariable(stmt.getName());
        resolveFunction(stmt, FunctionType.FUNCTION);
    }

    @Override
    public void visitExprStmt(ExprStmt stmt) {
        stmt.getExpression().accept(this);
    }

    @Override
    public void visitIfStmt(If stmt) {
        stmt.getCondition().accept(this);
        stmt.getThenBranch().accept(this);
        if (stmt.getElseBranch() != null)
            stmt.getElseBranch().accept(this);
    }

    @Override
    public void visitReturnStmt(Return stmt) {
        if (currentFunction == FunctionType.NONE)
            throw new RuntimeError("Cannot return from top-level code.", stmt.getKeyword());
        if (stmt.getValue() != null) {
            if (currentFunction == FunctionType.INITIALIZER)
                throw new RuntimeError("Cannot return a value from an initializer.", stmt.getKeyword());
            stmt.getValue().accept(this);
        }
    }

    @Override
    public void visitLetStmt(Let stmt) {
        declareVariable(stmt.getName());
        if (stmt.getInitializer() != null)
            stmt.getInitializer().accept(this);
        defineVariable(stmt.getName());
    }

    @Override
    public void visitWhileStmt(While stmt) {
        LoopType enclosingLoop = currentLoop;
        currentLoop = LoopType.LOOP;
        stmt.getCondition().accept(this);
        stmt.getBody().accept(this);
        currentLoop = enclosingLoop;
    }

    @Override
    public Object visitAssignExpr(Assign expr) {
        expr.getValue().accept(this);
        resolveLocal(expr, expr.getName());
        return null;
    }

    @Override
    public Object visitBinaryExpr(Binary expr) {
        expr.getLeft().accept(this);
        expr.getRight().accept(this);
        return null;
    }

    @Override
    public Object visitCallExpr(Call expr) {
        expr.getCallee().accept(this);
        for (Expr arg : expr.getArguments())
            arg.accept(this);
        return null;
    }

    @Override
    public Object visitGetExpr(Get expr) {
        return expr.getObject().accept(this);
    }

    @Override
    public Object visitGroupingExpr(Grouping expr) {
        return expr.getExpression().accept(this);
    }

    @Override
    public Object visitLiteralExpr(Literal expr) {
        return null;
    }

    @Override
    public Object visitSetExpr(Set expr) {
        expr.getValue().accept(this);
        expr.getObject().accept(this);
        return null;
    }

    @Override
    public Object visitSuperExpr(Super expr) {
        if (currentClass == ClassType.NONE)
            throw new RuntimeError("Cannot use 'super' outside of a class.", expr.getKeyword());
        else if (currentClass != ClassType.SUBCLASS)
            throw new RuntimeError("Cannot use 'super' in a class with no superclass.", expr.getKeyword());
        resolveLocal(expr, expr.getKeyword());
        return null;
    }

    @Override
    public Object visitThisExpr(This expr) {
        if (currentClass == ClassType.NONE)
            throw new RuntimeError("Cannot use 'this' outside of a class.", expr.getKeyword());
        resolveLocal(expr, expr.getKeyword());
        return null;
    }

    @Override
    public Object visitUnaryExpr(Unary expr) {
        return expr.getRight().accept(this);
    }

    @Override
    public Object visitVariableExpr(Variable expr) {
        if (!scopes.isEmpty() && scopes.peek().get(expr.getName().getLiteral()) == Boolean.FALSE)
            throw new RuntimeError("Cannot read local variable in its own initializer.", expr.getName());
        resolveLocal(expr, expr.getName());
        return null;
    }

    private void defineVariable(Token name) {
        if (scopes.isEmpty())
            return;
        scopes.peek().put((String) name.getLiteral(), true);
    }

    private void declareVariable(Token name) {
        if (scopes.isEmpty())
            return;

        if (scopes.peek().containsKey(name.getLiteral()))
            throw new RuntimeError("Variable with this name already declared in this scope.", name);
        scopes.peek().put((String) name.getLiteral(), false);
    }

    private void beginScope() {
        scopes.push(new HashMap<String, Boolean>());
    }

    private void endScope() {
        scopes.pop();
    }

    private void resolveLocal(Expr expr, Token name) {
        for (int i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.getLiteral())) {
                int depth = scopes.size() - 1 - i;
                locals.put(expr, depth);
                return;
            }
        }
    }

    @Override
    public void visitBreakStmt(Break stmt) {
        if (currentLoop == LoopType.NONE)
            throw new RuntimeError("Cannot break outside of a loop.", stmt.getKeyword());
    }

    @Override
    public void visitContinueStmt(Continue stmt) {
        if (currentLoop == LoopType.NONE)
            throw new RuntimeError("Cannot continue outside of a loop.", stmt.getKeyword());
    }

    @Override
    public Object visitArrayExpr(Array expr) {
        for (Expr value : expr.getValues())
            value.accept(this);
        return null;
    }

    @Override
    public Object visitMapExpr(com.example.ast.Expr.Map expr) {
        for (Map.Entry<Expr, Expr> entry : expr.getValues().entrySet()) {
            entry.getKey().accept(this);
            entry.getValue().accept(this);
        }
        return null;
    }

    @Override
    public Object visitGetIndexExpr(GetIndex expr) {
        expr.getObject().accept(this);
        expr.getIndex().accept(this);
        return null;
    }

    @Override
    public Object visitSetIndexExpr(SetIndex expr) {
        expr.getValue().accept(this);
        expr.getObject().accept(this);
        expr.getIndex().accept(this);
        return null;
    }
}
