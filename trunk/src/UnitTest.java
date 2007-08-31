// Copyright (c) 2007 Mihai Preda.
// Available under the MIT License (see COPYING).

class FormatCase {
    public FormatCase(int rounding, double v, String s) {
        this.rounding = rounding;
        this.val = v;
        this.res = s;
    }
    
    public int rounding;
    public double val;
    public String res;            
}

class TestFormat {
    static FormatCase cases[] = { 
        new FormatCase(0, 0.1, "0.1"),
        new FormatCase(0, 0.12, "0.12"),
        new FormatCase(0, 0.001, "0.001"),
        new FormatCase(0, 0.0012, "0.0012"),
        new FormatCase(0, 0.0000001, "1E-7"),
        new FormatCase(0, 0.00000012, "1.2E-7"),
        new FormatCase(0, 0.123456789012345, "0.123456789012345"),

        new FormatCase(0, 0, "0"),
        new FormatCase(0, 1, "1"),
        new FormatCase(0, 12, "12"),
        new FormatCase(0, 1234567890.,   "1234567890"), 
        new FormatCase(0, 1000000000.,   "1000000000"),
        
        new FormatCase(0, 1.23456789012345,  "1.23456789012345"),
        new FormatCase(0, 12345.6789012345,  "12345.6789012345"),
        new FormatCase(0, 1234567890.12345,  "1234567890.12345"),  
        new FormatCase(0, 123456789012345.,   "1.23456789012345E14"), 
        new FormatCase(0, 100000000000000.,   "1E14"),
        new FormatCase(0, 120000000000000.,   "1.2E14"),
        new FormatCase(0, 100000000000001.,   "1.00000000000001E14"),

        new FormatCase(2, 0.1, "0.1"),
        new FormatCase(2, 0.00000012, "1.2E-7"),
        new FormatCase(1, 0.123456789012345, "0.12345678901235"),

        new FormatCase(2, 0, "0"),
        
        new FormatCase(1, 1.23456789012345,  "1.2345678901235"),
        new FormatCase(2, 1.23456789012345,  "1.234567890123"),
        
        new FormatCase(0, 12345.6789012345,  "12345.6789012345"),
        new FormatCase(1, 1234567890.12345,  "1234567890.1235"),  
        new FormatCase(2, 123456789012345.,  "1.234567890123E14"), 
        new FormatCase(1, 100000000000001.,  "1E14"),

        new FormatCase(0, 12345678901234567.,   "1.2345678901234568E16"),
        new FormatCase(1, 12345678901234567.,   "1.2345678901235E16"),

        new FormatCase(0, 99999999999999999.,   "1E17"),
        new FormatCase(0, 9999999999999999.,    "1E16"),
        new FormatCase(0, 999999999999999.,     "9.99999999999999E14"),
        new FormatCase(1, 999999999999999.,     "1E15"),
        new FormatCase(1, 999999999999994.,     "9.9999999999999E14"),

        new FormatCase(1, MoreMath.log2(1+.00002), "00000.28853612282487")
    };

    static boolean testFormat() {
        boolean ret = true;
        for (int i = 0; i < cases.length; ++i) {
            FormatCase c = cases[i];
            double v = Double.parseDouble(c.res);
            if (c.rounding == 0 && v != c.val) {
                System.out.println("wrong test? " + c.res + " " + v + " " + c.val);
            }
            String res = Util.doubleToString(c.val, c.rounding);
            if (!res.equals(c.res)) {
                System.out.println("Expected '" + c.res + "', got '" + res + "'. " + Double.toString(c.val));
                ret = false;
            }
        }
        return ret;
    }
}

class UnitTest {
    public static void main(String argv[]) {
        //UnitTest tester = new UnitTest();
        checkCounter = 0;

        cheq(MoreMath.log(-1), Double.NaN);
        cheq(MoreMath.log(-0.03), Double.NaN);
        cheq(MoreMath.intLog10(-0.03), 0);
        cheq(MoreMath.intLog10(0.03), -2);
        cheq(MoreMath.intExp10(3), 1000);
        cheq(MoreMath.intExp10(-1), 0.1);
        cheq(Util.shortApprox( 1.235, 0.02),  1.24);
        cheq(Util.shortApprox( 1.235, 0.4),   1.2000000000000002);
        cheq(Util.shortApprox(-1.235, 0.02), -1.24);
        cheq(Util.shortApprox(-1.235, 0.4),  -1.2000000000000002);

        check(TestFormat.testFormat());

        if (!allOk) {
            System.exit(1);
        } else {
            System.out.println("All tests passed ok");
        }
        
/*
        System.out.println(Util.doubleToTrimmedString(0.9999, 5));

        Compiler compiler = new Compiler();
        String[] defs = { "f:=x", "g:=x*y", "b:=x", "h:=x*b(x)" };
        for (int i = 0; i < defs.length; ++i) {
            char[] ch = new char[defs[i].length() + 1];
            defs[i].getChars(0, defs[i].length(), ch, 0);
            if (compiler.compile(ch, defs[i].length())) {
                final int idx = Compiler.result.definedSymbol - VMConstants.FIRST_VAR;
                Variables.funcs[idx] = new CompiledFunction(Compiler.result.function);
                Variables.types[idx] = Variables.TYPE_FUNC;
            } else
                System.out.println("error");
        }

        String input = "a:=x+x+y*y+x^x";
        char[] chs = new char[input.length() + 1];
        input.getChars(0, input.length(), chs, 0);
        System.out.println(input);
        if (compiler.compile(chs, input.length())) {
            System.out.println(Compiler.result.function);
            Compiler.result.function.xyFragment();
        } else
            System.out.println("error " + Compiler.result.errorStart + "-" + Compiler.result.errorEnd);
*/
    }

    static void cheq(double v1, double v2) {
        ++checkCounter;
        if (v1 != v2 && !(Double.isNaN(v1) && Double.isNaN(v2))) {
            allOk = false;
            Log.log("check equal " + checkCounter + " failed: " + v1 + " " + v2);
        }
    }
    
    static void check(boolean cond) {
        ++checkCounter;
        if (!cond) {
            allOk = false;
            Log.log("check " + checkCounter + " failed");
        }
    }

    static boolean allOk = true;
    static int checkCounter = 0;
}
