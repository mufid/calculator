import java.util.Hashtable;

class Expr {
    Object test[] = {"foo", "bar"};

    static SymbolTable symbols = new SymbolTable();
    static { 
        BuiltinFun.init(symbols);
        symbols.put(new Constant("\u03c0", Math.PI));
        symbols.put(new Constant("e",  Math.E));
        //symbols.put(new Constant("ans", 0));
        symbols.put(new DefinedFun("hypot", new String[]{"x", "y"}, "sqrt(x*x+y*y)")); 
    }

    char text[] = null;
    int n = 0;

    char tokenType;
    double tokenValue;
    StringBuffer tokenID = new StringBuffer();
    int pos = 0, tokenStart = -1;
    
    boolean insideFunDef;
    int arity;

    final static double fact(double n) {
        double r = n;
        while (--n > 0) {
            r *= n;
        }
        return r;
    }
    
    public static void main(String argv[]) {
        /*
        Expr parser = new Expr();
        int n = argv.length;
        for (int i = 0; i < n; ++i) {
            double v = parser.parseDecl(argv[i]);
            System.out.println("   = " + v);
        }
        */
    }

    Expr() {
    }

    private void init(String s) {
        text = s.toCharArray();
        n = text.length;
        pos = 0;
        tokenStart = -1;
        tokenType = '$';
    }

    double parseNoDecl(String str) {
        init("((" + str + '$');
        insideFunDef = false;
        return parseWholeExpression();
    }

    double parseDecl(String str) {
        int equalPos = str.indexOf('=');
        if (equalPos != -1) {
            String text1 = str.substring(0, equalPos);
            String text2 = str.substring(equalPos + 1);
            init(text1);
            scan();
            if (tokenType != 'a') {
                throw new Error("Def: expected id, found " + text1);
            }
            String id = tokenID.toString();
            scan();
            if (tokenType != '$') {
                throw new Error("Def: expected id, found " + text1);
            }
            init(text2);
            insideFunDef = true;
            arity = 0;
            double val = parseWholeExpression();
            if (arity == 0) {
                symbols.put(new Constant(id, val));
                return val;
            } else {
                String args[] = new String[arity];
                char ca[] = {'x'}; 
                for (int i = 0; i < arity; ++i) {
                    ca[0] = (char)('x' + i);
                    args[i] = new String(ca);
                    //System.out.println("arg " + args[i]);
                }
                symbols.put(new DefinedFun(id, args, text2));
                return 0;
            }
        } else {
            return parseNoDecl(str);
        }
    }

    static final boolean isLetter(char c) {
        return ('a' <= c && c <= 'z') || c == '_' || c == '\u03c0';
    }

    private void scan() {
        tokenType = '$';
        tokenStart = pos;
        if (pos < n) { 
            char c = text[pos];
            if ((c >= '0' && c <= '9') || c == '.') {
                boolean ok = false;
                tokenType = '0';
                int start = pos;
                while (c >= '0' && c <= '9') {
                    ok = true;
                    c = text[++pos];
                }
                if (c == '.') {
                    c = text[++pos];
                    while (c >= '0' && c <= '9') {
                        ok = true;
                        c = text[++pos];
                    }
                }
                if (!ok) {
                    tokenStart = pos;
                    tokenType = 'x';
                    return;
                }
                if (c == 'E') {
                    c = text[++pos];
                    if (c == '-') {
                        c = text[++pos];
                    }
                    if (!('0' <= c && c <= '9')) {
                        tokenStart = pos;
                        tokenType = 'x';
                        return;
                    }
                    while (c >= '0' && c <= '9') {
                        c = text[++pos];
                    }
                }
                try {
                    tokenValue = Double.parseDouble(new String(text, start, pos - start));
                } catch (NumberFormatException e) {

                }
            } else try {
                c = Character.toLowerCase(c);
                if (isLetter(c)) {
                    tokenType = 'a';
                    tokenID.setLength(0);
                    while (isLetter(c) || Character.isDigit(c)) {
                        tokenID.append(c);
                        ++pos;
                        c = Character.toLowerCase(text[pos]);
                    }
                } else {
                    //+-*/%^()
                    ++pos;
                    tokenType = c;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
            }
        }
        /*
        if (tokenType == '0') {
            System.out.println(tokenValue);
        } else if (tokenType == 'a') {
            System.out.println(tokenID.toString());
        } else {
            System.out.println(tokenType);
        }
        */
    }

    private final double parseWholeExpression() {
        double ret = parseExpression();
        if (tokenType != '$') {
            throw new Error(); //tokenType);
        }
        return ret;
    }

    private final double parseExpression() {
        double ret = parseAdd();
        return ret;
    }

    private final double parseAdd() {
        double left = parseMul();
        while (tokenType == '+' || tokenType == '-') {
            char save = tokenType;
            double right = parseMul();
            left += (save == '+') ? right : -right;
        }
        return left;
    }
    
    private final double parseMul() {
        double left = parseExp();
        double right;
        while (tokenType == '*' || tokenType == '/' || tokenType == '%') {
            char save = tokenType;
            right = parseExp();
            switch (save) {
            case '*': left *= right; break;
            case '/': left /= right; break;
            case '%': left = left % right; break;
            }
        }
        return left;
    }
    
    private final double parseExp() {
        double left = parseUnary();
        if (tokenType == '^') {
            double right = parseExp();
            left = MoreMath.pow(left, right); //MoreMath
        }
        return left;
    }

    private final double parseUnary() {
        double value;
        scan();
        //System.out.println("token " + tokenType);
        switch (tokenType) {
            
        case '-': 
            return -parseUnary();

        case '(': 
            value = parseExpression();
            if (tokenType == ')') {
                scan();
            }
            return value;
            
        case '0': 
            value = tokenValue; 
            break;

        case 'a':
            String id = tokenID.toString();
            double[] params = null;
            scan();
            if (tokenType == '(') {
                double vect[] = new double[8];
                int pos = 0;
                do {
                    //scan();
                    //System.out.println(" " + tokenType);
                    vect[pos++] = parseExpression();
                } while (tokenType == ',');
                params = new double[pos];
                System.arraycopy(vect, 0, params, 0, pos);
                if (tokenType == ')') { scan(); }
            }
            if (insideFunDef && (id.length() == 1)) {
                int p = "xyz".indexOf(id.charAt(0));
                if (p != -1) {
                    if (arity <= p) { arity = p + 1; }
                }
                return 0.;
            }
            return Symbol.evaluate(symbols, id, params);
            
        default:
            throw new Error(); //tokenType);
        }
        scan();
        return value;
    }
}
