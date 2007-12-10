// Copyright (C) 2007 Mihai Preda

import java.util.Vector;
import java.util.Hashtable;

class Codegen {
    private static final int MAX_STACK  = 32;
    private static final int MAX_CONSTS = 16;
    private static final int MAX_FUNCS  = 16;
    private static final int MAX_CODE   = 128;

    Hashtable[] builtins = {null, new Hashtable(), new Hashtable()};

    double stack[]  = new double[MAX_STACK];        
    double consts[] = new double[MAX_CONSTS];
    byte[] code = new byte[MAX_CODE];
    Fun[] funcs = new Fun[MAX_FUNCS];

    int sp, nConst, pc, nf;
    int arity;

    Fun tracer = new Fun(0, new byte[2], new double[1], new Fun[1]);

    void init() {
        sp     = -1;
        nConst = 0;
        pc     = 0;
        nf     = 0;
        arity  = 0;
    }

    Codegen() {
        for (int i = 0; i < VM.BYTECODE_END; ++i) {
            int arity = VM.builtinArity[i];
            if (arity == 1 || arity == 2) {
                builtins[arity].put(VM.opcodeName[i], new Byte((byte)i));
            }
        }
        init();
    }

    double lookupVar(String name) {
        //todo: implement
        return 0;
    }

    byte lookupBuiltin(String name, int arity) {
        if (arity == 1 || arity == 2) {
            Byte vmop = (Byte) builtins[arity].get(name);
            if (vmop != null) {
                return vmop.byteValue();
            }
        }
        return 0;
    }

    Fun lookupFun(String name, int arity) {
        //todo: implement
        return null;
    }

    Fun gen(Vector tokens) {
        init();
        int size = tokens.size();
        double lastConst = 0;
        Fun lastFun = null;

        for (int i = 0; i < size; ++i) {
            byte op;

            Token token = (Token) tokens.elementAt(i);
            TokenType type = token.type;
            switch (type.id) {
            case Lexer.NUMBER:
                op = Fun.CONST;
                lastConst = consts[nConst++] = token.value;
                break;

            case Lexer.CONST:
                String name = token.name;
                if (name.equals("rnd")) {
                    op  = Fun.RND;
                } else {
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
                    op = Fun.CONST;
                    lastConst = consts[nConst++] = lookupVar(name);
                }
                break;

            case Lexer.CALL:
                op = lookupBuiltin(token.name, token.arity);
                if (op <= 0) {
                    op = Fun.CALL;
                    lastFun = funcs[nf++] = lookupFun(token.name, token.arity);
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
            if (sp > oldSP || stack[sp] == Double.NaN) {
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
        
        double[] trimmedConsts = new double[nConst];
        System.arraycopy(consts, 0, trimmedConsts, 0, nConst);

        Fun[] trimmedFuncs = new Fun[nf];
        System.arraycopy(funcs, 0, trimmedFuncs, 0, nf);

        code[pc++] = Fun.RET;
        byte[] trimmedCode = new byte[pc];
        System.arraycopy(code, 0, trimmedCode, 0, pc);

        return pc > 1 ? 
            new Fun(arity, trimmedCode, trimmedConsts, trimmedFuncs) :
            null;
    }
}