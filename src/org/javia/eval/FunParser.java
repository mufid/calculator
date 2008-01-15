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

    public static CompiledFunction compile(String source, SymbolTable symbols) {
        return funParser.compileDefinition(source, symbols);
    }

    private Lexer lexer         = new Lexer();
    private RPN rpn             = new RPN();
    private DeclarationParser declParser = new DeclarationParser();

    private OptCodeGen    optGen    = new OptCodeGen();
    //private SimpleCodeGen simpleGen = new SimpleCodeGen();
    //private SimpleCodeGen codeGen;

    private synchronized CompiledFunction compileDefinition(String source, SymbolTable symbols) {
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
        String argNames[] = null;
        int arity;

        if (decl != null) {
            try {
                lexer.scan(decl, declParser);
            } catch (SyntaxException e) {
                Log.log("error on '" + decl + "' : " + e + " : " + e.token);
                return null;
            }
            name     = declParser.name;
            argNames = declParser.argNames();
            arity    = declParser.arity;
        } else {
            name     = null;
            arity    = DeclarationParser.UNKNOWN_ARITY;
        }

        boolean isUnknownArity = arity == DeclarationParser.UNKNOWN_ARITY;
        if (isUnknownArity) {
            argNames = DeclarationParser.IMPLICIT_ARGS;
        }

        CompiledFunction fun = compileBody(def, argNames, isUnknownArity, symbols);
        Log.log("compile '" + source + "': " + fun);
        if (name != null && fun != null) {
            if (isUnknownArity && fun.arity() == 0) {
                try {
                    symbols.add(new Symbol(name, fun.eval()));
                } catch (ArityException e) {
                    throw new Error(""+e);
                }                
            } else {
                symbols.add(new Symbol(name, fun));
            }
        }
        return fun;
    }

    private CompiledFunction compileBody(String body, 
                                         String args[], boolean isUnknownArity,
                                         SymbolTable symbols) {
        symbols.pushFrame();        
        for (int i = 0; i < args.length; ++i) {
            //Log.log("arg " + args[i]);
            symbols.add(new Symbol(args[i], Symbol.CONST_ARITY, (byte)(VM.LOAD0 + i)));
        }
        rpn.setConsumer(optGen.setSymbols(symbols));
        try {
            lexer.scan(body, rpn);
        } catch (SyntaxException e) {
            Log.log("error on '" + body + "' : " + e + " : " + e.token);
            return null;
        } finally {
            symbols.popFrame();
        }

        int arity = isUnknownArity ? optGen.intrinsicArity : args.length;
        return optGen.getFun(arity);
    }
}
