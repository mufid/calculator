// Copyright (C) 2007 Mihai Preda

public class Token {
    final TokenType type;
    double value = 0; //for NUMBER only
    String name  = null; //for CONST & CALL
    int arity = 0; //for CALL only

    Token(TokenType type) {
        this.type = type;
    }

    Token(TokenType type, double value, String alpha) {
        this(type);
        this.value = value;
        this.name  = alpha;
    }

    public String toString() {
        if (type == Lexer.NUMBER) {
            return "" + value;
        } else if (type == Lexer.CALL) {
            return name + '(' + arity + ')';
        } else if (type == Lexer.CONST) {
            return name;
        }
        return type.name;
    }
}
