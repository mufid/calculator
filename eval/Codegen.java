// Copyright (C) 2007 Mihai Preda

import java.util.Vector;
import java.util.Hashtable;

class Codegen {
    static final int MAX_STACK  = 32;
    static final int MAX_CONSTS = 16;
    static final int MAX_FUNCS  = 16;
    static final int MAX_CODE   = 128;

    double stack[] = new double[MAX_STACK];
    int sp = -1;
        
    double consts[] = new double[MAX_CONSTS];
    int nConst = 0;

    byte[] code = new byte[MAX_CODE];
    int pc = 0;

    Fun[] funcs = new Fun[MAX_FUNCS];
    int nf = 0;

    Codegen() {
        def("sin",  1, Fun.SIN);
        def("cos",  1, Fun.COS);
        def("tan",  1, Fun.TAN);
        def("asin", 1, Fun.ASIN);
        def("acos", 1, Fun.ACOS);
        def("atan", 1, Fun.ATAN);
        
        def("sinh",  1, Fun.SINH);
        def("cosh",  1, Fun.COSH);
        def("tanh",  1, Fun.TANH);
        def("asinh", 1, Fun.ASINH);
        def("acosh", 1, Fun.ACOSH);
        def("atanh", 1, Fun.ATANH);

        def("exp",   1, Fun.EXP);
        def("ln",    1, Fun.LN);
        def("log10", 1, Fun.LOG10);
        def("log2",  1, Fun.LOG2);

        def("sqrt",  1, Fun.SQRT);
        def("cbrt",  1, Fun.CBRT);

        def("abs",   1, Fun.ABS);
        def("floor", 1, Fun.FLOOR);
        def("ceil",  1, Fun.CEIL);
        def("sign",  1, Fun.SIGN);
        
        def("min", 2, Fun.MIN);
        def("max", 2, Fun.MAX);

        def("gcd",  2, Fun.GCD);
        def("comb", 2, Fun.COMB);
        def("perm", 2, Fun.PERM);
    }

    Hashtable[] builtins = {null, new Hashtable(), new Hashtable()};

    private void def(String name, int arity, byte vmop) {
        builtins[arity].put(name, new Byte(vmop));
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
        Fun tracer = new Fun(0, new byte[2], new double[1], new Fun[1]);
        int size = tokens.size();
        byte op;
        double lastConst;
        Fun lastFun;
        for (int i = 0; i < size; ++i) {
            Token token = (Token) tokens.elementAt(i);
            TokenType type = token.type;
            switch (type.id) {
            case Lexer.NUMBER:
                op = Fun.CONST;
                lastConst = consts[nConst++] = token.value;
                break;

            case Lexer.CONST:
                if (token.name == "rnd") {
                    op  = Fun.RND;
                } else {
                    op = Fun.CONST;
                    lastConst = consts[nConst++] = lookupVar(token.name);
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
    }
}
