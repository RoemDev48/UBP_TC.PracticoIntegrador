package compiler.ast;

import compiler.visitor.ASTVisitor;

/**
 * Nodo que representa un bucle while.
 */
public class WhileNode extends ASTNode {
    private final ASTNode condition;
    private final ASTNode body;

    public WhileNode(ASTNode condition, ASTNode body, int line, int column) {
        super(line, column);
        this.condition = condition;
        this.body = body;
    }

    public ASTNode getCondition() {
        return condition;
    }

    public ASTNode getBody() {
        return body;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
