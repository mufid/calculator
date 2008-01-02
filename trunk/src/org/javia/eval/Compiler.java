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
import java.util.Hashtable;

class Compiler {
    private static final int MAX_STACK  = 32;
    private static final int MAX_CONSTS = 16;
    private static final int MAX_FUNCS  = 16;
    private static final int MAX_CODE   = 128;

    static final int MAX_ARITY = 5;

    double stack[]  = new double[MAX_STACK];        
    double consts[] = new double[MAX_CONSTS];
    byte[] code = new byte[MAX_CODE];
    Fun[] funcs = new Fun[MAX_FUNCS];
    Fun tracer = new Fun(0, new byte[2], new double[1], new Fun[1]);

    int sp = -1;
    int nConst, pc, nf;
    int arity;

    String argNames[];
    SymbolTable symbols;

    void start(SymbolTable newSymbols, String argNames[]) {
        sp = -1;
        nConst = pc = nf = arity = 0;

        if (symbols != null) {
            symbols.popFrame();
        }
        symbols = newSymbols;
        symbols.pushFrame();
        arity   = argNames.length;
        if (arity > MAX_ARITY) {
            throw new Error("Arity too large " + arity);
        }
        for (int i = 0; i < arity; ++i) {
            symbols.add(new Symbol(argNames[i], -1, (byte)(VM.LOAD0 + i)));
        }
    }
    
    void push(Token token) {
        double lastConst = 0;
        Fun lastFun = null;
        byte op;
        TokenType type = token.type;
        switch (type.id) {
        case Lexer.NUMBER:
            op = Fun.CONST;
            lastConst = consts[nConst++] = token.value;
            break;
            
        case Lexer.CONST:
        case Lexer.CALL:
            String name = token.name;
            Symbol symbol = symbols.lookup(token.name, token.arity);
            if (symbol == null) {
                throw new SyntaxException("undefined '" + token.name + "' with arity" + token.arity, token); 
            }
            if (symbol.op > 0) { // built-in
                op = symbol.op;
            } else if (symbol.fun != null) { // function call
                op = Fun.CALL;
                lastFun = funcs[nf++] = symbol.fun;
            } else { // variable reference
                op = Fun.CONST;
                lastConst = consts[nConst++] = symbol.value;
            }
            break;
                        
        default:
            op = type.vmop;
            if (op <= 0) {
                throw new Error("wrong vmop");
            }
        }
        int oldSP = sp;
        sp = tracer.trace(stack, sp, op, lastConst, lastFun);
        if (op == Fun.RND) {
            stack[sp] = Double.NaN;
        }
        if (sp > oldSP || Double.isNaN(stack[sp])) {
            code[pc++] = op;
        } else {
            //constant folding
            pc -= oldSP - sp;
            nConst -= oldSP - sp;
            consts[nConst-1] = stack[sp];
            if (code[pc-1] != Fun.CONST) {
                throw new Error("Expected CONST on fold");
            }
        }
    }
    
    Fun getFun() {
        if (pc <= 0) {
            throw new Error("empty fun");
        }

        double[] trimmedConsts = new double[nConst];
        System.arraycopy(consts, 0, trimmedConsts, 0, nConst);

        Fun[] trimmedFuncs = new Fun[nf];
        System.arraycopy(funcs, 0, trimmedFuncs, 0, nf);

        code[pc++] = Fun.RET;
        byte[] trimmedCode = new byte[pc];
        System.arraycopy(code, 0, trimmedCode, 0, pc);

        symbols.popFrame();
        symbols = null;
        return new Fun(arity, trimmedCode, trimmedConsts, trimmedFuncs);
    }
}


    /*
    void start(String argNames[]) {
        clear();

         todo: load args symbols
            int arg;
            if (name.equals("rnd")) {
                op  = Fun.RND;
            } else if ((arg = lookupArg(name)) >= 0) {
                if (effectiveArity < arg + 1) {
                    effectiveArity = arg + 1;
                }
                op = (byte) (Fun.LOAD0 + arg);
            } else {

            }
    }
    */

    /*
                if (name.length() == 1) {
                    char c = name.charAt(0);
                    if (c == 'x' || c == 'y' || c == 'z') {
                        op = (byte) (Fun.LDX + (c - 'x'));
                        if (arity < c - 'x' + 1) {
                            arity = c - 'x' + 1;
                        }
                        break;
                    }
                }
    */
