package compiler.ast;

import compiler.visitor.ASTVisitor;

/**
 * Nodo que representa una operación binaria (aritmética, relacional o lógica).
 * Ejemplos: x + y, a < b, cond1 && cond2
 */
public class BinaryOpNode extends ASTNode {
    private final ASTNode left;
    private final String operator;
    private final ASTNode right;

    public BinaryOpNode(ASTNode left, String operator, ASTNode right, int line, int column) {
        super(line, column);
        this.left = left;
        this.operator = operator;
        this.right = right;
    }

    public ASTNode getLeft() {
        return left;
    }

    public String getOperator() {
        return operator;
    }

    public ASTNode getRight() {
        return right;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
