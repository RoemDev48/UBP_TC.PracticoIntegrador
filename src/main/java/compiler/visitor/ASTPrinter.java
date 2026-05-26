package compiler.visitor;

import compiler.ast.*;
import java.util.List;

/**
 * Visitante encargado de generar una representación textual jerárquica del AST.
 * Dibuja un árbol en formato ASCII legible y con sangrías.
 */
public class ASTPrinter implements ASTVisitor<String> {
    private int indentLevel = 0;

    private String indent() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < indentLevel; i++) {
            sb.append("  ");
        }
        return sb.toString();
    }

    private String getHeader(ASTNode node, String name) {
        return indent() + "|- " + name + " (line " + node.getLine() + ", col " + node.getColumn() + ")";
    }

    @Override
    public String visit(ProgramNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append("ProgramNode (line ").append(node.getLine()).append(", col ").append(node.getColumn()).append(")\n");
        indentLevel++;
        for (ASTNode decl : node.getDeclarations()) {
            sb.append(decl.accept(this)).append("\n");
        }
        indentLevel--;
        return sb.toString().trim();
    }

    @Override
    public String visit(VarDeclNode node) {
        StringBuilder sb = new StringBuilder();
        
        // Construir representación textual del tipo complejo
        StringBuilder typeRepr = new StringBuilder();
        typeRepr.append(node.getType());
        for (int i = 0; i < node.getPointerDepth(); i++) {
            typeRepr.append("*");
        }
        if (node.isArray()) {
            typeRepr.append("[").append(node.getArraySize()).append("]");
        }

        sb.append(getHeader(node, "VarDeclNode [Type: " + typeRepr.toString() + ", ID: " + node.getIdentifier() + "]"));
        if (node.hasInitializer()) {
            sb.append("\n");
            indentLevel++;
            sb.append(node.getInitializer().accept(this));
            indentLevel--;
        }
        return sb.toString();
    }

    @Override
    public String visit(FuncDeclNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(getHeader(node, "FuncDeclNode [RetType: " + node.getReturnType() + ", ID: " + node.getIdentifier() + "]"));
        sb.append("\n");
        indentLevel++;
        
        // Parámetros
        sb.append(indent()).append("|- Parameters:\n");
        indentLevel++;
        if (node.getParameters().isEmpty()) {
            sb.append(indent()).append("+- (None)\n");
        } else {
            for (ParameterNode param : node.getParameters()) {
                sb.append(param.accept(this)).append("\n");
            }
            if (sb.charAt(sb.length() - 1) == '\n') sb.deleteCharAt(sb.length() - 1);
        }
        indentLevel--;
        
        // Cuerpo
        sb.append("\n").append(indent()).append("|- Body:\n");
        indentLevel++;
        sb.append(node.getBody().accept(this));
        indentLevel--;
        
        indentLevel--;
        return sb.toString();
    }

    @Override
    public String visit(ParameterNode node) {
        return getHeader(node, "ParameterNode [Type: " + node.getType() + ", ID: " + node.getIdentifier() + "]");
    }

    @Override
    public String visit(BlockNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(getHeader(node, "BlockNode"));
        indentLevel++;
        for (ASTNode stmt : node.getStatements()) {
            if (stmt != null) {
                sb.append("\n").append(stmt.accept(this));
            }
        }
        indentLevel--;
        return sb.toString();
    }

    @Override
    public String visit(AssignNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(getHeader(node, "AssignNode"));
        sb.append("\n");
        indentLevel++;
        
        sb.append(indent()).append("|- LHS:\n");
        indentLevel++;
        sb.append(node.getLhs().accept(this)).append("\n");
        indentLevel--;
        
        sb.append(indent()).append("|- RHS:\n");
        indentLevel++;
        sb.append(node.getExpression().accept(this));
        indentLevel--;
        
        indentLevel--;
        return sb.toString();
    }

    @Override
    public String visit(IfNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(getHeader(node, "IfNode"));
        
        sb.append("\n");
        indentLevel++;
        sb.append(indent()).append("|- Condition:\n");
        indentLevel++;
        sb.append(node.getCondition().accept(this));
        indentLevel--;
        
        sb.append("\n").append(indent()).append("|- Then:\n");
        indentLevel++;
        sb.append(node.getThenBranch().accept(this));
        indentLevel--;
        
        if (node.hasElseBranch()) {
            sb.append("\n").append(indent()).append("|- Else:\n");
            indentLevel++;
            sb.append(node.getElseBranch().accept(this));
            indentLevel--;
        }
        indentLevel--;
        return sb.toString();
    }

    @Override
    public String visit(WhileNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(getHeader(node, "WhileNode"));
        
        sb.append("\n");
        indentLevel++;
        sb.append(indent()).append("|- Condition:\n");
        indentLevel++;
        sb.append(node.getCondition().accept(this));
        indentLevel--;
        
        sb.append("\n").append(indent()).append("|- Body:\n");
        indentLevel++;
        sb.append(node.getBody().accept(this));
        indentLevel--;
        
        indentLevel--;
        return sb.toString();
    }

    @Override
    public String visit(ForNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(getHeader(node, "ForNode"));
        
        sb.append("\n");
        indentLevel++;
        
        sb.append(indent()).append("|- Init:\n");
        indentLevel++;
        if (node.getInitialization() != null) {
            sb.append(node.getInitialization().accept(this));
        } else {
            sb.append(indent()).append("+- (Empty)");
        }
        indentLevel--;
        
        sb.append("\n").append(indent()).append("|- Condition:\n");
        indentLevel++;
        if (node.getCondition() != null) {
            sb.append(node.getCondition().accept(this));
        } else {
            sb.append(indent()).append("+- (Empty)");
        }
        indentLevel--;
        
        sb.append("\n").append(indent()).append("|- Increment:\n");
        indentLevel++;
        if (node.getIncrement() != null) {
            sb.append(node.getIncrement().accept(this));
        } else {
            sb.append(indent()).append("+- (Empty)");
        }
        indentLevel--;
        
        sb.append("\n").append(indent()).append("|- Body:\n");
        indentLevel++;
        sb.append(node.getBody().accept(this));
        indentLevel--;
        
        indentLevel--;
        return sb.toString();
    }

    @Override
    public String visit(BreakNode node) {
        return getHeader(node, "BreakNode");
    }

    @Override
    public String visit(ContinueNode node) {
        return getHeader(node, "ContinueNode");
    }

    @Override
    public String visit(ReturnNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(getHeader(node, "ReturnNode"));
        if (node.hasExpression()) {
            sb.append("\n");
            indentLevel++;
            sb.append(node.getExpression().accept(this));
            indentLevel--;
        }
        return sb.toString();
    }

    @Override
    public String visit(BinaryOpNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(getHeader(node, "BinaryOpNode [Op: " + node.getOperator() + "]"));
        sb.append("\n");
        indentLevel++;
        sb.append(node.getLeft().accept(this)).append("\n");
        sb.append(node.getRight().accept(this));
        indentLevel--;
        return sb.toString();
    }

    @Override
    public String visit(UnaryOpNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(getHeader(node, "UnaryOpNode [Op: " + node.getOperator() + "]"));
        sb.append("\n");
        indentLevel++;
        sb.append(node.getExpression().accept(this));
        indentLevel--;
        return sb.toString();
    }

    @Override
    public String visit(LiteralNode node) {
        return getHeader(node, "LiteralNode [Type: " + node.getLiteralType() + ", Value: " + node.getValue() + "]");
    }

    @Override
    public String visit(IdNode node) {
        return getHeader(node, "IdNode [ID: " + node.getIdentifier() + "]");
    }

    @Override
    public String visit(FuncCallNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(getHeader(node, "FuncCallNode [ID: " + node.getIdentifier() + "]"));
        if (!node.getArguments().isEmpty()) {
            sb.append("\n");
            indentLevel++;
            for (ASTNode arg : node.getArguments()) {
                if (arg != null) {
                    sb.append(arg.accept(this)).append("\n");
                }
            }
            if (sb.charAt(sb.length() - 1) == '\n') sb.deleteCharAt(sb.length() - 1);
            indentLevel--;
        }
        return sb.toString();
    }

    @Override
    public String visit(ArrayAccessNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(getHeader(node, "ArrayAccessNode [Array: " + node.getArrayName() + "]"));
        sb.append("\n");
        indentLevel++;
        sb.append(node.getIndex().accept(this));
        indentLevel--;
        return sb.toString();
    }

    @Override
    public String visit(DereferenceNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(getHeader(node, "DereferenceNode [*]"));
        sb.append("\n");
        indentLevel++;
        sb.append(node.getExpression().accept(this));
        indentLevel--;
        return sb.toString();
    }

    @Override
    public String visit(AddressOfNode node) {
        StringBuilder sb = new StringBuilder();
        sb.append(getHeader(node, "AddressOfNode [&]"));
        sb.append("\n");
        indentLevel++;
        sb.append(node.getExpression().accept(this));
        indentLevel--;
        return sb.toString();
    }
}
