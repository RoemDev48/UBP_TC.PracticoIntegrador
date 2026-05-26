package compiler.ast;

import compiler.visitor.ASTVisitor;
import java.util.List;

/**
 * Nodo que representa la declaración de una función.
 * Ejemplo: int suma(int a, int b) { return a + b; }
 */
public class FuncDeclNode extends ASTNode {
    private final String returnType;
    private final String identifier;
    private final List<ParameterNode> parameters;
    private final BlockNode body;

    public FuncDeclNode(String returnType, String identifier, List<ParameterNode> parameters, BlockNode body, int line, int column) {
        super(line, column);
        this.returnType = returnType;
        this.identifier = identifier;
        this.parameters = parameters;
        this.body = body;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public List<ParameterNode> getParameters() {
        return parameters;
    }

    public BlockNode getBody() {
        return body;
    }

    @Override
    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
