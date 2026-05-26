package compiler.semantic;

import java.util.HashMap;
import java.util.Map;

/**
 * Representa un ámbito (scope) léxico en el compilador.
 * Soporta anidamiento mediante referencias a ámbitos padres.
 */
public class Scope {
    private final Scope parent;
    private final Map<String, Symbol> symbols;

    public Scope(Scope parent) {
        this.parent = parent;
        this.symbols = new HashMap<>();
    }

    public Scope getParent() {
        return parent;
    }

    /**
     * Define un nuevo símbolo en este ámbito local.
     *
     * @param symbol Símbolo a definir.
     * @return true si se definió correctamente, false si ya existía en este ámbito local.
     */
    public boolean define(Symbol symbol) {
        if (symbols.containsKey(symbol.getName())) {
            return false;
        }
        symbols.put(symbol.getName(), symbol);
        return true;
    }

    /**
     * Busca un símbolo localmente en este ámbito.
     */
    public Symbol lookupLocal(String name) {
        return symbols.get(name);
    }

    /**
     * Busca un símbolo subiendo recursivamente por la cadena de ámbitos padres.
     */
    public Symbol lookup(String name) {
        Symbol sym = symbols.get(name);
        if (sym != null) {
            return sym;
        }
        if (parent != null) {
            return parent.lookup(name);
        }
        return null;
    }

    public Map<String, Symbol> getSymbols() {
        return symbols;
    }
}
