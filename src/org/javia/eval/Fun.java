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
import org.javia.lib.Log;

public class Fun extends VM {
    static final int INI_STACK_SIZE =  16;
    static final int MAX_STACK_SIZE = 128; // if stack ever grows above this something is wrong

    private static final double[] NO_ARGS = new double[0];
    private static Random random = new Random();

    private double[] consts;
    private Fun[] funcs;
    private byte[] code;
    public final int arity; 
    public final String name, source;

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
        buf.append("Fun ").append('"').append(source).append('"');
        int cpos = 0, fpos = 0;
        for (int i = 0; i < code.length; ++i) {
            byte op = code[i];
            buf.append("\n    ").append(opcodeName[op]);
            if (op == VM.CONST) {
                buf.append(' ').append(consts[cpos++]);
            } else if (op == VM.CALL) {
                buf.append(' ').append(funcs[fpos++].name);
            }
        }
        if (cpos != consts.length) {
            buf.append("\nuses only ").append(cpos).append(" consts out of ").append(consts.length);
        }
        if (fpos != funcs.length) {
            buf.append("\nuses only ").append(cpos).append(" funcs out of ").append(consts.length);
        }
        return buf.toString();
    }

    public double eval() throws ArityException {
        return eval(NO_ARGS);
    }

    public double eval(double args[]) throws ArityException {
        double stack[] = new double[INI_STACK_SIZE];
        while (true) {
            try {
                return eval(args, stack);
            } catch (ArrayIndexOutOfBoundsException e) {
                if (stack.length >= MAX_STACK_SIZE) {
                    throw e;
                }
                Log.log("Fun " + name + ": growing stack to " + (stack.length << 1)); 
                stack = new double[stack.length << 1];
            }
        }
    }

    public double eval(double args[], double stack[]) throws ArityException {
        // check if arity matches the number of arguments passed (args.length)
        if (!(arity == args.length || (arity == -1 && args.length == 0))) {
            throw new ArityException("Expected " + arity + " arguments, got " + args.length);
        }

        System.arraycopy(args, 0, stack, 0, args.length);
        int sp = exec(stack, -1);
        if (sp != 0) {
            throw new Error("stack pointer after eval: expected 0, got " + sp);
        }
        return stack[0];
    }
    
    int exec(double[] s, int p) {
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

        int codeLen = code.length;
        for (int pc = 0; pc < codeLen; ++pc) {
            //Log.log("p " + p);

            switch (code[pc]) {
            case CONST: s[++p] = consts[constp++]; break;
            case CALL: { 
                Fun fun = funcs[funp++];
                p = fun.exec(s, p); 
                //p -= fun.arity - 1; 
                break;
            }
                
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
            }
        }
        return p;
    }
}
