package compiler.ast;

import compiler.visitor.ASTVisitor;

/**
 * Nodo que representa el acceso indexado a un arreglo.
 * Ejemplo: datos[i] o arr[5]
 */
public class ArrayAccessNode extends ASTNode {
    private final String arrayName;
    private final ASTNode index;

    public ArrayAccessNode(String arrayName, ASTNode index, int line, int column) {
        super(line, column);
        this.arrayName = arrayName;
        this.index = index;
    }

    public String getArrayName() {
        return arrayName;
    }

    public ASTNode getIndex() {
        return index;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
