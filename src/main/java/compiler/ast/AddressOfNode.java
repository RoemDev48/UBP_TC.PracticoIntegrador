package compiler.ast;

import compiler.visitor.ASTVisitor;

/**
 * Nodo que representa el direccionamiento de memoria.
 * Ejemplo: &x
 */
public class AddressOfNode extends ASTNode {
    private final ASTNode expression;

    public AddressOfNode(ASTNode expression, int line, int column) {
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
