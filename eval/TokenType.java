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
    final int id;

    TokenType(int id, String name, int priority, int assoc, boolean isOperand) {
        this.id = id;
        this.name = name;
        this.priority = priority;
        this.assoc = assoc;
        this.isOperand = isOperand;
    }
}
