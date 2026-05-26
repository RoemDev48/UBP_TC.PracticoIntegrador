package compiler.ast;

import compiler.visitor.ASTVisitor;

/**
 * Nodo que representa la desreferenciación de un puntero.
 * Ejemplo: *ptr
 */
public class DereferenceNode extends ASTNode {
    private final ASTNode expression;

    public DereferenceNode(ASTNode expression, int line, int column) {
        super(line, column);
        this.expression = expression;
    }

    public ASTNode getExpression() {
        return expression;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
