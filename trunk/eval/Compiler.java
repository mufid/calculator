import java.util.Stack;

class Compiler {
    Stack stack = new Stack();
    Stack code  = new Stack();
    TokenType prevType  = null;

    private Token top() {
        try {
            return (Token) stack.peek();
        } catch (EmptyStackException e) {
            return null;
        }
    }

    void popHigher(int priority) {
        Token t = top();
        while (t != null && top.type.priority >= priority) {
            code.push(t);
            stack.pop();
            t = top();
        }
    }

    boolean add(Token token) {
        TokenType type = token.type;

        //when appropriate change '-' to unary minus
        if (type == Lexer.SUB && prevType != null && !prevType.isOperand) {
            token = Lexer.TOK_UMIN;
            stack.push(token);
            prevType = token.type;
            return true;
        }
        
        if (type == Lexer.NUMBER || type == Lexer.CONST || type == LPAREN) {
            if (prevType != null && prevType.isOperand()) {
                add(Lexer.TOK_MUL);
            }
            stack.push(token);
        } else if (type == RPAREN) {
            if (prevType == Lexer.CALL) {
                top().arity--; //compensate for ++ below
            } else if (!prevType.isOperand) {
                System.out.println("misplaced ')'");
                return false;
            }

            popHigher(type.priority);
            Token t = top();
            if (t != null) {
                if (t == Lexer.LPAREN) {
                } else if (t == Lexer.CALL) {
                    t.arity++;
                    code.push(t);
                } else {
                    throw new Error("expected LPAREN or CALL");
                }
                stack.pop();
            }
        } else if (type == COMMA) {
            
        }

        
    }
}
