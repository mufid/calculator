// Copyright (C) 2007 Mihai Preda

public class VM {
    public static final byte
        RET = 0,

    //ANS = 5,
        CONST_PI = 6,
        CONST_E = 7,

        ADD = 8,
        SUB = 9,
        MUL = 10,
        DIV = 11,
        MOD = 12,

        UMIN = 13,
        POWER = 14,
        FACT  = 15,

        RND = 16,

        SIN = 17,
        COS = 18,
        TAN = 19,
        ASIN = 20,
        ACOS = 21,
        ATAN = 22,
        
        EXP = 23,
        LN  = 24,
        LOG10 = 25,
        LOG2  = 26,
        
        SQRT = 27,
        CBRT = 28,

        SINH = 29,
        COSH = 30,
        TANH = 31,
        ASINH = 32,
        ACOSH = 33,
        ATANH = 34,
                
    //INT   = 35,
    //FRAC  = 36,
        ABS   = 37,
        FLOOR = 38,
        CEIL  = 39,
        SIGN  = 40,

        MIN = 41,
        MAX = 42,
        GCD = 43,
        COMB = 44,
        PERM = 45,

        LD0 = 46,
        LD1 = 47,
        LD2 = 48,
    //LD3 = 49,
        
        CALL0 = 50,
        CALL1 = 51,
        CALL2 = 52,
        CALL3 = 53,
       
        CALL4 = 54,
        CALL5 = 55,
        CALL6 = 56,
        CALL7 = 57,

        CONST0 = 58,
        CONST1 = 59,
        CONST2 = 60,
        CONST3 = 61,

        CONST4 = 62,
        CONST5 = 63,
        CONST6 = 64,
        CONST7 = 65,

        CONST8  = 66,
        CONST9  = 67,
        CONST10 = 68,
        CONST11 = 69
        ;

    static final int MAX_CONSTS = CONST11 - CONST0 + 1;
    static final int MAX_CODE   = 512;

    Fun(int arity, byte[] code, double[] consts, Fun[] funcs) {
        this.arity = arity;
        this.code   = code;
        this.consts = consts;
        this.funcs  = funcs;
    }

    double[] consts;
    private Fun[] funcs;
    private byte[] code;
    int arity; 

    int exec(double[] s, int p) {
        int op;
        int pc = 0; //program counter
        byte[] prog = mProg; //local variable
        int initialSP = p;
        double x, y, z;
        switch (arity) {
        case 3: z = s[p--];
        case 2: y = s[p--];
        case 1: x = s[p--];
        }
        final double angleFactor = 1; // 1/Calc.cfg.trigFactor;
        
        while (true) {
            op = code[pc++];
            switch (op) {
            case CONST0: s[++p] = c0; break;
            case CONST1: s[++p] = c1; break;
            case CONST2: s[++p] = c2; break;
            case CONST3: s[++p] = c3; break;
                
            case CONST4: s[++p] = c4; break;
            case CONST5: s[++p] = c5; break;
            case CONST6: s[++p] = c6; break;
            case CONST7: s[++p] = c7; break;
                
            case ANS:      s[++p] = 0;           break; //todo: fix ans
            case CONST_PI: s[++p] = MoreMath.PI; break;
            case CONST_E:  s[++p] = MoreMath.E;  break;
            case RND:      s[++p] = rng.nextDouble(); break;
                    
            case ADD: s[--p] += s[p+1]; break;
            case SUB: s[--p] -= s[p+1]; break;
            case MUL: s[--p] *= s[p+1]; break;
            case DIV: s[--p] /= s[p+1]; break;
            case MOD: s[--p] %= s[p+1]; break;
            case POWER: s[--p] = MoreMath.pow(s[p], s[p+1]); break;
                
            case UMIN: s[p] = -s[p]; break;
            case FACT: s[p] = MoreMath.factorial(s[p]); break;                   
                
            case SIN:  s[p] = MoreMath.sin(s[p] * angleFactor); break;
            case COS:  s[p] = MoreMath.cos(s[p] * angleFactor); break;
            case TAN:  s[p] = MoreMath.tan(s[p] * angleFactor); break;
            case ASIN: s[p] = MoreMath.asin(s[p]) / angleFactor; break;
            case ACOS: s[p] = MoreMath.acos(s[p]) / angleFactor; break;
            case ATAN: s[p] = MoreMath.atan(s[p]) / angleFactor; break;
                    
            case EXP:   s[p] = MoreMath.exp(s[p]); break;
            case LN:    s[p] = MoreMath.log(s[p]); break;
            case LOG10: s[p] = MoreMath.log10(s[p]); break;
            case LOG2:  s[p] = MoreMath.log2(s[p]); break;
                
            case SQRT: s[p] = MoreMath.sqrt(s[p]); break;
            case CBRT: s[p] = MoreMath.cbrt(s[p]); break;
                
            case SINH: s[p] = MoreMath.sinh(s[p]); break;
            case COSH: s[p] = MoreMath.cosh(s[p]); break;
            case TANH: s[p] = MoreMath.tanh(s[p]); break;
            case ASINH: s[p] = MoreMath.asinh(s[p]); break;
            case ACOSH: s[p] = MoreMath.acosh(s[p]); break;
            case ATANH: s[p] = MoreMath.atanh(s[p]); break;
                    
                //INT: s[p] = MoreMath.trunc(s[p]);
                //FRAC:
            case ABS:   s[p] = Math.abs(s[p]); break;
            case FLOOR: s[p] = Math.floor(s[p]); break;
            case CEIL:  s[p] = Math.ceil(s[p]); break;
            case SIGN:  s[p] = s[p] > 0 ? 1 : s[p] < 0 ? -1 : 0; break;
                
            case MIN:  s[--p] = Math.min(s[p], s[p+1]); break;
            case MAX:  s[--p] = Math.min(s[p], s[p+1]); break;
            case GCD:  s[--p] = MoreMath.gcd(s[p], s[p+1]); break;
            case COMB: s[--p] = MoreMath.comb(s[p], s[p+1]); break;
            case PERM: s[--p] = MoreMath.perm(s[p], s[p+1]); break;
                    
            case LD0: s[++p] = x; break;
            case LD1: s[++p] = y; break;
            case LD2: s[++p] = z; break;
                    
            case CALL0: 
                f0.exec(s, p); 
                p -= f0.arity - 1; 
                break;
                
            case CALL1: 
                f1.exec(s, p); 
                p -= f1.arity - 1; 
                break;
                
            case CALL2: case CALL3: case CALL4: case CALL5: case CALL6: case CALL7: {
                Function f = funcs[prog[pc-1] - CALL2];
                f.exec(s, p);
                p -= f.arity - 1; //the return value is on top of stack
                break;
            }
                
            case RET:
                return p;
            }
        }
    }
}
