// Copyright (c) 2007 Carlo Teubner.
// Available under the MIT License (see COPYING).

import java.util.Hashtable;

public class Lexer {
    public final static int
        TOK_LPAREN = VM.CUSTOM + 0,
        TOK_RPAREN = VM.CUSTOM + 1,
        TOK_COMMA  = VM.CUSTOM + 2,
        TOK_PAR_T  = VM.CUSTOM + 3,
        TOK_END    = VM.CUSTOM + 4;
    
    private static Hashtable symnames;

    private static void initSymnames() {
        symnames = new Hashtable(50);
        a(VM.PAR_X, "x"); a(VM.PAR_Y, "y"); a(VM.PAR_Z, "z"); a(TOK_PAR_T, "t");
        a(VM.VAR_A, "a"); a(VM.VAR_B, "b"); a(VM.VAR_C, "c"); a(VM.VAR_D, "d");
        a(VM.VAR_M, "m"); a(VM.VAR_N, "n"); a(VM.VAR_F, "f"); a(VM.VAR_G, "g"); a(VM.VAR_H, "h");
        a(VM.CONST_PI, "pi"); a(VM.CONST_E, "e"); a(VM.CONST_ANS, "ans"); a(VM.CONST_RND, "rnd");
        a(VM.SIN, "sin"); a(VM.COS, "cos"); a(VM.TAN, "tan"); a(VM.ASIN, "asin"); a(VM.ACOS, "acos"); a(VM.ATAN, "atan");
        a(VM.SINH, "sinh"); a(VM.COSH, "cosh"); a(VM.TANH, "tanh"); a(VM.ASINH, "asinh"); a(VM.ACOSH, "acosh"); a(VM.ATANH, "atanh");
        a(VM.LOG, "ln"); a(VM.LOG10, "lg"); a(VM.LOG2, "lb");
        a(VM.SQRT, "sqrt"); a(VM.CBRT, "cbrt");
        a(VM.INT, "int"); a(VM.FRAC, "frac"); a(VM.ABS, "abs"); a(VM.FLOOR, "floor"); a(VM.CEIL, "ceil"); a(VM.SIGN, "sign");
        a(VM.MIN, "min"); a(VM.MAX, "max"); a(VM.GCD, "gcd"); a(VM.COMB, "C"); a(VM.PERM, "P");
        a(VM.PLOT, "plot"); a(VM.MAP, "map"); a(VM.PARPLOT, "par");
    }

    private static void a(int code, String str) {
        symnames.put(str, new Integer(code));
    }

    public static int getSymbol(String symname) {
        Integer i = (Integer) symnames.get(symname);
        return i == null ? -1 : i.intValue();
    }
    
    public static boolean isVariable(int symbol) {
        return VM.FIRST_VAR <= symbol && symbol <= VM.LAST_VAR;
    }

    public static boolean isBuiltinFunction(int symbol) {
        return VM.FIRST_FUNCTION <= symbol && symbol <= VM.LAST_FUNCTION;
    }

    public static boolean isPlotCommand(int symbol) {
        return VM.FIRST_PLOT_COMMAND <= symbol && symbol <= VM.LAST_PLOT_COMMAND;
    }

    public static int getBuiltinArity(int symbol) {
        if (symbol < VM.FIRST_FUNCTION)
            return 0;
        else if (symbol <= VM.LAST_FUNCTION1)
            return 1;
        else if (symbol <= VM.LAST_FUNCTION2)
            return 2;
        else if (symbol == VM.PLOT)
            return 3;
        else if (symbol == VM.MAP)
            return 5;
        else if (symbol == VM.PARPLOT)
            return 4;
        else
            return 0;
    }

    public static int plotFunctionArity(int symbol) {
        switch (symbol) {
        case VM.PLOT:
        case VM.PARPLOT:
            return 1;
        case VM.MAP:
            return 2;
        default:
            return -1;
        }
    }

    public static final boolean isLetter(char c) {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z');
    }

    public static boolean isAssignment(String str) {
        return str.length() >= 3 && str.charAt(1) == ':' && str.charAt(2) == '=';
    }


    public static boolean matchesPlotArity(int arity, String sw) {
        if (!(arity == 1 || arity == 2))
            return false;
        int[] cmdSlot = getPlotCommandAndSlot(sw);
        if (cmdSlot[0] == -1)
            return false;
        if (isPlotFunctionSlot(cmdSlot))
            return arity == plotFunctionArity(cmdSlot[0]);
        return false;
    }

    public static int getFunctionPlotCommand(String sw) {
        int[] cmdSlot = getPlotCommandAndSlot(sw);
        int cmd = cmdSlot[0];
        if (cmd != -1)
            if (!isPlotFunctionSlot(cmdSlot))
                cmd = -1;
        return cmd;
    }

    private static boolean isPlotFunctionSlot(int[] cmdSlot) {
        switch (cmdSlot[0]) {
        case VM.PLOT:
        case VM.MAP:
            return cmdSlot[1] == 0;
        case VM.PARPLOT:
            return cmdSlot[1] == 0 || cmdSlot[1] == 1;
        default:
            return false;
        }
    }

    public static int[] getPlotCommandAndSlot(String sw) {
        return getPlotCommandAndSlot(sw, sw.length());
    }

    private static int[] result;
    public static int[] getPlotCommandAndSlot(String sw, int len) {
        if (result == null)
            result = new int[2];
        int i;
        if (sw.startsWith("plot(")) {
            result[0] = VM.PLOT;
            i = 5;
        } else if (sw.startsWith("map(")) {
            result[0] = VM.MAP;
            i = 4;
        } else if (sw.startsWith("par(")) {
            result[0] = VM.PARPLOT;
            i = 4;
        } else {
            result[0] = -1;
            return result;
        }
        if (len >= i) {
            result[1] = 0;
            int parens = 0;
            for (; i < len; ++i)
                switch (sw.charAt(i)) {
                case '(': ++parens; break;
                case ')': --parens; break;
                case ',': if (parens == 0) ++result[1]; break;
                }
            if (parens < 0)
                result[1] = -1;
        } else {
            result[1] = -1;
        }
        return result;
    }

    private char[] input;
    private int pos, last_pos;
    private double tok_number;
    private boolean has_peeked;
    private int peek_tok;
    private double peek_number;
    private int peek_pos;

    Lexer() {
        if (symnames == null)
            initSymnames();
    }

    public void init(char[] input, int start, int end)
    {
        this.input = input;
        input[end] = '$';
        pos = start;
        last_pos = start - 1;
        has_peeked = false;        
    }

    int nextToken() throws SyntaxError
    {
        if (has_peeked) {
            last_pos = pos;
            pos = peek_pos;
            tok_number = peek_number;
            has_peeked = false;
            return peek_tok;
        } else
            return nextToken0();
    }
    
    int peekToken() throws SyntaxError
    {
        if (!has_peeked) {
            int pre_last_pos = last_pos;
            peek_tok = nextToken0();
            peek_pos = pos;
            pos = last_pos;
            last_pos = pre_last_pos;
            peek_number = tok_number;
            has_peeked = true;
        }
        return peek_tok;
    }

    double number() {
        return tok_number;
    }

    double peekNumber() {
//        assert has_peeked;
        return peek_number;
    }

    int curPos() {
        return pos;
    }
    
    int lastPos() {
        return last_pos;
    }

    private int nextToken0() throws SyntaxError
    {
        last_pos = pos;

        char c = input[pos];
        ++pos;
        switch (c) {
        case '+': return VM.PLUS;
        case '-': return VM.MINUS;
        case '*': return VM.TIMES;
        case '/': return VM.DIVIDE;
        case '^': return VM.POWER;
        case '%': return VM.MODULO;
        case '!': return VM.FACTORIAL;
        case '(': return TOK_LPAREN;
        case ')': return TOK_RPAREN;
        case ',': return TOK_COMMA;
        case '$': --pos; return TOK_END;
        }
        --pos;

        if ('0' <= c && c <= '9' || c == '.') {
            int start = pos;
            while ('0' <= c && c <= '9')
                c = input[++pos];
            if (c == '.') {
                c = input[++pos];
                while ('0' <= c && c <= '9')
                    c = input[++pos];
            }
            if (c == 'E') {
                c = input[++pos];
                if (c == '-')
                    c = input[++pos];
                while ('0' <= c && c <= '9')
                    c = input[++pos];
            }

            int end = pos;
            if (input[end-1] == 'E') 
                --end;
            if (input[start] == '.' && 
                       (end == start+1  ||
                        end >= start+2 && input[start+1] == 'E')) {
                // tok_number = 0.;
                throw Compiler.error;
            } else try {
                tok_number = Double.parseDouble(String.valueOf(input, start, pos - start));
            } catch (NumberFormatException e) {
                //Log.log("number: " + String.valueOf(input, start, pos - start));
                throw Compiler.error;
            }
            return VM.LITERAL;
        }

        if (isLetter(c)) {
            int start = pos;
            while (isLetter(c))
                c = input[++pos];
            Integer sym = (Integer) symnames.get(new String(input, start, pos));
            if (sym == null)
                throw Compiler.error;
            return sym.intValue();
        }

        if (c == '\u03c0') {
            ++pos;
            return VM.CONST_PI;
        }

        throw Compiler.error;
    }
}
