package compiler.ast;

import compiler.visitor.ASTVisitor;

/**
 * Clase base abstracta para todos los nodos del Árbol de Sintaxis Abstracta (AST).
 */
public abstract class ASTNode {
    private final int line;
    private final int column;

    public ASTNode(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    /**
     * Acepta un visitante para recorrer este nodo en particular.
     *
     * @param <T> El tipo de retorno del visitante.
     * @param visitor El visitante que procesará el nodo.
     * @return El resultado del procesamiento del visitante.
     */
    public abstract <T> T accept(ASTVisitor<T> visitor);
}
