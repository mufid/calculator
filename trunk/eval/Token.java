// Copyright (C) 2007 Mihai Preda

public class Token {
    final TokenType type;
    double value = 0; //for NUMBER only
    String name  = null; //for CONST & CALL
    int arity = 0; //for CALL only

    Token(TokenType type) {
        this.type = type;
    }

    Token(int id, String name, int priority, int assoc, boolean isOperand) {
        type = new TokenType(id, name, priority, assoc, isOperand);
    }

    Token(TokenType type, double value) {
        this(type);
        this.value = value;
    }

    Token(TokenType type, String alpha) {
        this(type);
        this.name  = alpha;
    }
    

    public String toString() {
        switch (type.id) {
        case Lexer.NUMBER:
            return "" + value;
        case Lexer.CALL:
            return name + '(' + arity + ')';
        case Lexer.CONST:
            return name;
        }
        return type.name;
    }
}
