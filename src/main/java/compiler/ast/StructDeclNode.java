package compiler.ast;

import compiler.visitor.ASTVisitor;
import java.util.List;

/**
 * Nodo que representa la declaración de una estructura (struct).
 * Ejemplo: struct Punto { int x; int y; };
 */
public class StructDeclNode extends ASTNode {
    private final String structName;
    private final List<VarDeclNode> members;

    public StructDeclNode(String structName, List<VarDeclNode> members, int line, int column) {
        super(line, column);
        this.structName = structName;
        this.members = members;
    }

    public String getStructName() {
        return structName;
    }

    public List<VarDeclNode> getMembers() {
        return members;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
