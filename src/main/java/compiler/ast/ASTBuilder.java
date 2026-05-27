package compiler.ast;

import compiler.parser.CPPSubsetBaseVisitor;
import compiler.parser.CPPSubsetParser;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase encargada de transformar el árbol de análisis sintáctico generado por ANTLR
 * en nuestro Árbol de Sintaxis Abstracta (AST) de forma ampliada para soportar la Fase 6.
 */
public class ASTBuilder extends CPPSubsetBaseVisitor<compiler.ast.ASTNode> {

    @Override
    public compiler.ast.ASTNode visitProgram(CPPSubsetParser.ProgramContext ctx) {
        List<compiler.ast.ASTNode> declarations = new ArrayList<>();
        if (ctx.declaration() != null) {
            for (CPPSubsetParser.DeclarationContext dCtx : ctx.declaration()) {
                compiler.ast.ASTNode decl = visit(dCtx);
                if (decl instanceof BlockNode) {
                    // Aplanar declaraciones de variables múltiples en el ámbito global (ej. int x, y;)
                    declarations.addAll(((BlockNode) decl).getStatements());
                } else if (decl != null) {
                    declarations.add(decl);
                }
            }
        }
        return new ProgramNode(declarations, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitVariableDeclaration(CPPSubsetParser.VariableDeclarationContext ctx) {
        String type = ctx.typeSpecifier().getText();
        List<compiler.ast.ASTNode> decls = new ArrayList<>();
        if (ctx.declarator() != null) {
            for (CPPSubsetParser.DeclaratorContext dCtx : ctx.declarator()) {
                String id = dCtx.IDENTIFIER().getText();
                
                // 1. Calcular profundidad del puntero (contando asteriscos)
                int pointerDepth = 0;
                for (int i = 0; i < dCtx.getChildCount(); i++) {
                    if (dCtx.getChild(i).getText().equals("*")) {
                        pointerDepth++;
                    }
                }

                // 2. Comprobar si es declaración de arreglo y obtener tamaño constante
                boolean isArray = false;
                int arraySize = 0;
                if (dCtx.INT_LITERAL() != null) {
                    isArray = true;
                    arraySize = Integer.parseInt(dCtx.INT_LITERAL().getText());
                }

                compiler.ast.ASTNode initializer = null;
                if (dCtx.expression() != null) {
                    initializer = visit(dCtx.expression());
                }

                decls.add(new VarDeclNode(type, id, initializer, pointerDepth, isArray, arraySize, 
                                          dCtx.getStart().getLine(), dCtx.getStart().getCharPositionInLine()));
            }
        }
        if (decls.size() == 1) {
            return decls.get(0);
        } else {
            return new BlockNode(decls, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        }
    }

    @Override
    public compiler.ast.ASTNode visitFunctionDeclaration(CPPSubsetParser.FunctionDeclarationContext ctx) {
        String returnType = ctx.typeSpecifier().getText();
        String identifier = ctx.IDENTIFIER().getText();
        List<ParameterNode> parameters = new ArrayList<>();
        
        if (ctx.parameterList() != null && ctx.parameterList().parameter() != null) {
            for (CPPSubsetParser.ParameterContext pCtx : ctx.parameterList().parameter()) {
                String pType = pCtx.typeSpecifier().getText();
                String pId = pCtx.IDENTIFIER().getText();
                
                // Contar profundidad de puntero para el parámetro
                int pointerDepth = 0;
                for (int i = 0; i < pCtx.getChildCount(); i++) {
                    if (pCtx.getChild(i).getText().equals("*")) {
                        pointerDepth++;
                    }
                }
                
                parameters.add(new ParameterNode(pType, pId, pointerDepth, pCtx.getStart().getLine(), pCtx.getStart().getCharPositionInLine()));
            }
        }
        
        BlockNode body = (BlockNode) visit(ctx.compoundStatement());
        return new FuncDeclNode(returnType, identifier, parameters, body, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    /**
     * Construye un L-Value válido a partir del contexto sintáctico lvalue.
     */
    private compiler.ast.ASTNode buildLValue(CPPSubsetParser.LvalueContext ctx) {
        return visit(ctx.unaryExpr());
    }

    @Override
    public compiler.ast.ASTNode visitVarDeclStmt(CPPSubsetParser.VarDeclStmtContext ctx) {
        return visit(ctx.variableDeclaration());
    }

    @Override
    public compiler.ast.ASTNode visitAssignStmt(CPPSubsetParser.AssignStmtContext ctx) {
        return visit(ctx.assignmentStatement());
    }

    @Override
    public compiler.ast.ASTNode visitAssignmentStatement(CPPSubsetParser.AssignmentStatementContext ctx) {
        compiler.ast.ASTNode lhs = buildLValue(ctx.lvalue());
        compiler.ast.ASTNode expr = visit(ctx.expression());
        return new AssignNode(lhs, expr, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitBlockStmt(CPPSubsetParser.BlockStmtContext ctx) {
        return visit(ctx.compoundStatement());
    }

    @Override
    public compiler.ast.ASTNode visitCompoundStatement(CPPSubsetParser.CompoundStatementContext ctx) {
        List<compiler.ast.ASTNode> statements = new ArrayList<>();
        if (ctx.statement() != null) {
            for (CPPSubsetParser.StatementContext sCtx : ctx.statement()) {
                compiler.ast.ASTNode stmt = visit(sCtx);
                if (stmt instanceof BlockNode) {
                    // Aplanar declaraciones de variables múltiples dentro de bloques (ej. int x, y;)
                    statements.addAll(((BlockNode) stmt).getStatements());
                } else if (stmt != null) {
                    statements.add(stmt);
                }
            }
        }
        return new BlockNode(statements, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitIfStmt(CPPSubsetParser.IfStmtContext ctx) {
        return visit(ctx.selectionStatement());
    }

    @Override
    public compiler.ast.ASTNode visitSelectionStatement(CPPSubsetParser.SelectionStatementContext ctx) {
        compiler.ast.ASTNode cond = visit(ctx.expression());
        compiler.ast.ASTNode thenBranch = visit(ctx.statement(0));
        compiler.ast.ASTNode elseBranch = null;
        if (ctx.statement().size() > 1) {
            elseBranch = visit(ctx.statement(1));
        }
        return new IfNode(cond, thenBranch, elseBranch, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitWhileLoop(CPPSubsetParser.WhileLoopContext ctx) {
        compiler.ast.ASTNode cond = visit(ctx.expression());
        compiler.ast.ASTNode body = visit(ctx.statement());
        return new WhileNode(cond, body, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitForLoop(CPPSubsetParser.ForLoopContext ctx) {
        compiler.ast.ASTNode init = null;
        if (ctx.initVar != null) {
            init = visit(ctx.initVar);
        } else if (ctx.initExpr != null) {
            init = visit(ctx.initExpr);
        }
        compiler.ast.ASTNode cond = ctx.cond != null ? visit(ctx.cond) : null;
        compiler.ast.ASTNode step = ctx.step != null ? visit(ctx.step) : null;
        compiler.ast.ASTNode body = visit(ctx.statement());
        return new ForNode(init, cond, step, body, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitExprStmt(CPPSubsetParser.ExprStmtContext ctx) {
        return visit(ctx.expressionStatement());
    }

    @Override
    public compiler.ast.ASTNode visitExpressionStatement(CPPSubsetParser.ExpressionStatementContext ctx) {
        if (ctx.expression() != null) {
            return visit(ctx.expression());
        }
        return null;
    }

    @Override
    public compiler.ast.ASTNode visitBreakStmt(CPPSubsetParser.BreakStmtContext ctx) {
        return new BreakNode(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitContinueStmt(CPPSubsetParser.ContinueStmtContext ctx) {
        return new ContinueNode(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitReturnStmt(CPPSubsetParser.ReturnStmtContext ctx) {
        compiler.ast.ASTNode expr = ctx.expression() != null ? visit(ctx.expression()) : null;
        return new ReturnNode(expr, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitAssignment(CPPSubsetParser.AssignmentContext ctx) {
        compiler.ast.ASTNode lhs = buildLValue(ctx.lvalue());
        compiler.ast.ASTNode expr = visit(ctx.expression());
        return new AssignNode(lhs, expr, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitExprOr(CPPSubsetParser.ExprOrContext ctx) {
        return visit(ctx.logicalOrExpr());
    }

    @Override
    public compiler.ast.ASTNode visitLogicalOrExpr(CPPSubsetParser.LogicalOrExprContext ctx) {
        compiler.ast.ASTNode left = visit(ctx.logicalAndExpr(0));
        for (int i = 1; i < ctx.logicalAndExpr().size(); i++) {
            compiler.ast.ASTNode right = visit(ctx.logicalAndExpr(i));
            left = new BinaryOpNode(left, "||", right, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        }
        return left;
    }

    @Override
    public compiler.ast.ASTNode visitLogicalAndExpr(CPPSubsetParser.LogicalAndExprContext ctx) {
        compiler.ast.ASTNode left = visit(ctx.equalityExpr(0));
        for (int i = 1; i < ctx.equalityExpr().size(); i++) {
            compiler.ast.ASTNode right = visit(ctx.equalityExpr(i));
            left = new BinaryOpNode(left, "&&", right, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        }
        return left;
    }

    @Override
    public compiler.ast.ASTNode visitEqualityExpr(CPPSubsetParser.EqualityExprContext ctx) {
        compiler.ast.ASTNode left = visit(ctx.relationalExpr(0));
        for (int i = 1; i < ctx.relationalExpr().size(); i++) {
            compiler.ast.ASTNode right = visit(ctx.relationalExpr(i));
            String op = ctx.getChild(2 * i - 1).getText();
            left = new BinaryOpNode(left, op, right, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        }
        return left;
    }

    @Override
    public compiler.ast.ASTNode visitRelationalExpr(CPPSubsetParser.RelationalExprContext ctx) {
        compiler.ast.ASTNode left = visit(ctx.additiveExpr(0));
        for (int i = 1; i < ctx.additiveExpr().size(); i++) {
            compiler.ast.ASTNode right = visit(ctx.additiveExpr(i));
            String op = ctx.getChild(2 * i - 1).getText();
            left = new BinaryOpNode(left, op, right, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        }
        return left;
    }

    @Override
    public compiler.ast.ASTNode visitAdditiveExpr(CPPSubsetParser.AdditiveExprContext ctx) {
        compiler.ast.ASTNode left = visit(ctx.multiplicativeExpr(0));
        for (int i = 1; i < ctx.multiplicativeExpr().size(); i++) {
            compiler.ast.ASTNode right = visit(ctx.multiplicativeExpr(i));
            String op = ctx.getChild(2 * i - 1).getText();
            left = new BinaryOpNode(left, op, right, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        }
        return left;
    }

    @Override
    public compiler.ast.ASTNode visitMultiplicativeExpr(CPPSubsetParser.MultiplicativeExprContext ctx) {
        compiler.ast.ASTNode left = visit(ctx.unaryExpr(0));
        for (int i = 1; i < ctx.unaryExpr().size(); i++) {
            compiler.ast.ASTNode right = visit(ctx.unaryExpr(i));
            String op = ctx.getChild(2 * i - 1).getText();
            left = new BinaryOpNode(left, op, right, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        }
        return left;
    }

    @Override
    public compiler.ast.ASTNode visitUnaryOp(CPPSubsetParser.UnaryOpContext ctx) {
        String op = ctx.op.getText();
        compiler.ast.ASTNode expr = visit(ctx.unaryExpr());
        
        // Desreferenciación (*ptr)
        if (op.equals("*")) {
            return new DereferenceNode(expr, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        }
        // Direccionamiento (&x)
        if (op.equals("&")) {
            return new AddressOfNode(expr, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
        }
        
        return new UnaryOpNode(op, expr, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitPrimary(CPPSubsetParser.PrimaryContext ctx) {
        return visit(ctx.primaryExpr());
    }

    @Override
    public compiler.ast.ASTNode visitMemberAccess(CPPSubsetParser.MemberAccessContext ctx) {
        compiler.ast.ASTNode base = visit(ctx.primaryExpr());
        String memberName = ctx.IDENTIFIER().getText();
        return new MemberAccessNode(base, memberName, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitArrayAccess(CPPSubsetParser.ArrayAccessContext ctx) {
        String arrayName = ctx.IDENTIFIER().getText();
        compiler.ast.ASTNode index = visit(ctx.expression());
        return new ArrayAccessNode(arrayName, index, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitId(CPPSubsetParser.IdContext ctx) {
        String id = ctx.IDENTIFIER().getText();
        return new IdNode(id, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitLit(CPPSubsetParser.LitContext ctx) {
        return visit(ctx.literal());
    }

    @Override
    public compiler.ast.ASTNode visitFuncCall(CPPSubsetParser.FuncCallContext ctx) {
        return visit(ctx.functionCall());
    }

    @Override
    public compiler.ast.ASTNode visitParenthesized(CPPSubsetParser.ParenthesizedContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public compiler.ast.ASTNode visitFunctionCall(CPPSubsetParser.FunctionCallContext ctx) {
        String id = ctx.IDENTIFIER().getText();
        List<compiler.ast.ASTNode> args = new ArrayList<>();
        if (ctx.argumentList() != null && ctx.argumentList().expression() != null) {
            for (CPPSubsetParser.ExpressionContext eCtx : ctx.argumentList().expression()) {
                args.add(visit(eCtx));
            }
        }
        return new FuncCallNode(id, args, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitIntLit(CPPSubsetParser.IntLitContext ctx) {
        return new LiteralNode(ctx.INT_LITERAL().getText(), LiteralNode.LiteralType.INT, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitDoubleLit(CPPSubsetParser.DoubleLitContext ctx) {
        return new LiteralNode(ctx.DOUBLE_LITERAL().getText(), LiteralNode.LiteralType.DOUBLE, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitCharLit(CPPSubsetParser.CharLitContext ctx) {
        return new LiteralNode(ctx.CHAR_LITERAL().getText(), LiteralNode.LiteralType.CHAR, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitBoolLit(CPPSubsetParser.BoolLitContext ctx) {
        return new LiteralNode(ctx.BOOLEAN_LITERAL().getText(), LiteralNode.LiteralType.BOOL, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitPostfixOp(CPPSubsetParser.PostfixOpContext ctx) {
        String op = ctx.op.getText();
        compiler.ast.ASTNode expr = visit(ctx.primaryExpr());
        return new UnaryOpNode(op, expr, true, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitStringLit(CPPSubsetParser.StringLitContext ctx) {
        return new LiteralNode(ctx.STRING_LITERAL().getText(), LiteralNode.LiteralType.STRING, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitStructDeclaration(CPPSubsetParser.StructDeclarationContext ctx) {
        String structName = ctx.IDENTIFIER().getText();
        List<VarDeclNode> members = new ArrayList<>();
        if (ctx.structMemberDeclaration() != null) {
            for (CPPSubsetParser.StructMemberDeclarationContext mCtx : ctx.structMemberDeclaration()) {
                members.add((VarDeclNode) visit(mCtx));
            }
        }
        return new StructDeclNode(structName, members, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public compiler.ast.ASTNode visitStructMemberDeclaration(CPPSubsetParser.StructMemberDeclarationContext ctx) {
        String type = ctx.typeSpecifier().getText();
        String id = ctx.IDENTIFIER().getText();
        
        int pointerDepth = 0;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i).getText().equals("*")) {
                pointerDepth++;
            }
        }
        
        boolean isArray = false;
        int arraySize = 0;
        if (ctx.INT_LITERAL() != null) {
            isArray = true;
            arraySize = Integer.parseInt(ctx.INT_LITERAL().getText());
        }
        
        return new VarDeclNode(type, id, null, pointerDepth, isArray, arraySize, 
                              ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }
}
