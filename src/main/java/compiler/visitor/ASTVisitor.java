package compiler.visitor;

import compiler.ast.*;

/**
 * Interfaz que define las operaciones de visita para cada tipo de nodo en el AST.
 * Implementa el patrón de diseño Visitor.
 *
 * @param <T> El tipo de retorno de las operaciones de visita.
 */
public interface ASTVisitor<T> {
    T visit(ProgramNode node);
    T visit(VarDeclNode node);
    T visit(FuncDeclNode node);
    T visit(ParameterNode node);
    T visit(BlockNode node);
    T visit(AssignNode node);
    T visit(IfNode node);
    T visit(WhileNode node);
    T visit(ForNode node);
    T visit(BreakNode node);
    T visit(ContinueNode node);
    T visit(ReturnNode node);
    T visit(BinaryOpNode node);
    T visit(UnaryOpNode node);
    T visit(LiteralNode node);
    T visit(IdNode node);
    T visit(FuncCallNode node);
    
    // Nuevas firmas para la Fase 6
    T visit(ArrayAccessNode node);
    T visit(DereferenceNode node);
    T visit(AddressOfNode node);
}
