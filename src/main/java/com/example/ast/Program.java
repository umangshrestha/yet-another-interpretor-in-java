package com.example.ast;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Program {
    private List<Stmt> stmts;

    public String toString() {
        return String.format("%s", stmts);
    }

}
