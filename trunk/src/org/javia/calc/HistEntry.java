/*
 * Copyright (C) 2006-2007 Mihai Preda.
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

import java.io.*;
import org.javia.lib.Log;

class HistEntry {
    String base, edited;
    int pos;

    HistEntry(String str) {
        base = str;
        //result = res;
        //hasResult = hasRes;
        flush();
    }

    HistEntry(DataInputStream in) {
        try {
            //result = in.readDouble();
            //hasResult = in.readBoolean();
            base = in.readUTF();
        } catch (IOException e) {
            Log.log(e);
            //throw new Error(e.toString());
        }
        flush();
    }

    void write(DataOutputStream out) {
        try {
            //out.writeDouble(result);
            //out.writeBoolean(hasResult);
            out.writeUTF(base);
        } catch (IOException e) {
            Log.log(e);
            //throw new Error(e.toString());
        }
    }

    void flush() {
        edited = base;
        pos = base.length();
    }

    void update(String str, int iniPos) {
        pos = iniPos;
        edited = str;
    }
}
