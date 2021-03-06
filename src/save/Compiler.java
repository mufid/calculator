// Copyright (c) 2007 Carlo Teubner.
// Available under the MIT License (see COPYING).

public class Compiler {
    private Lexer lexer;
    private CompiledFunction func, func2;
    private int arity;
    private int definedSymbol;
    private int parameterCallArity;
    private int plotCommand;
    private double[] plotArgs;
    private boolean x_is_t;
    private boolean unaryDone;
    private int errorStart, errorEnd;
    private int selfRefPos;

    public static Result result;
    
    static SyntaxError error = new SyntaxError();

    public boolean compile(char[] input, int len) {
        init();
        try {
            int start;
            if (Lexer.isAssignment(new String(input, 0, len))) {
                definedSymbol = Lexer.getSymbol(new String(input, 0, 1));
                if (!(VM.FIRST_VAR <= definedSymbol && definedSymbol <= VM.LAST_VAR))
                    throw error;
                start = 3;
            } else
                start = 0;
            lexer.init(input, start, len);
            if (Lexer.isPlotCommand(lexer.peekToken())) {
                if (definedSymbol != -1)
                    throw error;
                compilePlotCommand();
            } else {
                compileExpr();
                while (lexer.peekToken() == Lexer.TOK_RPAREN) {
                    lexer.nextToken();
                    unaryDone = true;
                    compileExpr();
                }
                if (definedSymbol != -1) {
                    func.setArity(arity);
                    if (arity > 0 && selfRefPos != -1) {
                        errorStart = errorEnd = selfRefPos;
                        throw error;
                    }
                }
            }
            if (lexer.peekToken() != Lexer.TOK_END)
                throw error;
        } catch (SyntaxError e) {
            if (errorStart == -1) {
                errorStart = lexer.lastPos();
                errorEnd = lexer.curPos() - 1;
            }
            /*
            if (len > 0)
                Log.log("syntax error at " + errorStart + '-' + errorEnd);
            */
            result.init(errorStart, errorEnd, plotCommand);
            return false;
        }
        result.init(func, func2, definedSymbol, plotCommand, plotArgs);
        return true;
    }

    private void init() {
        if (result == null)
            result = new Result();
        if (func == null)
            func = new CompiledFunction();
        else
            func.init();
        if (lexer == null)
            lexer = new Lexer();
        arity = 0;
        definedSymbol = -1;
        parameterCallArity = -1;
        plotCommand = -1;
        x_is_t = false;
        unaryDone = false;
        errorStart = errorEnd = -1;
        selfRefPos = -1;
    }

    private void compileExpr(CompiledFunction cf) throws SyntaxError {
        CompiledFunction funcOrig = func;
        func = cf;
        try {
            compileExpr();
        } catch (SyntaxError e) {
            func = funcOrig;
            throw e;
        }
        func = funcOrig;
    }

    private void compileExpr() throws SyntaxError {
        compileProduct();
        int op = lexer.peekToken();
        while (op == VM.PLUS || op == VM.MINUS) {
            lexer.nextToken();
            compileProduct();
            func.pushInstr(op);
            op = lexer.peekToken();
        }
    }

    private void compileProduct() throws SyntaxError {
        compilePower();
        int op = lexer.peekToken();
        while (op != VM.PLUS && op != VM.MINUS && op != Lexer.TOK_COMMA && op != Lexer.TOK_RPAREN && op != Lexer.TOK_END) {
            if (op == VM.TIMES || op == VM.DIVIDE || op == VM.MODULO)
                lexer.nextToken();
            else
                op = VM.TIMES;
            compilePower();
            func.pushInstr(op);
            op = lexer.peekToken();
        }
    }

    private void compilePower() throws SyntaxError {
        boolean uminus = false;
        while (true) {
            switch (lexer.peekToken()) {
            case VM.MINUS: uminus = !uminus;  // fall through
            case VM.PLUS: lexer.nextToken(); continue;
            }
            break;
        }
        compileFactorial();
        int op = lexer.peekToken();
        if (op == VM.POWER) {
            lexer.nextToken();
            boolean e = func.topInstr() == VM.CONST_E;
            if (e)
                func.popInstr();
            compilePower();
            func.pushInstr(e ? VM.EXP : VM.POWER);
        }
        if (uminus)
            func.pushInstr(VM.UMINUS);
    }

    private void compileFactorial() throws SyntaxError {
        compileUnary();
        while (lexer.peekToken() == VM.FACTORIAL) {
            lexer.nextToken();
            func.pushInstr(VM.FACTORIAL);
        }
    }

    private void compileUnary() throws SyntaxError
    {
        if (unaryDone) {
            unaryDone = false;
            return;
        }

        int c = lexer.nextToken();

        if (c == Lexer.TOK_LPAREN) {
            compileExpr();
            c = lexer.nextToken();
            if (!(c == Lexer.TOK_RPAREN || c == Lexer.TOK_END))
                throw error;
        } else if (c == VM.LITERAL) {
            func.pushLiteral(lexer.number());
            func.pushInstr(VM.LITERAL);
        } else if (VM.FIRST_PAR <= c && c <= VM.LAST_PAR) {
            if (definedSymbol == -1 && c - VM.FIRST_PAR + 1 > parameterCallArity)
                throw error;
            if (x_is_t && c == VM.PAR_X)
                throw error;
            arity = Math.max(arity, c - VM.FIRST_PAR + 1);
            func.pushInstr(c);
        } else if (c == Lexer.TOK_PAR_T) {
            if (!x_is_t)
                throw error;
            arity = 1;
            func.pushInstr(VM.PAR_X);
        } else if (VM.FIRST_CONST <= c && c <= VM.LAST_CONST) {
            func.pushInstr(c);
        } else if (VM.FIRST_VAR <= c && c <= VM.LAST_VAR) {
            switch (Variables.getType(c)) {
            case Variables.TYPE_FUNC:
            {
                CompiledFunction fn = Variables.getFunction(c);
                final int forbidden = 1 << (c - VM.FIRST_VAR),
                          flaggable = definedSymbol != -1 ? (1 << (definedSymbol - VM.FIRST_VAR)) : 0;
                final int status = fn.check(forbidden, flaggable);
                if (status == CompiledFunction.FAIL)
                    throw error;
                if (status == CompiledFunction.FLAG || c == definedSymbol)
                    selfRefPos = lexer.lastPos();
                final int fn_arity = fn.arity();
                compileArgList(fn_arity);
                func.pushInstr(c + VM.VARFUN_OFFSET, fn_arity);
                break;
            }
            case Variables.TYPE_NUM:
                func.pushInstr(c);
                break;
            default:
                throw error;
            }
        } else if (VM.FIRST_FUNCTION <= c && c <= VM.LAST_FUNCTION) {
            compileArgList(Lexer.getBuiltinArity(c));
            func.pushInstr(c);
        } else
            throw error;
    }

    private void compileArgList(int fn_arity) throws SyntaxError {
        if (lexer.peekToken() != Lexer.TOK_LPAREN) {
            if (fn_arity == parameterCallArity) {
                for (int i = 0; i < fn_arity; ++i)
                    func.pushInstr(VM.FIRST_PAR + i);
                return;
            }
            throw error;
        }
        lexer.nextToken();
        for (int i = 0; i < fn_arity; ++i) {
            compileExpr();
            final int tok = lexer.nextToken();
            if (!(i == fn_arity - 1 && (tok == Lexer.TOK_RPAREN || tok == Lexer.TOK_END) || i < fn_arity - 1 && tok == Lexer.TOK_COMMA))
                throw error;
        }
    }

    private CompiledFunction parFunc = null;

    private void compilePlotCommand() throws SyntaxError {
        plotCommand = lexer.nextToken();
        if (lexer.nextToken() != Lexer.TOK_LPAREN)
            throw error;
        parameterCallArity = Lexer.plotFunctionArity(plotCommand);
        if (plotCommand == VM.PARPLOT)
            x_is_t = true;
        compileExpr();
        func.setArity(parameterCallArity);
        if (plotCommand == VM.PARPLOT) {
            if (lexer.nextToken() != Lexer.TOK_COMMA)
                throw error;
            if (func2 == null)
                func2 = new CompiledFunction();
            else
                func2.init();
            compileExpr(func2);
            func2.setArity(parameterCallArity);
            x_is_t = false;
        }
        parameterCallArity = -1;
        final int remainingArity = Lexer.getBuiltinArity(plotCommand) - (plotCommand == VM.PARPLOT ? 2 : 1);
        if (plotArgs == null)
            plotArgs = new double[4];
        boolean lastMapArgMissing = false;
        double prevPlotArg = 0; // compiler complains without initialization
        for (int i = 0; i < remainingArity; ++i) {
            if (lexer.peekToken() != Lexer.TOK_COMMA) {
                if (i == remainingArity - 1 && plotCommand == VM.MAP) {
                    lastMapArgMissing = true;
                    break;
                } else {
                    lexer.nextToken();
                    throw error;
                }
            } else
                lexer.nextToken();
            if (parFunc == null)
                parFunc = new CompiledFunction();
            else
                parFunc.init();
            final int lexerStart = lexer.curPos();
            compileExpr(parFunc);
            final double curPlotArg = parFunc.evaluate();
            boolean throwError = false;
            if (!isReal(curPlotArg))
                throwError = true;
            else {
                if ((i & 1) == 0) {
                    prevPlotArg = curPlotArg;
                } else {
                    if (curPlotArg <= prevPlotArg)
                        throwError = true;
                }
            }
            if (throwError) {
                errorStart = lexerStart;
                errorEnd = lexer.curPos() - 1;
                throw error;
            }
            plotArgs[i] = curPlotArg;
        }
        final int tok = lexer.nextToken();
        if (!(tok == Lexer.TOK_RPAREN || tok == Lexer.TOK_END))
            throw error;
        if (lastMapArgMissing)
            plotArgs[remainingArity - 1] = Double.NaN;
    }

    private static boolean isReal(double d) {
        return !(Double.isInfinite(d) || Double.isNaN(d));
    }
}
