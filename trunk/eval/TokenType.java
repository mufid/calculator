// Copyright (C) 2007 Mihai Preda

class TokenType {
    //kind
    static final int 
        PREFIX = 1,
        LEFT   = 2,
        RIGHT  = 3,
        SUFIX  = 4;

    final String name;
    final int priority;
    final int assoc;
    final boolean isOperand;

    TokenType(String name, int priority, int kind, boolean isOperand) {
        this.name = name;
        this.priority = priority;
        this.assoc = kind;
        this.isOperand = isOperand;
    }
}
