package compiler.ast;

import compiler.visitor.ASTVisitor;
import java.util.List;

/**
 * Nodo que representa la llamada a una función.
 * Ejemplo: suma(x, 10)
 */
public class FuncCallNode extends ASTNode {
    private final String identifier;
    private final List<ASTNode> arguments;

    public FuncCallNode(String identifier, List<ASTNode> arguments, int line, int column) {
        super(line, column);
        this.identifier = identifier;
        this.arguments = arguments;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<ASTNode> getArguments() {
        return arguments;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
