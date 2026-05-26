package compiler.ast;

import compiler.visitor.ASTVisitor;

/**
 * Nodo que representa un bucle for.
 * Ejemplo: for (int i = 0; i < 10; i = i + 1) { ... }
 */
public class ForNode extends ASTNode {
    private final ASTNode initialization; // Puede ser null, o un VarDeclNode / AssignNode
    private final ASTNode condition;      // Puede ser null
    private final ASTNode increment;      // Puede ser null, típicamente AssignNode u otro nodo de expresión
    private final ASTNode body;

    public ForNode(ASTNode initialization, ASTNode condition, ASTNode increment, ASTNode body, int line, int column) {
        super(line, column);
        this.initialization = initialization;
        this.condition = condition;
        this.increment = increment;
        this.body = body;
    }

    public ASTNode getInitialization() {
        return initialization;
    }

    public ASTNode getCondition() {
        return condition;
    }

    public ASTNode getIncrement() {
        return increment;
    }

    public ASTNode getBody() {
        return body;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
