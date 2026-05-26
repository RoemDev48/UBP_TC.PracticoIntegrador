package compiler.ast;

import compiler.visitor.ASTVisitor;

/**
 * Nodo que representa una sentencia break.
 */
public class BreakNode extends ASTNode {
    public BreakNode(int line, int column) {
        super(line, column);
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
