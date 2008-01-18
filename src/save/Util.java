
    /*
    static String doubleToTrimmedString(double v, int targetChars) {
        return doubleToTrimmedString(v, targetChars, Double.toString(Math.abs(v)));
    }

    private static String doubleToTrimmedString(double v, int targetChars, String str) {
        if (Double.isInfinite(v) || Double.isNaN(v)) {
            str = Double.toString(v);
            return str.length() > targetChars ? str.substring(0, targetChars) : str;
        }

        boolean negative = v < 0;
        if (negative)
            --targetChars;

        int dotpos = str.indexOf('.'), epos = str.indexOf('E');
        StringBuffer buf = new StringBuffer(str);
        int exp = dotpos - 1;
        if (epos != -1) {
            exp += Integer.parseInt(str.substring(epos + 1));
            buf.setLength(epos);
        }
        buf.deleteCharAt(dotpos);
        --targetChars;

        StringBuffer mantissa = null;
        if (exp < -3 || exp > Math.min(6, targetChars)) {
            mantissa = new StringBuffer("E");
            mantissa.append(exp);
            targetChars -= mantissa.length();
            dotpos = 1;
        }

        boolean noDot = false;
        if (targetChars <= 0) {
            targetChars = 1;
            noDot = true;
        } else if (targetChars < dotpos) {
            targetChars = dotpos;
            noDot = true;
        }

        if (targetChars < buf.length()) {
            if (buf.charAt(targetChars) >= '5') {
                int i = targetChars - 1;
                while (i >= 0 && buf.charAt(i) == '9') {
                    buf.setCharAt(i, '0');
                    --i;
                }
                if (i >= 0)
                    buf.setCharAt(i, (char) (buf.charAt(i) + 1));
                else {
                    buf.insert(0, '1');
                    ++dotpos;
                }
            }
            buf.setLength(targetChars);
        }

        if (!noDot && dotpos <= buf.length()) {
            buf.insert(dotpos, '.');
            int lastZero = buf.length() - 1;
            for (; buf.charAt(lastZero) == '0'; --lastZero) ;
            ++lastZero;  // leave one zero at the end: "1.0" looks better than "1."
            if (lastZero + 1 < buf.length())
                buf.setLength(lastZero + 1);
        }

        if (negative)
            buf.insert(0, '-');
        
        if (mantissa != null)
            buf.append(mantissa);

        return buf.toString();
    }
    */

    /*
    static String fitDouble(double d, Font font, int pxWidth) {
        String s = null, str = Double.toString(Math.abs(d));
        for (int i = 15; i > 0 && font.stringWidth(s = doubleToTrimmedString(d, i, str)) > pxWidth; --i) ;
        return s;
    }
    */
