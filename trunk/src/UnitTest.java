#include "defines.inc"

class UnitTest {

    class FormatCase {
        public FormatCase(double v, String s) {
            this.val = v;
            this.res = s;
        }

        public double val;
        public String res;            
    }

    FormatCase cases[] = { 
        new FormatCase(0.1, "0.1"),
        new FormatCase(0.12, "0.12"),
        new FormatCase(0.001, "0.001"),
        new FormatCase(0.0012, "0.0012"),
        new FormatCase(0.0000001, "1E-7"),
        new FormatCase(0.00000012, "1.2E-7"),
        new FormatCase(0.123456789012345, "0.123456789012345"),

        new FormatCase(0, "0"),
        new FormatCase(1, "1"),
        new FormatCase(12, "12"),
        new FormatCase(1234567890.,   "1234567890"), 
        new FormatCase(1000000000.,   "1000000000"),
        
        new FormatCase(1.23456789012345,  "1.23456789012345"),
        new FormatCase(12345.6789012345,  "12345.6789012345"),
        new FormatCase(1234567890.12345,  "1234567890.12345"),  
        new FormatCase(123456789012345.,   "1.23456789012345E14"), 
        new FormatCase(100000000000000.,   "1E14"),
        new FormatCase(120000000000000.,   "1.2E14"),
        new FormatCase(100000000000001.,   "1.00000000000001E14"),
    };

    boolean testFormat() {
        boolean ret = true;
        for (int i = 0; i < cases.length; ++i) {
            FormatCase c = cases[i];
            double v = Double.parseDouble(c.res);
            if (v != c.val) {
                System.out.println("wrong test? " + c.res + " " + v + " " + c.val);
            }
            String res = Util.doubleToString(c.val, 20);
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
        System.out.println("");
        long 
            b1 = 0, 
            b2 = 0x000fffffffffffffL,
            b3 = 0x0000000000000001L;

        System.out.println(" " + Double.longBitsToDouble(b1) +
                           " " + Double.longBitsToDouble(b2) +
                           " " + Double.longBitsToDouble(b3));
        
        double d1 = 1.;
        long l1 = Double.doubleToLongBits(d1);
        System.out.println(" " + (l1 >> 52) + 
                           " " + (l1 & ~0xfff0000000000000L));

        //System.out.println("% " + (0.100234 % 0.00001));
        //System.out.println("log10(2) " + Math.log10(2));
    }
}
