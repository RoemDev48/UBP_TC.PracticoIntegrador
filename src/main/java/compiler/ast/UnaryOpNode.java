package compiler.ast;

import compiler.visitor.ASTVisitor;

/**
 * Nodo que representa una operación unaria.
 * Ejemplos: -x, !flag, +value
 */
public class UnaryOpNode extends ASTNode {
    private final String operator;
    private final ASTNode expression;

    public UnaryOpNode(String operator, ASTNode expression, int line, int column) {
        super(line, column);
        this.operator = operator;
        this.expression = expression;
    }

    public String getOperator() {
        return operator;
    }

    public ASTNode getExpression() {
        return expression;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
