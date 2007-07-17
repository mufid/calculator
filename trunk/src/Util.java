class Util {
    static String doubleToString(double v, int roundingStart) {
        // Return unchanged if:
        // anyway contains a decimal dot (< 1),
        // or isn't in exponent notation,
        // or the number is large enough to warrant exponent notation.

        String str = Double.toString(v);
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
        if (roundingStart < len) {
            if (buf.charAt(roundingStart) >= '5') {
                buf.setCharAt(roundingStart-1, (char)(buf.charAt(roundingStart-1)+1));
            }
            buf.setLength(roundingStart);
        }

        //re-insert dot
        //System.out.println(""+exp);
        if ((exp < -5) || (exp > 10)) {
            buf.insert(1, '.');
            --exp;
        } else {
            for (int i = len; i < exp; ++i) {
                buf.append('0');
            }
            buf.insert(exp, '.');
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
        return buf.toString();
    }
}
