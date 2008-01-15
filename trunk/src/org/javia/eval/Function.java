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

abstract public class Function {
    static final double NO_ARGS[] = new double[0];

    abstract public int arity();
    abstract public double eval(double args[]) throws ArityException;

    public double eval() throws ArityException {
        return eval(NO_ARGS);
    }
}
