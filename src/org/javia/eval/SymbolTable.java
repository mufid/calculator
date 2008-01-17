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

/**
   The collection of names (<em>symbols</em>) used for parsing an expression 
   (the context in which the parsing takes place).<p>

   Each symbol maps to either a {@link Function} or a constant.<p>

   A symbol is identified by the pair (name, arity).
   So a constant and a function with the same name,
   or two function with the same name but with different arity 
   are distinct symbols.<p>

   The SymbolTable is organized as a stack of <em>frames</em>. 
 */

public class SymbolTable {

    /**
       Returns the default SymbolTable which contains the built-in symbols.
    */
    public SymbolTable() {
        for (int i = 0; i < builtin.length; ++i) {
            Symbol s = builtin[i];
            symbols.put(s, s);
        }
    }

    /**
       Adds a new function symbol to the top-most frame of this SymbolTable.
       @param name the name of the function (e.g. "sin")
       @param function the function to which the name maps
    */    
    public void addFunction(String name, Function function) {
        add(new Symbol(name, function));
    }

    /**
       Adds a new constant symbol to the top-most frame of this SymbolTable.
       @param name the name of the constant (e.g. "pi")
       @param value the value of the constant
    */
    public void addConstant(String name, double value) {
        add(new Symbol(name, value));
    }

    /**
       Pushes a new top frame.<p>

       All modifications (defining new symbols) happen in the top-most frame.
       When the frame is pop-ed the modifications that happened in it are reverted.
    */
    public void pushFrame() {
        frames.push(delta);
        delta = null;
    }

    /**
       Pops the top frame.<p>

       All the modifications done since this frame was pushed are reverted.
       @throws EmptyStackException if there were fewer <code>pushFrame</code> than <code>popFrame</code>.
    */
    public void popFrame() {
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



    //--- non-public below

    private final static Symbol builtin[];
    private static Symbol shell = new Symbol(null, 0);

    private Hashtable symbols = new Hashtable(); //Hashtable<Symbol, Symbol>
    private Vector delta = null;                 //Vector<Symbol>
    private Stack frames = new Stack();          //Stack<Vector>

    static {
        Vector vect = new Vector();
        int arity;
        for (byte i = 0; i < VM.BYTECODE_END; ++i) {
            if ((arity = VM.builtinArity[i]) >= 0) {
                vect.addElement(new Symbol(VM.opcodeName[i], arity, i));
            }
        }
        vect.addElement(new Symbol("pi", Math.PI));
        vect.addElement(new Symbol("e", Math.E));
        int size = vect.size();
        builtin = new Symbol[size];
        vect.copyInto(builtin);
    }
    
    void addArguments(String args[]) {
        for (int i = 0; i < args.length; ++i) {
            add(new Symbol(args[i], Symbol.CONST_ARITY, (byte)(VM.LOAD0 + i)));
        }
    }

    void addDefinition(String name, Function fun, boolean canBeConst) {
        if (canBeConst && fun.arity() == 0) {
            try {
                addConstant(name, fun.eval());
            } catch (ArityException e) {
                throw new Error(""+e); //never
            }                
        } else {
            addFunction(name, fun);
        }
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
}
