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

class TokenType {
    //kind
    static final int 
        PREFIX = 1,
        LEFT   = 2,
        RIGHT  = 3,
        SUFIX  = 4;

    final String name;
    final int priority;
    final int assoc;
    final boolean isOperand;
    final int id;
    final byte vmop;

    TokenType(int id, String name, int priority, int assoc, boolean isOperand, int vmop) {
        this.id = id;
        this.name = name;
        this.priority = priority;
        this.assoc = assoc;
        this.isOperand = isOperand;
        this.vmop = (byte)vmop;
    }
}
