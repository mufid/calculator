// Copyright 2007, Mihai Preda.
// Available under the MIT License (see COPYING).

class Util {
    /* returns a number which is an approximation of v (within maxError)
       and which has fewer digits in base-10).
    */
    static double shortApprox(double iniV, double maxError) {
        final double v = Math.abs(iniV);
        final double tail = MoreMath.intExp10(MoreMath.intLog10(Math.abs(maxError)));
        //return v - v % tail;
        final double ret = ((int)(v/tail +.5))*tail;
        return (iniV < 0) ? -ret : ret;
    }

    static String doubleToString(double v, int roundingDigits) {
        
        if (roundingDigits > 13) {
            roundingDigits = 0;
        }
        int roundingStart = roundingDigits == 0 ? 17 : 15 - roundingDigits;

        String str = Double.toString(Math.abs(v));
        StringBuffer buf = new StringBuffer(str);
        int ePos = str.lastIndexOf('E');
        int exp  =  (ePos != -1) ? Integer.parseInt(str.substring(ePos + 1)) : 0;
        if (ePos != -1) {
            buf.setLength(ePos);
        }
        int len = buf.length();

        //remove dot
        int dotPos;
        for (dotPos = 0; dotPos < len && buf.charAt(dotPos) != '.';) ++dotPos;
        exp += dotPos;
        if (dotPos < len) {
            buf.deleteCharAt(dotPos);
            --len;
        }

        //round
        for (int p = 0; p < len && buf.charAt(p) == '0'; ++p) { 
            ++roundingStart; 
        }

        if (roundingStart < len) {
            if (buf.charAt(roundingStart) >= '5') {
                int p;
                for (p = roundingStart-1; p >= 0 && buf.charAt(p)=='9'; --p) {
                    buf.setCharAt(p, '0');
                }
                if (p >= 0) {
                    buf.setCharAt(p, (char)(buf.charAt(p)+1));
                } else {
                    buf.insert(0, '1');
                    ++roundingStart;
                    ++exp;
                }
            }
            buf.setLength(roundingStart);
        }

        //re-insert dot
        if ((exp < -5) || (exp > 10)) {
            buf.insert(1, '.');
            --exp;
        } else {
            for (int i = len; i < exp; ++i) {
                buf.append('0');
            }
            buf.insert((exp<0)? 0 : exp, '.');
            for (int i = exp; i <= 0; ++i) {
                buf.insert(0, '0');
            }
            exp = 0;
        }
        len = buf.length();
        
        //remove trailing dot and 0s.
        int tail;
        for (tail = len-1; tail >= 0 && buf.charAt(tail) == '0'; --tail) {
            buf.deleteCharAt(tail);
        }
        if (tail >= 0 && buf.charAt(tail) == '.') {
            buf.deleteCharAt(tail);
        }

        if (exp != 0) {
            buf.append('E').append(exp);
        }
        if (v < 0) {
            buf.insert(0, '-');
        }
        return buf.toString();
    }
}