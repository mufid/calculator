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

import java.util.Hashtable;
import java.util.Stack;
import java.util.Vector;

public class SymbolTable {
    private final static Symbol builtin[];
    private static Symbol shell = new Symbol(null, 0);

    private Hashtable symbols = new Hashtable(); //Hashtable<Symbol, Symbol>
    private Vector delta = null;                 //Vector<Symbol>
    private Stack frames = new Stack();          //Stack<Vector>

    static {
        Vector builtinVect = new Vector();
        int arity;
        for (byte i = 0; i < VM.BYTECODE_END; ++i) {
            if ((arity = VM.builtinArity[i]) >= 0) {
                builtinVect.addElement(new Symbol(VM.opcodeName[i], arity, i));
            }
        }
        builtinVect.addElement(new Symbol("pi", Math.PI));
        builtinVect.addElement(new Symbol("e", Math.E));
        int size = builtinVect.size();
        builtin = new Symbol[size];
        builtinVect.copyInto(builtin);
    }
    
    public SymbolTable() {
        for (int i = 0; i < builtin.length; ++i) {
            Symbol s = builtin[i];
            symbols.put(s, s);
        }
    }

    public void add(String name, Function f) {
        add(new Symbol(name, f));
    }

    public void add(String name, double value) {
        add(new Symbol(name, value));
    }

    void add(Symbol s) {
        Object previous = symbols.put(s, s);
        if (delta == null) {
            delta = new Vector();
        }
        delta.addElement(previous != null ? previous : Symbol.newEmpty(s));
    }

    synchronized Symbol lookup(String name, int arity) {
        return (Symbol) symbols.get(shell.setKey(name, arity));
    }

    void pushFrame() {
        frames.push(delta);
        delta = null;
    }

    void popFrame() {
        if (delta != null) {
            for (int i = delta.size() - 1; i >= 0; --i) {
                Symbol previous = (Symbol) delta.elementAt(i);
                if (previous.isEmpty()) {
                    symbols.remove(previous);
                } else {
                    symbols.put(previous, previous);
                }
            }
        }
        delta = (Vector) frames.pop();
    }
}
