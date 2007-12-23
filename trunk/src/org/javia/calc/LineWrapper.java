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

package org.javia.calc;

import javax.microedition.lcdui.Font;

class LineWrapper {
    private Font font;
    private int width;

    int nLines;
    String lines[];
    int cursorRow, cursorCol;

    LineWrapper(Font font, int width, int maxLines) {
        this.font  = font;
        this.width = width;
        lines = new String[maxLines];
    }

    /* returns the number of chars from str that fit within targetWidth
     */
    int fitWidth(String str, int targetWidth) {
        int len = str.length();
        int end = 0;
        int w;
        while (end+4 <= len && (w=font.substringWidth(str, end, 4)) < targetWidth) {
            end += 4;
            targetWidth -= w;
        }
        while (end < len && (targetWidth -= font.charWidth(str.charAt(end))) >= 0) {
            ++end;
        }
        return end;
    }

    /* Splits str in lines, each fitting within width.
       returns number of lines.
     */
    int wrap(String str) {
        int cnt = 0;
        if (str.length() == 0) {
            lines[0] = str;
            cnt = 1;
        } else {
            int n;
            while ((n = fitWidth(str, width, font)) > 0) {
                lines[cnt++] = str.substring(0, n);
                str = str.substring(n);
            }
        }
        lines[cnt] = null;
        return cnt;
    }

    void cursorRowCol(int pos, int outRowCol[]) {
        if (pos == -1) {
            outRowCol[0] = 0;
            outRowCol[1] = 0;
        } else {
            int line = -1;
            do {
                pos -= lines[++line].length();            
            } while (pos > 0);
            outRowCol[0] = line;
            outRowCol[1] = pos + lines[line].length();
        }
    }
}
