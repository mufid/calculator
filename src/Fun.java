// Copyright (C) 2007 Mihai Preda

import java.util.Random;

public class Fun {
    public static final byte
        RET   = 0,
        CONST = 1,
        CALL  = 2,

    //ANS = 5,
        CONST_PI = 6,
        CONST_E  = 7,

        ADD = 8,
        SUB = 9,
        MUL = 10,
        DIV = 11,
        MOD = 12,

        UMIN  = 13,
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

        LDX = 46,
        LDY = 47,
        LDZ = 48
        ;

    static Random random = new Random();

    double[] consts;
    private Fun[] funcs;
    private byte[] code;
    int arity; 

    Fun(int arity, byte[] code, double[] consts, Fun[] funcs) {
        this.arity  = arity;
        this.code   = code;
        this.consts = consts;
        this.funcs  = funcs;
    }

    int trace(double[] stack, int sp, byte op, double lastConst, Fun lastFun) {
        consts[0] = lastConst;
        funcs[0]  = lastFun;
        code[0]   = op;
        code[1]   = RET;
        return exec(stack, sp);
    }

    private static double[] globalStack = new double[128];

    double eval() {
        int sp = exec(globalStack, -1);
        if (sp != 0) {
            throw new Error("unexpected SP: " + sp);
        }
        return globalStack[0];
    }

    int exec(double[] s, int p) {
        int pc = 0; //program counter
        byte[] code = this.code;
        int initialSP = p;
        double x, y, z;
        int constp = 0;
        int funp   = 0;
        final double angleFactor = 1; // 1/Calc.cfg.trigFactor;

        x = y = z = Double.NaN;
        switch (arity) {
        case 3: z = s[p--];
        case 2: y = s[p--];
        case 1: x = s[p--];
        }

        while (true) {
            switch (code[pc++]) {
            case CONST: s[++p] = consts[constp++]; break;
            case CALL: { 
                Fun fun = funcs[funp++];
                fun.exec(s, p); 
                p -= fun.arity - 1; 
                break;
            }
                
                //case ANS:      s[++p] = 0;           break; //todo: fix ans
            case CONST_PI: s[++p] = Math.PI; break;
            case CONST_E:  s[++p] = Math.E;  break;
            case RND:      s[++p] = random.nextDouble(); break;
                    
            case ADD: s[--p] += s[p+1]; break;
            case SUB: s[--p] -= s[p+1]; break;
            case MUL: s[--p] *= s[p+1]; break;
            case DIV: s[--p] /= s[p+1]; break;
            case MOD: s[--p] %= s[p+1]; break;
            case POWER: s[--p] = MoreMath.pow(s[p], s[p+1]); break;
                
            case UMIN: s[p] = -s[p]; break;
            case FACT: s[p] = MoreMath.factorial(s[p]); break;                   
                
            case SIN:  s[p] = Math.sin(s[p] * angleFactor); break;
            case COS:  s[p] = Math.cos(s[p] * angleFactor); break;
            case TAN:  s[p] = Math.tan(s[p] * angleFactor); break;
            case ASIN: s[p] = MoreMath.asin(s[p]) / angleFactor; break;
            case ACOS: s[p] = MoreMath.acos(s[p]) / angleFactor; break;
            case ATAN: s[p] = MoreMath.atan(s[p]) / angleFactor; break;
                    
            case EXP:   s[p] = MoreMath.exp(s[p]); break;
            case LN:    s[p] = MoreMath.log(s[p]); break;
            case LOG10: s[p] = MoreMath.log10(s[p]); break;
            case LOG2:  s[p] = MoreMath.log2(s[p]); break;
                
            case SQRT: s[p] = Math.sqrt(s[p]); break;
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
                    
            case LDX: s[++p] = x; break;
            case LDY: s[++p] = y; break;
            case LDZ: s[++p] = z; break;

            case RET: return p;
            }
        }
    }
}
