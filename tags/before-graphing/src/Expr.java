// Copyright (c) 2006-2007, Mihai Preda

#include "defines.inc"

final class Result {
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
        Result result = new Result();
        int n = argv.length;
        for (int i = 0; i < n; ++i) {
            boolean ok = parser.parse(argv[i], result);
            LOG("   = " + result.value);
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

    static void define(Result def) {
        symbols.persistPut(def.arity==0 ? 
                    new Symbol(def.name, def.value) : 
                    new Symbol(def.name, def.arity, def.definition));
    }

    static boolean splitDefinition(String str, Result outResult) {
        outResult.reset();
        if (str.length() == 0) {
            outResult.errorPos = 0;
            return false;
        }
        if (str.length() >= 3 && str.charAt(1) == ':' && str.charAt(2) == '=') {
            char c = str.charAt(0);
            if ('a' <= c && c < 'x') {
                String name = String.valueOf(c);
                Symbol s = symbols.get(name);
                if (s == null || s.code == 0) {
                    outResult.name = name;
                    outResult.definition = str.substring(3);
                    return true;
                }
            }
            outResult.errorPos = 0;
            return false;
        } else {
            outResult.definition = str;
        }
        return true;
    }

    boolean parseSplitted(Result result) {
        isDefinition = result.name != null;
        try {
            result.value = parseThrow(result.definition);
        } catch (Error e) {
            result.value = 0;
            result.errorPos = tokenStart - nAddedBefore + 
                (result.name == null ? 0 : (result.name.length() + 2));
            //LOG("errorPos " + result.errorPos);
            result.arity = 0;
            return false;
        }
        result.arity = arity;
        return true;
    }

    boolean parse(String str, Result outResult) {
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
        return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z')  || c == '\u03c0';
    }

    private void scan() {
        tokenType = '$';
        tokenStart = pos;
        //LOG("pos " + pos + "; n " + n + "; c " + text[pos]);
        if (pos < n) { 
            char c = text[pos];
            if ((c >= '0' && c <= '9') || c == '.') {
                tokenType = '0';
                int start = pos;
                while (c >= '0' && c <= '9') {
                    c = text[++pos];
                }
                if (c == '.') {
                    c = text[++pos];
                    while (c >= '0' && c <= '9') {
                        c = text[++pos];
                    }
                }
                if (c == 'E') {
                    c = text[++pos];
                    if (c == '-') {
                        c = text[++pos];
                    }
                    while (c >= '0' && c <= '9') {
                        c = text[++pos];
                    }
                }
                
                int end = pos;
                if (text[end-1] == 'E') { 
                    --end; 
                }
                if (text[start] == '.' && 
                           (end == start+1  ||
                            end >= start+2 && text[start+1] == 'E')) {
                    tokenValue = 0;
                } else try {
                    tokenValue = Double.parseDouble(String.valueOf(text, start, pos - start));
                } catch (NumberFormatException e) {
                    LOG("number: " + String.valueOf(text, start, pos - start));
                    tokenType = 'e';
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
        //LOG("token " + tokenType);
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
            scan();
            if (symbol == null) {
                char c = id.charAt(0);
                if (isDefinition && id.length() == 1 && 'x' <= c && c <= 'z') {
                    arity = Math.max(arity, c - 'x' + 1);
                    value = 0;
                    break;
                } else {
                    throw error;
                }
            }
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
            value = symbols.evaluate(id, params);
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
