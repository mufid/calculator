// Copyright (c) 2007 Carlo Teubner.
// Available under the MIT License (see COPYING).

import java.io.DataInputStream;
import java.io.IOException;

public class Variables implements VMConstants
{
    public static final char TYPE_UNDEF = 0, TYPE_NUM = 1, TYPE_FUNC = 2;

    private static final int VARS_CNT = LAST_VAR - FIRST_VAR + 1;

    static char[] types             = new char[VARS_CNT]; // init'd to 0 = TYPE_UNDEF
    static double[] numbers         = new double[VARS_CNT];
    static CompiledFunction[] funcs = new CompiledFunction[VARS_CNT];
    static DataOut os = new DataOut();

    public static void persistDefine(Result result, double number) {
        final int i = result.definedSymbol - FIRST_VAR;
        try {
            if (result.function.arity() == 0) {
                os.writeChar(types[i] = TYPE_NUM);
                os.writeDouble(numbers[i] = number);
            } else {
                os.writeChar(types[i] = TYPE_FUNC);
                funcs[i] = new CompiledFunction(result.function);
                funcs[i].write(os);
            }
            C.rs.write(C.RS_SYMB_START + i, os.getBytesAndReset());
        } catch (IOException e) {
            Log.log(e);
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
        try {
            for (int i = 0; i < VARS_CNT; ++i) {
                is = C.rs.readIS(C.RS_SYMB_START + i);
                if (is == null)
                    continue;
                switch (types[i] = is.readChar()) {
                case TYPE_NUM:  numbers[i] = is.readDouble(); break;
                case TYPE_FUNC: funcs[i] = new CompiledFunction(is); break;
                }
            }
        } catch (IOException e) {
            Log.log(e);
        }
    }
}
