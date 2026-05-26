package compiler.ast;

import compiler.visitor.ASTVisitor;

/**
 * Nodo que representa un identificador o variable en una expresión.
 */
public class IdNode extends ASTNode {
    private final String identifier;

    public IdNode(String identifier, int line, int column) {
        super(line, column);
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
