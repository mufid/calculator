import java.util.Hashtable;

final class SymbolTable {
    private Hashtable ht = new Hashtable(50);

    Symbol get(String name) {
        return (Symbol) ht.get(name);
    }
    
    Symbol put(Symbol s) {
        return (Symbol) ht.put(s.name, s);
    }

    void remove(String name) {
        ht.remove(name);
    }

    /*
    Symbol[] push(String names[], Symbol symbols[]) {
        int n = names.length;
        Symbol saves[] = new Symbol[n];
        for (int i = 0; i < n; ++i) {
            saves[i] = ht.put(names[i], symbols[i]);
        }
        return saves;
    }
    */
}
