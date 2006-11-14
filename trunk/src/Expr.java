final class ExprResult {
    String name, definition;
    int arity;
    double value;
    int errorPos; //-1 when no error

    void reset() {
        name = definition = null;
        arity = 0;
        value = 0;
        errorPos = -1;
    }

    boolean hasValue() {
        return errorPos == -1 && arity == 0;
    }    
}

final class Expr {
    static SymbolTable symbols = new SymbolTable();
    static { 
        BuiltinFun.init(symbols);
        symbols.put(new Constant("\u03c0", Math.PI));
        symbols.put(new Constant("pi", Math.PI));
        symbols.put(new Constant("e",  Math.E));
        //symbols.put(new Constant("ans", 0));
        //symbols.put(new DefinedFun("hypot", 2, "sqrt(x*x+y*y)"));
        /*
        for (int i = 0; i < 3; ++i) {
            symbols.put(new Constant(DefinedFun.args[i], i)); //NaN
        }
        */
    }
    private static final Error error = new Error();
    private char text[] = new char[256];
    private int n = 0;

    private char tokenType;
    private double tokenValue;
    private StringBuffer tokenID = new StringBuffer();
    private int pos = 0, tokenStart = -1, nAddedBefore = 0;
    
    private int arity;
    private boolean isDefinition;

    public static void main(String argv[]) {
        Expr parser = new Expr();
        ExprResult result = new ExprResult();
        int n = argv.length;
        for (int i = 0; i < n; ++i) {
            boolean ok = parser.parse(argv[i], result);
            System.out.println("   = " + result.value);
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

    void define(ExprResult def) {
        symbols.put(def.arity == 0 ? 
            (Symbol) new Constant(def.name, def.value) : 
            (Symbol) new DefinedFun(def.name, def.arity, def.definition));
    }

    boolean splitDefinition(String str, ExprResult outResult) {
        outResult.reset();
        if (str.length() == 0) {
            outResult.errorPos = 0;
            return false;
        }

        if (str.length() > 3 && str.charAt(1) == ':' && str.charAt(2) == '=') {
            char c = str.charAt(0);
            if (!(('a' <= c && c <= 'c') || ('f' <= c && c <= 'h'))) {
                outResult.errorPos = 0;
                return false;
            }
            outResult.name = String.valueOf(c);
            outResult.definition = str.substring(3);
        } else {
            outResult.definition = str;
        }
        return true;
    }

    boolean parseSplitted(ExprResult result) {
        isDefinition = result.name != null;
        try {
            result.value = parseThrow(result.definition);
        } catch (Error e) {
            result.value = 0;
            result.errorPos = tokenStart - nAddedBefore;
            result.arity = 0;
            return false;
        }
        result.arity = arity;
        return true;
    }

    boolean parse(String str, ExprResult outResult) {
        if (!splitDefinition(str, outResult)) {
            return false;
        }
        return parseSplitted(outResult);
    }
    
    double parseThrow(String str) {
        int openParens = countParens(str);
        int p = 0;
        for (int i = openParens; i < 0; ++i) {
            text[p++] = '(';
        }
        nAddedBefore = p;
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
                char c = id.charAt(0);
                if (isDefinition && id.length() == 1 && 'x' <= c && c <= 'z') {
                    arity = Math.max(arity, c - 'x' + 1);
                    scan();
                    value = 0;
                    break;
                } else {
                    throw error;
                }
            }
            scan();
            double[] params = null;
            int symbolArity = symbol.arity;
            if (symbolArity > 0) {
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
            throw error;
        }
        //scan();
        if (tokenType == '!') {
            value = MoreMath.factorial(value);
            scan();
        }
        return value;
    }
}
