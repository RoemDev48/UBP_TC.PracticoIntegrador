package compiler.semantic;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa un símbolo (variable o función) en la tabla de símbolos.
 */
public class Symbol {
    public enum Kind {
        VARIABLE,
        FUNCTION
    }

    private final String name;
    private final Type type;
    private final Kind kind;
    
    // Solo para funciones
    private final List<Type> parameterTypes;
    private final Type returnType;

    /**
     * Constructor para variables.
     */
    public Symbol(String name, Type type) {
        this.name = name;
        this.type = type;
        this.kind = Kind.VARIABLE;
        this.parameterTypes = null;
        this.returnType = null;
    }

    /**
     * Constructor para funciones.
     */
    public Symbol(String name, Type returnType, List<Type> parameterTypes) {
        this.name = name;
        this.type = returnType;
        this.kind = Kind.FUNCTION;
        this.returnType = returnType;
        this.parameterTypes = parameterTypes;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public Kind getKind() {
        return kind;
    }

    public boolean isVariable() {
        return kind == Kind.VARIABLE;
    }

    public boolean isFunction() {
        return kind == Kind.FUNCTION;
    }

    public List<Type> getParameterTypes() {
        return parameterTypes != null ? parameterTypes : new ArrayList<>();
    }

    public Type getReturnType() {
        return returnType;
    }

    @Override
    public String toString() {
        if (isFunction()) {
            return "Function " + name + " -> return: " + returnType + ", params: " + parameterTypes;
        } else {
            return "Variable " + name + " -> type: " + type;
        }
    }
}
