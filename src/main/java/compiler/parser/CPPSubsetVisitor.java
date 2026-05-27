// Generated from src/main/antlr/CPPSubset.g4 by ANTLR 4.13.1
package compiler.parser;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link CPPSubsetParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface CPPSubsetVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#program}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitProgram(CPPSubsetParser.ProgramContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#declaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclaration(CPPSubsetParser.DeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#structDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructDeclaration(CPPSubsetParser.StructDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#structMemberDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStructMemberDeclaration(CPPSubsetParser.StructMemberDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#variableDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVariableDeclaration(CPPSubsetParser.VariableDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#declarator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeclarator(CPPSubsetParser.DeclaratorContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#typeSpecifier}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTypeSpecifier(CPPSubsetParser.TypeSpecifierContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#functionDeclaration}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionDeclaration(CPPSubsetParser.FunctionDeclarationContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#parameterList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameterList(CPPSubsetParser.ParameterListContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#parameter}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParameter(CPPSubsetParser.ParameterContext ctx);
	/**
	 * Visit a parse tree produced by the {@code varDeclStmt}
	 * labeled alternative in {@link CPPSubsetParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVarDeclStmt(CPPSubsetParser.VarDeclStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignStmt}
	 * labeled alternative in {@link CPPSubsetParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignStmt(CPPSubsetParser.AssignStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code blockStmt}
	 * labeled alternative in {@link CPPSubsetParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBlockStmt(CPPSubsetParser.BlockStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code ifStmt}
	 * labeled alternative in {@link CPPSubsetParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIfStmt(CPPSubsetParser.IfStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code loopStmt}
	 * labeled alternative in {@link CPPSubsetParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLoopStmt(CPPSubsetParser.LoopStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code jumpStmt}
	 * labeled alternative in {@link CPPSubsetParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitJumpStmt(CPPSubsetParser.JumpStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exprStmt}
	 * labeled alternative in {@link CPPSubsetParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprStmt(CPPSubsetParser.ExprStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#assignmentStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignmentStatement(CPPSubsetParser.AssignmentStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#lvalue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLvalue(CPPSubsetParser.LvalueContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#compoundStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCompoundStatement(CPPSubsetParser.CompoundStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#selectionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectionStatement(CPPSubsetParser.SelectionStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code whileLoop}
	 * labeled alternative in {@link CPPSubsetParser#iterationStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhileLoop(CPPSubsetParser.WhileLoopContext ctx);
	/**
	 * Visit a parse tree produced by the {@code forLoop}
	 * labeled alternative in {@link CPPSubsetParser#iterationStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitForLoop(CPPSubsetParser.ForLoopContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#expressionStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpressionStatement(CPPSubsetParser.ExpressionStatementContext ctx);
	/**
	 * Visit a parse tree produced by the {@code breakStmt}
	 * labeled alternative in {@link CPPSubsetParser#jumpStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBreakStmt(CPPSubsetParser.BreakStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code continueStmt}
	 * labeled alternative in {@link CPPSubsetParser#jumpStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitContinueStmt(CPPSubsetParser.ContinueStmtContext ctx);
	/**
	 * Visit a parse tree produced by the {@code returnStmt}
	 * labeled alternative in {@link CPPSubsetParser#jumpStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitReturnStmt(CPPSubsetParser.ReturnStmtContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(CPPSubsetParser.ExpressionContext ctx);
	/**
	 * Visit a parse tree produced by the {@code assignment}
	 * labeled alternative in {@link CPPSubsetParser#assignmentExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAssignment(CPPSubsetParser.AssignmentContext ctx);
	/**
	 * Visit a parse tree produced by the {@code exprOr}
	 * labeled alternative in {@link CPPSubsetParser#assignmentExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExprOr(CPPSubsetParser.ExprOrContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#logicalOrExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalOrExpr(CPPSubsetParser.LogicalOrExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#logicalAndExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLogicalAndExpr(CPPSubsetParser.LogicalAndExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#equalityExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitEqualityExpr(CPPSubsetParser.EqualityExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#relationalExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRelationalExpr(CPPSubsetParser.RelationalExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#additiveExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAdditiveExpr(CPPSubsetParser.AdditiveExprContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#multiplicativeExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMultiplicativeExpr(CPPSubsetParser.MultiplicativeExprContext ctx);
	/**
	 * Visit a parse tree produced by the {@code unaryOp}
	 * labeled alternative in {@link CPPSubsetParser#unaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUnaryOp(CPPSubsetParser.UnaryOpContext ctx);
	/**
	 * Visit a parse tree produced by the {@code postfixOp}
	 * labeled alternative in {@link CPPSubsetParser#unaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPostfixOp(CPPSubsetParser.PostfixOpContext ctx);
	/**
	 * Visit a parse tree produced by the {@code primary}
	 * labeled alternative in {@link CPPSubsetParser#unaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitPrimary(CPPSubsetParser.PrimaryContext ctx);
	/**
	 * Visit a parse tree produced by the {@code memberAccess}
	 * labeled alternative in {@link CPPSubsetParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMemberAccess(CPPSubsetParser.MemberAccessContext ctx);
	/**
	 * Visit a parse tree produced by the {@code lit}
	 * labeled alternative in {@link CPPSubsetParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLit(CPPSubsetParser.LitContext ctx);
	/**
	 * Visit a parse tree produced by the {@code parenthesized}
	 * labeled alternative in {@link CPPSubsetParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParenthesized(CPPSubsetParser.ParenthesizedContext ctx);
	/**
	 * Visit a parse tree produced by the {@code arrayAccess}
	 * labeled alternative in {@link CPPSubsetParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArrayAccess(CPPSubsetParser.ArrayAccessContext ctx);
	/**
	 * Visit a parse tree produced by the {@code id}
	 * labeled alternative in {@link CPPSubsetParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitId(CPPSubsetParser.IdContext ctx);
	/**
	 * Visit a parse tree produced by the {@code funcCall}
	 * labeled alternative in {@link CPPSubsetParser#primaryExpr}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFuncCall(CPPSubsetParser.FuncCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#functionCall}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionCall(CPPSubsetParser.FunctionCallContext ctx);
	/**
	 * Visit a parse tree produced by {@link CPPSubsetParser#argumentList}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitArgumentList(CPPSubsetParser.ArgumentListContext ctx);
	/**
	 * Visit a parse tree produced by the {@code intLit}
	 * labeled alternative in {@link CPPSubsetParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitIntLit(CPPSubsetParser.IntLitContext ctx);
	/**
	 * Visit a parse tree produced by the {@code doubleLit}
	 * labeled alternative in {@link CPPSubsetParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDoubleLit(CPPSubsetParser.DoubleLitContext ctx);
	/**
	 * Visit a parse tree produced by the {@code charLit}
	 * labeled alternative in {@link CPPSubsetParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCharLit(CPPSubsetParser.CharLitContext ctx);
	/**
	 * Visit a parse tree produced by the {@code boolLit}
	 * labeled alternative in {@link CPPSubsetParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitBoolLit(CPPSubsetParser.BoolLitContext ctx);
	/**
	 * Visit a parse tree produced by the {@code stringLit}
	 * labeled alternative in {@link CPPSubsetParser#literal}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStringLit(CPPSubsetParser.StringLitContext ctx);
}