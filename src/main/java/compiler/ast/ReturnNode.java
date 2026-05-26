package compiler.ast;

import compiler.visitor.ASTVisitor;

/**
 * Nodo que representa una sentencia return con expresión opcional.
 */
public class ReturnNode extends ASTNode {
    private final ASTNode expression; // Puede ser null

    public ReturnNode(ASTNode expression, int line, int column) {
        super(line, column);
        this.expression = expression;
    }

    public ASTNode getExpression() {
        return expression;
    }

    public boolean hasExpression() {
        return expression != null;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
