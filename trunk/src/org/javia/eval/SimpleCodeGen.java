/*
 * Copyright (C) 2008 Mihai Preda.
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

/* Non-optimizing Code Generator
   Reads tokens in RPN (Reverse Polish Notation) order,
   and generates VM opcodes,
   without any optimization.
 */

class SimpleCodeGen extends TokenConsumer {
    ByteStack code      = new ByteStack();
    DoubleStack consts  = new DoubleStack();
    FunctionStack funcs = new FunctionStack();

    String argNames[];
    SymbolTable symbols;

    void start(SymbolTable symbols) {
        code.clear();
        consts.clear();
        funcs.clear();
        this.symbols = symbols;
    }
    
    void push(Token token) throws SyntaxException {
        byte op;
        TokenType type = token.type;
        switch (type.id) {
        case Lexer.NUMBER:
            op = VM.CONST;
            consts.push(token.value);
            break;
            
        case Lexer.CONST:
        case Lexer.CALL:
            Symbol symbol = symbols.lookup(token.name, token.arity);
            if (symbol == null) {
                throw SyntaxException.get("undefined '" + token.name + "' with arity" + token.arity, token); 
            }
            if (symbol.op > 0) { // built-in
                op = symbol.op;
            } else if (symbol.fun != null) { // function call
                op = VM.CALL;
                funcs.push(symbol.fun);
            } else { // variable reference
                op = VM.CONST;
                consts.push(symbol.value);
            }
            break;
                        
        default:
            op = type.vmop;
            if (op <= 0) {
                throw new Error("wrong vmop");
            }
        }
        code.push(op);
    }
    
    CompiledFunction getFun(String name, int arity, String source) {
        return new CompiledFunction(name, arity, source, code.toArray(), consts.toArray(), funcs.toArray());
    }
}
