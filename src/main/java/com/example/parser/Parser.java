package com.example.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.example.App;
import com.example.ast.Expr;
import com.example.ast.Program;
import com.example.ast.Stmt;
import com.example.lexer.Lexer;
import com.example.token.Token;
import com.example.token.TokenType;

public class Parser {
    private Token current = null;
    private Token peek = null;
    private final Lexer lexer;

    private static final HashMap<TokenType, TokenType> assignToBinary = new HashMap<TokenType, TokenType>() {
        {
            put(TokenType.PlusAssign, TokenType.Plus);
            put(TokenType.MinusAssign, TokenType.Minus);
            put(TokenType.MultiplyAssign, TokenType.Multiply);
            put(TokenType.DivideAssign, TokenType.Divide);
            put(TokenType.ExponentAssign, TokenType.Exponent);
            put(TokenType.BitwiseAndAssign, TokenType.BitwiseAnd);
            put(TokenType.BitwiseOrAssign, TokenType.BitwiseOr);
            put(TokenType.LeftShiftAssign, TokenType.LeftShift);
            put(TokenType.RightShiftAssign, TokenType.RightShift);
            put(TokenType.ModuloAssign, TokenType.Modulo);
            put(TokenType.ExponentAssign, TokenType.Exponent);
        }
    };

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        advance();
    }

    public Program getProgram() {
        List<Stmt> stmts = new ArrayList<Stmt>();
        while (!isEof()) {
            try {
                stmts.add(statement());
            } catch (ParserError e) {
                App.setHasError();
                System.out.println(e.getMessage());
                synchronize();
            }
        }
        return new Program(stmts);
    }

    private Token shouldBe(TokenType tok, String message) throws ParserError {
        if (isNextToken(tok)) {
            return advance();
        }
        throw new ParserError(
                message,
                current.getLine(),
                current.getCol(),
                lexer.getCurrentLine());
    }

    private Stmt statement() throws ParserError {
        if (match(TokenType.Class))
            return classDeclaration();
        else if (match(TokenType.Function))
            return functionDeclaration();
        else if (match(TokenType.Let))
            return letStatement();
        else if (match(TokenType.If))
            return ifStatement();
        else if (match(TokenType.While))
            return whileStatement();
        else if (match(TokenType.For))
            return forStatement();
        else if (match(TokenType.Return))
            return returnStatement();
        // else if (match(TokenType.LeftBrace))
        // return blockStatment();
        else if (match(TokenType.Break)) {
            shouldBe(TokenType.Semicolon, "Expect ';' after break");
            return new Stmt.Break(current);
        } else if (match(TokenType.Continue)) {
            shouldBe(TokenType.Semicolon, "Expect ';' after continue");
            return new Stmt.Continue(current);
        } else {
            Expr expr = expression();
            shouldBe(TokenType.Semicolon, "Expect ';' after expression");
            return new Stmt.ExprStmt(expr);
        }
    }

    private Stmt.Return returnStatement() throws ParserError {
        Token keyword = current;
        Expr value = null;
        if (!isNextToken(TokenType.Semicolon)) {
            value = expression();
        }
        shouldBe(TokenType.Semicolon, "Expect ';' after return value");
        return new Stmt.Return(keyword, value);
    }

    private Stmt.Block blockStatment() throws ParserError {
        List<Stmt> stmts = new ArrayList<Stmt>();
        while (!isNextToken(TokenType.RightBrace) && !isEof()) {
            try {
                stmts.add(statement());
            } catch (ParserError e) {
                App.setHasError();
                System.out.println(e.getMessage());
                synchronize();
            }
        }
        shouldBe(TokenType.RightBrace, "Expect '}' after block");
        return new Stmt.Block(stmts);
    }

    private Stmt.Class classDeclaration() throws ParserError {
        Token name = shouldBe(TokenType.Identifier, "Expect class name");
        Expr.Variable superclass = null;
        if (match(TokenType.LessThan)) {
            shouldBe(TokenType.Identifier, "Expect superclass name");
            superclass = new Expr.Variable(current);
        }
        shouldBe(TokenType.LeftBrace, "Expect '{' before class body");
        List<Stmt.Function> methods = new ArrayList<>();
        while (!isNextToken(TokenType.RightBrace) && !isEof()) {
            methods.add(functionDeclaration());
        }
        shouldBe(TokenType.RightBrace, "Expect '}' after class body");
        return new Stmt.Class(name, superclass, methods);
    }

    private Stmt.Function functionDeclaration() throws ParserError {
        shouldBe(TokenType.Identifier, "Expect function name");
        Token name = current;
        shouldBe(TokenType.LeftParen, "Expect '(' after function name");
        List<Token> params = new ArrayList<Token>();
        if (!isNextToken(TokenType.RightParen)) {
            do {
                if (params.size() >= 255) {
                    throw new ParserError(
                            "Cannot have more than 255 parameters",
                            current.getLine(),
                            current.getCol(),
                            lexer.getCurrentLine());
                }
                params.add(shouldBe(TokenType.Identifier, "Expect parameter name"));
            } while (match(TokenType.Comma));
        }
        shouldBe(TokenType.RightParen, "Expect ')' after parameters");
        shouldBe(TokenType.LeftBrace, "Expect '{' before function body");
        return new Stmt.Function(name, params, blockStatment());
    }

    private Stmt.If ifStatement() throws ParserError {
        shouldBe(TokenType.LeftParen, "Expect '(' after 'if'");
        Expr condition = expression();
        shouldBe(TokenType.RightParen, "Expect ')' after if condition");
        Stmt thenBranch = (match(TokenType.LeftBrace)) ? blockStatment() : statement();

        Stmt elseBranch = null;
        if (match(TokenType.Else)) {
            elseBranch = (match(TokenType.LeftBrace)) ? blockStatment() : statement();
        }
        return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt.While whileStatement() throws ParserError {
        shouldBe(TokenType.LeftParen, "Expect '(' after 'while'");
        Expr condition = expression();
        shouldBe(TokenType.RightParen, "Expect ')' after while condition");
        Stmt body = (match(TokenType.LeftBrace)) ? blockStatment() : statement();
        return new Stmt.While(condition, body);
    }

    private Stmt forStatement() throws ParserError {
        shouldBe(TokenType.LeftParen, "Expect '(' after 'for'");
        Stmt initializer;
        if (match(TokenType.Semicolon)) {
            initializer = null;
        } else if (match(TokenType.Let)) {
            initializer = letStatement();
        } else {
            initializer = new Stmt.ExprStmt(expression());
            shouldBe(TokenType.Semicolon, "Expect ';' after loop initializer");
        }
        Expr condition = null;
        if (!isNextToken(TokenType.Semicolon)) {
            condition = expression();
        }
        shouldBe(TokenType.Semicolon, "Expect ';' after loop condition");
        Expr increment = null;
        if (!isNextToken(TokenType.RightParen)) {
            increment = expression();
        }
        shouldBe(TokenType.RightParen, "Expect ')' after for clauses");
        Stmt body = (match(TokenType.LeftBrace)) ? blockStatment() : statement();
        if (increment != null) {
            body = new Stmt.Block(
                    List.of(body, new Stmt.ExprStmt(increment)));
        }
        if (condition == null) {
            condition = new Expr.Literal(true);
        }
        body = new Stmt.While(condition, body);
        if (initializer != null) {
            return new Stmt.Block(List.of(initializer, body));
        }
        return body;
    }

    private Stmt.Let letStatement() throws ParserError {
        Expr expr = null;
        Token name = shouldBe(TokenType.Identifier, "Expect variable name");
        if (match(TokenType.Assign)) {
            expr = expression();
        }
        if (match(TokenType.PlusAssign, TokenType.MinusAssign, TokenType.MultiplyAssign,
                TokenType.DivideAssign, TokenType.ModuloAssign, TokenType.BitwiseAndAssign,
                TokenType.BitwiseOrAssign, TokenType.ExponentAssign, TokenType.LeftShiftAssign,
                TokenType.RightShiftAssign))
            throw new ParserError(
                    String.format("Cannot use %s operator in let statement", current.getType()),
                    current.getLine(),
                    current.getCol(),
                    lexer.getCurrentLine());

        shouldBe(TokenType.Semicolon, "Expect ';' after let declaration");
        return new Stmt.Let(name, expr);
    }

    private Expr expression() throws ParserError {
        return assignment();
    }

    private Expr assignment() throws ParserError {
        Expr expr = or();
        if (match(
                TokenType.Assign,
                TokenType.PlusAssign,
                TokenType.MinusAssign,
                TokenType.MultiplyAssign,
                TokenType.DivideAssign,
                TokenType.ModuloAssign,
                TokenType.BitwiseAndAssign,
                TokenType.BitwiseOrAssign,
                TokenType.ExponentAssign,
                TokenType.LeftShiftAssign,
                TokenType.RightShiftAssign)) {
            Token op = current;
            Expr value = assignment();
            if (assignToBinary.containsKey(op.getType())) {
                Token newOp = new Token(
                        assignToBinary.get(op.getType()),
                        op.getLine(),
                        op.getCol(),
                        null);
                value = new Expr.Binary(
                        expr,
                        newOp,
                        value);
                op.setType(TokenType.Assign);
            }
            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).getName();
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get) expr;
                return new Expr.Set(get.getObject(), get.getName(), value, op);
            }
            throw new ParserError(
                    "Invalid assignment target",
                    op.getLine(),
                    op.getCol(),
                    lexer.getCurrentLine());
        }
        return expr;
    }

    private Expr or() throws ParserError {
        Expr expr = and();
        while (match(TokenType.Or)) {
            Token operator = current;
            Expr right = and();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr and() throws ParserError {
        Expr expr = bitwiseOr();
        while (match(TokenType.And)) {
            Token operator = current;
            Expr right = bitwiseOr();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr bitwiseOr() throws ParserError {
        Expr expr = bitwiseAnd();
        while (match(TokenType.Or)) {
            Token operator = current;
            Expr right = bitwiseAnd();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr bitwiseAnd() throws ParserError {
        Expr expr = equality();
        while (match(TokenType.And)) {
            Token operator = current;
            Expr right = equality();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr equality() throws ParserError {
        Expr expr = comparision();
        while (match(TokenType.Equal, TokenType.NotEqual)) {
            Token operator = current;
            Expr right = comparision();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr comparision() throws ParserError {
        Expr expr = addition();
        while (match(
                TokenType.GreaterThan,
                TokenType.GreaterThanOrEqual,
                TokenType.LessThan,
                TokenType.LessThanOrEqual)) {
            Token operator = current;
            Expr right = addition();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr addition() throws ParserError {
        Expr expr = multiplication();
        while (match(TokenType.Plus, TokenType.Minus)) {
            Token operator = current;
            Expr right = multiplication();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr multiplication() throws ParserError {
        Expr expr = divide();
        while (match(TokenType.Multiply, TokenType.Divide, TokenType.Modulo)) {
            Token operator = current;
            Expr right = divide();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr divide() throws ParserError {
        Expr expr = unary();
        while (match(TokenType.Divide)) {
            Token operator = current;
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr unary() throws ParserError {
        if (match(TokenType.Not, TokenType.Minus)) {
            Token operator = current;
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }
        return exponent();
    }

    private Expr exponent() throws ParserError {
        Expr expr = call();
        while (match(TokenType.Exponent)) {
            Token operator = current;
            Expr right = call();
            expr = new Expr.Binary(expr, operator, right);
        }
        return expr;
    }

    private Expr call() throws ParserError {
        Expr expr = primary();
        while (true) {
            Token current = this.current;
            if (match(TokenType.LeftParen))
                return call(expr);
            else if (match(TokenType.Dot)) {
                Token name = shouldBe(TokenType.Identifier, "Expect property name after '.'");
                expr = new Expr.Get(expr, name, current);
            } else if (match(TokenType.LeftBracket)) {
                Expr index = expression();
                shouldBe(TokenType.RightBracket, "Expect ']' after index");
                if (match(TokenType.Assign)) {
                    Expr value = expression();
                    expr = new Expr.SetIndex(expr, index, value, current);
                } else
                    expr = new Expr.GetIndex(expr, index, current);
            } else {
                break;
            }
        }
        return expr;
    }

    private Expr primary() throws ParserError {
        if (match(TokenType.False))
            return new Expr.Literal(false);
        else if (match(TokenType.True))
            return new Expr.Literal(true);
        else if (match(TokenType.Null))
            return new Expr.Literal(null);
        else if (match(TokenType.Number, TokenType.String))
            return new Expr.Literal(current.getLiteral());
        else if (match(TokenType.Identifier))
            return new Expr.Variable(current);
        else if (match(TokenType.LeftBracket))
            return array();
        else if (match(TokenType.LeftBrace))
            return map();
        else if (match(TokenType.LeftParen)) {
            Expr expr = expression();
            shouldBe(TokenType.RightParen, "Expect ')' after expression");
            return new Expr.Grouping(expr);
        }
        throw new ParserError(
                String.format("Expected expression, got '%s'", peek.getType()),
                peek.getLine(),
                peek.getCol(),
                lexer.getCurrentLine());

    }

    private Expr call(Expr callee) throws ParserError {
        List<Expr> args = new ArrayList<Expr>();
        if (!isNextToken(TokenType.RightParen)) {
            do {
                args.add(expression());
            } while (match(TokenType.Comma));
        }
        Token paren = shouldBe(TokenType.RightParen, "Expect ')' after arguments");
        return new Expr.Call(callee, paren, args);
    }

    private Expr array() throws ParserError {
        List<Expr> elements = new ArrayList<Expr>();
        if (!isNextToken(TokenType.RightBracket)) {
            do {
                elements.add(expression());
            } while (match(TokenType.Comma));
        }
        shouldBe(TokenType.RightBracket, "Expect ']' after array elements");
        return new Expr.Array(elements);
    }

    private Expr map() throws ParserError {
        HashMap<Expr, Expr> map = new HashMap<>();
        Token operator = current;
        if (!isNextToken(TokenType.RightBrace)) {
            do {
                Expr key = expression();
                shouldBe(TokenType.Colon, "Expect ':' after map key");
                Expr value = expression();
                map.put(key, value);
            } while (match(TokenType.Comma));
        }
        shouldBe(TokenType.RightBrace, "Expect '}' after map elements");
        return new Expr.Map(map);
    }

    private Token advance() {
        current = peek;
        peek = lexer.nextToken();
        return current;
    }

    boolean isEof() {
        return peek.getType() == TokenType.Eof;
    }

    boolean isNextToken(TokenType t) {
        if (isEof())
            return false;
        return peek.getType() == t;
    }

    boolean match(TokenType... tok) {
        for (TokenType t : tok) {
            if (isNextToken(t)) {
                advance();
                return true;
            }
        }
        return false;
    }

    public void synchronize() {
        advance();
        while (!isEof()) {
            if (current.getType() == TokenType.Semicolon)
                return;
            TokenType tok = peek.getType();
            switch (tok) {
                case Class:
                case Function:
                case Let:
                case For:
                case If:
                case While:
                case Return:
                    return;
            }
            advance();
        }
    }
}
