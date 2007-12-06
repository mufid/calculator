// Copyright (C) 2007 Mihai Preda

public class VM {
    public static final byte
        CONST0,
        CONST1,
        CONST2,
        CONST3,

        ANS,
        CONST_PI,
        CONST_E,

        ADD,
        SUB,
        MUL,
        DIV,
        MOD,

        UMIN,
        POWER,
        FACT,

        RND,

        SIN,
        COS,
        TAN,
        ASIN,
        ACOS,
        ATAN,
        
        EXP,
        LN,
        LOG10,
        LOG2,
        
        SQRT,
        CBRT,

        SINH,
        COSH,
        TANH,
        ASINH,
        ACOSH,
        ATANH,
                
        INT,
        FRAC,
        ABS,
        FLOOR,
        CEIL,
        SIGN,

        MIN,
        MAX,
        GCD,
        COMB,
        PERM,

        LD0,
        LD1,
        LD2,
        LD3,
        
        CALL0,
        CALL1,
        CALL2,
        CALL3,
       
        CALL4,
        CALL5,
        CALL6,
        CALL7,

        CONST4,
        CONST5,
        CONST6,
        CONST7;

    private double   c0, c1, c2, c3, c4, c5, c6, c7;
    private Function f0, f1;
    private Function[] funcs; // f2, f3, f4, f5, f6, f7;
    private byte[] mProg;
    int arity; 

    double exec(double[] s, int p) {
        int pc = 0; //program counter
        byte[] prog = mProg; //local variable
        int initialSP = p;
        double x, y, z;
        switch (arity) {
        case 3: z = s[p--];
        case 2: y = s[p--];
        case 1: x = s[p--];
        }

        switch (arity) {
        case 4: t = 
        case 3:
        case 2:
        case 1:
        case 0:
        }
        final double angleFactor = 1; // 1/Calc.cfg.trigFactor;
        try {
            while (true) {
                switch (prog[pc++]) {
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
                    
                INT: s[p] = MoreMath.trunc(s[p]);
                FRAC:
                ABS:
                FLOOR:
                CEIL:
                SIGN:
                    
                MIN:
                MAX:
                GCD:
                COMB:
                PERM:
                    
                LD0:
                LD1:
                LD2:
                LD3:
                    
                CALL0: 
                    f0.exec(s, p); 
                    p -= f0.arity - 1; 
                    break;
 
                CALL1: 
                    f1.exec(s, p); 
                    p -= f1.arity - 1; 
                    break;

                CALL2: CALL3: CALL4: CALL5: CALL6: CALL7: {
                        Function f = funcs[prog[pc-1] - CALL2];
                        f.exec(s, p);
                        p -= f.arity - 1; //the return value is on top of stack
                    }                
                }
            }
        }
    } catch (ArrayIndexOutOfBoundsException e) {

    }
}
