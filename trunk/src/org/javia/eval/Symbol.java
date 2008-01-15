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

class Symbol {
    private String name;
    private int arity;

    byte op;
    Function fun;
    double value = 0;

    Symbol(String name, int arity, byte op) {
        setKey(name, arity);
        this.op = op;        
    }

    Symbol(String name, Function fun) {
        //String name = fun.name;
        if (name == null) {
            throw new Error("fun without name: " + fun);
        }

        int arity = fun.arity();
        if (arity == -1) {
            //it's a const
            try {
                this.value = fun.eval();
            } catch (ArityException e) {
                throw new Error(""+e);
            }
        } else {
            this.fun = fun;            
        }
        setKey(name, arity);
    }

    Symbol(String name, double value) {
        setKey(name, -1);
        this.value = value;
    }

    static Symbol newEmpty(Symbol s) {
        return new Symbol(s.name, s.arity, (byte)0);
    }

    boolean isEmpty() {
        return op == 0 && fun == null && value == 0;
    }

    Symbol setKey(String name, int arity) {
        this.name = name;
        this.arity = arity;
        return this;
    }

    public boolean equals(Object other) {
        Symbol symbol = (Symbol) other;
        return name.equals(symbol.name) && arity == symbol.arity;
    }

    public int hashCode() {
        return name.hashCode() + arity;
    }
}
