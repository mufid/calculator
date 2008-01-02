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
import javax.microedition.lcdui.Graphics;

import org.javia.lib.Log;

class EditBox {
    static final int MAX_SIZE  = 512;
    static final int MAX_LINES = 20;

    char buffer[]   = new char[MAX_SIZE];
    int lineStart[] = new int[MAX_LINES];
    int size   = 0;
    int width;
    Font font;
    int nLines;
    int maxLines;
    int lineHeight;

    EditBox(int width, Font font, int maxLines) {
        this.width = width;
        this.font = font;
        this.maxLines = maxLines;
        size = 0;
        lineStart[0] = 0;
        lineStart[1] = size;
        nLines = 0;
        lineHeight = font.getHeight();
    }

    private int lineEnd(int start, int widthLeft) {
        int end = start;
        int w;
        while (end+5 <= size && (w = font.charsWidth(buffer, end, 5)) < widthLeft) {
            end += 5;
            widthLeft -= w;
        }
        while (end < size && (widthLeft -= font.charWidth(buffer[end])) >= 0) {
            ++end;
        }
        return end;
    }

    private void updateLineStart(int pos) {
        int line = getRow(pos);
        int start = lineStart[line];
        do { 
            start = lineEnd(start, width);
            lineStart[++line] = start;
        } while (start < size && line <= maxLines);
        nLines = line;
    }

    int set(String str) {
        size = str.length();
        str.getChars(0, size, buffer, 0);
        updateLineStart(0);
        return nLines;
    }

    // insert str after pos
    int insert(int pos, String str) {
        Log.log("insert " + pos + ' ' + str);
        if (pos > size) {
            pos = size;
        }
        int len = str.length();
        System.arraycopy(buffer, pos, buffer, pos + len, size - pos);
        str.getChars(0, len, buffer, pos);
        size += len;
        updateLineStart(pos);
        if (nLines > maxLines) {
            delete(pos + len, len); // undo insert
            return pos;
        }
        return pos + len;
    }

    // delete delSize chars in front of pos
    int delete(int pos, int delSize) {
        if (delSize > pos) {
            delSize = pos;
        }
        if (delSize < 0) {
            delSize = 0;
        }
        System.arraycopy(buffer, pos, buffer, pos-delSize, size - pos);
        size -= delSize;
        updateLineStart(pos - delSize);
        return pos - delSize;
    }


    // getters below
    
    String textLine(int n) {
        int start = lineStart[n];
        return String.valueOf(buffer, start, lineStart[n+1] - start);
    }

    public String toString() {
        return String.valueOf(buffer, 0, size);
    }

    int getRow(int pos) {
        if (pos > size) {
            pos = size;
        }
        int row = 1;
        while (pos > lineStart[row]) {
            ++row;
        }
        return row - 1;
    }

    int getX(int row, int pos) {
        Log.log("getX " + row + ' ' + pos);
        int start = lineStart[row];
        return font.charsWidth(buffer, start, pos-start);
    }   

    int getPos(int row, int x) {
        return lineEnd(lineStart[row], x);
    }

    int nLines() {
        return nLines;
    }

    void drawLines(Graphics g, int x, int y) {
        int end = lineStart[0];
        for (int i = 1; i <= nLines; ++i, y+=lineHeight) {
            int start = end;
            end = lineStart[i];
            g.drawChars(buffer, start, end - start, x, y, 0);
            //Graphics.BOTTOM|Graphics.LEFT);
        }
    }

    /*
    int posInRow(int row, int pos) {
        return pos - lineStart[row];
    }
    */
}
