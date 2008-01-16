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

public class Compiler {
    public static void main(String[] argv) {
        System.out.println(argv[0] + ":\n" + (new Compiler()).compile(argv[0]));
    }

    private Lexer lexer = new Lexer();
    private RPN rpn     = new RPN();
    private DeclarationParser declParser = new DeclarationParser();
    private OptCodeGen codeGen = new OptCodeGen();
    private Declaration decl = new Declaration();
    private SymbolTable defaultSymbols = new SymbolTable();

    public Function compile(String source) {
        return compile(source, defaultSymbols);
    }

    public Function compile(String source, SymbolTable symbols) {
        return compile(source, symbols, false);
    }

    public Function compileAndDefine(String source, SymbolTable symbols) {
        return compile(source, symbols, true);
    }

    public synchronized Function compile(String source, SymbolTable symbols, boolean addDefinition) {
        try {
            decl.parse(source, lexer, declParser);
        } catch (SyntaxException e) {
            Log.log("error on '" + decl + "' : " + e + " : " + e.token);
            return null;
        }
        Function fun = compileExpression(decl.expression, symbols, decl.args, decl.arity);
        Log.log("compile '" + source + "': " + fun);
        if (decl.name != null && fun != null && addDefinition) {
            symbols.addDefinition(decl.name, fun, decl.arity==DeclarationParser.UNKNOWN_ARITY);
        }
        return fun;
    }
        
    CompiledFunction compileExpression(String body, SymbolTable symbols, String args[], int arity) {
        symbols.pushFrame();
        symbols.addArguments(args);
        CompiledFunction fun = compileExpression(body, symbols, arity);
        symbols.popFrame();
        return fun;
    }

    CompiledFunction compileExpression(String body, SymbolTable symbols, int arity) {
        rpn.setConsumer(codeGen.setSymbols(symbols));
        try {
            lexer.scan(body, rpn);
        } catch (SyntaxException e) {
            Log.log("error on '" + body + "' : " + e + " : " + e.token);
            return null;
        }
        if (arity == DeclarationParser.UNKNOWN_ARITY) {
            arity = codeGen.intrinsicArity;
        }
        return codeGen.getFun(arity);
    }
}
