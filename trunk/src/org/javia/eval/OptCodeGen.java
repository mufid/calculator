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

/* Optimizing Code Generator
   Reads tokens in RPN (Reverse Polish Notation) order,
   and generates VM opcodes,
   doing constant-folding optimization.
 */

class OptCodeGen extends TokenConsumer {
    ByteStack code     = new ByteStack();
    DoubleStack consts = new DoubleStack();
    FunStack funcs     = new FunStack();

    double stack[]  = new double[Fun.MAX_STACK];        
    int sp = -1;

    double traceConsts[] = new double[1];
    Fun traceFuncs[] = new Fun[1];
    byte traceCode[] = new byte[1];
    Fun tracer = new Fun("<tracer>", 0, "<tracer>", traceCode, traceConsts, traceFuncs);

    String argNames[];
    SymbolTable symbols;

    void start(SymbolTable symbols) {
        sp = -1;
        //nf = 0;
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
            op = Fun.CONST;
            traceConsts[0] = token.value;
            break;
            
        case Lexer.CONST:
        case Lexer.CALL:
            String name = token.name;
            Symbol symbol = symbols.lookup(token.name, token.arity);
            if (symbol == null) {
                throw SyntaxException.get("undefined '" + token.name + "' with arity" + token.arity, token); 
            }
            if (symbol.op > 0) { // built-in
                op = symbol.op;
            } else if (symbol.fun != null) { // function call
                op = Fun.CALL;
                traceFuncs[0] = symbol.fun;
            } else { // variable reference
                op = Fun.CONST;
                traceConsts[0] = symbol.value;
            }
            break;
                        
        default:
            op = type.vmop;
            if (op <= 0) {
                throw new Error("wrong vmop");
            }
        }
        int oldSP = sp;
        traceCode[0] = op;
        if (op != Fun.RND) {
            sp = tracer.exec(stack, sp);
        } else {
            stack[++sp] = Double.NaN;
        }

        //constant folding
        if (!Double.isNaN(stack[sp])) {
            code.pop(oldSP + 1 - sp);
            consts.pop(oldSP + 1 - sp);
            consts.push(stack[sp]);
            op = VM.CONST;
        } else if (op == VM.CALL) {
            funcs.push(traceFuncs[0]);
        }
        code.push(op);
    }
    
    Fun getFun(String name, int arity, String source) {
        return new Fun(name, arity, source, code.toArray(), consts.toArray(), funcs.toArray());
    }
}
