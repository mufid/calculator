/*
 * Copyright (C) 2007 Mihai Preda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javia.eval;

import java.util.Stack;
import java.util.EmptyStackException;
import org.javia.lib.Log;

public class Parser {
    private static Lexer lexer       = new Lexer();
    private static Compiler compiler = new Compiler();
    private static Parser parser     = new Parser(compiler);
    private static SymbolTable symbols = new SymbolTable();

    Stack stack = new Stack();
    //Stack code  = new Stack();
    TokenType prevType;
    Compiler consumer;

    public Parser(Compiler consumer) {
        init();
        this.consumer = consumer;
    }

    private static final String NO_ARGS[] = new String[0];
    public static Fun compile(String str) {
        return compile(str, NO_ARGS);
    }

    synchronized public static Fun compile(String str, String argNames[]) {
        lexer.init(str);
        compiler.start(symbols, argNames);
        parser.init();
        Token token;
        try {
            do {
                parser.push(token = lexer.nextToken());
            } while (token != Lexer.TOK_END);
        } catch (SyntaxException e) {
            Log.log("compile error on '" + str + "' : " + e + " : " + e.token);
            return null;
        }
        Fun fun = compiler.getFun();

        //fun.source = str;
        //parser.init();
        Log.log("compile '" + str + "': " + fun);
        return fun;
    }

    public void init() {
        stack.removeAllElements();
        //code.removeAllElements();
        prevType = null;
    }

    /*
    public String toString() {
        StringBuffer buf = new StringBuffer();
        int size = code.size();
        for (int i = 0; i < size; ++i) {
            buf.append(((Token) code.elementAt(i)).toString()).append('\n');
        }
        return buf.toString();
    }
    */

    private Token top() {
        try {
            return (Token) stack.peek();
        } catch (EmptyStackException e) {
            return null;
        }
    }

    private void popHigher(int priority) {
        Token t = top();
        while (t != null && t.type.priority >= priority) {
            consumer.push(t);
            //code.push(t);
            stack.pop();
            t = top();
        }
    }

    void push(Token token) {
        //TokenType type = token.type;
        int priority = token.type.priority;
        int id = token.type.id;
        switch (id) {
        case Lexer.CALL:
            token.arity++;
            //fall through

        case Lexer.NUMBER:
        case Lexer.CONST:
        case Lexer.LPAREN:
            if (prevType != null && prevType.isOperand) {
                push(Lexer.TOK_MUL);
            }
            stack.push(token);
            break;
            
        case Lexer.RPAREN: {
            if (prevType != null && prevType.id == Lexer.CALL) {
                top().arity--;
            } else if (prevType == null || !prevType.isOperand) {
                throw new SyntaxException("misplaced ')'", token);
            }

            popHigher(priority);
            Token t = top();
            if (t != null) {
                if (t.type.id == Lexer.CALL) {
                    consumer.push(t);
                } else if (t != Lexer.TOK_LPAREN) {
                    throw new SyntaxException("expected LPAREN or CALL", token);
                }
                stack.pop();
            }
            break;
        }
        
        case Lexer.COMMA: {            
            if (prevType == null || !prevType.isOperand) {
                throw new SyntaxException("misplaced COMMA", token);
            }
            popHigher(priority);
            Token t = top();
            if (t==null || t.type.id != Lexer.CALL) {
                throw new SyntaxException("COMMA not inside CALL", token);
            }
            t.arity++;
            //code.push(stack.pop());
            break;
        }
        
        case Lexer.END:
            do {
                push(Lexer.TOK_RPAREN);
            } while (top() != null);
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
                throw new SyntaxException("operator without operand", token);
            }
            popHigher(priority + (token.type.assoc==TokenType.RIGHT ? 1 : 0));
            stack.push(token);
        }
        prevType = token.type;
    }
}
