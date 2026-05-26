package compiler.ast;

import compiler.visitor.ASTVisitor;

/**
 * Nodo que representa una asignación de variable.
 * Se generaliza para L-Values (asignaciones simples, a arreglos o a punteros).
 * Ejemplo: x = 5, arr[i] = 10, *ptr = 20
 */
public class AssignNode extends ASTNode {
    private final ASTNode lhs; // L-Value del lado izquierdo
    private final ASTNode expression; // Expresión del lado derecho

    public AssignNode(ASTNode lhs, ASTNode expression, int line, int column) {
        super(line, column);
        this.lhs = lhs;
        this.expression = expression;
    }

    public ASTNode getLhs() {
        return lhs;
    }

    public ASTNode getExpression() {
        return expression;
    }

    /**
     * Retorna el identificador principal para retrocompatibilidad.
     */
    public String getIdentifier() {
        if (lhs instanceof IdNode) {
            return ((IdNode) lhs).getIdentifier();
        }
        if (lhs instanceof ArrayAccessNode) {
            return ((ArrayAccessNode) lhs).getArrayName();
        }
        return null;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
