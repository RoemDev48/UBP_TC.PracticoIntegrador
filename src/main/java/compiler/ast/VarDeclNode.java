package compiler.ast;

import compiler.visitor.ASTVisitor;

/**
 * Nodo que representa la declaración de una variable individual.
 * Soporta escalares, punteros y arreglos de tamaño fijo.
 */
public class VarDeclNode extends ASTNode {
    private final String type;
    private final String identifier;
    private final ASTNode initializer; // Puede ser null
    
    // Metadatos para tipos complejos
    private final int pointerDepth;
    private final boolean isArray;
    private final int arraySize;

    /**
     * Constructor por defecto para variables escalares simples.
     */
    public VarDeclNode(String type, String identifier, ASTNode initializer, int line, int column) {
        this(type, identifier, initializer, 0, false, 0, line, column);
    }

    /**
     * Constructor extendido para punteros y arreglos.
     */
    public VarDeclNode(String type, String identifier, ASTNode initializer, 
                       int pointerDepth, boolean isArray, int arraySize, int line, int column) {
        super(line, column);
        this.type = type;
        this.identifier = identifier;
        this.initializer = initializer;
        this.pointerDepth = pointerDepth;
        this.isArray = isArray;
        this.arraySize = arraySize;
    }

    public String getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public ASTNode getInitializer() {
        return initializer;
    }

    public boolean hasInitializer() {
        return initializer != null;
    }

    public int getPointerDepth() {
        return pointerDepth;
    }

    public boolean isArray() {
        return isArray;
    }

    public int getArraySize() {
        return arraySize;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
