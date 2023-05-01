package com.example.token;

import com.example.ast.Expr.Super;

public enum TokenType {
    // Data types
    Identifier,
    String,
    Number,
    True,
    False,
    Null,
    // Math operators
    Plus,
    Minus,
    Multiply,
    Divide,
    Modulo,
    Exponent,
    // Brackets
    LeftParen,
    RightParen,
    LeftBrace,
    RightBrace,
    LeftBracket,
    RightBracket,
    // Keywords
    Function,
    Let,
    If,
    Else,
    Return,
    While,
    For,
    Class,
    // Comparison operators
    Equal,
    NotEqual,
    LessThan,
    LessThanOrEqual,
    GreaterThan,
    GreaterThanOrEqual,
    // Logical operators
    And,
    Or,
    Not,
    // Bitwise operators
    BitwiseAnd,
    BitwiseOr,
    BitwiseNot,
    LeftShift,
    RightShift,
    // Assignment operators
    Assign,
    PlusAssign,
    MinusAssign,
    MultiplyAssign,
    DivideAssign,
    ModuloAssign,
    ExponentAssign,
    BitwiseAndAssign,
    BitwiseOrAssign,
    BitwiseNotAssign,
    LeftShiftAssign,
    RightShiftAssign,
    // Delimiters
    Comma,
    Colon,
    Semicolon,
    Dot,
    // Flow
    Break,
    Continue,
    Super,
    This,
    // End of file
    Eof
}
