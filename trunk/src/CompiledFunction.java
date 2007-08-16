// Copyright (c) 2007, Carlo Teubner
// Available under the MIT License (see COPYING).

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

public class CompiledFunction implements VMConstants {
    
    private static final int MAX_INST = 50, MAX_LITERALS = 50, MAX_STACKSIZE = 50;

    private static double[] stack = new double[MAX_STACKSIZE];
    private static int s = -1;  // stack[s] is top stack element (-1 means stack is empty)

    private int arity;
    private int inst_cnt, lit_cnt;
    private int[] inst;
    private double[] literals;
    
    private static Random rng = new Random();
    
    public CompiledFunction() {
        inst = new int[MAX_INST];
        literals = new double[MAX_LITERALS];
        init();
    }

    public CompiledFunction(CompiledFunction from) {
        arity = from.arity;
        inst_cnt = from.inst_cnt;
        lit_cnt = from.lit_cnt;
        inst = new int[inst_cnt];
        literals = new double[lit_cnt];
        System.arraycopy(from.inst, 0, inst, 0, inst_cnt);
        System.arraycopy(from.literals, 0, literals, 0, lit_cnt);
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
    }

    public void init() {
        inst_cnt = lit_cnt = arity = 0;
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

    public void pushInstr(int op, int arg) {
        inst[inst_cnt++] = op;
        inst[inst_cnt++] = arg;
    }

    public void pushLiteral(double literal) {
        literals[lit_cnt++] = literal;
    }

    public int arity() { return arity; }

    public double evaluate() {
        // assert arity == 0;
        evaluate0();
        s = -1;
        return stack[0];
    }

    public double evaluate(double x) {
        // assert arity == 1;
        stack[s = 0] = x;
        //System.out.print("At start: ");
        //printStack();
        evaluate0();
        s = -1;
        return stack[1];
    }

    public double evaluate(double x, double y) {
        // assert arity == 2;
        stack[0] = x;
        stack[s = 1] = y;
        evaluate0();
        s = -1;
        return stack[2];
    }

    /*
    private void printStack() {
        System.out.print("Stack: ");
        for (int i = 0; i <= s; ++i) {
            System.out.print(stack[i]);
            if (i < s) System.out.print(", ");
        }
        System.out.println();
    }
    */

    private void evaluate0() {
        final int s0 = s;
        int litIdx = -1;
        int op;
        for (int i = 0; i < inst_cnt; ++i) {
            op = inst[i];
            //System.out.println("op: " + op);
            if (SIN <= op && op <= ATAN) {
                stack[s] = trigEval(op, stack[s]);
                continue;
            }
            switch (op) {
            case LITERAL:
                stack[++s] = literals[++litIdx];
                break;
            case PAR_X: case PAR_Y: case PAR_Z:
                stack[++s] = stack[s0 - arity + 1 + op - FIRST_PAR];
                break;
            case VAR_A: case VAR_B: case VAR_C: case VAR_D:
            case VAR_M: case VAR_N: case VAR_F: case VAR_G: case VAR_H:
                stack[++s] = Variables.getNumber(op);
                break;
            case VARFUN_A: case VARFUN_B: case VARFUN_C: case VARFUN_D:
            case VARFUN_M: case VARFUN_N: case VARFUN_F: case VARFUN_G: case VARFUN_H:
            {
                //System.out.print("Before call: ");
                //printStack();
                Variables.funcs[op - FIRST_VARFUN].evaluate0();  // bypass Variables.getFunction for max performance
                int s1 = s - inst[++i];
                stack[s1] = stack[s];
                s = s1;
                //System.out.print("After call: ");
                //printStack();
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
//        assert j == lit_cnt - 1;
        //return stack[s--];
    }

    /* This method checks for the following types of illegal behaviour:
     * (a) a variable being referred to as a number is actually a function, or vice-versa
     * (b) a function being called has the wrong arity
     * (c) a function calls itself, either directly or indirectly (since we have no
     *     conditional statements ('if'), this will always result in infinite recursion)
     * The parameter callHistory is a bitmask of variable indices which this function is
     * not allowed to call, because they are (direct or indirect) callers of this function,
     * which would -- due to the absence of an 'if'-like construct -- always lead to
     * infinite recursion.
     */
    public boolean check(int callHistory) {
        for (int i = 0; i < inst_cnt; ++i) {
            int op = inst[i];
            if (FIRST_VAR <= op && op <= LAST_VAR) {
                if (!Variables.isNumber(op))
                    return false;
            } else if (FIRST_VARFUN <= op && op <= LAST_VARFUN) {
                op -= VARFUN_OFFSET;
                if (!Variables.isFunction(op))
                    return false;
                int varBit = 1 << (op - FIRST_VAR);
                if ((callHistory & varBit) != 0)
                    return false;
                CompiledFunction func = Variables.getFunction(op);
                if (inst[++i] != func.arity)
                    return false;
                if (!func.check(callHistory | varBit))
                    return false;
            }
        }
        return true;
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
