// Copyright (c) 2006-2007, Mihai Preda

import java.util.Hashtable;
import java.util.Random;
import java.io.*;

final class SymbolTable {
    //private RMS rs = C.rs;
    int nextRecId;
    Hashtable ht = new Hashtable(50);
    
    SymbolTable() {
        load();
    }

    double evaluate(String name, double params[]) {
        Symbol symb = get(name);
        if (symb == null) {
            throw new Error("Unknown id '" + name + "'");
        }
        return symb.eval(this, params);
    }

    private void load() {
        for (int i = Symbol.names.length - 1; i >= 0; --i) {
            putInt(new Symbol(i));
        }

        int recId = C.RS_SYMB_START;
        DataInputStream is;
        Result entry = new Result();
        try {
            while ((is = C.rs.read(recId)) != null) {
                putInt(new Symbol(is, recId));
                ++recId;
            }
        } catch (IOException e) {
        }
        nextRecId = recId;
    }

    Symbol get(String name) {
        return (Symbol) ht.get(name);
    }
    
    void persistPut(Symbol s) {
        Symbol old = get(s.name);
        int recId;
        if (old != null && old.recId != 0) {
            recId = old.recId;
        } else {
            recId = nextRecId;
            ++nextRecId;
        }
        s.write(C.rs.out);
        C.rs.write(recId);
        putInt(s);
    }

    /*
    void persistClear() {
        for (int i = C.RS_SYMB_START; i < nextRecId; ++i) {
            rs.write(i);
        }
        nextRecId = C.RS_SYMB_START;
        ht.clear();
        load();
    }
    */

    Symbol putInt(Symbol s) {
        return (Symbol) ht.put(s.name, s);
    }
}

final class Symbol {
    static final int         
        SIN   = 1, COS   = 2, TAN   = 3,
        ASIN  = 4, ACOS  = 5, ATAN  = 6,
        SINH  = 7, COSH  = 8, TANH  = 9,
        ASINH = 10, ACOSH = 11, ATANH = 12,
        EXP   = 13, LOG  = 14, LOG10  = 15, LOG2  = 16,
        SQRT  = 17, CBRT = 18, POW  = 19,
        INT   = 20, FRAC = 21, ABS  = 22,
        FLOOR = 23, CEIL = 24, SIGN = 25,
        MIN   = 26, MAX  = 27, GCD  = 28,
        COMB  = 29, PERM = 30, RND  = 31,
        PI    = 52, E = 53, 
        ANS = 54,
        PLOT = 55;

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
        "C",    "P", 
        "pi",   "\u03c0", "e",
        "ans",
        "plot"
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
        COMB, PERM,
        PI,   PI,   E,
        ANS,
        PLOT
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
        2, 2,
        0, 0, 0,
        0,
        3
    };
    static final Random random = new Random();
    static final String args[] = {"x", "y", "z"}; 

    String name;
    int arity;
    int code;
    String definition;
    int recId;

    Symbol(String iniName, int iniArity) {
        name = iniName;
        arity = iniArity;
        code = 0;
        definition = null;
        recId = 0;
    }

    private Symbol(String name, int arity, int iniCode) {
        this(name, arity);
        code = iniCode;
    }

    Symbol(int i) {
        this(names[i], arities[i], codes[i]);
    }
    
    Symbol(String name, int arity, String iniDef) {
        this(name, arity);
        definition = iniDef;
    }
    
    Symbol(String name, double value) {
        this(name, 0, Double.toString(value)); 
    }

    Symbol(DataInputStream is, int recId) throws IOException {
        this(is.readUTF(), is.readShort(), is.readUTF());
        this.recId = recId;
    }
    
    private double trigEval(int code, double x) {
        //radians
        switch (code) {
        case SIN:   return MoreMath.isPiMultiple(x) ? 0 : Math.sin(x);
        case COS:   return MoreMath.isPiMultiple(x + MoreMath.PI_2) ? 0 : Math.cos(x);
        case TAN:   return Math.tan(x);

        case ASIN:  return MoreMath.asin(x);
        case ACOS:  return MoreMath.acos(x);
        case ATAN:  return MoreMath.atan(x);
        }
        return 0;
    }
    
    double evalBuiltin(double params[]) {
        double x = 0, y = 0, z = 0;
        switch (arity) {
        case 3: z = params[2];
        case 2: y = params[1];
        case 1: x = params[0];
        }
        
        if (SIN <= code && code <= ATAN) {
            if (C.cfg.angleInRadians) {
                return trigEval(code, x);
            } else { //angle in degrees
                if (code <= TAN) {//sin,cos,tan
                    return trigEval(code, Math.toRadians(x));
                } else { //asin etc
                    return Math.toDegrees(trigEval(code, x));
                }
            }
        }

        switch (code) {
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
        case PI:   return Math.PI;
        case E:    return Math.E;
        case ANS:  return History.ans;
        }
        throw new Error("unhandled code " + code);
    }

    double eval(SymbolTable symbols, double params[]) {
        if (code != 0) {
            return evalBuiltin(params);
        }

        if (arity == 0) {
            return Double.parseDouble(definition);
        }

        Symbol saves[] = new Symbol[arity];
        for (int i = 0; i < arity; ++i) {
            saves[i] = symbols.putInt(new Symbol(args[i], params[i]));
        }
        try {
            return new Expr().parseThrow(definition);
        } finally {
            for (int i = 0; i < arity; ++i) {
                if (saves[i] == null) {
                    symbols.ht.remove(args[i]);
                } else {
                    symbols.putInt(saves[i]);
                }
            }
        }
    }

    void write(DataOutputStream os) {
        try {
            os.writeUTF(name);
            os.writeShort(arity);
            os.writeUTF(definition);
        } catch (IOException e) {
        }
    }
}
