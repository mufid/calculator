// Copyright (C) 2007 Mihai Preda

import java.util.Vector;

class Codegen {
    static final int MAX_STACK  = 100;

    byte tracer[] = {Fun.RET, Fun.RET};
    double stack[] = new double[MAX_STACK];
    int sp = -1;
        
    double consts[] = new double[MAX_CONSTS];
    int nConst = 0;

    byte[] code = new byte[MAX_CODE];
    int pc = 0;

    Codegen() {
    }

    double lookupVar(String name) {
        //todo: implement
        return 0;
    }

    void pushConst(double value) {
        code[pc++]  = Fun.CONST0 + nConst;
        consts[nConst++] = value;
        stack[++sp] = token.value;
    }

    addInstr(byte intsr) {
        code[pc++] = instr;
        tracer[0]  = instr;
        exec(
    }

    Fun gen(Vector tokens) {
        int size = tokenVect.size();
        for (int i = 0; i < size; ++i) {
            Token token = tokens.elementAt(i);
            TokenType type = token.type;
            switch (type.id) {
            case Lexer.NUMBER:
                pushConst(token.value);
                break;

            case Lexer.CONST:
                if (token.name == 'rnd') {
                    code[pc++]  = Fun.RND;
                    stack[++sp] = Double.NaN;
                } else {
                    pushConst(lookupVar(token.name));
                }
                break;

            case Lexer.CALL:
                
                break;
                
            }
        }
    }
}


    /*
    Token[] vectorToArray(Vector vect) {
        Token[] tokens = new Token[vect.size()];
        vect.copyInto(tokens);
        return tokens;
        }*/
