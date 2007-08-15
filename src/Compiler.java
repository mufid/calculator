// Copyright (c) 2007, Carlo Teubner
// Available under the MIT License (see COPYING).

public class Compiler implements VMConstants
{
    private Lexer lexer;
    private CompiledFunction func;
    private int arity;
    private int definedSymbol;
    private int parameterCallArity;
    private int plotCommand;
    private double[] plotArgs;
    private boolean unaryDone;

    public static Result result;
    
    static SyntaxError error = new SyntaxError();

    public boolean compile(String input) {
        init();
        try {
            if (Lexer.isAssignment(input)) {
                definedSymbol = Lexer.getSymbol(input.substring(0, 1));
                if (!(FIRST_VAR <= definedSymbol && definedSymbol <= LAST_VAR))
                    throw error;
                input = input.substring(3);
            }
            lexer.init(input);
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
                if (definedSymbol != -1)
                    func.setArity(arity);
            }
            if (lexer.peekToken() != Lexer.TOK_END)
                throw error;
            if (!func.check(definedSymbol != -1 && arity > 0 ? 1 << (definedSymbol - FIRST_VAR) : 0))
                throw error;
        } catch (SyntaxError e) {
            int errorPos = lexer.lastPos();
            if (definedSymbol != -1)
                errorPos += 3;
            if (input.length() > 0)
                Log.log("syntax error at " + errorPos);
            result.init(errorPos);
            return false;
        }
        result.init(func, definedSymbol, plotCommand, plotArgs);
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
        plotArgs = null;
        unaryDone = false;
    }

    private void compileExpr() throws SyntaxError {
        compileProduct();
        int op = lexer.peekToken();
        while (op == PLUS || op == MINUS) {
            lexer.nextToken();
            compileProduct();
            func.pushInstr(op);
            op = lexer.peekToken();
        }
    }

    private void compileProduct() throws SyntaxError {
        compilePower();
        int op = lexer.peekToken();
        while (op != PLUS && op != MINUS && op != Lexer.TOK_COMMA && op != Lexer.TOK_RPAREN && op != Lexer.TOK_END) {
            if (op == TIMES || op == DIVIDE || op == MODULO)
                lexer.nextToken();
            else
                op = TIMES;
            compilePower();
            func.pushInstr(op);
            op = lexer.peekToken();
        }
    }
    
    private void compilePower() throws SyntaxError {
        compileFactorial();
        int op = lexer.peekToken();
        if (op == POWER) {
            lexer.nextToken();
            compilePower();
            func.pushInstr(op);
        }
    }
    
    private void compileFactorial() throws SyntaxError {
        compileUnary();
        while (lexer.peekToken() == FACTORIAL) {
            lexer.nextToken();
            func.pushInstr(FACTORIAL);
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
        } else if (c == MINUS) {
            compileUnary();
            func.pushInstr(UMINUS);
        } else if (c == PLUS) {
            compileUnary();
        } else if (c == LITERAL) {
            func.pushLiteral(lexer.number());
            func.pushInstr(LITERAL);
        } else if (FIRST_PAR <= c && c <= LAST_PAR) {
            if (definedSymbol == -1 && c - FIRST_PAR > parameterCallArity - 1)
                throw error;
            arity = Math.max(arity, c - FIRST_PAR + 1);
            func.pushInstr(c);
        } else if (FIRST_CONST <= c && c <= LAST_CONST) {
            func.pushInstr(c);
        } else if (FIRST_VAR <= c && c <= LAST_VAR) {
            switch (Variables.getType(c)) {
            case Variables.TYPE_FUNC:
            {
                CompiledFunction fn = Variables.getFunction(c);
                int fn_arity = fn.arity();
                compileArgList(fn_arity);
                func.pushInstr(c + VARFUN_OFFSET, fn_arity);
                break;
            }
            case Variables.TYPE_NUM:
                func.pushInstr(c);
                break;
            default:
                throw error;
            }
        } else if (FIRST_FUNCTION <= c && c <= LAST_FUNCTION) {
            compileArgList(Lexer.getBuiltinArity(c));
            func.pushInstr(c);
        } else
            throw error;
    }

    private void compileArgList(int fn_arity) throws SyntaxError {
        if (lexer.peekToken() != Lexer.TOK_LPAREN) {
            if (fn_arity == parameterCallArity) {
                for (int i = 0; i < fn_arity; ++i)
                    func.pushInstr(FIRST_PAR + i);
                return;
            }
            throw error;
        }
        lexer.nextToken();
        for (int i = 0; i < fn_arity; ++i) {
            compileExpr();
            int tok = lexer.nextToken();
            if (!(i == fn_arity - 1 && (tok == Lexer.TOK_RPAREN || tok == Lexer.TOK_END) || i < fn_arity - 1 && tok == Lexer.TOK_COMMA))
                throw error;
        }
    }

    private void compilePlotCommand() throws SyntaxError {
        plotCommand = lexer.nextToken();
        if (lexer.nextToken() != Lexer.TOK_LPAREN)
            throw error;
        parameterCallArity = Lexer.plotFunctionArity(plotCommand);
        compileExpr();
        func.setArity(parameterCallArity);
        parameterCallArity = -1;
        CompiledFunction plotFunction = func;
        int commandArity = Lexer.getBuiltinArity(plotCommand);
        plotArgs = new double[commandArity - 1];
        for (int i = 0; i < commandArity - 1; ++i) {
            if (lexer.nextToken() != Lexer.TOK_COMMA)
                throw error;
            func = new CompiledFunction();
            compileExpr();
            plotArgs[i] = func.evaluate();
        }
        func = plotFunction;
        int tok = lexer.nextToken();
        if (!(tok == Lexer.TOK_RPAREN || tok == Lexer.TOK_END))
            throw error;
    }
}