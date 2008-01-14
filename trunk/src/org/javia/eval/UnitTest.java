/*
 * Copyright (C) 2007 Mihai Preda.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.javia.eval;

import org.javia.lib.Log;
import org.javia.eval.MoreMath;

class EvalCase {
    String expr;
    double result;
    
    static final double ERR = -13;

    EvalCase(String expr, double result) {
        this.expr = expr;
        this.result = result;
    }
}

class TestEval {
    static EvalCase cases[] = {
        new EvalCase("1", 1),
        new EvalCase("1+", EvalCase.ERR),
        new EvalCase("1+1", 2),
        new EvalCase("1+-1", 0),
        new EvalCase("-0.5", -.5),
        new EvalCase("+1e2", 100),
        new EvalCase("-2^3!", -64),
        new EvalCase("(-2)^3!", 64),
        new EvalCase("-2^1^2", -2),
        new EvalCase("--1", 1),
        new EvalCase("-3^--2", -9),
        new EvalCase("1+2)(2+3", 15),
        new EvalCase("1+2)!^-2", 1./36),
        new EvalCase("sin(0)", 0),
        new EvalCase("cos(0)", 1),
        new EvalCase("sin(-1--1)", 0),
        new EvalCase("-(2+1)*-(4/2)", 6),
        new EvalCase("-.5E-1", -.05),
        new EvalCase("1E1.5", 5),
        new EvalCase("2 3 4", 24)
    };

    static boolean testEval() {
        boolean allOk = true;
        SymbolTable symbols = new SymbolTable();
        double actual = 0;
        for (int i = 0; i < cases.length; ++i) {
            EvalCase c = cases[i];
            Fun f = FunParser.compile(c.expr, symbols);
            boolean ok;
            try {
                ok = (f == null && c.result == EvalCase.ERR) || 
                    (f != null && c.result == (actual = f.eval()));
            } catch (ArityException e) {
                ok = c.result == EvalCase.ERR;
            }
            if (!ok) {
                allOk = false;
                Log.log(c.expr + " expected " + c.result + " got " + actual);
            }
        }
        return allOk;
    }
}


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
            int nKeep = c.rounding == 0 ? 17 : 15 - c.rounding;
            //System.out.println("" + Double.toString(c.val) + " " + Util.round(c.val, nKeep) + " " + c.res + ", got " + res);
        }
        return ret;
    }
}

public class UnitTest {
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
        check(TestEval.testEval());

        if (!allOk) {
            System.exit(1);
        } else {
            System.out.println("All tests passed ok");
        }
        
/*
        System.out.println(Util.doubleToTrimmedString(0.9999, 5));

        Parser parser = new Parser();
        String[] defs = { "f:=x", "g:=x*y", "b:=x", "h:=x*b(x)" };
        for (int i = 0; i < defs.length; ++i) {
            char[] ch = new char[defs[i].length() + 1];
            defs[i].getChars(0, defs[i].length(), ch, 0);
            if (parser.compile(ch, defs[i].length())) {
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
