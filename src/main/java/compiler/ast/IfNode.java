package compiler.ast;

import compiler.visitor.ASTVisitor;

/**
 * Nodo que representa una sentencia condicional (if-else).
 */
public class IfNode extends ASTNode {
    private final ASTNode condition;
    private final ASTNode thenBranch;
    private final ASTNode elseBranch; // Puede ser null

    public IfNode(ASTNode condition, ASTNode thenBranch, ASTNode elseBranch, int line, int column) {
        super(line, column);
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public ASTNode getCondition() {
        return condition;
    }

    public ASTNode getThenBranch() {
        return thenBranch;
    }

    public ASTNode getElseBranch() {
        return elseBranch;
    }

    public boolean hasElseBranch() {
        return elseBranch != null;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
