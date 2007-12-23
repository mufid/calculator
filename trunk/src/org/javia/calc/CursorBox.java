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

class CursorBox {
    EditBox box;
    int cursorPos;
    //int lineHeight;
    
    CursorBox(int width, Font font, int maxLines) {
        box = new EditBox(width, font, maxLines);
        //lineHeight = font.getHeight();
        cursorPos = 0;
    }

    void set(String str, int pos) {
        box.set(str);
        cursorPos = pos;
    }

    void drawLines(Graphics g, int x, int y) {
        box.drawLines(g, x, y);
    }

    public String toString() {
        return box.toString();
    }

    int nLines() {
        return box.nLines();
    }

    boolean moveUp() {
        int row = box.getRow(cursorPos);
        if (row <= 0) {
            return false;
        }
        int x = box.getX(row, cursorPos);
        cursorPos = box.getPos(row-1, x);
        return true;
    }

    boolean moveDown() {
        int row = box.getRow(cursorPos);
        if (row >= box.nLines() - 1) {
            return false;
        }
        int x = box.getX(row, cursorPos);
        cursorPos = box.getPos(row+1, x);
        return true;
    }

    void moveLeft() {
        if (cursorPos > 0) {
            --cursorPos;
        }
    }

    void moveRight() {
        if (cursorPos < box.size - 1) {
            ++cursorPos;
        }
    }
    
    void delete() {
        delete(1);
    }

    void delete(int size) {
       cursorPos = box.delete(cursorPos, size);
    }

    void insert(String s) {
        cursorPos = box.insert(cursorPos, s);
    }

    /*
    int cursorRow() {
        return box.getRow(cursorPos);
    }
    */

    void getCursorRowX(int outRowX[]) {
        int row = box.getRow(cursorPos);
        int x = box.getX(row, cursorPos);
        outRowX[0] = row;
        outRowX[1] = x;
    }

    int cursorPos() {
        return cursorPos;
    }
}
