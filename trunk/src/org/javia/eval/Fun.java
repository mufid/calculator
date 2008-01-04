/*
 * Copyright (C) 2007 Mihai Preda.
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

import java.util.Random;

public class Fun extends VM {
    private static Random random = new Random();
    private double[] consts;
    private Fun[] funcs;
    private byte[] code;

    public final int arity; 
    String name, source;

    Fun(String name, int arity, String source, byte[] code, double[] consts, Fun[] funcs) {
        this.name   = name;
        this.arity  = arity;
        this.source = source;
        this.code   = code;
        this.consts = consts;
        this.funcs  = funcs;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("Fun ").append(source);
        buf.append("; sub-funcs ").append(funcs.length);
        /*
        if (source.length() > 0) {
            buf.append("\n  source: '").append(source).append("'");
        }
        */
        buf.append("\n  consts: ");
        for (int i = 0; i < consts.length; ++i) {
            buf.append("\n    ").append(consts[i]);
        }
        buf.append("\n  code: ");
        for (int i = 0; i < code.length; ++i) {
            buf.append("\n    ").append(opcodeName[code[i]]);
        }
        return buf.toString();
    }

    int trace(double[] stack, int sp, byte op) {
        code[0]   = op;
        return exec(stack, sp);
    }

    private static double[] globalStack = new double[128];

    public double eval() {
        if (arity != 0) {
            throw new Error("eval() on arity " + arity);
        }
        int sp = exec(globalStack, -1);
        if (sp != 0) {
            throw new Error("unexpected SP: " + sp);
        }
        return globalStack[0];
    }

    public int exec(double[] s, int p) {
        int pc = 0; //program counter
        byte[] code = this.code;
        int initialSP = p;
        int constp = 0;
        int funp   = 0;
        final double angleFactor = 1; // 1/Calc.cfg.trigFactor;
        
        // arguments, read from stack on exec entry
        // we don't use an array in order to avoid the dynamic allocation (new)
        // @see Compiler.MAX_ARITY
        double a0, a1, a2, a3, a4;
        a0 = a1 = a2 = a3 = a4 = Double.NaN;
        switch (arity) {
        case 5: a4 = s[p--];
        case 4: a3 = s[p--];
        case 3: a2 = s[p--];
        case 2: a1 = s[p--];
        case 1: a0 = s[p--];
        }

        while (true) {
            switch (code[pc++]) {
            case CONST: s[++p] = consts[constp++]; break;
            case CALL: { 
                Fun fun = funcs[funp++];
                p = fun.exec(s, p); 
                //p -= fun.arity - 1; 
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
                    
            case LOAD0: s[++p] = a0; break;
            case LOAD1: s[++p] = a1; break;
            case LOAD2: s[++p] = a2; break;
            case LOAD3: s[++p] = a3; break;
            case LOAD4: s[++p] = a4; break;

            case RET: return p;
            }
        }
    }
}
