package com.example.interpreter;

import com.example.ast.StmtVisitor;
import com.example.token.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.ast.Expr;
import com.example.ast.Expr.Array;
import com.example.ast.Expr.Call;
import com.example.ast.Expr.Get;
import com.example.ast.Expr.GetIndex;
import com.example.ast.Expr.Set;
import com.example.ast.Expr.SetIndex;
import com.example.ast.Expr.Super;
import com.example.ast.Expr.This;
import com.example.ast.ExprVisitor;
import com.example.ast.Program;
import com.example.ast.Stmt;
import com.example.ast.Stmt.Break;
import com.example.ast.Stmt.Class;
import com.example.ast.Stmt.Continue;
import com.example.ast.Stmt.Function;
import com.example.environment.Environment;
import com.example.environment.Resolver;
import com.example.environment.RuntimeError;

public class Interpreter implements ExprVisitor, StmtVisitor {

    Environment globals = new Environment();

    private Environment environment = globals;
    private final Resolver resolver = new Resolver();

    public Environment getEnvironment() {
        return environment;
    }

    public Interpreter() {
        globals.define("clock", new builtIn.Clock());
        globals.define("print", new builtIn.Print());
        globals.define("input", new builtIn.Input());
        globals.define("exit", new builtIn.Exit());
        globals.define("typeOf", new builtIn.TypeOf());
        globals.define("bool", new builtIn.Bool());
        globals.define("length", new builtIn.Length());
    }

    public void interpret(Program p) {
        resolver.resolve(p.getStmts());
        for (Stmt stmt : p.getStmts())
            stmt.accept(this);
    }

    @Override
    public void visitBlockStmt(Stmt.Block stmt) {
        executeBlock(stmt.getStmts(), new Environment(environment));
    }

    public void executeBlock(List<Stmt> stmts, Environment environment) {
        Environment previous = this.environment;
        try {
            this.environment = environment;
            for (Stmt stmt : stmts)
                stmt.accept(this);
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public void visitIfStmt(Stmt.If stmt) {
        if (bool(stmt.getCondition().accept(this))) {
            stmt.getThenBranch().accept(this);
        } else if (stmt.getElseBranch() != null) {
            stmt.getElseBranch().accept(this);
        }
    }

    @Override
    public void visitReturnStmt(Stmt.Return stmt) {
        Object value = null;
        if (stmt.getValue() != null)
            value = stmt.getValue().accept(this);
        throw new CustomError.Return(value);
    }

    @Override
    public void visitWhileStmt(Stmt.While stmt) {
        while (bool(stmt.getCondition().accept(this))) {
            try {
                stmt.getBody().accept(this);
            } catch (CustomError.Break b) {
                break;
            } catch (CustomError.Continue c) {
                continue;
            }
        }
    }

    @Override
    public void visitLetStmt(Stmt.Let stmt) {
        Object value = null;
        if (stmt.getInitializer() != null) {
            value = stmt.getInitializer().accept(this);
        }
        environment.declare(stmt.getName(), value);
    }

    @Override
    public void visitExprStmt(Stmt.ExprStmt stmt) {
        stmt.getExpression().accept(this);
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        return expr.getValue();
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        return expr.getExpression().accept(this);
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        Object right = expr.getRight().accept(this);
        switch (expr.getOperator().getType()) {
            case Minus:
                return -(double) right;
            case Not:
                return !bool(right);
        }
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        return lookUpVariable(expr.getName(), expr);
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        Object value = expr.getValue().accept(this);
        int depth = resolver.getDepth(expr);
        if (depth != -1) {
            environment.assignAt(depth, expr.getName(), value);
        } else {
            globals.assign(expr.getName(), value);
        }
        return value;
    }

    boolean bool(Object o) {
        if (o == null)
            return false;
        else if (o instanceof Boolean)
            return (boolean) o;
        else if (o instanceof Double)
            return (double) o != 0;
        else if (o instanceof String)
            return !((String) o).isEmpty();
        else if (o instanceof List)
            return !((List) o).isEmpty();
        else if (o instanceof Map)
            return !((Map<Object, Object>) o).isEmpty();
        else
            return true;
    }

    private boolean isEqual(Object left, Object right) {
        if (left == null && right == null)
            return true;
        if (left == null)
            return false;
        return left.equals(right);
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        Object left = expr.getLeft().accept(this);
        Object right = expr.getRight().accept(this);
        Token op = expr.getOperator();
        switch (op.getType()) {
            case Minus:
                return (double) left - (double) right;
            case Divide:
                return (double) left / (double) right;
            case Plus:
                if (left instanceof Double && right instanceof Double)
                    return (double) left + (double) right;
                if (left instanceof String && right instanceof String)
                    return (String) left + (String) right;
                throw new RuntimeError("Operands must be two numbers or two strings", op);
            case Multiply:
                return (double) left * (double) right;
            case Exponent:
                return Math.pow((double) left, (double) right);
            case BitwiseAnd:
                return (int) (double) left & (int) (double) right;
            case BitwiseOr:
                return (int) (double) left | (int) (double) right;
            case And:
                return bool(left) && bool(right);
            case Or:
                return bool(left) || bool(right);
            case Equal:
                return isEqual(left, right);
            case NotEqual:
                return !isEqual(left, right);
            case GreaterThan:
                if (left instanceof Double && right instanceof Double)
                    return (double) left > (double) right;
                if (left instanceof String && right instanceof String)
                    return ((String) left).compareTo((String) right) > 0;
                throw new RuntimeError("Operands must be two numbers or two strings", op);
            case GreaterThanOrEqual:
                if (left instanceof Double && right instanceof Double)
                    return (double) left >= (double) right;
                if (left instanceof String && right instanceof String)
                    return ((String) left).compareTo((String) right) >= 0;
                throw new RuntimeError("Operands must be two numbers or two strings", op);
            case LessThan:
                if (left instanceof Double && right instanceof Double)
                    return (double) left < (double) right;
                if (left instanceof String && right instanceof String)
                    return ((String) left).compareTo((String) right) < 0;
                throw new RuntimeError("Operands must be two numbers or two strings", op);
            case LessThanOrEqual:
                if (left instanceof Double && right instanceof Double)
                    return (double) left <= (double) right;
                if (left instanceof String && right instanceof String)
                    return ((String) left).compareTo((String) right) <= 0;
                return (double) left <= (double) right;
            case Modulo:
                return (double) left % (double) right;
        }

        throw new RuntimeError("Unknown operator", op);
    }

    @Override
    public void visitClassStmt(Class stmt) {
        environment.define(
                (String) stmt.getName().getLiteral(),
                null);
        Object superclass = null;
        if (stmt.getSuperclass() != null) {
            superclass = stmt.getSuperclass().accept(this);
            if (!(superclass instanceof CustomClass)) {
                throw new RuntimeError(
                        "Superclass must be a class",
                        stmt.getName());
            }
            environment = new Environment(environment);
            environment.define("super", superclass);
        }
        Map<String, CustomFunction> methods = new HashMap<>();
        for (Stmt.Function method : stmt.getMethods()) {
            methods.put(
                    (String) method.getName().getLiteral(),
                    new CustomFunction(
                            method,
                            environment,
                            method.getName().getLiteral().equals("init")));
        }
        CustomClass klass = new CustomClass(
                (String) stmt.getName().getLiteral(),
                (CustomClass) superclass,
                methods);
        if (superclass != null) {
            environment = environment.getEnclosing();
        }
        environment.assign(
                stmt.getName(),
                klass);
    }

    @Override
    public void visitFunctionStmt(Function stmt) {
        CustomFunction function = new CustomFunction(stmt, environment, false);
        environment.define((String) stmt.getName().getLiteral(), function);
    }

    @Override
    public Object visitCallExpr(Call expr) {
        Object callee = expr.getCallee().accept(this);
        List<Object> arguments = new ArrayList<>();
        for (Expr argument : expr.getArguments()) {
            arguments.add(argument.accept(this));
        }
        if (!(callee instanceof CustomCallable)) {
            throw new RuntimeError("Can only call functions and classes", expr.getParen());
        }
        CustomCallable function = (CustomCallable) callee;
        if (function.arity() == -1 || function.arity() == arguments.size()) {
            return function.call(this, arguments);
        }
        throw new RuntimeError("Expected " + function.arity() + " arguments but got " + arguments.size(),
                expr.getParen());
    }

    @Override
    public Object visitGetExpr(Get expr) {
        Object object = expr.getObject().accept(this);
        if (object instanceof CustomInstance) {
            return ((CustomInstance) object).get(expr.getName());
        }
        throw new RuntimeError("Only instances have properties", expr.getName());
    }

    @Override
    public Object visitSetExpr(Set expr) {
        Object object = expr.getObject().accept(this);
        if (!(object instanceof CustomInstance)) {
            throw new RuntimeError("Only instances have fields", expr.getName());
        }
        Object value = expr.getValue().accept(this);
        ((CustomInstance) object).set(expr.getName(), value);
        return value;
    }

    @Override
    public Object visitSuperExpr(Super expr) {
        int distance = resolver.getDepth(expr);
        CustomClass superclass = (CustomClass) environment.getAt(distance, "super");
        CustomInstance object = (CustomInstance) environment.getAt(distance - 1, "this");
        CustomFunction method = superclass.findMethod(object, (String) expr.getMethod().getLiteral());
        if (method == null) {
            throw new RuntimeError(
                    String.format("Undefined property '%s'", expr.getMethod().getLiteral()),
                    expr.getMethod());
        }
        return method.bind(object);
    }

    @Override
    public Object visitThisExpr(This expr) {
        return lookUpVariable(expr.getKeyword(), expr);
    }

    private Object lookUpVariable(Token name, Expr expr) {
        int distance = resolver.getDepth(expr);
        if (distance != -1) {
            return environment.getAt(distance, (String) name.getLiteral());
        } else {
            return globals.get(name);
        }
    }

    @Override
    public void visitBreakStmt(Break expr) {
        throw new CustomError.Break(expr.getKeyword());
    }

    @Override
    public void visitContinueStmt(Continue expr) {
        throw new CustomError.Continue(expr.getKeyword());
    }

    @Override
    public Object visitArrayExpr(Array expr) {
        List<Object> values = new ArrayList<>();
        for (Expr value : expr.getValues()) {
            values.add(value.accept(this));
        }
        return values;
    }

    @Override
    public Object visitMapExpr(com.example.ast.Expr.Map expr) {
        Map<Object, Object> values = new HashMap<>();
        for (Map.Entry<Expr, Expr> entry : expr.getValues().entrySet()) {
            values.put(entry.getKey().accept(this), entry.getValue().accept(this));
        }
        return values;
    }

    @Override
    public Object visitGetIndexExpr(GetIndex expr) {
        Object object = expr.getObject().accept(this);
        Object index = expr.getIndex().accept(this);
        if (object instanceof List) {
            if (!(index instanceof Double)) {
                throw new RuntimeError("Index must be a number", expr.getOperator());
            }
            return ((List) object).get(((Double) index).intValue());
        } else if (object instanceof Map) {
            return ((Map) object).get(index);
        }
        throw new RuntimeError("Only lists and maps have indexes", expr.getOperator());
    }

    @Override
    public Object visitSetIndexExpr(SetIndex expr) {
        Object object = expr.getObject().accept(this);
        Object index = expr.getIndex().accept(this);
        Object value = expr.getValue().accept(this);
        if (object instanceof List) {
            if (!(index instanceof Double)) {
                throw new RuntimeError("Index must be a number", expr.getOperator());
            }
            ((List) object).set(((Double) index).intValue(), value);
            return value;
        } else if (object instanceof Map) {
            ((Map) object).put(index, value);
            return value;
        }
        throw new RuntimeError("Only lists and maps have indexes", expr.getOperator());
    }

}
