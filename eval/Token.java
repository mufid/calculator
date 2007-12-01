class Token {
    final String name;
    final int priority;
    final boolean isOperator;

    Token(String name, int priority, boolean isOperator) {
        this.name = name;
        this.priority = priority;
        this.isOperator = isOperator;
    }
}
