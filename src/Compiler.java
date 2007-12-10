// Copyright (C) 2007 Mihai Preda

import java.util.Stack;
import java.util.EmptyStackException;

class Compiler {
    static Lexer lexer       = new Lexer();
    static Compiler compiler = new Compiler();
    static Codegen codegen   = new Codegen();

    Stack stack = new Stack();
    Stack code  = new Stack();
    TokenType prevType;

    synchronized public static Fun compile(String str) {
        lexer.init(str);
        codegen.init();
        //compiler.init();
        Token token;
        do {
            token = lexer.nextToken();
            compiler.add(token);
        } while (token != Lexer.TOK_END && token != Lexer.TOK_ERROR);
        Fun fun = codegen.gen(compiler.code);
        compiler.init();
        return fun;
    }

    public Compiler() {
        init();
    }

    public void init() {
        stack.removeAllElements();
        code.removeAllElements();
        prevType = null;
    }

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
        //TokenType type = token.type;
        int priority = token.type.priority;
        int id = token.type.id;
        switch (id) {
        case Lexer.NUMBER:
        case Lexer.CONST:
        case Lexer.LPAREN:
        case Lexer.CALL:
            if (prevType != null && prevType.isOperand) {
                add(Lexer.TOK_MUL);
            }
            stack.push(token);
            break;
            
        case Lexer.RPAREN: {
            if (prevType.id == Lexer.CALL) {
                top().arity--; //compensate for ++ below
            } else if (!prevType.isOperand) {
                System.out.println("misplaced ')'");
                return false;
            }

            popHigher(priority);
            Token t = top();
            if (t != null) {
                if (t.type.id == Lexer.CALL) {
                    t.arity++;
                    code.push(t);
                } else if (t != Lexer.TOK_LPAREN) {
                    throw new Error("expected LPAREN or CALL");
                }
                stack.pop();
            }
            break;
        }
        
        case Lexer.COMMA: {            
            if (prevType == null || !prevType.isOperand) {
                throw new Error("misplaced COMMA");
            }
            popHigher(priority);
            Token t = top();
            if (t==null || t.type.id != Lexer.CALL) {
                throw new Error("COMMA not inside CALL");
            }
            t.arity++;
            //code.push(stack.pop());
            break;
        }
        
        case Lexer.END:
            popHigher(priority);
            break;
            
        case Lexer.SUB:
            if (prevType != null && !prevType.isOperand) {
                //change SUB to unary minus
                token = Lexer.TOK_UMIN;
                stack.push(token);
                break; //only break inside if, otherwise fall-through
            }
            
        default: //operators
            if (prevType == null || !prevType.isOperand) {
                throw new Error("operator without operand");
            }
            popHigher(priority + (token.type.assoc==TokenType.RIGHT ? 1 : 0));
            stack.push(token);
        }
        prevType = token.type;
        return true;
    }
}
