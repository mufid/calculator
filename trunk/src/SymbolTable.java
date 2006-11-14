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
}
