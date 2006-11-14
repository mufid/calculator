final class Expr {
    static SymbolTable symbols = new SymbolTable();
    static { 
        BuiltinFun.init(symbols);
        symbols.put(new Constant("\u03c0", Math.PI));
        symbols.put(new Constant("pi", Math.PI));
        symbols.put(new Constant("e",  Math.E));
        //symbols.put(new Constant("ans", 0));
        symbols.put(new DefinedFun("hypot", 2, "sqrt(x*x+y*y)"));
        for (int i = 0; i < 3; ++i) {
            symbols.put(new Constant(DefinedFun.args[i], i)); //NaN
        }
    }
    private static final Error error = new Error();

    char text[] = new char[256];
    int n = 0;

    char tokenType;
    double tokenValue;
    StringBuffer tokenID = new StringBuffer();
    int pos = 0, tokenStart = -1;
    
    int arity;

    public static void main(String argv[]) {
        Expr parser = new Expr();
        int n = argv.length;
        for (int i = 0; i < n; ++i) {
            double v = parser.parse(argv[i]);
            System.out.println("   = " + v);
        }
    }

    Expr() {
    }

    static int countParens(String str) {
        int openParens = 0;
        int p = 0;
        while ((p = str.indexOf('(', p)+1) != 0) { 
            ++openParens; 
        }
        p = 0;
        while ((p = str.indexOf(')', p)+1) != 0) { 
            --openParens; 
        }
        return openParens;
    }

    double parse(String str) {
        int openParens = countParens(str);
        int p = 0;
        for (int i = openParens; i < 0; ++i) {
            text[p++] = '(';
        }
        int size = str.length();
        str.getChars(0, size, text, p);
        p += size;
        for (int i = 0; i < openParens; ++i) {
            text[p++] = ')';
        }
        text[p++] = '$';
        n = p;
        pos = 0;
        tokenStart = -1;
        tokenType = '$';
        arity = 0;
        return parseWholeExpression();
    }

    /*
    double parseDecl(String str) {
        int equalPos = str.indexOf('=');
        if (equalPos != -1) {
            String text1 = str.substring(0, equalPos);
            String text2 = str.substring(equalPos + 1);
            //!!! init(text1);
            scan();
            if (tokenType != 'a') {
                throw error; //new Error("Def: expected id, found " + text1);
            }
            String id = tokenID.toString();
            scan();
            if (tokenType != '$') {
                throw error; //new Error("Def: expected id, found " + text1);
            }
            //!!! init(text2);
            arity = 0;
            double val = parseWholeExpression();
            if (arity == 0) {
                symbols.put(new Constant(id, val));
                return val;
            } else {
                symbols.put(new DefinedFun(id, arity, text2));
                return Double.NaN;
                //throw error;
            }
        } else {
            return parseNoDecl(str);
        }
    }
    */

    static final boolean isLetter(char c) {
        return ('a' <= c && c <= 'z') || c == '_' || c == '\u03c0';
    }

    private void scan() {
        tokenType = '$';
        tokenStart = pos;
        //System.out.println("pos " + pos + "; n " + n + "; c " + text[pos]);
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
            } else {
                if (isLetter(c)) {
                    tokenType = 'a';
                    tokenID.setLength(0);
                    while (isLetter(c) || Character.isDigit(c)) {
                        tokenID.append(c);
                        ++pos;
                        c = text[pos];
                    }
                } else {
                    //+-*/%^()
                    ++pos;
                    tokenType = c;
                }
            }
        }
    }

    private final double parseWholeExpression() {
        double ret = parseExpression();
        if (tokenType != '$') {
            throw error; //new Error(); //tokenType);
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
            if (tokenType != ')') {
                throw error; //new Error();
            }
            scan();
            break;
            
        case '0': 
            value = tokenValue;
            scan();
            break;

        case 'a':
            String id = tokenID.toString();
            Symbol symbol = symbols.get(id);
            if (symbol == null) {
                throw error; //new Error();
            }
            scan();
            double[] params = null;
            int symbolArity = symbol.arity;
            if (symbolArity == 0) {
                char c = id.charAt(0);
                if (id.length() == 1 && 'x' <= c && c <= 'z') {
                    arity = Math.max(arity, c - 'x' + 1);
                }
            } else {
                if (tokenType != '(') {
                    throw error;
                }
                params = new double[symbolArity];
                for (int i = 0; i < symbolArity; ++i) {
                    params[i] = parseExpression();
                    if (tokenType != ((i == symbolArity - 1) ? ')' : ',')) {
                        throw error;
                    }
                }
                scan();
            }
            value = Symbol.evaluate(symbols, id, params);
            break;
            
        default:
            throw error; //new Error(); //tokenType);
        }
        //scan();
        if (tokenType == '!') {
            value = MoreMath.factorial(value);
            scan();
        }
        return value;
    }
}
