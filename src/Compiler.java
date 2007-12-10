// Copyright (C) 2007 Mihai Preda

import java.util.Stack;
import java.util.EmptyStackException;
import org.javia.lib.Log;

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
        compiler.init();
        Token token;
        do {
            token = lexer.nextToken();
            if (!compiler.add(token)) {
                Log.log("compile error on '" + str + "'");
                return null;
            }
        } while (token != Lexer.TOK_END);
        Fun fun = codegen.gen(compiler.code);
        //fun.source = str;
        //compiler.init();
        Log.log("compile '" + str + "': " + fun);
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
        case Lexer.ERROR:
            return false;

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
                Log.log("misplaced ')'");
                return false;
            }

            popHigher(priority);
            Token t = top();
            if (t != null) {
                if (t.type.id == Lexer.CALL) {
                    t.arity++;
                    code.push(t);
                } else if (t != Lexer.TOK_LPAREN) {
                    Log.log("expected LPAREN or CALL");
                    return false;
                }
                stack.pop();
            }
            break;
        }
        
        case Lexer.COMMA: {            
            if (prevType == null || !prevType.isOperand) {
                Log.log("misplaced COMMA");
                return false;
            }
            popHigher(priority);
            Token t = top();
            if (t==null || t.type.id != Lexer.CALL) {
                Log.log("COMMA not inside CALL");
                return false;
            }
            t.arity++;
            //code.push(stack.pop());
            break;
        }
        
        case Lexer.END:
            if (prevType == null || !prevType.isOperand) {
                Log.log("misplaced END");
                return false;
            }
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
                Log.log("operator without operand");
                return false;
            }
            popHigher(priority + (token.type.assoc==TokenType.RIGHT ? 1 : 0));
            stack.push(token);
        }
        prevType = token.type;
        return true;
    }
}
