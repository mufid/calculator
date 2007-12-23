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

class EditBuffer {
    int cursorPos = -1;
    private StringBuffer buffer;
    private String cached;

    String toString() {
        return cached == null ?
            (cached = buffer.toString()) :
            cached;
    }
    
    private void changed() {
        cached = null;
    }

    private void delete(int size) {
        changed();
        cursorPos -= size;
        buffer.delete(cursorPos + 1, cursorPos + 1 + size);
    }
    
    void delete() {
        delete(1);
    }
    
    void insert(String s) {
        //cached = null;
        changed();
        buffer.insert(cursorPos + 1, s);
        cursorPos += s.length();
    }

    int cursorPos() { 
        return cursorPos;
    }

    void moveLeft() {
        if (cursorPos >= 0) {
            //changed();
            cursorPos--;
        }
    }

    void moveRight() {
        if (cursorPos < buffer.length() - 1) {
            //changed();
            cursorPos++;
        }
    }

    void advanceCursor(int delta) {
        int newPos = cursorPos + delta;
        if (newPos < -1) {
            newPos = -1;
        }
        int maxPos = buffer.length() - 1;
        if (newPos > maxPos) {
            newPos = maxPos;
        }
        if (newPos != cursorPos) {
            cursorPos = newPos;
        }
    }
}
