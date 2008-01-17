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

/**
   Abstract base class for functions.<p>
   A function has an arity (the number of arguments), and a way for evaluation
   given the values of the arguments.<p>
   Derive from this class to create user-defined functions.
 */

abstract public class Function {

    /**
       Gives the arity of this function. 
       @return the arity (the number of arguments). Arity >= 0.
    */
    abstract public int arity();

    /**
       Evaluates the function given the argument values.
       @param args array containing the arguments. 
       The length of the array must exactly match the arity.
       @return the value of the function
    */
    abstract public double eval(double args[]) throws ArityException;

    /**
       Evaluates a 0-arity function (a function with no arguments).
       @return the value of the function
    */
    public double eval() throws ArityException {
        return eval(NO_ARGS);
    }

    static final double NO_ARGS[] = new double[0];
}
