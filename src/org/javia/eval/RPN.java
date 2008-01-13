/*
 * Copyright (C) 2007-2008 Mihai Preda.
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

/* Reverse Polish Notation
   reads tokens in normal infix order (e.g.: 1 + 2)
   and outputs them in Reverse Polish order (e.g.: 1 2 +).
   See Dijkstra's Shunting Yard algorithm: 
   http://en.wikipedia.org/wiki/Shunting_yard_algorithm
 */ 
public class RPN extends TokenConsumer {
    Stack stack = new Stack();
    TokenType prevType;
    TokenConsumer consumer;

    void setConsumer(TokenConsumer consumer) {
        this.consumer = consumer;
    }

    //@Override
    void start() {
        stack.removeAllElements();
        prevType = null;
        consumer.start();
    }

    //@Override
    void done() {
        consumer.done();
        consumer = null;
    }

    private Token top() {
        try {
            return (Token) stack.peek();
        } catch (EmptyStackException e) {
            return null;
        }
    }

    private void popHigher(int priority) throws SyntaxException {
        Token t = top();
        while (t != null && t.type.priority >= priority) {
            consumer.push(t);
            //code.push(t);
            stack.pop();
            t = top();
        }
    }

    void push(Token token) throws SyntaxException {
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
        case Lexer.SQRT:
            if (prevType != null && prevType.isOperand) {
                push(Lexer.TOK_MUL);
            }
            stack.push(token);
            break;
            
        case Lexer.RPAREN: {
            if (prevType != null && prevType.id == Lexer.CALL) {
                top().arity--;
            } else if (prevType == null || !prevType.isOperand) {
                throw SyntaxException.get("misplaced ')'", token);
            }

            popHigher(priority);
            Token t = top();
            if (t != null) {
                if (t.type.id == Lexer.CALL) {
                    consumer.push(t);
                } else if (t != Lexer.TOK_LPAREN) {
                    throw SyntaxException.get("expected LPAREN or CALL", token);
                }
                stack.pop();
            }
            break;
        }
        
        case Lexer.COMMA: {            
            if (prevType == null || !prevType.isOperand) {
                throw SyntaxException.get("misplaced COMMA", token);
            }
            popHigher(priority);
            Token t = top();
            if (t==null || t.type.id != Lexer.CALL) {
                throw SyntaxException.get("COMMA not inside CALL", token);
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
            
        default: //operators
            if (prevType == null || !prevType.isOperand) {
                if (id == Lexer.SUB) {
                    //change SUB to unary minus
                    token = Lexer.TOK_UMIN;
                    stack.push(token);
                    break;
                } else if (id == Lexer.ADD) {
                    // ignore, keep prevType unchanged
                    return;
                }
                throw SyntaxException.get("operator without operand", token);
            }
            popHigher(priority + (token.type.assoc==TokenType.RIGHT ? 1 : 0));
            stack.push(token);
        }
        prevType = token.type;
    }
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
