package compiler.ast;

import compiler.visitor.ASTVisitor;

/**
 * Nodo que representa el acceso a un miembro de una estructura.
 * Ejemplo: p.x
 */
public class MemberAccessNode extends ASTNode {
    private final ASTNode expression;
    private final String memberName;

    public MemberAccessNode(ASTNode expression, String memberName, int line, int column) {
        super(line, column);
        this.expression = expression;
        this.memberName = memberName;
    }

    public ASTNode getExpression() {
        return expression;
    }

    public String getMemberName() {
        return memberName;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
