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
import org.javia.lib.Log;

class Compiler extends TokenConsumer {
    private static final int MAX_STACK  = 32;
    private static final int MAX_CONSTS = 16;
    private static final int MAX_FUNCS  = 16;
    private static final int MAX_CODE   = 128;

    double stack[]  = new double[MAX_STACK];        
    double consts[] = new double[MAX_CONSTS];
    byte[] code = new byte[MAX_CODE];
    Fun[] funcs = new Fun[MAX_FUNCS];
    double traceConsts[] = new double[1];
    Fun traceFuncs[] = new Fun[1];
    Fun tracer = new Fun("<tracer>", 0, "<tracer>", new byte[]{VM.RET, VM.RET}, traceConsts, traceFuncs);

    int sp = -1;
    int nConst, pc, nf;
    //int arity;

    String argNames[];
    SymbolTable symbols;

    void start(SymbolTable symbols) {
        sp = -1;
        nConst = pc = nf = 0;
        this.symbols = symbols;
    }
    
    void push(Token token) {
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
                traceFuncs[0] = funcs[nf++] = symbol.fun;
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
        sp = tracer.trace(stack, sp, op);
        if (op == Fun.RND) {
            stack[sp] = Double.NaN;
        }

        //constant folding
        if (!Double.isNaN(stack[sp])) {
            if (op == VM.CALL) {
                --nf;
            }
            //Log.log("op " + VM.opcodeName[op] + " nConst " + nConst);
            pc -= oldSP + 1 - sp;
            nConst -= oldSP + 1 - sp;
            consts[nConst++] = stack[sp];
            op = VM.CONST;
            //Log.log("op " + VM.opcodeName[op] + " nConst " + nConst);
        }
        code[pc++] = op;
    }
    
    Fun getFun(String name, int arity, String source) {
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

        return new Fun(name, arity, source, trimmedCode, trimmedConsts, trimmedFuncs);
    }
}
