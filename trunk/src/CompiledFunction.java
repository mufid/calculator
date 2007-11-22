// Copyright (c) 2007 Carlo Teubner.
// Available under the MIT License (see COPYING).

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;

import org.javia.lib.*;

final public class CompiledFunction {

    private static final int MAX_INST = 100, MAX_LITERALS = 50, MAX_STACKSIZE = 50;

    static double[] stack = new double[MAX_STACKSIZE];
    private static int s = -1;  // stack[s] is top stack element (-1 means stack is empty)

    static CompiledFunction fragmentsX, fragmentsY;
    static int fragmentsXcnt, fragmentsYcnt;
    static double[] precompFragmentsX, precompFragmentsY;
    static int fragmentsXidx, fragmentsYidx;

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
            Log.log(e);
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
            Log.log(e);
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

    public int topInstr() {
        return inst[inst_cnt - 1];
    }

    public void popInstr() {
        --inst_cnt;
    }

    public void pushLiteral(double literal) {
        literals[lit_cnt++] = literal;
    }

    public int arity() { return arity; }

    public double evaluate() {
        // assert arity == 0;
        s = -1;
        evaluate0();
        return stack[0];
    }

    public double evaluate(double x) {
        // assert arity == 1;
        stack[s = 0] = x;
        //System.out.print("At start: ");
        //printStack();
        evaluate0();
        return stack[1];
    }

    public double evaluate(double x, double y) {
        // assert arity == 2;
        stack[0] = x;
        stack[s = 1] = y;
        evaluate0();
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
            switch (op) {
            case VM.LITERAL:
                stack[++s] = literals[++litIdx];
                break;
            case VM.PRECOMP_X_FRAG:
                stack[++s] = precompFragmentsX[++fragmentsXidx];
                break;
            case VM.PRECOMP_Y_FRAG:
                stack[++s] = precompFragmentsY[++fragmentsYidx];
                break;
            case VM.PAR_X: case VM.PAR_Y: case VM.PAR_Z:
                stack[++s] = stack[s0 - arity + 1 + op - VM.FIRST_PAR];
                break;
            case VM.VAR_A: case VM.VAR_B: case VM.VAR_C: case VM.VAR_D:
            case VM.VAR_M: case VM.VAR_N: case VM.VAR_F: case VM.VAR_G: case VM.VAR_H:
                stack[++s] = Variables.getNumber(op);
                break;
            case VM.VARFUN_A: case VM.VARFUN_B: case VM.VARFUN_C: case VM.VARFUN_D:
            case VM.VARFUN_M: case VM.VARFUN_N: case VM.VARFUN_F: case VM.VARFUN_G: case VM.VARFUN_H:
            {
                Variables.funcs[op - VM.FIRST_VARFUN].evaluate0();
                final int s1 = s - inst[++i];
                stack[s1] = stack[s];
                s = s1;
                break;
            }
            case VM.CONST_PI:
                stack[++s] = Math.PI;
                break;
            case VM.CONST_E:
                stack[++s] = Math.E;
                break;
            case VM.CONST_RND:
                stack[++s] = rng.nextDouble();
                break;
            case VM.CONST_ANS:
                stack[++s] = History.ans;
                break;
            case VM.UMINUS:
                stack[s] = -stack[s];
                break;
            case VM.PLUS:
                stack[s-1] += stack[s];
                --s;
                break;
            case VM.MINUS:
                stack[s-1] -= stack[s];
                --s;
                break;
            case VM.TIMES:
                stack[s-1] *= stack[s];
                --s;
                break;
            case VM.DIVIDE:
                stack[s-1] /= stack[s];
                --s;
                break;
            case VM.MODULO:
                stack[s-1] %= stack[s];
                --s;
                break;
            case VM.POWER:
                stack[s-1] = MoreMath.pow(stack[s-1], stack[s]);
                --s;
                break;
            case VM.SIN: case VM.COS: case VM.TAN: case VM.ASIN: case VM.ACOS: case VM.ATAN:
                stack[s] = trigEval(op, stack[s]);
                break;
            case VM.FACTORIAL:
                stack[s] = MoreMath.factorial(stack[s]);
                break;
            case VM.ABS:
                stack[s] = Math.abs(stack[s]);
                break;
            case VM.INT:
                stack[s] = MoreMath.trunc(stack[s]);
                break;
            case VM.FRAC:
                stack[s] = stack[s] - MoreMath.trunc(stack[s]);
                break;
            case VM.FLOOR:
                stack[s] = Math.floor(stack[s]);
                break;
            case VM.CEIL:
                stack[s] = Math.ceil(stack[s]);
                break;
            case VM.SIGN:
                stack[s] = stack[s] > 0. ? 1. : stack[s] < 0. ? -1. : 0.;
                break;
            case VM.EXP:
                stack[s] = MoreMath.exp(stack[s]);
                break;
            case VM.LOG:
                stack[s] = MoreMath.log(stack[s]);
                break;
            case VM.LOG10:
                stack[s] = MoreMath.log10(stack[s]);
                break;
            case VM.LOG2:
                stack[s] = MoreMath.log2(stack[s]);
                break;
            case VM.SQRT:
                stack[s] = Math.sqrt(stack[s]);
                break;
            case VM.CBRT:
                stack[s] = MoreMath.cbrt(stack[s]);
                break;
            case VM.SINH:
                stack[s] = MoreMath.sinh(stack[s]);
                break;
            case VM.COSH:
                stack[s] = MoreMath.cosh(stack[s]);
                break;
            case VM.TANH:
                stack[s] = MoreMath.tanh(stack[s]);
                break;
            case VM.ASINH:
                stack[s] = MoreMath.asinh(stack[s]);
                break;
            case VM.ACOSH:
                stack[s] = MoreMath.acosh(stack[s]);
                break;
            case VM.ATANH:
                stack[s] = MoreMath.atanh(stack[s]);
                break;
            case VM.MIN:
                stack[s-1] = Math.min(stack[s-1], stack[s]);
                --s;
                break;
            case VM.MAX:
                stack[s-1] = Math.max(stack[s-1], stack[s]);
                --s;
                break;
            case VM.GCD:
                stack[s-1] = MoreMath.gcd(stack[s-1], stack[s]);
                --s;
                break;
            case VM.COMB:
                stack[s-1] = MoreMath.comb(stack[s-1], stack[s]);
                --s;
                break;
            case VM.PERM:
                stack[s-1] = MoreMath.perm(stack[s-1], stack[s]);
                --s;
                break;
//            default:
//                throw new Error("Internal VM error");
//                assert false : "unknown opcode";
            }
        }
    }

    public static final int PASS = 0, FLAG = 1, FAIL = 2;

    /* This method checks for the following types of illegal behaviour:
     * (a) a variable being referred to as a number is actually a function, or vice-versa
     * (b) a function being called has the wrong arity
     * (c) a function calls itself, either directly or indirectly (since we have no
     *     conditional statements ('if'), this will always result in infinite recursion)
     * The parameter forbidden is a bitmask of variable indices which this function is
     * not allowed to call, because they are (direct or indirect) callers of this function,
     * which would -- due to the absence of an 'if'-like construct -- always lead to
     * infinite recursion.
     * If any of the above illegal behaviour occurs, FAIL is returned.
     * The parameter flaggable is a bitmask of variable indices which, if this function
     * calls, will result in a return value of FLAG (unless it returns FAIL for another reason).
     * If none of the above applies, PASS is returned.
     */
    public int check(int forbidden, int flaggable) {
        int result = PASS;
        for (int i = 0; i < inst_cnt; ++i) {
            int op = inst[i];
            if (VM.FIRST_VAR <= op && op <= VM.LAST_VAR) {
                if (!Variables.isNumber(op))
                    return FAIL;
            } else if (VM.FIRST_VARFUN <= op && op <= VM.LAST_VARFUN) {
                op -= VM.VARFUN_OFFSET;
                if (!Variables.isFunction(op))
                    return FAIL;
                final int varBit = 1 << (op - VM.FIRST_VAR);
                if ((forbidden & varBit) != 0)
                    return FAIL;
                if ((flaggable & varBit) != 0)
                    result = FLAG;
                CompiledFunction func = Variables.getFunction(op);
                if (inst[++i] != func.arity)
                    return FAIL;
                switch (func.check(forbidden | varBit, flaggable)) {
                case FAIL: return FAIL;
                case FLAG: result = FLAG;
                }
            }
        }
        return result;
    }

    private double trigEval(int op, double x) {
        final double f = Calc.cfg.trigFactor;
        switch (op) {
        case VM.SIN:   x /= f; return MoreMath.isPiMultiple(x) ? 0 : Math.sin(x);
        case VM.COS:   x /= f; return MoreMath.isPiMultiple(x + MoreMath.PI_2) ? 0 : Math.cos(x);
        case VM.TAN:   return Math.tan(x/f);
        case VM.ASIN:  return MoreMath.asin(x) * f;
        case VM.ACOS:  return MoreMath.acos(x) * f;
        case VM.ATAN:  return MoreMath.atan(x) * f;
        }
        return 0;
    }

    public static CompiledFunction trivialInline(CompiledFunction func) {
        if (func.inst_cnt != func.arity + 2)
            return func;
        int called_fn = func.inst[func.inst_cnt - 2];
        if (!(VM.FIRST_VARFUN <= called_fn && called_fn <= VM.LAST_VARFUN))
            return func;
        for (int i = 0; i < func.arity; ++i)
            if (func.inst[i] != VM.FIRST_PAR + i)
                return func;
        return Variables.funcs[called_fn - VM.FIRST_VARFUN];
    }

    private final static int DEPEND_NONE = 0, DEPEND_X = 1, DEPEND_Y = 2, DEPEND_MIX = 3;

    // Represents a contiguous fragment of the instructions, with associated literals.
    // Each fragment depends either only on x (type == DEPEND_X) or only on y (type == DEPEND_Y), but not both. 
    final static class Fragment implements Comparable
    {
        int inst_start, inst_end, lit_start, lit_end, type;

        Fragment(int inst_start, int inst_end, int lit_start, int lit_end, int type) {
            this.inst_start = inst_start;
            this.inst_end = inst_end;
            this.lit_start = lit_start;
            this.lit_end = lit_end;
            this.type = type;
            switch (type) {
            case DEPEND_X: ++fragmentsXcnt; break;
            case DEPEND_Y: ++fragmentsYcnt; break;
            }
        }

        public int compareTo(Object o) {
            return inst_start - ((Fragment) o).inst_start;
        }

        public String toString() {
            return (type == DEPEND_X ? "X" : "Y") + ":" + inst_start + "-" + inst_end + ";" + lit_start + "-" + lit_end;
        }
    }

    /* Fragment this function into portions that depend only on x and those that depend only on y.
     * All the X-fragments are concatenated into the static CompiledFunction fragmentsX (and similarly for Y).
     * Hence, after executing fragmentsX (or Y), the return values of all fragments
     * lie on the stack in the order that the fragments originally appeared in this function.
     * The X-fragments are replaced in this function by PRECOMP_X_FRAGMENT, which simply does
     * "stack[++s] = precompFragmentsX[++fragmentsXidx]" (and similarly for Y).
     * Thus, a caller needs to
     *  (a) call xyFragment;
     *  (b) evaluate CompiledFunction.fragmentsX (and Y) as required;
     *  (c) after each such evaluation, make a copy of the return values, which reside
     *      in stack[1...(fragmentsXcnt-1)] (and Y);
     *  (d) set precompFragmentsX to an array containing values computed in (c), and set fragmentsXidx
     *      to the first index into the array, minus one (and similarly for Y);
     *  (e) evaluate this function by calling evaluate() (no parameters, since all instances of x and y
     *      have been replaced).
     * The function paintMap in PlotCanvas does the above to gain a significant speed increase (depending
     * on the particular function) by first evaluating the X and Y fragments at all x and y coordinates
     * (of which there are only width + height as opposed to width * height), and then evaluating the
     * remaining, simplified function at all pixel values.
     */
    public void xyFragment() {
        int[] depends    = new int[MAX_STACKSIZE];
        int[] inst_start = new int[MAX_STACKSIZE];
        int[] inst_end   = new int[MAX_STACKSIZE];
        int[] lit_start  = new int[MAX_STACKSIZE];
        int[] lit_end    = new int[MAX_STACKSIZE];
        int last_lit = -1;

        Fragment[] fragments = new Fragment[inst_cnt];
        int fragment_cnt = 0;
        fragmentsXcnt = fragmentsYcnt = 0;

        s = -1;

        // Iterate through all instructions, maintaining information about each stack element.
        // depends describes its dependency on X and/or Y.
        // inst_start/end describes the (contiguous) range of instructions that have manipulated it.
        // lit_start/end describe the range of literals used in its computation.
        for (int i = 0; i < inst_cnt; ++i) {
            int op = inst[i];
            if (VM.FIRST_OP1 <= op && op <= VM.LAST_OP1 || VM.FIRST_FUNCTION1 <= op && op <= VM.LAST_FUNCTION1) {
                inst_end[s] = i;
                lit_end[s] = last_lit;
            } else if (VM.FIRST_OP2 <= op && op <= VM.LAST_OP2 || VM.FIRST_FUNCTION2 <= op && op <= VM.LAST_FUNCTION2) {
                final int d1 = depends[s - 1], d2 = depends[s], dx = d1 | d2;
                if (dx == DEPEND_MIX) {
                    if (d1 == DEPEND_X || d1 == DEPEND_Y)
                        fragments[fragment_cnt++] = new Fragment(inst_start[s-1], inst_end[s-1],
                                                                 lit_start[s-1], lit_end[s-1], d1);
                    if (d2 == DEPEND_X || d2 == DEPEND_Y)
                        fragments[fragment_cnt++] = new Fragment(inst_start[s], inst_end[s],
                                                                 lit_start[s], lit_end[s], d2);
                }
                --s;
                depends[s] = dx;
                inst_end[s] = i;
                lit_end[s] = last_lit;
            } else
                switch (op) {
                case VM.LITERAL:
                case VM.CONST_PI: case VM.CONST_E: case VM.CONST_ANS:
                case VM.VAR_A: case VM.VAR_B: case VM.VAR_C: case VM.VAR_D:
                case VM.VAR_M: case VM.VAR_N: case VM.VAR_F: case VM.VAR_G: case VM.VAR_H:
                    depends[++s] = DEPEND_NONE;
                    inst_start[s] = inst_end[s] = i;
                    lit_start[s] = last_lit + 1;
                    if (op == VM.LITERAL)
                        ++last_lit;
                    lit_end[s] = last_lit;
                    break;
                case VM.VARFUN_A: case VM.VARFUN_B: case VM.VARFUN_C: case VM.VARFUN_D:
                case VM.VARFUN_M: case VM.VARFUN_N: case VM.VARFUN_F: case VM.VARFUN_G: case VM.VARFUN_H:
                {
                    final int arity = inst[++i];
                    int dx;
                    if (Variables.funcs[op - VM.FIRST_VARFUN].isRandom())
                        dx = DEPEND_MIX;
                    else {
                        dx = 0;
                        for (int t = s - arity + 1; t <= s; ++t)
                            dx |= depends[t];
                    }
                    if (dx == DEPEND_MIX) {
                        for (int t = s - arity + 1; t <= s; ++t) {
                            final int d = depends[t];
                            if (d == DEPEND_X || d == DEPEND_Y)
                                fragments[fragment_cnt++] = new Fragment(inst_start[t], inst_end[t],
                                                                         lit_start[t], lit_end[t], d);
                        }
                    }
                    s -= (arity - 1);
                    depends[s] = dx;
                    inst_end[s] = i;
                    lit_end[s] = last_lit;
                    break;
                }
                case VM.PAR_X:
                    depends[++s] = DEPEND_X;
                    inst_start[s] = inst_end[s] = i;
                    lit_start[s] = last_lit + 1;
                    lit_end[s] = last_lit;
                    break;
                case VM.PAR_Y:
                    depends[++s] = DEPEND_Y;
                    inst_start[s] = inst_end[s] = i;
                    lit_start[s] = last_lit + 1;
                    lit_end[s] = last_lit;
                    break;
                case VM.CONST_RND:
                    depends[++s] = DEPEND_MIX;
                    inst_start[s] = inst_end[s] = i;
                    lit_start[s] = last_lit + 1;
                    lit_end[s] = last_lit;
                    break;
                default:
                    System.out.println("Unknown opcode: " + op);
                    return;
                }
        }

        final int d = depends[s];
        if (d == DEPEND_X || d == DEPEND_Y)
            fragments[fragment_cnt++] = new Fragment(inst_start[s], inst_end[s],
                                                     lit_start[s], lit_end[s], d);

        Util.sort(fragments, fragment_cnt);

/*
        System.out.println("Before: " + this);
        for (int i = 0; i < fragment_cnt; ++i)
            System.out.print(fragments[i] + "  ");
        System.out.println();
*/

        // Add dummy fragment at end to make sure any instructions/literals at the end are added back in.
        fragments[fragment_cnt++] = new Fragment(inst_cnt, inst_cnt, lit_cnt, lit_cnt, 0);

        if (fragmentsX == null) {
            fragmentsX = new CompiledFunction();
            fragmentsY = new CompiledFunction();
        } else {
            fragmentsX.init();
            fragmentsY.init();
        }
        fragmentsX.arity = fragmentsY.arity = 1;
        arity = 0;

        // Now that we know where the fragments are, we move them into fragmentsX/Y and replace them
        // by PRECOMP_X/Y_FRAG instructions.
        int new_inst_cnt = 0;
        int fragIdx = 0;
        Fragment frag = fragments[0]; 
        for (int i = 0; i < inst_cnt; ++i) {
            final int start = frag.inst_start, end = frag.inst_end;
            if (i < start) {
                inst[new_inst_cnt++] = inst[i];
            } else {
                final CompiledFunction fn = frag.type == DEPEND_X ? fragmentsX : fragmentsY;
                fn.inst[fn.inst_cnt++] = inst[i] == VM.PAR_Y ? VM.PAR_X : inst[i];
                if (i == start)
                    inst[new_inst_cnt++] = frag.type == DEPEND_X ? VM.PRECOMP_X_FRAG : VM.PRECOMP_Y_FRAG;
                if (i == end)
                    frag = fragments[++fragIdx];
            }
        }

        inst_cnt = new_inst_cnt;

        // Similarly, we move the fragments' literals into fragmentsX/Y.
        int new_lit_cnt = 0;
        fragIdx = 0;
        frag = fragments[0];
        for (int i = 0; i < lit_cnt; ++i) {
            while (frag.lit_start > frag.lit_end)
                frag = fragments[++fragIdx];                
            final int start = frag.lit_start, end = frag.lit_end;
            if (i < start) {
                literals[new_lit_cnt++] = literals[i];
            } else {
                final CompiledFunction fn = frag.type == DEPEND_X ? fragmentsX : fragmentsY;
                fn.literals[fn.lit_cnt++] = literals[i];
                if (i == end)
                    frag = fragments[++fragIdx];
            }
        }

        lit_cnt = new_lit_cnt;
/*
        System.out.println("After: " + this);
        System.out.println("X: " + fragmentsXcnt + " - " + fragmentsX);
        System.out.println("Y: " + fragmentsYcnt + " - " + fragmentsY);
*/
    }

    /* Returns true iff this function contains a CONST_RND invocation, directly or indirectly. */
    public boolean isRandom() {
        int op;
        for (int i = 0; i < inst_cnt; ++i) {
            switch (op = inst[i]) {
            case VM.CONST_RND: return true;
            case VM.VARFUN_A: case VM.VARFUN_B: case VM.VARFUN_C: case VM.VARFUN_D:
            case VM.VARFUN_M: case VM.VARFUN_N: case VM.VARFUN_F: case VM.VARFUN_G: case VM.VARFUN_H:
                if (Variables.funcs[op - VM.FIRST_VARFUN].isRandom())
                    return true;
                ++i;
            }
        }
        return false;
    }

/*
    public String toString() {
        StringBuffer sb = new StringBuffer("[");
        sb.append(arity).append("] ");
        for (int i = 0; i < inst_cnt; ++i) {
            sb.append(i).append(':').append(inst[i]);
            if (i < inst_cnt - 1)
                sb.append(", ");
        }
        sb.append(" ; ");
        for (int i = 0; i < lit_cnt; ++i) {
            sb.append(literals[i]);
            if (i < lit_cnt - 1)
                sb.append(", ");
        }
        return sb.toString();
    }
*/
}
