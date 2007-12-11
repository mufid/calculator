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

public class Token {
    final TokenType type;
    double value = 0; //for NUMBER only
    String name  = null; //for CONST & CALL
    int arity = 0; //for CALL only

    Token(TokenType type) {
        this.type = type;
    }

    Token(int id, String name, int priority, int assoc, boolean isOperand, int vmop) {
        type = new TokenType(id, name, priority, assoc, isOperand, vmop);
    }

    Token(TokenType type, double value) {
        this(type);
        this.value = value;
    }

    Token(TokenType type, String alpha) {
        this(type);
        this.name  = alpha;
    }
    

    public String toString() {
        switch (type.id) {
        case Lexer.NUMBER:
            return "" + value;
        case Lexer.CALL:
            return name + '(' + arity + ')';
        case Lexer.CONST:
            return name;
        }
        return type.name;
    }
}
