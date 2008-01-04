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

import java.util.Vector;

class DeclarationParser extends TokenConsumer {
    static final String NO_ARGS[] = new String[0];
    static final int MAX_ARITY = 5;

    String name;
    String argNames[];
    int arity = -1;
    Vector args = new Vector();

    //@Override
    void start() {
        name = null;
        argNames = null;
        arity = -1;
        args.setSize(0);
    }

    //@Override
    void push(Token token) {
        switch (token.type.id) {
        case Lexer.CALL:
            if (name == null) {
                name = token.name;
            } else {
                throw SyntaxException.get("repeated CALL in declaration", token);
            }
            break;

        case Lexer.CONST:
            args.addElement(token.name);
            if (args.size() > MAX_ARITY) {
                throw SyntaxException.get("Arity too large " + arity, token);
            }
            break;

        case Lexer.RPAREN:            
        case Lexer.COMMA:
            break;

        default:
            throw SyntaxException.get("invalid token in declaration", token);
        }
    }
    
    //@Override
    void done() {
        arity = args.size();
        if (arity == 0) {
            argNames = NO_ARGS;
        } else {
            argNames = new String[args.size()];
            args.copyInto(argNames);
            args.setSize(0);
        }
    }
}
