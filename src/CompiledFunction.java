// Copyright (c) 2007, Carlo Teubner
// Available under the MIT License (see COPYING).

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import MoreMath;

public class CompiledFunction implements VMConstants {
    
    private static final int MAX_INST = 50, MAX_LITERALS = 50, MAX_STACKSIZE = 50;
    
    private int arity;
    private int inst_cnt, lit_cnt;
    private int[] inst;
    private double[] literals;
    
    private double[] stack;
    
    private static Random rng = new Random();
    
    public CompiledFunction() {
        inst = new int[MAX_INST];
        literals = new double[MAX_LITERALS];
        stack = new double[MAX_STACKSIZE];
        inst_cnt = lit_cnt = arity = 0;
    }

    public CompiledFunction(DataInputStream is) {
        try {
            arity = is.readInt();
            inst_cnt = is.readInt();
            inst = new int[inst_cnt];
            for (int i = 0; i < inst_cnt; ++i)
                inst[i] = is.readInt();
            lit_cnt = is.readInt();
            literals = new double[lit_cnt];
            for (int i = 0; i < lit_cnt; ++i)
                literals[i] = is.readDouble();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stack = new double[MAX_STACKSIZE];
    }

    public void write(DataOutputStream os) {
        try {
            os.writeInt(arity);
            os.writeInt(inst_cnt);
            for (int i = 0; i < inst_cnt; ++i)
                os.writeInt(inst[i]);
            os.writeInt(lit_cnt);
            for (int i = 0; i < lit_cnt; ++i)
                os.writeDouble(literals[i]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setArity(int arity) {
        this.arity = arity;
    }

    public void pushInstr(int op) {
        inst[inst_cnt++] = op;
    }
    
    public void pushLiteral(double literal) {
        literals[lit_cnt++] = literal;
    }

    public int arity() { return arity; }

    private double[] params;

    public double evaluate(double[] pars) {
        int s = -1, j = -1;
        for (int i = 0; i < inst_cnt; ++i) {
            int op = inst[i];
            if (SIN <= op && op <= ATAN) {
                stack[s] = trigEval(op, stack[s]);
                continue;
            }
            switch (op) {
            case LITERAL:
                stack[++s] = literals[++j];
                break;
            case PAR_X: case PAR_Y: case PAR_Z:
                stack[++s] = pars[op - FIRST_PAR];
                break;
            case VAR_A: case VAR_B: case VAR_C: case VAR_D:
            case VAR_M: case VAR_N: case VAR_F: case VAR_G: case VAR_H:
                stack[++s] = Variables.getNumber(op);
                break;
            case VARFUN_A: case VARFUN_B: case VARFUN_C: case VARFUN_D:
            case VARFUN_M: case VARFUN_N: case VARFUN_F: case VARFUN_G: case VARFUN_H:
            {
                CompiledFunction fn = Variables.getFunction(op - FIRST_VARFUN + FIRST_VAR);
                int fn_arity = fn.arity();
                if (params == null)
                    params = new double[3];
                for (int k = 0; k < fn_arity; ++k)
                    params[k] = stack[s - fn_arity + 1 + k];
                s -= fn_arity - 1;
                stack[s] = fn.evaluate(params);
                break;
            }
            case CONST_PI:
                stack[++s] = Math.PI;
                break;
            case CONST_E:
                stack[++s] = Math.E;
                break;
            case CONST_RND:
                stack[++s] = rng.nextDouble();
                break;
            case CONST_ANS:
                stack[++s] = History.ans;
                break;
            case UMINUS:
                stack[s] = -stack[s];
                break;
            case PLUS:
                stack[s-1] += stack[s];
                --s;
                break;
            case MINUS:
                stack[s-1] -= stack[s];
                --s;
                break;
            case TIMES:
                stack[s-1] *= stack[s];
                --s;
                break;
            case DIVIDE:
                stack[s-1] /= stack[s];
                --s;
                break;
            case MODULO:
                stack[s-1] %= stack[s];
                --s;
                break;
            case POWER:
                stack[s-1] = MoreMath.pow(stack[s-1], stack[s]);
                --s;
                break;
            case FACTORIAL:
                stack[s] = MoreMath.factorial(stack[s]);
                break;
            case ABS:
                stack[s] = Math.abs(stack[s]);
                break;
            case INT:
                stack[s] = MoreMath.trunc(stack[s]);
                break;
            case FRAC:
                stack[s] = stack[s] - MoreMath.trunc(stack[s]);
                break;
            case FLOOR:
                stack[s] = Math.floor(stack[s]);
                break;
            case CEIL:
                stack[s] = Math.ceil(stack[s]);
                break;
            case SIGN:
                stack[s] = stack[s] > 0. ? 1. : stack[s] < 0. ? -1. : 0.;
                break;
            case EXP:
                stack[s] = MoreMath.exp(stack[s]);
                break;
            case LOG:
                stack[s] = MoreMath.log(stack[s]);
                break;
            case LOG10:
                stack[s] = MoreMath.log10(stack[s]);
                break;
            case LOG2:
                stack[s] = MoreMath.log2(stack[s]);
                break;
            case SQRT:
                stack[s] = Math.sqrt(stack[s]);
                break;
            case CBRT:
                stack[s] = MoreMath.cbrt(stack[s]);
                break;
            case SINH:
                stack[s] = MoreMath.sinh(stack[s]);
                break;
            case COSH:
                stack[s] = MoreMath.cosh(stack[s]);
                break;
            case TANH:
                stack[s] = MoreMath.tanh(stack[s]);
                break;
            case ASINH:
                stack[s] = MoreMath.asinh(stack[s]);
                break;
            case ACOSH:
                stack[s] = MoreMath.acosh(stack[s]);
                break;
            case ATANH:
                stack[s] = MoreMath.atanh(stack[s]);
                break;
            case MIN:
                stack[s-1] = Math.min(stack[s-1], stack[s]);
                --s;
                break;
            case MAX:
                stack[s-1] = Math.max(stack[s-1], stack[s]);
                --s;
                break;
            case GCD:
                stack[s-1] = MoreMath.gcd(stack[s-1], stack[s]);
                --s;
                break;
            case COMB:
                stack[s-1] = MoreMath.comb(stack[s-1], stack[s]);
                --s;
                break;
            case PERM:
                stack[s-1] = MoreMath.perm(stack[s-1], stack[s]);
                --s;
                break;
            default:
                throw new Error("Internal VM error");
 //               assert false : "unknown opcode";
            }
        }
//        assert s == 0;
//        assert j == lit_cnt - 1;
        return stack[0];
    }

    private double trigEval(int op, double x) {
        double f = C.cfg.angleInRadians ? 1. : 180. / Math.PI;
        switch (op) {
        case SIN:   return MoreMath.isPiMultiple(x/f) ? 0 : Math.sin(x/f);
        case COS:   return MoreMath.isPiMultiple(x/f + MoreMath.PI_2) ? 0 : Math.cos(x/f);
        case TAN:   return Math.tan(x/f);
        case ASIN:  return MoreMath.asin(x) * f;
        case ACOS:  return MoreMath.acos(x) * f;
        case ATAN:  return MoreMath.atan(x) * f;
        }
        return 0;
    }

/*
    import java.lang.reflect.Field;
    import java.util.HashMap;

    private static HashMap<Integer, String> opnames;
    private static void toString_init() {
        if (opnames != null) return;
        Field[] fields = VMConstants.class.getFields();
        opnames = new HashMap<Integer, String>(fields.length);
        try {
            for (Field field : fields)
                opnames.put(field.getInt(null), field.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public String toString() {
        toString_init();
        StringBuilder sb = new StringBuilder("Code: ");
        for (int i = 0; i < inst_cnt; ++i) {
            sb.append(opnames.get(inst[i]));
            if (i < inst_cnt - 1) sb.append(", ");
        }
        sb.append("\nLiterals: ");
        for (int j = 0; j < lit_cnt; ++j) {
            sb.append(Double.toString(literals[j]));
            if (j < lit_cnt - 1) sb.append(", ");
        }
        return sb.toString();
    }
    
    public boolean equal(CompiledFunction other) {
        if (inst_cnt != other.inst_cnt) return false;
        if (lit_cnt != other.lit_cnt) return false;
        for (int i = 0; i < inst_cnt; ++i)
            if (inst[i] != other.inst[i]) return false;
        for (int i = 0; i < lit_cnt; ++i)
            if (literals[i] != other.literals[i]) return false;
        return true;
    }
*/
}