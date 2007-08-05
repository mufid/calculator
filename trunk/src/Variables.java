// Copyright (c) 2007, Carlo Teubner
// Available under the MIT License (see COPYING).

// XXX persistency doesn't work

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Variables implements VMConstants
{
    public static final char TYPE_UNDEF = 0, TYPE_NUM = 1, TYPE_FUNC = 2;

    private static final int VARS_CNT = LAST_VAR - FIRST_VAR + 1;

    private static char[] types             = new char[VARS_CNT]; // init'd to 0 = TYPE_UNDEF
    private static double[] numbers         = new double[VARS_CNT];
    private static CompiledFunction[] funcs = new CompiledFunction[VARS_CNT];

    public static void persistDefine(Result result, double number) {
        DataOutputStream os = C.rs.out;
        int i = result.definedSymbol - FIRST_VAR;
        try {
            if (result.function.arity() == 0) {
                os.writeChar(types[i] = TYPE_NUM);
                os.writeDouble(numbers[i] = number);
                Log.log("Saving var " + i + " = " + number);
            } else {
                os.writeChar(types[i] = TYPE_FUNC);
                funcs[i] = new CompiledFunction(result.function);
                funcs[i].write(os);
                Log.log("Saving var " + i + " = fn");
            }
            C.rs.write(C.RS_SYMB_START + i);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double getNumber(int symbol) {
        return numbers[symbol - FIRST_VAR];
    }

    public static CompiledFunction getFunction(int symbol) {
        return funcs[symbol - FIRST_VAR];
    }
    
    public static char getType(int symbol) {
        return types[symbol - FIRST_VAR];
    }

    public static boolean isNumber(int symbol) {
        return types[symbol - FIRST_VAR] == TYPE_NUM;
    }
    
    public static boolean isFunction(int symbol) {
        return types[symbol - FIRST_VAR] == TYPE_FUNC;
    }

    public static boolean isDefined(int symbol) {
        return types[symbol - FIRST_VAR] != TYPE_UNDEF;
    }

    public static void load() {
        DataInputStream is;
        Log.log("Loading variables:");
        try {
            for (int i = 0; i < VARS_CNT && (is = C.rs.read(C.RS_SYMB_START + i)) != null; ++i)
                switch (types[i] = is.readChar()) {
                case TYPE_NUM:  numbers[i] = is.readDouble(); Log.log("var " + i + " = " + numbers[i]); break;
                case TYPE_FUNC: funcs[i] = new CompiledFunction(is); Log.log("var " + i + " = fn"); break;
                }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}