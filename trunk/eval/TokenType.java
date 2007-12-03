class TokenType {
    //kind
    static final int 
        PREFIX  = 1,
        INFIX   = 2,
        POSTFIX = 3;

    final String name;
    final int priority;
    final int kind;
    final boolean isOperand;

    TokenType(String name, int priority, int kind, boolean isOperand) {
        this.name = name;
        this.priority = priority;
        this.kind = kind;
        this.isOperand = isOperand;
    }
}
