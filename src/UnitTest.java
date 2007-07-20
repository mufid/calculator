#include "defines.inc"

class UnitTest {

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

    FormatCase cases[] = { 
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
    };

    boolean testFormat() {
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

    public static void main(String argv[]) {
        UnitTest tester = new UnitTest();
        tester.testFormat();
    }
}
