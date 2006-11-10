import java.util.*;

abstract class Symbol {
    Symbol(String iniName, int iniArity) {
        name = iniName;
        arity = iniArity;
    }
    String name;
    //boolean isFun   = false;
    //boolean isValue = false;
    int arity;

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
        super(name, 0);
        value = iniValue;
    }

    double value;
    double eval(SymbolTable symbols, double params[]) {
        /*
        if (params != null && params.length > 0) {
            throw new Error("Args for " + name + ": expected 0, got " + params.length);
        }
        */
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
        "exp",  "ln", "lg", "lb",  "pow",
        "sqrt", "cbrt",
        //"\u221a", "\u221b",
        "int", "frac", "abs",
        "floor", "ceil", "sign",
        "min", "max", "gcd",
        "comb", "perm", "rnd",
    };
        
    /* keep 'codes' in sync with 'names' above */
    static final int codes[] = {
        SIN,  COS,  TAN,  ASIN,  ACOS,  ATAN,
        SINH, COSH, TANH, ASINH, ACOSH, ATANH,
        EXP,  LOG,  LOG10, LOG2,  POW,
        SQRT, CBRT,
        //SQRT, CBRT,
        INT,  FRAC, ABS,
        FLOOR,CEIL, SIGN,
        MIN,  MAX,  GCD,
        COMB, PERM, RND,
    };
    /* keep in sync with codes above */
    static final int arities[] = {
        1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1,
        1, 1,
        1, 1, 1,
        1, 1, 1,
        2, 2, 2,
        2, 2, 0,
    };

    static void init(SymbolTable ht) {
        for (int i = names.length - 1; i >= 0; --i) {
            ht.put(new BuiltinFun(names[i], codes[i], arities[i]));
        } 
    }
    
    BuiltinFun(String name, int iniCode, int arity) {
        super(name, arity);
        code = iniCode;
    }
        
    int code;
    Random random = new Random();
    
    double eval(SymbolTable symbols, double params[]) {
        double x = 0, y = 0, z = 0;
        switch (arity) {
        case 3: z = params[2];
        case 2: y = params[1];
        case 1: x = params[0];
        }
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
        case POW:  return MoreMath.pow(x, y);

        case INT:  return MoreMath.trunc(x);
        case FRAC: return x - MoreMath.trunc(x);        
        case ABS:  return Math.abs(x);
        case FLOOR: return Math.floor(x);
        case CEIL: return Math.ceil(x);
        case SIGN: return x > 0 ? 1. : x < 0 ? -1. : 0.;
        case MIN:  return Math.min(x, y);
        case MAX:  return Math.max(x, y);
        case GCD:  return MoreMath.gcd(x, y);
        case COMB: return MoreMath.comb(x, y);
        case PERM: return MoreMath.perm(x, y);
        case RND:  return random.nextDouble();
        }
        throw new Error("unhandled code " + code);
    }
}

class DefinedFun extends Symbol {
    static final String args[] = {"x", "y", "z"}; 
    String definition;
    
    DefinedFun(String name, int arity, String iniDef) {
        super(name, arity);
        definition = iniDef;
    }
          
    double eval(SymbolTable symbols, double params[]) {
        //System.out.println(name + " " + args + " " + params);
        /*
        int n = args.length;
        int nParams = params == null ? 0 : params.length;
        if (nParams != n) {
            throw new Error("Args for " + name + 
                            ": expected " + n + ", got " + nParams);
        }
        */
        
        Symbol saves[] = new Symbol[arity];
        for (int i = 0; i < arity; ++i) {
            saves[i] = symbols.put(new Constant(args[i], params[i]));
        }
        double ret = new Expr().parse(definition);
        for (int i = 0; i < arity; ++i) {
            if (saves[i] == null) {
                symbols.remove(args[i]);
            } else {
                symbols.put(saves[i]);
            }
        }
        return ret;
    }
}
