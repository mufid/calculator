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

/**
   Compiles a textual arithmetic expression to a {@link Function}.<p>

   Usage example: 
   <code>
   (new Compiler()).compile("1+1").eval();
   </code>
*/
public class Compiler {
    /**
       Takes a single command-line argument, an expression, and compiles it.
     */
    public static void main(String[] argv) {
        if (argv.length == 0) {
            System.out.println("example usage: Compiler \"1+1\"");
        } else {
            System.out.println("Compiling " + argv[0] + ":\n" + (new Compiler()).compile(argv[0]));
        }
    }

    /**
       Compiles an expression using the default {@link SymbolTable}.
       @param source the expression
       @return the function evaluating the expression. 
       If the expression has no arguments, this function has zero arity and calling
       <code>eval</code> on it gives the value of the expression.<p>

       Returns <code>null</code> if there are errors compiling the expression.
    */
    public Function compile(String source) {
        return compile(source, defaultSymbols);
    }

    /**
       Compiles the expression in the context of the given SymbolTable.
       @param source the expression
       @param symbols the SymbolTable used for parsing the expression.
       The symbols are left unchanged (are not modified).
       @return the function evaluating the expression, 
       or <code>null</code> if there were errors compiling the expression.
    */
    public Function compile(String source, SymbolTable symbols) {
        return compile(source, symbols, false);
    }

    /**
       Compiles the expression in the context of the given SymbolTable,
       updating the symbols if the expression is a function or constant definition.
       @param source the expression, e.g. "foo(n)=n^2"
       @param symbols the SymbolTable used for parsing the expression.
       The symbols are updated if the expression is a function or constant definition.
       @return the function evaluating the expression,
       or <code>null</code> if there were errors compiling the expression.
    */
    public Function compileAndDefine(String source, SymbolTable symbols) {
        return compile(source, symbols, true);
    }

    static private final SymbolTable defaultSymbols = new SymbolTable();
    private Lexer lexer = new Lexer();
    private RPN rpn     = new RPN();
    private DeclarationParser declParser = new DeclarationParser();
    private OptCodeGen codeGen = new OptCodeGen();
    private Declaration decl   = new Declaration();

    private synchronized Function compile(String source, SymbolTable symbols, boolean addDefinition) {
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
        
    private CompiledFunction compileExpression(String body, SymbolTable symbols, String args[], int arity) {
        symbols.pushFrame();
        symbols.addArguments(args);
        CompiledFunction fun = compileExpression(body, symbols, arity);
        symbols.popFrame();
        return fun;
    }

    private CompiledFunction compileExpression(String body, SymbolTable symbols, int arity) {
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
