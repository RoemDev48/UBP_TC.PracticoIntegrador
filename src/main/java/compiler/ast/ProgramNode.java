package compiler.ast;

import compiler.visitor.ASTVisitor;
import java.util.List;

/**
 * Nodo raíz del AST que representa un programa completo.
 * Contiene una lista de declaraciones globales (variables o funciones).
 */
public class ProgramNode extends ASTNode {
    private final List<ASTNode> declarations;

    public ProgramNode(List<ASTNode> declarations, int line, int column) {
        super(line, column);
        this.declarations = declarations;
    }

    public List<ASTNode> getDeclarations() {
        return declarations;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
