// Copyright (C) 2007 Mihai Preda

import java.util.Random;

public class Fun extends VM {
    static Random random = new Random();

    double[] consts;
    private Fun[] funcs;
    private byte[] code;
    int arity; 
    String source;

    Fun(int arity, byte[] code, double[] consts, Fun[] funcs) {
        this.source = "";
        this.arity  = arity;
        this.code   = code;
        this.consts = consts;
        this.funcs  = funcs;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Function with arity ").append(arity);
        buf.append("; sub-funcs ").append(funcs.length);
        if (source.length() > 0) {
            buf.append("\n  source: '").append(source).append("'");
        }
        buf.append("\n  consts: ");
        for (int i = 0; i < consts.length; ++i) {
            buf.append("\n    ").append(consts[i]);
        }
        buf.append("\n  code: ");
        for (int i = 0; i < consts.length; ++i) {
            buf.append("\n    ").append(opcodeName[code[i]]);
        }
        return buf.toString();
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
            case PI:  s[++p] = Math.PI; break;
            case E:   s[++p] = Math.E;  break;
            case RND: s[++p] = random.nextDouble(); break;
                    
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
