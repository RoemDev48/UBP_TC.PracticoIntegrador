package compiler.ast;

import compiler.visitor.ASTVisitor;

/**
 * Nodo que representa un valor literal constante (int, double, char, bool).
 */
public class LiteralNode extends ASTNode {
    public enum LiteralType {
        INT,
        DOUBLE,
        CHAR,
        BOOL
    }

    private final String value;
    private final LiteralType literalType;

    public LiteralNode(String value, LiteralType literalType, int line, int column) {
        super(line, column);
        this.value = value;
        this.literalType = literalType;
    }

    public String getValue() {
        return value;
    }

    public LiteralType getLiteralType() {
        return literalType;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
