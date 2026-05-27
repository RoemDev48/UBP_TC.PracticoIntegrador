package compiler.ast;

import compiler.visitor.ASTVisitor;

/**
 * Nodo que representa una operación unaria.
 * Ejemplos: -x, !flag, +value, ++x, x--
 */
public class UnaryOpNode extends ASTNode {
    private final String operator;
    private final ASTNode expression;
    private final boolean isPostfix;

    public UnaryOpNode(String operator, ASTNode expression, int line, int column) {
        this(operator, expression, false, line, column);
    }

    public UnaryOpNode(String operator, ASTNode expression, boolean isPostfix, int line, int column) {
        super(line, column);
        this.operator = operator;
        this.expression = expression;
        this.isPostfix = isPostfix;
    }

    public String getOperator() {
        return operator;
    }

    public ASTNode getExpression() {
        return expression;
    }

    public boolean isPostfix() {
        return isPostfix;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
