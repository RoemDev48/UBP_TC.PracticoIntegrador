package compiler.ast;

import compiler.visitor.ASTVisitor;
import java.util.List;

/**
 * Nodo que representa un bloque de sentencias agrupadas en llaves.
 * Ejemplo: { int x = 1; x = x + 1; }
 */
public class BlockNode extends ASTNode {
    private final List<ASTNode> statements;

    public BlockNode(List<ASTNode> statements, int line, int column) {
        super(line, column);
        this.statements = statements;
    }

    public List<ASTNode> getStatements() {
        return statements;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
