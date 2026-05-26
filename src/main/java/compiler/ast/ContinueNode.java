package compiler.ast;

import compiler.visitor.ASTVisitor;

/**
 * Nodo que representa una sentencia continue.
 */
public class ContinueNode extends ASTNode {
    public ContinueNode(int line, int column) {
        super(line, column);
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
