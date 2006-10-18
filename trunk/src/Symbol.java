abstract class Symbol {
    Symbol(String iniName) {
        name = iniName;
    }

    String name;
    abstract double eval(SymbolTable symbols, double params[]);

    static final double evaluate(SymbolTable symbols, String name, double params[]) {
        Symbol s = symbols.get(name);
        if (s == null) {
            throw new Error("Unknown id '" + name + "'");
        }
        return s.eval(symbols, params);
    }
}

class Constant extends Symbol {
    Constant(String name, double iniValue) {
        super(name);
        value = iniValue;
    }

    double value;
    double eval(SymbolTable symbols, double params[]) {
        if (params != null && params.length > 0) {
            throw new Error("Args for " + name + ": expected 0, got " + params.length);
        }
        return value;
    }
}

class BuiltinFun extends Symbol {
    static final int         
        SIN  = 1, COS  = 2, TAN  = 3,
        ASIN = 4, ACOS = 5, ATAN = 6,
        SINH = 7, COSH = 8, TANH = 9,
        ASINH = 10, ACOSH = 11, ATANH = 12,
        EXP  = 20, LOG  = 21, LOG10 = 22, LOG2  = 23,
        ABS  = 30, SQRT = 31, CBRT  = 32, POW   = 33;

    static final String names[] = {
        "sin",  "cos",  "tan",  "asin",  "acos",  "atan",
        "sinh", "cosh", "tanh", "asinh", "acosh", "atanh",
        "exp",  "log",  "ln",   "log10", "log2",  "pow",
        "abs",  "sqrt", "cbrt" 
    };
        
    /* keep 'codes' in sync with 'names' above */
    static final int codes[] = {
        SIN,  COS,  TAN,  ASIN,  ACOS,  ATAN,
        SINH, COSH, TANH, ASINH, ACOSH, ATANH,
        EXP,  LOG,  LOG,  LOG10, LOG2,  POW,
        ABS,  SQRT, CBRT 
    };

    static void init(SymbolTable ht) {
        for (int i = names.length - 1; i >= 0; --i) {
            ht.put(new BuiltinFun(names[i], codes[i]));
        } 
    }
    
    BuiltinFun(String name, int iniCode) {
        super(name);
        code = iniCode;
    }
        
    int code;
    double eval(SymbolTable symbols, double params[]) {
        double x = params[0];
        switch (code) {
        case SIN:   return Math.sin(x);
        case COS:   return Math.cos(x);
        case TAN:   return Math.tan(x);

        case ASIN:  return Math.asin(x);
        case ACOS:  return Math.acos(x);
        case ATAN:  return Math.atan(x);

        case SINH:  return MoreMath.sinh(x);
        case COSH:  return MoreMath.cosh(x);
        case TANH:  return MoreMath.tanh(x);

        case ASINH: return MoreMath.asinh(x);
        case ACOSH: return MoreMath.acosh(x);
        case ATANH: return MoreMath.atanh(x);

        case EXP:   return MoreMath.exp(x);
        case LOG:   return MoreMath.log(x);
        case LOG10: return MoreMath.log10(x);
        case LOG2:  return MoreMath.log2(x);
            
        case ABS:  return Math.abs(x);
        case SQRT: return Math.sqrt(x);
        case CBRT: return MoreMath.cbrt(x);
        case POW:  return MoreMath.pow(x, params[1]);
        }
        throw new Error("unhandled code " + code);
    }
}

class DefinedFun extends Symbol {
    DefinedFun(String name, String iniArgs[], String iniDef) {
        super(name);
        args = iniArgs;
        definition = iniDef;
    }

    String args[];
    String definition;
              
    double eval(SymbolTable symbols, double params[]) {
        //System.out.println(name + " " + args + " " + params);
        int n = args.length;
        int nParams = params == null ? 0 : params.length;
        if (nParams != n) {
            throw new Error("Args for " + name + 
                            ": expected " + n + ", got " + nParams);
        }
        
        Symbol saves[] = new Symbol[n];
        for (int i = 0; i < n; ++i) {
            saves[i] = symbols.put(new Constant(args[i], params[i]));
        }
        double ret = new Expr().parseNoDecl(definition);
        for (int i = 0; i < n; ++i) {
            if (saves[i] == null) {
                symbols.remove(args[i]);
            } else {
                symbols.put(saves[i]);
            }
        }
        return ret;
    }
}
