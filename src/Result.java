// Copyright (c) 2007 Carlo Teubner.
// Available under the MIT License (see COPYING).

public class Result {
    public CompiledFunction function, function2;
    public int definedSymbol;
    public int plotCommand;
    public double[] plotArgs;
    public int errorStart, errorEnd;

    public void init(CompiledFunction function, CompiledFunction function2, int definedSymbol, int plotCommand, double[] plotArgs) {
        this.function = function;
        this.function2 = function2;
        this.definedSymbol = definedSymbol;
        this.plotCommand = plotCommand;
        this.plotArgs = plotArgs;
        this.errorStart = -1;
        this.errorEnd = -1;
    }
    
    public void init(int errorStart, int errorEnd, int plotCommand) {
        this.errorStart = errorStart;
        this.errorEnd = errorEnd;
        this.function = null;
        this.function = null;
        this.definedSymbol = -1;
        this.plotCommand = plotCommand;
        this.plotArgs = null;
    }

    public boolean hasValue() {
        return errorStart == -1 && plotCommand == -1 && function.arity() == 0; 
    }    
}