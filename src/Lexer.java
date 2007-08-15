// Copyright (c) 2007, Carlo Teubner
// Available under the MIT License (see COPYING).

import java.util.Hashtable;

public class Lexer implements VMConstants
{
    public final static int
        TOK_LPAREN = CUSTOM + 0,
        TOK_RPAREN = CUSTOM + 1,
        TOK_COMMA  = CUSTOM + 2,
        TOK_END    = CUSTOM + 3;
    
    private final static int MAX_INPUT_LEN = 200;

    private static Hashtable symnames;

    private static void initSymnames() {
        symnames = new Hashtable(50);
        a(PAR_X, "x"); a(PAR_Y, "y"); a(PAR_Z, "z");
        a(VAR_A, "a"); a(VAR_B, "b"); a(VAR_C, "c"); a(VAR_D, "d");
        a(VAR_M, "m"); a(VAR_N, "n"); a(VAR_F, "f"); a(VAR_G, "g"); a(VAR_H, "h");
        a(CONST_PI, "pi"); a(CONST_E, "e"); a(CONST_ANS, "ans"); a(CONST_RND, "rnd");
        a(SIN, "sin"); a(COS, "cos"); a(TAN, "tan"); a(ASIN, "asin"); a(ACOS, "acos"); a(ATAN, "atan");
        a(SINH, "sinh"); a(COSH, "cosh"); a(TANH, "tanh"); a(ASINH, "asinh"); a(ACOSH, "acosh"); a(ATANH, "atanh");
        a(EXP, "exp"); a(LOG, "ln"); a(LOG10, "lg"); a(LOG2, "lb");
        a(SQRT, "sqrt"); a(CBRT, "cbrt");
        a(INT, "int"); a(FRAC, "frac"); a(ABS, "abs"); a(FLOOR, "floor"); a(CEIL, "ceil"); a(SIGN, "sign");
        a(MIN, "min"); a(MAX, "max"); a(GCD, "gcd"); a(COMB, "C"); a(PERM, "P");
        a(PLOT, "plot"); a(MAP, "map"); a(PARPLOT, "par");
    }

    private static void a(int code, String str) {
        symnames.put(str, new Integer(code));
    }

    public static int getSymbol(String symname) {
        Integer i = (Integer) symnames.get(symname);
        return i == null ? -1 : i.intValue();
    }
    
    public static boolean isVariable(int symbol) {
        return FIRST_VAR <= symbol && symbol <= LAST_VAR;
    }

    public static int getBuiltinArity(int symbol) {
        if (symbol < FIRST_FUNCTION)
            return 0;
        else if (symbol <= LAST_FUNCTION1)
            return 1;
        else if (symbol <= LAST_FUNCTION2)
            return 2;
        else if (symbol == PLOT)
            return 3;
        else if (symbol == MAP)
            return 5;
        else if (symbol == PARPLOT)
            return 4;
        else
            return 0;
    }

    public static boolean isPlotCommand(int symbol) {
        return symbol == PLOT || symbol == MAP || symbol == PARPLOT;
    }

    public static int plotFunctionArity(int symbol) {
        switch (symbol) {
        case PLOT: return 1;
        case MAP: return 2;
        case PARPLOT: return 1;
        default: return -1;
        }
    }

    public static final boolean isLetter(char c) {
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')  || c == '\u03c0';
    }

    public static boolean isAssignment(String str) {
        return str.length() > 3 && str.charAt(1) == ':' && str.charAt(2) == '=';
    }

    public static boolean isPlotFunctionStart(String str) {
        if ("plot(".equals(str) || "map(".equals(str) || "par(".equals(str))
            return true;
        else if (str.startsWith("par(")) {
            // check that str has the form "par(F," for some F
            final int len = str.length();
            if (str.charAt(len - 1) != ',')
                return false;
            int parens = 0;
            for (int i = 4; i < len - 1; ++i)
                switch (str.charAt(i)) {
                case '(': ++parens; break;
                case ')': --parens; break;
                case ',': if (parens == 0) return false; break;
                }
            return true;
        } else
            return false;
    }


    private char[] input;
    private int input_len;
    private int pos;
    private int last_pos;
    private double tok_number;
    private boolean has_peeked;
    private int peek_tok;
    private double peek_number;

    Lexer() {
        input = new char[MAX_INPUT_LEN];
        if (symnames == null)
            initSymnames();
    }

    public void init(String str) {
        input_len = str.length() + 1;
        str.getChars(0, input_len - 1, input, 0);
        input[input_len - 1] = '$';
        pos = 0;
        last_pos = -1;
        has_peeked = false;        
    }

    int nextToken() throws SyntaxError
    {
        if (has_peeked) {
            tok_number = peek_number;
            has_peeked = false;
            return peek_tok;
        } else
            return nextToken0();
    }
    
    int peekToken() throws SyntaxError
    {
        if (!has_peeked) {
            peek_tok = nextToken0();
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

    int lastPos() {
        return last_pos;
    }

    private int nextToken0() throws SyntaxError
    {
        last_pos = pos;

        char c = input[pos];
        ++pos;
        switch (c) {
        case '+': return PLUS;
        case '-': return MINUS;
        case '*': return TIMES;
        case '/': return DIVIDE;
        case '^': return POWER;
        case '%': return MODULO;
        case '!': return FACTORIAL;
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
                tok_number = 0.;
            } else try {
                tok_number = Double.parseDouble(String.valueOf(input, start, pos - start));
            } catch (NumberFormatException e) {
                Log.log("number: " + String.valueOf(input, start, pos - start));
                throw Compiler.error;
            }
            return LITERAL;
        }

        if ('a' <= c && c <= 'z') {
            int start = pos;
            while ('a' <= c && c <= 'z')
                c = input[++pos];
            Integer sym = (Integer) symnames.get(String.valueOf(input, start, pos - start));
            if (sym == null)
                throw Compiler.error;
            return sym.intValue();
        }

        throw Compiler.error;
    }
}