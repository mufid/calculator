/*
 * Copyright (C) 2008 Mihai Preda.
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

import org.javia.lib.Log;

class FunStack {
    private Fun[] data = new Fun[8];
    private int size = 0;

    void clear() {
        size = 0;
    }

    void push(Fun b) {
        if (size >= data.length) {
            Fun[] newData = new Fun[data.length << 1];
            System.arraycopy(data, 0, newData, 0, data.length);
            data = newData;
        }
        data[size++] = b;
    }

    void pop(int cnt) {
        size -= cnt;
    }

    Fun[] toArray() {
        Fun[] trimmed = new Fun[size];
        System.arraycopy(data, 0, trimmed, 0, size);
        return trimmed;
    }
}
