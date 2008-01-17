/*
 * Copyright (C) 2007-2008 Mihai Preda.
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

import org.javia.eval.MoreMath;

/**
   Contains static helper methods for formatting double values.
 */
public class Util {

    /** Returns a number which is an approximation of v (within maxError)
       and which has fewer digits in base-10).
       @param value the value to be approximated
       @param maxError the maximum deviation from value
       @return an approximation with a more compact base-10 representation.
    */
    public static double shortApprox(double value, double maxError) {
        final double v = Math.abs(value);
        final double tail = MoreMath.intExp10(MoreMath.intLog10(Math.abs(maxError)));
        final double ret = Math.floor(v/tail +.5)*tail;
        return (value < 0) ? -ret : ret;
    }

    /*
    public static double round(double v, int keepDigits) {
        //int keepDigits = 15 - roundingDigits;
        if (keepDigits >= 17 || v == 0) {
            return v;
        }
        final double factor = MoreMath.intExp10(MoreMath.intLog10(Math.abs(v))-keepDigits+1);
        return Math.floor(v/factor + .5)*factor;
    }
    */

    /**
       Rounds by dropping roundingDigits of double precision 
       (similar to 'hidden precision digits' on calculators),
       and formats to String.
       @param v the value to be converted to String
       @param roundingDigits the number of 'hidden precision' digits (e.g. 2).
       @return a String representation of v
     */
    public static String doubleToString(double v, int roundingDigits) {        
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
