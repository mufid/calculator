// Copyright (c) 2007, Carlo Teubner
// Available under the MIT License (see COPYING).

public class Result {
    public CompiledFunction function;
    public int definedSymbol;
    public int plotCommand;
    public double[] plotArgs;
    public int errorPos;

    public void init(CompiledFunction function, int definedSymbol, int plotCommand, double[] plotArgs) {
        this.function = function;
        this.definedSymbol = definedSymbol;
        this.plotCommand = plotCommand;
        this.plotArgs = plotArgs;
        this.errorPos = -1;
    }
    
    public void init(int errorPos) {
        this.errorPos = errorPos;
        this.function = null;
        this.definedSymbol = -1;
        this.plotCommand = -1;
        this.plotArgs = null;
    }

    public boolean hasValue() {
        return errorPos == -1 && plotCommand == -1 && function.arity() == 0; 
    }    
}