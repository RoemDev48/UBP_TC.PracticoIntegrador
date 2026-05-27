package compiler.semantic;

import compiler.ast.*;
import compiler.visitor.ASTVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

/**
 * Visitante del AST encargado del análisis semántico con soporte extendido (Fase 6).
 */
public class SemanticAnalyzer implements ASTVisitor<Type> {
    private Scope currentScope;
    private Type currentFunctionReturnType;
    private int loopNestingLevel;
    private Set<String> initializedVars;
    
    private final List<String> errors;
    private final List<String> warnings;

    public SemanticAnalyzer() {
        this.currentScope = null;
        this.currentFunctionReturnType = null;
        this.loopNestingLevel = 0;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
        this.initializedVars = new HashSet<>();
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    private void reportError(ASTNode node, String message) {
        errors.add("[ERROR SEMÁNTICO] Línea " + node.getLine() + ", Columna " + node.getColumn() + ": " + message);
    }

    private void reportWarning(ASTNode node, String message) {
        warnings.add("[WARNING SEMÁNTICO] Línea " + node.getLine() + ", Columna " + node.getColumn() + ": " + message);
    }

    @Override
    public Type visit(ProgramNode node) {
        currentScope = new Scope(null);
        
        for (ASTNode decl : node.getDeclarations()) {
            if (decl != null) {
                decl.accept(this);
            }
        }
        
        // Verificar main
        Symbol mainSym = currentScope.lookupLocal("main");
        if (mainSym == null || !mainSym.isFunction()) {
            errors.add("[ERROR SEMÁNTICO] Error de enlazado: El programa debe contener una función 'main'.");
        } else {
            if (mainSym.getReturnType().getKind() != Type.Kind.INT || mainSym.getReturnType().isPointer()) {
                errors.add("[ERROR SEMÁNTICO] Error en 'main': La función 'main' debe retornar un tipo 'int' escalar.");
            }
            if (!mainSym.getParameterTypes().isEmpty()) {
                errors.add("[ERROR SEMÁNTICO] Error en 'main': La función 'main' no debe recibir parámetros.");
            }
        }
        
        return Type.VOID;
    }

    @Override
    public Type visit(VarDeclNode node) {
        Type baseType = Type.fromString(node.getType());
        if (baseType == Type.VOID) {
            reportError(node, "La variable '" + node.getIdentifier() + "' no puede ser de tipo 'void'.");
            return Type.ERROR;
        }
        if (baseType == Type.ERROR) {
            reportError(node, "Tipo de dato desconocido '" + node.getType() + "' para la variable '" + node.getIdentifier() + "'.");
            return Type.ERROR;
        }

        // Construir el tipo complejo (punteros y arreglos)
        Type varType = new Type(baseType.getKind(), node.getPointerDepth(), node.isArray(), node.getArraySize());

        // Registrar en el ámbito actual
        Symbol varSym = new Symbol(node.getIdentifier(), varType);
        boolean defined = currentScope.define(varSym);
        if (!defined) {
            reportError(node, "Redeclaración del identificador '" + node.getIdentifier() + "' en el mismo ámbito.");
            return Type.ERROR;
        }

        // Si tiene inicializador, validar compatibilidad de tipos
        if (node.hasInitializer()) {
            Type initType = node.getInitializer().accept(this);
            if (initType != Type.ERROR) {
                if (!Type.isCompatible(varType, initType)) {
                    reportError(node, "Tipos incompatibles en la inicialización de '" + node.getIdentifier() + 
                                      "'. No se puede convertir " + initType + " a " + varType + ".");
                } else if (Type.needsCoercionWarning(varType, initType)) {
                    reportWarning(node, "Coerción implícita de " + initType + " a " + varType + 
                                        " al inicializar '" + node.getIdentifier() + "'. Pérdida potencial de precisión.");
                }
            }
        }

        // Si tiene inicializador o es una variable global, considerarla inicializada
        if (node.hasInitializer() || currentScope.getParent() == null) {
            initializedVars.add(node.getIdentifier());
        }

        return Type.VOID;
    }

    @Override
    public Type visit(FuncDeclNode node) {
        Type baseReturnType = Type.fromString(node.getReturnType());
        if (baseReturnType == Type.ERROR) {
            reportError(node, "Tipo de retorno desconocido '" + node.getReturnType() + "' en la función '" + node.getIdentifier() + "'.");
            baseReturnType = Type.ERROR;
        }

        // Por simplicidad en la declaración de funciones, asumimos que no retornan punteros directamente
        Type returnType = baseReturnType;

        // Construir tipos de parámetros (soporta punteros)
        List<Type> paramTypes = new ArrayList<>();
        for (ParameterNode param : node.getParameters()) {
            Type pBase = Type.fromString(param.getType());
            if (pBase == Type.VOID) {
                reportError(param, "El parámetro '" + param.getIdentifier() + "' en la función '" + node.getIdentifier() + "' no puede ser 'void'.");
                pBase = Type.ERROR;
            }
            Type pType = new Type(pBase.getKind(), param.getPointerDepth(), false, 0);
            paramTypes.add(pType);
        }

        // Definir la función en el ámbito global
        Scope globalScope = currentScope;
        while (globalScope.getParent() != null) {
            globalScope = globalScope.getParent();
        }

        Symbol funcSym = new Symbol(node.getIdentifier(), returnType, paramTypes);
        boolean defined = globalScope.define(funcSym);
        if (!defined) {
            reportError(node, "Redeclaración de la función '" + node.getIdentifier() + "'.");
        }

        // Abrir un nuevo ámbito para el cuerpo de la función
        currentScope = new Scope(currentScope);
        currentFunctionReturnType = returnType;
        
        // Registrar parámetros en el ámbito local de la función
        for (int i = 0; i < node.getParameters().size(); i++) {
            ParameterNode param = node.getParameters().get(i);
            Type pType = paramTypes.get(i);
            if (pType != Type.ERROR) {
                Symbol paramSym = new Symbol(param.getIdentifier(), pType);
                boolean paramDefined = currentScope.define(paramSym);
                if (!paramDefined) {
                    reportError(param, "Nombre de parámetro duplicado '" + param.getIdentifier() + "' en la función '" + node.getIdentifier() + "'.");
                }
                initializedVars.add(param.getIdentifier());
            }
        }

        // Analizar cuerpo de la función
        node.getBody().accept(this);

        // Verificación de garantía de retorno para funciones no-void
        if (returnType.getKind() != Type.Kind.VOID || returnType.isPointer()) {
            if (!hasGuaranteedReturn(node.getBody())) {
                reportError(node, "La función no-void '" + node.getIdentifier() + "' debe retornar un valor en todos sus caminos de ejecución.");
            }
        }

        // Cerrar ámbito de la función
        for (String symName : currentScope.getSymbols().keySet()) {
            initializedVars.remove(symName);
        }
        currentScope = currentScope.getParent();
        currentFunctionReturnType = null;

        return Type.VOID;
    }

    @Override
    public Type visit(ParameterNode node) {
        Type base = Type.fromString(node.getType());
        return new Type(base.getKind(), node.getPointerDepth(), false, 0);
    }

    @Override
    public Type visit(BlockNode node) {
        currentScope = new Scope(currentScope);
        boolean flowTerminated = false;
        
        for (ASTNode stmt : node.getStatements()) {
            if (stmt == null) continue;
            
            if (flowTerminated) {
                reportWarning(stmt, "Código inalcanzable (código muerto) detectado.");
            }
            
            stmt.accept(this);
            
            if (hasGuaranteedReturn(stmt) || stmt instanceof BreakNode || stmt instanceof ContinueNode || stmt instanceof ReturnNode) {
                flowTerminated = true;
            }
        }
        
        for (String symName : currentScope.getSymbols().keySet()) {
            initializedVars.remove(symName);
        }
        
        currentScope = currentScope.getParent();
        return Type.VOID;
    }

    @Override
    public Type visit(AssignNode node) {
        // Validar el L-Value del lado izquierdo
        ASTNode lhs = node.getLhs();
        boolean isLValue = (lhs instanceof IdNode) || (lhs instanceof ArrayAccessNode) || (lhs instanceof DereferenceNode);
        if (!isLValue) {
            reportError(node, "El lado izquierdo de la asignación debe ser una dirección de memoria modificable (L-Value).");
            return Type.ERROR;
        }

        // Si el LHS es un IdNode, temporalmente pretendemos que está inicializado para evitar el warning
        boolean tempInitialized = false;
        String lhsId = null;
        if (lhs instanceof IdNode) {
            lhsId = ((IdNode) lhs).getIdentifier();
            if (!initializedVars.contains(lhsId)) {
                initializedVars.add(lhsId);
                tempInitialized = true;
            }
        }

        Type lhsType = lhs.accept(this);
        Type rhsType = node.getExpression().accept(this);

        // Si agregamos temporalmente el identificador, lo removemos ahora
        if (tempInitialized && lhsId != null) {
            initializedVars.remove(lhsId);
        }

        if (lhsType != Type.ERROR && rhsType != Type.ERROR) {
            if (lhs.accept(new ASTVisitor<Boolean>() {
                @Override public Boolean visit(ProgramNode n) { return false; }
                @Override public Boolean visit(VarDeclNode n) { return false; }
                @Override public Boolean visit(FuncDeclNode n) { return false; }
                @Override public Boolean visit(ParameterNode n) { return false; }
                @Override public Boolean visit(BlockNode n) { return false; }
                @Override public Boolean visit(AssignNode n) { return false; }
                @Override public Boolean visit(IfNode n) { return false; }
                @Override public Boolean visit(WhileNode n) { return false; }
                @Override public Boolean visit(ForNode n) { return false; }
                @Override public Boolean visit(BreakNode n) { return false; }
                @Override public Boolean visit(ContinueNode n) { return false; }
                @Override public Boolean visit(ReturnNode n) { return false; }
                @Override public Boolean visit(BinaryOpNode n) { return false; }
                @Override public Boolean visit(UnaryOpNode n) { return false; }
                @Override public Boolean visit(LiteralNode n) { return false; }
                @Override public Boolean visit(FuncCallNode n) { return false; }
                @Override public Boolean visit(ArrayAccessNode n) { return false; }
                @Override public Boolean visit(DereferenceNode n) { return false; }
                @Override public Boolean visit(AddressOfNode n) { return false; }
                
                @Override
                public Boolean visit(IdNode n) {
                    Symbol s = currentScope.lookup(n.getIdentifier());
                    return s != null && s.isFunction();
                }
            })) {
                reportError(node, "No se puede asignar un valor al identificador de una función.");
                return Type.ERROR;
            }

            if (!Type.isCompatible(lhsType, rhsType)) {
                reportError(node, "Tipos incompatibles en la asignación. No se puede asignar " + rhsType + " a " + lhsType + ".");
                return Type.ERROR;
            } else if (Type.needsCoercionWarning(lhsType, rhsType)) {
                reportWarning(node, "Coerción implícita de " + rhsType + " a " + lhsType + " en la asignación. Pérdida potencial de precisión.");
            }
        }

        // Si el LHS es una variable (IdNode), marcarla como inicializada permanentemente
        if (lhs instanceof IdNode) {
            initializedVars.add(((IdNode) lhs).getIdentifier());
        }

        return lhsType;
    }

    @Override
    public Type visit(IfNode node) {
        Type condType = node.getCondition().accept(this);
        if (condType != Type.ERROR) {
            if (condType.getKind() == Type.Kind.VOID) {
                reportError(node.getCondition(), "La condición del 'if' no puede ser de tipo 'void'.");
            }
        }

        Set<String> thenSet = new java.util.HashSet<>(initializedVars);
        Set<String> elseSet = new java.util.HashSet<>(initializedVars);
        Set<String> oldInitializedVars = this.initializedVars;

        this.initializedVars = thenSet;
        if (node.getThenBranch() != null) node.getThenBranch().accept(this);

        if (node.hasElseBranch()) {
            this.initializedVars = elseSet;
            node.getElseBranch().accept(this);

            // Intersección de variables inicializadas en ambas ramas
            oldInitializedVars.clear();
            for (String var : thenSet) {
                if (elseSet.contains(var)) {
                    oldInitializedVars.add(var);
                }
            }
        } else {
            // Sin else, no hay garantía de inicialización tras el if
        }

        this.initializedVars = oldInitializedVars;
        return Type.VOID;
    }

    @Override
    public Type visit(WhileNode node) {
        Type condType = node.getCondition().accept(this);
        if (condType != Type.ERROR) {
            if (condType.getKind() == Type.Kind.VOID) {
                reportError(node.getCondition(), "La condición del 'while' no puede ser de tipo 'void'.");
            }
        }

        Set<String> loopSet = new java.util.HashSet<>(initializedVars);
        Set<String> oldInitializedVars = this.initializedVars;

        this.initializedVars = loopSet;
        loopNestingLevel++;
        if (node.getBody() != null) node.getBody().accept(this);
        loopNestingLevel--;

        this.initializedVars = oldInitializedVars;
        return Type.VOID;
    }

    @Override
    public Type visit(ForNode node) {
        currentScope = new Scope(currentScope);
        if (node.getInitialization() != null) node.getInitialization().accept(this);
        
        Set<String> preLoopSet = new java.util.HashSet<>(initializedVars);

        if (node.getCondition() != null) {
            Type condType = node.getCondition().accept(this);
            if (condType != Type.ERROR && condType.getKind() == Type.Kind.VOID) {
                reportError(node.getCondition(), "La condición del 'for' no puede ser de tipo 'void'.");
            }
        }

        Set<String> loopSet = new java.util.HashSet<>(preLoopSet);
        this.initializedVars = loopSet;

        if (node.getIncrement() != null) node.getIncrement().accept(this);
        loopNestingLevel++;
        if (node.getBody() != null) node.getBody().accept(this);
        loopNestingLevel--;

        this.initializedVars = preLoopSet;

        for (String symName : currentScope.getSymbols().keySet()) {
            initializedVars.remove(symName);
        }
        currentScope = currentScope.getParent();
        return Type.VOID;
    }

    @Override
    public Type visit(BreakNode node) {
        if (loopNestingLevel <= 0) {
            reportError(node, "Sentencia 'break' fuera de un bucle ('while' o 'for').");
        }
        return Type.VOID;
    }

    @Override
    public Type visit(ContinueNode node) {
        if (loopNestingLevel <= 0) {
            reportError(node, "Sentencia 'continue' fuera de un bucle ('while' o 'for').");
        }
        return Type.VOID;
    }

    @Override
    public Type visit(ReturnNode node) {
        if (currentFunctionReturnType == null) {
            reportError(node, "Sentencia 'return' fuera del cuerpo de una función.");
            return Type.VOID;
        }

        if (node.hasExpression()) {
            if (currentFunctionReturnType.getKind() == Type.Kind.VOID && !currentFunctionReturnType.isPointer()) {
                reportError(node, "Función de tipo 'void' no debe retornar un valor.");
            } else {
                Type exprType = node.getExpression().accept(this);
                if (exprType != Type.ERROR) {
                    if (!Type.isCompatible(currentFunctionReturnType, exprType)) {
                        reportError(node, "Tipo de retorno incompatible en la función. Se esperaba " + 
                                          currentFunctionReturnType + " pero se retornó " + exprType + ".");
                    } else if (Type.needsCoercionWarning(currentFunctionReturnType, exprType)) {
                        reportWarning(node, "Coerción implícita de " + exprType + " a " + currentFunctionReturnType + 
                                            " en la sentencia 'return'. Pérdida potencial de precisión.");
                    }
                }
            }
        } else {
            if (currentFunctionReturnType.getKind() != Type.Kind.VOID || currentFunctionReturnType.isPointer()) {
                reportError(node, "Se esperaba un valor de retorno de tipo " + currentFunctionReturnType + " en una función no-void.");
            }
        }

        return Type.VOID;
    }

    @Override
    public Type visit(BinaryOpNode node) {
        Type leftType = node.getLeft().accept(this);
        Type rightType = node.getRight().accept(this);
        String op = node.getOperator();

        if (leftType == Type.ERROR || rightType == Type.ERROR) {
            return Type.ERROR;
        }

        if (leftType.getKind() == Type.Kind.VOID || rightType.getKind() == Type.Kind.VOID) {
            reportError(node, "Operación binaria '" + op + "' no permitida sobre tipo 'void'.");
            return Type.ERROR;
        }

        switch (op) {
            case "+":
            case "-":
                // Aritmética de punteros o aritmética normal
                Type res = Type.getResultType(leftType, rightType);
                if (res == Type.ERROR) {
                    reportError(node, "Tipos incompatibles para la operación binaria '" + op + "'. Operandos: " + leftType + " y " + rightType + ".");
                }
                return res;
                
            case "*":
            case "/":
                if (leftType.isPointer() || rightType.isPointer()) {
                    reportError(node, "Operación binaria '" + op + "' no está permitida sobre punteros.");
                    return Type.ERROR;
                }
                return Type.getResultType(leftType, rightType);
                
            case "%":
                if (leftType.getKind() == Type.Kind.DOUBLE || rightType.getKind() == Type.Kind.DOUBLE || leftType.isPointer() || rightType.isPointer()) {
                    reportError(node, "Operador de residuo '%' solo es válido para enteros no-punteros.");
                    return Type.ERROR;
                }
                return Type.INT;

            case "&&":
            case "||":
                if (leftType.isPointer() || rightType.isPointer()) {
                    reportError(node, "Operaciones lógicas binarias '" + op + "' no recomendadas sobre punteros.");
                }
                return Type.BOOL;

            case "==":
            case "!=":
                if (leftType.isPointer() || rightType.isPointer()) {
                    // Permitir comparación de punteros del mismo tipo
                    if (leftType.getKind() == rightType.getKind() && leftType.getPointerDepth() == rightType.getPointerDepth()) {
                        return Type.BOOL;
                    }
                    reportError(node, "No se pueden comparar punteros de diferentes tipos: " + leftType + " y " + rightType + ".");
                    return Type.ERROR;
                }
                return Type.BOOL;

            case "<":
            case ">":
            case "<=":
            case ">=":
                if (leftType.isPointer() || rightType.isPointer()) {
                    reportError(node, "Operadores relacionales '" + op + "' no permitidos sobre punteros.");
                    return Type.ERROR;
                }
                return Type.BOOL;

            default:
                reportError(node, "Operador binario desconocido '" + op + "'.");
                return Type.ERROR;
        }
    }

    @Override
    public Type visit(UnaryOpNode node) {
        Type exprType = node.getExpression().accept(this);
        String op = node.getOperator();

        if (exprType == Type.ERROR) {
            return Type.ERROR;
        }

        if (exprType.getKind() == Type.Kind.VOID) {
            reportError(node, "Operación unaria '" + op + "' no permitida sobre tipo 'void'.");
            return Type.ERROR;
        }

        if (op.equals("!")) {
            return Type.BOOL;
        } else if (op.equals("+") || op.equals("-")) {
            if (exprType.isPointer()) {
                reportError(node, "Operador unario aritmético '" + op + "' no permitido sobre puntero.");
                return Type.ERROR;
            }
            return exprType;
        } else {
            reportError(node, "Operador unario desconocido '" + op + "'.");
            return Type.ERROR;
        }
    }

    @Override
    public Type visit(LiteralNode node) {
        switch (node.getLiteralType()) {
            case INT: return Type.INT;
            case DOUBLE: return Type.DOUBLE;
            case CHAR: return Type.CHAR;
            case BOOL: return Type.BOOL;
            default: return Type.ERROR;
        }
    }

    @Override
    public Type visit(IdNode node) {
        Symbol sym = currentScope.lookup(node.getIdentifier());
        if (sym == null) {
            reportError(node, "La variable '" + node.getIdentifier() + "' no ha sido declarada.");
            return Type.ERROR;
        }
        if (sym.isFunction()) {
            reportError(node, "El identificador '" + node.getIdentifier() + "' es una función, no una variable escalar.");
            return Type.ERROR;
        }
        
        // Detección de variables no inicializadas
        if (!initializedVars.contains(node.getIdentifier())) {
            reportWarning(node, "La variable '" + node.getIdentifier() + "' podría no estar inicializada al usarse.");
        }
        
        return sym.getType();
    }

    @Override
    public Type visit(FuncCallNode node) {
        Symbol sym = currentScope.lookup(node.getIdentifier());
        if (sym == null) {
            reportError(node, "La función '" + node.getIdentifier() + "' no ha sido declarada.");
            return Type.ERROR;
        }
        if (!sym.isFunction()) {
            reportError(node, "El identificador '" + node.getIdentifier() + "' no es una función.");
            return Type.ERROR;
        }

        List<Type> paramTypes = sym.getParameterTypes();
        List<ASTNode> args = node.getArguments();

        if (args.size() != paramTypes.size()) {
            reportError(node, "Número incorrecto de argumentos en la llamada a '" + node.getIdentifier() + 
                              "'. Se esperaban " + paramTypes.size() + " pero se enviaron " + args.size() + ".");
            return sym.getReturnType();
        }

        for (int i = 0; i < args.size(); i++) {
            Type argType = args.get(i).accept(this);
            Type paramType = paramTypes.get(i);
            
            if (argType != Type.ERROR && paramType != Type.ERROR) {
                if (!Type.isCompatible(paramType, argType)) {
                    reportError(args.get(i), "Argumento " + (i + 1) + " incompatible en la llamada a '" + node.getIdentifier() + 
                                             "'. Se esperaba " + paramType + " pero se recibió " + argType + ".");
                } else if (Type.needsCoercionWarning(paramType, argType)) {
                    reportWarning(args.get(i), "Coerción implícita de argumento " + (i + 1) + " de " + argType + " a " + 
                                               paramType + " en la llamada a '" + node.getIdentifier() + "'. Pérdida potencial de precisión.");
                }
            }
        }

        return sym.getReturnType();
    }

    @Override
    public Type visit(ArrayAccessNode node) {
        Symbol sym = currentScope.lookup(node.getArrayName());
        if (sym == null) {
            reportError(node, "El arreglo '" + node.getArrayName() + "' no ha sido declarado.");
            return Type.ERROR;
        }
        if (sym.isFunction()) {
            reportError(node, "El identificador '" + node.getArrayName() + "' es una función y no puede ser indexado.");
            return Type.ERROR;
        }

        Type arrType = sym.getType();
        
        // Si no es un arreglo estático (sino un puntero escalar) y no está inicializado, reportar warning
        if (!arrType.isArray() && !initializedVars.contains(node.getArrayName())) {
            reportWarning(node, "El puntero '" + node.getArrayName() + "' podría no estar inicializado al usarse.");
        }

        // C++ permite indexación sobre arreglos y sobre punteros escalares (aritmética de punteros implicita)
        if (!arrType.isArray() && !arrType.isPointer()) {
            reportError(node, "La variable '" + node.getArrayName() + "' no es un arreglo ni un puntero, por lo que no puede ser indexada.");
            return Type.ERROR;
        }

        Type indexType = node.getIndex().accept(this);
        if (indexType != Type.ERROR) {
            if (indexType.isPointer() || indexType.isArray() || (indexType.getKind() != Type.Kind.INT && indexType.getKind() != Type.Kind.CHAR)) {
                reportError(node.getIndex(), "El índice del arreglo debe ser un tipo entero escalar (int o char).");
            }
        }

        if (arrType.isArray()) {
            return arrType.getElementType();
        } else {
            // Si es puntero T*, indexar arr[i] equivale a desreferenciar y devuelve T
            return arrType.dereference();
        }
    }

    @Override
    public Type visit(DereferenceNode node) {
        Type exprType = node.getExpression().accept(this);
        if (exprType == Type.ERROR) {
            return Type.ERROR;
        }

        if (!exprType.isPointer()) {
            reportError(node, "Operador de desreferenciación '*' inválido sobre tipo no-puntero " + exprType + ".");
            return Type.ERROR;
        }

        return exprType.dereference();
    }

    @Override
    public Type visit(AddressOfNode node) {
        ASTNode expr = node.getExpression();
        
        // El operador & solo se puede aplicar sobre un L-Value válido (variable en memoria)
        boolean isLValue = (expr instanceof IdNode) || (expr instanceof ArrayAccessNode) || (expr instanceof DereferenceNode);
        if (!isLValue) {
            reportError(node, "Operador de dirección '&' solo se puede aplicar sobre una dirección de memoria modificable (L-Value).");
            return Type.ERROR;
        }

        boolean tempInitialized = false;
        String exprId = null;
        if (expr instanceof IdNode) {
            exprId = ((IdNode) expr).getIdentifier();
            if (!initializedVars.contains(exprId)) {
                initializedVars.add(exprId);
                tempInitialized = true;
            }
        }

        Type exprType = expr.accept(this);

        if (tempInitialized && exprId != null) {
            initializedVars.remove(exprId);
        }

        if (exprType == Type.ERROR) {
            return Type.ERROR;
        }

        return exprType.makePointer();
    }

    /**
     * Helper recursivo para determinar si un nodo del AST garantiza que se ejecute una sentencia 'return'.
     */
    private boolean hasGuaranteedReturn(ASTNode node) {
        if (node == null) return false;
        
        if (node instanceof ReturnNode) {
            return true;
        }
        
        if (node instanceof BlockNode) {
            for (ASTNode stmt : ((BlockNode) node).getStatements()) {
                if (hasGuaranteedReturn(stmt)) {
                    return true;
                }
            }
            return false;
        }
        
        if (node instanceof IfNode) {
            IfNode ifNode = (IfNode) node;
            return ifNode.hasElseBranch() 
                && hasGuaranteedReturn(ifNode.getThenBranch()) 
                && hasGuaranteedReturn(ifNode.getElseBranch());
        }
        
        return false;
    }
}
