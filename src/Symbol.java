import java.util.*;

abstract class Symbol {
    Symbol(String iniName) {
        name = iniName;
    }
    String name;
    boolean isFun   = false;
    boolean isValue = false;

    abstract double eval(SymbolTable symbols, double params[]);
    static final double evaluate(SymbolTable symbols, String name, double params[]) {
        Symbol symb = symbols.get(name);
        if (symb == null) {
            throw new Error("Unknown id '" + name + "'");
        }
        return symb.eval(symbols, params);
    }
}

class Constant extends Symbol {
    Constant(String name, double iniValue) {
        super(name);
        value = iniValue;
        isValue = true;
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
        EXP   = 20, LOG  = 21, LOG10 = 22, LOG2  = 23,
        SQRT = 31, CBRT  = 32, POW   = 33,
        INT   = 40, FRAC = 41, ABS = 42,
        FLOOR = 43, CEIL = 44, SIGN = 45,
        MIN   = 46, MAX  = 47, GCD  = 48,
        COMB  = 49, PERM = 50, RND  = 51,
        FACT  = 52;

    static final String names[] = {
        "sin",  "cos",  "tan",  "asin",  "acos",  "atan",
        "sinh", "cosh", "tanh", "asinh", "acosh", "atanh",
        "exp",  "log",  "ln",   "log10", "log2",  "pow",
        "sqrt", "cbrt",
        "\u221a", "\u221b",
        "int", "frac", "abs",
        "floor", "ceil", "sign",
        "min", "max", "gcd",
        "comb", "perm", "rnd",
    };
        
    /* keep 'codes' in sync with 'names' above */
    static final int codes[] = {
        SIN,  COS,  TAN,  ASIN,  ACOS,  ATAN,
        SINH, COSH, TANH, ASINH, ACOSH, ATANH,
        EXP,  LOG,  LOG,  LOG10, LOG2,  POW,
        SQRT, CBRT,
        SQRT, CBRT,
        INT,  FRAC, ABS,
        FLOOR,CEIL, SIGN,
        MIN,  MAX,  GCD,
        COMB, PERM, RND,
    };

    static void init(SymbolTable ht) {
        for (int i = names.length - 1; i >= 0; --i) {
            ht.put(new BuiltinFun(names[i], codes[i]));
        } 
    }
    
    BuiltinFun(String name, int iniCode) {
        super(name);
        code = iniCode;
        isFun = true;
    }
        
    int code;
    Random random = new Random();
    
    double eval(SymbolTable symbols, double params[]) {
        double x = params[0];
        switch (code) {
        case SIN:   return MoreMath.isPiMultiple(x) ? .0 : Math.sin(x);
        case COS:   return MoreMath.isPiMultiple(x + MoreMath.PI_2) ? .0 : Math.cos(x);
        case TAN:   return Math.tan(x);

        case ASIN:  return MoreMath.asin(x);
        case ACOS:  return MoreMath.acos(x);
        case ATAN:  return MoreMath.atan(x);

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
            
        case SQRT: return Math.sqrt(x);
        case CBRT: return MoreMath.cbrt(x);
        case POW:  return MoreMath.pow(x, params[1]);

        case INT:  return MoreMath.trunc(x);
        case FRAC: return x - MoreMath.trunc(x);        
        case ABS:  return Math.abs(x);
        case FLOOR: return Math.floor(x);
        case CEIL: return Math.ceil(x);
        case SIGN: return x > 0 ? 1. : x < 0 ? -1. : 0.;
        case MIN:  return Math.min(x, params[1]);
        case MAX:  return Math.max(x, params[1]);
        case GCD:  return MoreMath.gcd(x, params[1]);
        case COMB: return 0;
        case PERM: return 0;
        case RND: return random.nextDouble();
        }
        throw new Error("unhandled code " + code);
    }
}

class DefinedFun extends Symbol {
    DefinedFun(String name, String iniArgs[], String iniDef) {
        super(name);
        args = iniArgs;
        definition = iniDef;
        isFun = true;
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
