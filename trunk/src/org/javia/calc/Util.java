/*
 * Copyright (C) 2007 Mihai Preda & Carlo Teubner.
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

package org.javia.calc;

import javax.microedition.lcdui.Font;
import org.javia.eval.MoreMath;

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

    static String doubleToTrimmedString(double v, int targetChars) {
        return doubleToTrimmedString(v, targetChars, Double.toString(Math.abs(v)));
    }

    private static String doubleToTrimmedString(double v, int targetChars, String str)
    {
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

    static String fitDouble(double d, Font font, int pxWidth) {
        String s = null, str = Double.toString(Math.abs(d));
        for (int i = 15; i > 0 && font.stringWidth(s = doubleToTrimmedString(d, i, str)) > pxWidth; --i) ;
        return s;
    }

    /*
    public static void sort(Object[] array, int length) {
        Object[] temp = new Object[length];
        System.arraycopy(array, 0, temp, 0, length);
        Util.mergeSort(temp, array, 0, length, 0);
    }
    */

    // taken from J2SE 6.0's Arrays.java
    /**
     * Src is the source array that starts at index 0
     * Dest is the (possibly larger) array destination with a possible offset
     * low is the index in dest to start sorting
     * high is the end index in dest to end sorting
     * off is the offset to generate corresponding low, high in src
     */
    /*
    private static void mergeSort(Object[] src, Object[] dest, int low, int high, int off) {
        final int length = high - low;

        // Insertion sort on smallest arrays
        if (length < 7) {
            for (int i = low; i < high; i++)
                for (int j = i; j > low
                        && ((Comparable) dest[j - 1]).compareTo(dest[j]) > 0; j--)
                    swap(dest, j, j - 1);
            return;
        }

        // Recursively sort halves of dest into src
        int destLow = low;
        int destHigh = high;
        low += off;
        high += off;
        int mid = (low + high) >>> 1;
        mergeSort(dest, src, low, mid, -off);
        mergeSort(dest, src, mid, high, -off);

        // If list is already sorted, just copy from src to dest. This is an
        // optimization that results in faster sorts for nearly ordered lists.
        if (((Comparable) src[mid - 1]).compareTo(src[mid]) <= 0) {
            System.arraycopy(src, low, dest, destLow, length);
            return;
        }

        // Merge sorted halves (now in src) into dest
        for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
            if (q >= high || p < mid
                    && ((Comparable) src[p]).compareTo(src[q]) <= 0)
                dest[i] = src[p++];
            else
                dest[i] = src[q++];
        }
    }
    */

    private static void swap(Object[] x, int a, int b) {
        Object t = x[a];
        x[a] = x[b];
        x[b] = t;
    }
}
