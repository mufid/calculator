// Copyright (C) 2007 Mihai Preda

import java.util.Stack;
import java.util.EmptyStackException;

class Compiler {
    Stack stack = new Stack();
    Stack code  = new Stack();
    TokenType prevType  = null;

    public String toString() {
        StringBuffer buf = new StringBuffer();
        int size = code.size();
        for (int i = 0; i < size; ++i) {
            buf.append(((Token) code.elementAt(i)).toString()).append('\n');
        }
        return buf.toString();
    }

    private Token top() {
        try {
            return (Token) stack.peek();
        } catch (EmptyStackException e) {
            return null;
        }
    }

    void popHigher(int priority) {
        Token t = top();
        while (t != null && t.type.priority >= priority) {
            code.push(t);
            stack.pop();
            t = top();
        }
    }

    boolean add(Token token) {
        TokenType type = token.type;
        
        if (type == Lexer.NUMBER || type == Lexer.CONST || type == Lexer.LPAREN || type == Lexer.CALL) {
            if (prevType != null && prevType.isOperand) {
                add(Lexer.TOK_MUL);
            }
            stack.push(token);
        } else if (type == Lexer.RPAREN) {
            if (prevType == Lexer.CALL) {
                top().arity--; //compensate for ++ below
            } else if (!prevType.isOperand) {
                System.out.println("misplaced ')'");
                return false;
            }

            popHigher(type.priority);
            Token t = top();
            if (t != null) {
                if (t.type == Lexer.CALL) {
                    t.arity++;
                    code.push(t);
                } else if (t != Lexer.TOK_LPAREN) {
                    throw new Error("expected LPAREN or CALL");
                }
                stack.pop();
            }
        } else if (type == Lexer.COMMA) {
            if (prevType == null || !prevType.isOperand) {
                throw new Error("misplaced COMMA");
            }
            popHigher(type.priority);
            Token t = top();
            if (t==null || t.type != Lexer.CALL) {
                throw new Error("COMMA not inside CALL");
            }
            t.arity++;
            //code.push(stack.pop());
        } else if (type == Lexer.SUB && prevType != null && !prevType.isOperand) {
            //change '-' to unary minus
            token = Lexer.TOK_UMIN;
            stack.push(token);
        } else if (type == Lexer.END) {
            popHigher(type.priority);
        } else { //operators
            if (prevType == null || !prevType.isOperand) {
                throw new Error("operator without operand");
            }
            popHigher(type.priority + (type.assoc==TokenType.RIGHT ? 1 : 0));
            stack.push(token);
        }
        prevType = type;
        return true;
    }
}
