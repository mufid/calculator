/*
 * Copyright (C) 2007-2008 Mihai Preda.
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

public class FunParser {
    private static FunParser funParser = new FunParser();

    public static Fun compile(String source, SymbolTable symbols) {
        return funParser.compileInt(source, symbols);
    }

    private Lexer lexer         = new Lexer();
    private OptCodeGen codeGen  = new OptCodeGen();
    private RPN rpn             = new RPN();
    private DeclarationParser declParser = new DeclarationParser();

    private synchronized Fun compileInt(String source, SymbolTable symbols) {
        //, String argNames[]) {
        int equalPos = source.indexOf('=');
        String decl, def;

        if (equalPos == -1) {
            decl = null;
            def  = source;
        } else {
            decl = source.substring(0, equalPos);
            def  = source.substring(equalPos + 1);
        }

        String name;
        String argNames[];
        int arity;

        if (decl != null) {
            SyntaxException err = lexer.scan(decl, declParser);
            if (err != null) {
                return null;
            }
            name     = declParser.name;
            argNames = declParser.argNames;
            arity    = declParser.arity;
        } else {
            name     = null;
            argNames = DeclarationParser.NO_ARGS;
            arity    = 0; //arity is not used when name==null
        }

        symbols.pushFrame();        
        for (int i = 0; i < argNames.length; ++i) {
            symbols.add(new Symbol(argNames[i], -1, (byte)(VM.LOAD0 + i)));
        }
        codeGen.start(symbols);
        rpn.setConsumer(codeGen);
        SyntaxException err = lexer.scan(def, rpn);
        symbols.popFrame();

        Fun fun;
        if (err == null) {
            fun = codeGen.getFun(name, arity, source);
        } else {
            fun = null;
        }
        Log.log("compile '" + source + "': " + fun);

        if (name != null && fun != null) {
            symbols.add(new Symbol(fun));
        }

        return fun;
    }
}
