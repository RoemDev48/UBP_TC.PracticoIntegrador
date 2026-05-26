package compiler.ast;

import compiler.visitor.ASTVisitor;

/**
 * Nodo que representa un parámetro en la firma de una función.
 * Soporta escalares y punteros.
 * Ejemplo: int x, int* ptr
 */
public class ParameterNode extends ASTNode {
    private final String type;
    private final String identifier;
    private final int pointerDepth;

    public ParameterNode(String type, String identifier, int line, int column) {
        this(type, identifier, 0, line, column);
    }

    public ParameterNode(String type, String identifier, int pointerDepth, int line, int column) {
        super(line, column);
        this.type = type;
        this.identifier = identifier;
        this.pointerDepth = pointerDepth;
    }

    public String getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }

    public int getPointerDepth() {
        return pointerDepth;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
