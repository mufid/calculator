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

package org.javia.eval;

class SyntaxException extends Exception {
    String mes;
    Token token;
    static SyntaxException inst = new SyntaxException();
    
    private SyntaxException() {
    }

    static SyntaxException get(String str, Token token) {
        inst.mes = str;
        inst.token = token;
        return inst;
    }

    public String toString() {
        return mes + ' ' + token;
    }
}
