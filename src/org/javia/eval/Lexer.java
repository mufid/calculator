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

class Lexer {
    static final int
        ADD = 1, 
        SUB = 2, 
        MUL = 3, 
        DIV = 4, 
        MOD = 5,
        UMIN   = 6, 
        POWER  = 7, 
        FACT   = 8,
        NUMBER = 9, 
        CONST  = 10,
        CALL   = 11, 
        COMMA  = 12, 
        LPAREN = 13, 
        RPAREN = 14,
        END    = 15,
        SQRT   = 16;
    //UPLUS  = 17;
        
    static final TokenType
        NUMBER_TYPE = new TokenType(NUMBER, "number", 9, 0, true,  0),
        CONST_TYPE  = new TokenType(CONST,  "alpha",  9, 0, true,  0),
        CALL_TYPE   = new TokenType(CALL,   "call",   0, 0, false, 0);
    
    static final Token
        TOK_ADD    = new Token(ADD, "+", 3, TokenType.LEFT, false, VM.ADD),
        TOK_SUB    = new Token(SUB, "-", 3, TokenType.LEFT, false, VM.SUB),

        TOK_MUL    = new Token(MUL, "*", 4, TokenType.LEFT, false, VM.MUL),
        TOK_DIV    = new Token(DIV, "/", 4, TokenType.LEFT, false, VM.DIV),
        TOK_MOD    = new Token(MOD, "%", 4, TokenType.LEFT, false, VM.MOD),

        TOK_UMIN   = new Token(UMIN, "-", 5, TokenType.PREFIX,  false, VM.UMIN),

        TOK_POWER  = new Token(POWER, "^", 6, TokenType.RIGHT,  false, VM.POWER),
        TOK_FACT   = new Token(FACT,  "!", 7, TokenType.SUFIX,  true,  VM.FACT),

        TOK_LPAREN = new Token(LPAREN, "(",    1, 0, false, 0),
        TOK_RPAREN = new Token(RPAREN, ")",    2, 0, true,  0),
        TOK_COMMA  = new Token(COMMA,  ",",    1, 0, false, 0),
        TOK_END    = new Token(END,   "end",   0, 0, false, 0);

    private static final String WHITESPACE = " \n\r\t";
    private static final char END_MARKER = '$';
    private char[] input = new char[32];
    private int pos;

    Lexer() {
        init("");
    }

    Lexer(String str) {
        init(str);
    }

    void scan(String str, TokenConsumer consumer) throws SyntaxException {
        init(str);
        consumer.start();
        Token token;
        do {
            consumer.push(token = nextToken());
        } while (token != TOK_END);
    }

    void init(String str) {
        int len = str.length();
        if (input.length < len + 1) {
            input = new char[len+1];
        }
        str.getChars(0, len, input, 0);
        input[len] = END_MARKER;
        pos = 0;
    }

    Token nextToken() throws SyntaxException {
        while (WHITESPACE.indexOf(input[pos]) != -1) {
            ++pos;
        }

        char c = input[pos];
        int begin = pos++;

        switch (c) {
        case '!': return TOK_FACT;
        case END_MARKER: return TOK_END;
        case '%': return TOK_MOD;
        case '(': return TOK_LPAREN;
        case ')': return TOK_RPAREN;
        case '*': return TOK_MUL;
        case '+': return TOK_ADD;
        case ',': return TOK_COMMA;
        case '-': return TOK_SUB;
        case '/': return TOK_DIV;
        }
        if (c == '^') { 
            return TOK_POWER;
        }

        int p  = pos;
        if (('0' <= c && c <= '9') || c == '.') {
            while ('0' <= c && c <= '9') {
                c = input[p++];
            } 
            if (c == '.') {
                c = input[p++];
                while ('0' <= c && c <= '9') {
                    c = input[p++];
                }
                }
            if (c == 'E' || c == 'e') {
                c = input[p++];
                if (c == '-') {
                    c = input[p++];
                }
                while ('0' <= c && c <= '9') {
                    c = input[p++];
                }
            }
            pos = p-1;
            String nbStr = String.valueOf(input, begin, p-1-begin);
            try {
                double numberValue = Double.parseDouble(nbStr);
                return new Token(NUMBER_TYPE, numberValue);
            } catch (NumberFormatException e) {
                throw SyntaxException.get("invalid number " + nbStr, null);
            }
        } else if (('a' <= c && c <= 'z') ||
                   ('A' <= c && c <= 'Z')) {
            do {
                c = input[p++];
            } while (('a' <= c && c <= 'z') ||
                     ('A' <= c && c <= 'Z') ||
                     ('0' <= c && c <= '9'));
            String nameValue = String.valueOf(input, begin, p-1-begin);
            while (WHITESPACE.indexOf(c) != -1) {
                c = input[p++];
            }
            if (c == '(') {
                pos = p;
                return new Token(CALL_TYPE, nameValue);
            } else {
                pos = p-1;
                return new Token(CONST_TYPE, nameValue);                    
            }
        } else {
            throw SyntaxException.get("invalid character '" + c + "'", null); 
        }
    }
}
