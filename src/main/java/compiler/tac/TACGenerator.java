package compiler.tac;

import compiler.ast.*;
import compiler.visitor.ASTVisitor;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Generador de Código de Tres Direcciones (TAC) a partir del AST.
 * Implementa la interfaz ASTVisitor.
 */
public class TACGenerator implements ASTVisitor<String> {
    private final List<TACInstruction> instructions;
    private int tempCounter;
    private int labelCounter;
    
    // Pilas para gestionar etiquetas de break y continue en bucles anidados
    private final Stack<String> breakStack;
    private final Stack<String> continueStack;

    public TACGenerator() {
        this.instructions = new ArrayList<>();
        this.tempCounter = 0;
        this.labelCounter = 0;
        this.breakStack = new Stack<>();
        this.continueStack = new Stack<>();
    }

    public List<TACInstruction> getInstructions() {
        return instructions;
    }

    /**
     * Retorna el código de tres direcciones formateado como una gran cadena.
     */
    public String getTACCode() {
        StringBuilder sb = new StringBuilder();
        for (TACInstruction inst : instructions) {
            sb.append(inst.toString()).append("\n");
        }
        return sb.toString();
    }

    private String newTemp() {
        return "t" + (tempCounter++);
    }

    private String newLabel() {
        return "L" + (labelCounter++);
    }

    private void emit(TACInstruction inst) {
        instructions.add(inst);
    }

    @Override
    public String visit(ProgramNode node) {
        for (ASTNode decl : node.getDeclarations()) {
            if (decl != null) {
                decl.accept(this);
            }
        }
        return null;
    }

    @Override
    public String visit(VarDeclNode node) {
        if (node.hasInitializer()) {
            String val = node.getInitializer().accept(this);
            emit(new TACInstruction(
                TACInstruction.Type.ASSIGN, 
                "=", 
                val, 
                null, 
                node.getIdentifier()
            ));
        }
        return null;
    }

    @Override
    public String visit(FuncDeclNode node) {
        emit(new TACInstruction(
            TACInstruction.Type.FUNC_START, 
            "function", 
            node.getIdentifier(), 
            null, 
            null
        ));

        // Analizar cuerpo de la función
        node.getBody().accept(this);

        emit(new TACInstruction(
            TACInstruction.Type.FUNC_END, 
            "end function", 
            node.getIdentifier(), 
            null, 
            null
        ));
        return null;
    }

    @Override
    public String visit(ParameterNode node) {
        // Los parámetros se declaran implícitamente en la firma de la función.
        return null;
    }

    @Override
    public String visit(BlockNode node) {
        for (ASTNode stmt : node.getStatements()) {
            if (stmt != null) {
                stmt.accept(this);
            }
        }
        return null;
    }

    @Override
    public String visit(AssignNode node) {
        ASTNode lhs = node.getLhs();
        if (lhs instanceof IdNode) {
            String val = node.getExpression().accept(this);
            emit(new TACInstruction(
                TACInstruction.Type.ASSIGN, 
                "=", 
                val, 
                null, 
                ((IdNode) lhs).getIdentifier()
            ));
        } else if (lhs instanceof ArrayAccessNode) {
            ArrayAccessNode arrayAccess = (ArrayAccessNode) lhs;
            String idxVal = arrayAccess.getIndex().accept(this);
            String val = node.getExpression().accept(this);
            emit(new TACInstruction(
                TACInstruction.Type.ASSIGN, 
                "=", 
                val, 
                null, 
                arrayAccess.getArrayName() + "[" + idxVal + "]"
            ));
        } else if (lhs instanceof DereferenceNode) {
            DereferenceNode deref = (DereferenceNode) lhs;
            String ptrVal = deref.getExpression().accept(this);
            String val = node.getExpression().accept(this);
            emit(new TACInstruction(
                TACInstruction.Type.ASSIGN, 
                "=", 
                val, 
                null, 
                "*" + ptrVal
            ));
        } else if (lhs instanceof MemberAccessNode) {
            MemberAccessNode memberAccess = (MemberAccessNode) lhs;
            String baseVal = memberAccess.getExpression().accept(this);
            String val = node.getExpression().accept(this);
            emit(new TACInstruction(
                TACInstruction.Type.ASSIGN, 
                "=", 
                val, 
                null, 
                baseVal + "." + memberAccess.getMemberName()
            ));
        }
        return null;
    }

    @Override
    public String visit(IfNode node) {
        String condVal = node.getCondition().accept(this);
        String labelElse = newLabel();
        String labelEnd = newLabel();

        if (node.hasElseBranch()) {
            // ifFalse condVal goto L_else
            emit(new TACInstruction(
                TACInstruction.Type.IF_FALSE_GOTO, 
                "ifFalse", 
                condVal, 
                null, 
                labelElse
            ));

            // Rama THEN
            node.getThenBranch().accept(this);
            
            // goto L_end
            emit(new TACInstruction(
                TACInstruction.Type.GOTO, 
                "goto", 
                null, 
                null, 
                labelEnd
            ));

            // L_else:
            emit(new TACInstruction(
                TACInstruction.Type.LABEL, 
                "label", 
                labelElse, 
                null, 
                null
            ));

            // Rama ELSE
            node.getElseBranch().accept(this);

            // L_end:
            emit(new TACInstruction(
                TACInstruction.Type.LABEL, 
                "label", 
                labelEnd, 
                null, 
                null
            ));
        } else {
            // ifFalse condVal goto L_end
            emit(new TACInstruction(
                TACInstruction.Type.IF_FALSE_GOTO, 
                "ifFalse", 
                condVal, 
                null, 
                labelEnd
            ));

            // Rama THEN
            node.getThenBranch().accept(this);

            // L_end:
            emit(new TACInstruction(
                TACInstruction.Type.LABEL, 
                "label", 
                labelEnd, 
                null, 
                null
            ));
        }

        return null;
    }

    @Override
    public String visit(WhileNode node) {
        String labelStart = newLabel();
        String labelEnd = newLabel();

        // L_start:
        emit(new TACInstruction(
            TACInstruction.Type.LABEL, 
            "label", 
            labelStart, 
            null, 
            null
        ));

        // Evaluar condición
        String condVal = node.getCondition().accept(this);

        // ifFalse condVal goto L_end
        emit(new TACInstruction(
            TACInstruction.Type.IF_FALSE_GOTO, 
            "ifFalse", 
            condVal, 
            null, 
            labelEnd
        ));

        // Registrar etiquetas para break y continue
        continueStack.push(labelStart);
        breakStack.push(labelEnd);

        // Rama del cuerpo
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }

        // Desapilar etiquetas
        continueStack.pop();
        breakStack.pop();

        // goto L_start
        emit(new TACInstruction(
            TACInstruction.Type.GOTO, 
            "goto", 
            null, 
            null, 
            labelStart
        ));

        // L_end:
        emit(new TACInstruction(
            TACInstruction.Type.LABEL, 
            "label", 
            labelEnd, 
            null, 
            null
        ));

        return null;
    }

    @Override
    public String visit(ForNode node) {
        String labelStart = newLabel();
        String labelStep = newLabel();
        String labelEnd = newLabel();

        // 1. Inicialización
        if (node.getInitialization() != null) {
            node.getInitialization().accept(this);
        }

        // L_start:
        emit(new TACInstruction(
            TACInstruction.Type.LABEL, 
            "label", 
            labelStart, 
            null, 
            null
        ));

        // 2. Condición
        if (node.getCondition() != null) {
            String condVal = node.getCondition().accept(this);
            // ifFalse condVal goto L_end
            emit(new TACInstruction(
                TACInstruction.Type.IF_FALSE_GOTO, 
                "ifFalse", 
                condVal, 
                null, 
                labelEnd
            ));
        }

        // Registrar etiquetas para break y continue
        // El continue salta a la fase de incremento (L_step), no al inicio de la condición (L_start)
        continueStack.push(labelStep);
        breakStack.push(labelEnd);

        // 3. Cuerpo del bucle
        if (node.getBody() != null) {
            node.getBody().accept(this);
        }

        continueStack.pop();
        breakStack.pop();

        // L_step:
        emit(new TACInstruction(
            TACInstruction.Type.LABEL, 
            "label", 
            labelStep, 
            null, 
            null
        ));

        // 4. Incremento
        if (node.getIncrement() != null) {
            node.getIncrement().accept(this);
        }

        // goto L_start
        emit(new TACInstruction(
            TACInstruction.Type.GOTO, 
            "goto", 
            null, 
            null, 
            labelStart
        ));

        // L_end:
        emit(new TACInstruction(
            TACInstruction.Type.LABEL, 
            "label", 
            labelEnd, 
            null, 
            null
        ));

        return null;
    }

    @Override
    public String visit(BreakNode node) {
        if (!breakStack.isEmpty()) {
            emit(new TACInstruction(
                TACInstruction.Type.GOTO, 
                "goto", 
                null, 
                null, 
                breakStack.peek()
            ));
        }
        return null;
    }

    @Override
    public String visit(ContinueNode node) {
        if (!continueStack.isEmpty()) {
            emit(new TACInstruction(
                TACInstruction.Type.GOTO, 
                "goto", 
                null, 
                null, 
                continueStack.peek()
            ));
        }
        return null;
    }

    @Override
    public String visit(ReturnNode node) {
        if (node.hasExpression()) {
            String val = node.getExpression().accept(this);
            emit(new TACInstruction(
                TACInstruction.Type.RETURN, 
                "return", 
                val, 
                null, 
                null
            ));
        } else {
            emit(new TACInstruction(
                TACInstruction.Type.RETURN, 
                "return", 
                null, 
                null, 
                null
            ));
        }
        return null;
    }

    @Override
    public String visit(BinaryOpNode node) {
        String leftVal = node.getLeft().accept(this);
        String rightVal = node.getRight().accept(this);
        String temp = newTemp();

        emit(new TACInstruction(
            TACInstruction.Type.BINARY_OP, 
            node.getOperator(), 
            leftVal, 
            rightVal, 
            temp
        ));

        return temp;
    }

    @Override
    public String visit(UnaryOpNode node) {
        String op = node.getOperator();

        if (op.equals("++") || op.equals("--")) {
            ASTNode lhs = node.getExpression();
            String val = lhs.accept(this);
            
            // Si es postfijo sobre IdNode, debemos salvar el valor original en un temporal
            String tempOrigVal = null;
            if (lhs instanceof IdNode && node.isPostfix()) {
                tempOrigVal = newTemp();
                emit(new TACInstruction(
                    TACInstruction.Type.ASSIGN,
                    "=",
                    val,
                    null,
                    tempOrigVal
                ));
            }

            String tempNewVal = newTemp();

            // tempNewVal = val + 1 o val - 1
            emit(new TACInstruction(
                TACInstruction.Type.BINARY_OP,
                op.equals("++") ? "+" : "-",
                val,
                "1",
                tempNewVal
            ));

            // Escribir el nuevo valor de vuelta al L-Value correspondientemente
            if (lhs instanceof IdNode) {
                emit(new TACInstruction(
                    TACInstruction.Type.ASSIGN,
                    "=",
                    tempNewVal,
                    null,
                    ((IdNode) lhs).getIdentifier()
                ));
            } else if (lhs instanceof ArrayAccessNode) {
                ArrayAccessNode arrayAccess = (ArrayAccessNode) lhs;
                String idxVal = arrayAccess.getIndex().accept(this);
                emit(new TACInstruction(
                    TACInstruction.Type.ASSIGN,
                    "=",
                    tempNewVal,
                    null,
                    arrayAccess.getArrayName() + "[" + idxVal + "]"
                ));
            } else if (lhs instanceof DereferenceNode) {
                DereferenceNode deref = (DereferenceNode) lhs;
                String ptrVal = deref.getExpression().accept(this);
                emit(new TACInstruction(
                    TACInstruction.Type.ASSIGN,
                    "=",
                    tempNewVal,
                    null,
                    "*" + ptrVal
                ));
            } else if (lhs instanceof MemberAccessNode) {
                MemberAccessNode memberAccess = (MemberAccessNode) lhs;
                String baseVal = memberAccess.getExpression().accept(this);
                emit(new TACInstruction(
                    TACInstruction.Type.ASSIGN,
                    "=",
                    tempNewVal,
                    null,
                    baseVal + "." + memberAccess.getMemberName()
                ));
            }

            // Postfijo devuelve el valor original (el temporal salvado para IdNode o val para otros), prefijo devuelve el nuevo valor
            return node.isPostfix() ? (tempOrigVal != null ? tempOrigVal : val) : tempNewVal;
        }

        String val = node.getExpression().accept(this);
        String temp = newTemp();

        emit(new TACInstruction(
            TACInstruction.Type.UNARY_OP, 
            op, 
            val, 
            null, 
            temp
        ));

        return temp;
    }

    @Override
    public String visit(LiteralNode node) {
        // Retornamos el valor literal tal cual para evitar crear temporales innecesarios (ej. t0 = 5)
        return node.getValue();
    }

    @Override
    public String visit(IdNode node) {
        return node.getIdentifier();
    }

    @Override
    public String visit(FuncCallNode node) {
        List<String> argVals = new ArrayList<>();
        for (ASTNode arg : node.getArguments()) {
            argVals.add(arg.accept(this));
        }

        // Emitir parámetros
        for (String argVal : argVals) {
            emit(new TACInstruction(
                TACInstruction.Type.PARAM, 
                "param", 
                argVal, 
                null, 
                null
            ));
        }

        String temp = newTemp();
        emit(new TACInstruction(
            TACInstruction.Type.CALL, 
            "call", 
            node.getIdentifier(), 
            String.valueOf(argVals.size()), 
            temp
        ));

        return temp;
    }

    @Override
    public String visit(ArrayAccessNode node) {
        String idxVal = node.getIndex().accept(this);
        String temp = newTemp();
        emit(new TACInstruction(
            TACInstruction.Type.ASSIGN,
            "=",
            node.getArrayName() + "[" + idxVal + "]",
            null,
            temp
        ));
        return temp;
    }

    @Override
    public String visit(DereferenceNode node) {
        String ptrVal = node.getExpression().accept(this);
        String temp = newTemp();
        emit(new TACInstruction(
            TACInstruction.Type.UNARY_OP,
            "*",
            ptrVal,
            null,
            temp
        ));
        return temp;
    }

    @Override
    public String visit(AddressOfNode node) {
        String varVal = node.getExpression().accept(this);
        String temp = newTemp();
        emit(new TACInstruction(
            TACInstruction.Type.UNARY_OP,
            "&",
            varVal,
            null,
            temp
        ));
        return temp;
    }

    @Override
    public String visit(StructDeclNode node) {
        // La declaración de una estructura no genera instrucciones en tiempo de ejecución.
        return null;
    }

    @Override
    public String visit(MemberAccessNode node) {
        String baseVal = node.getExpression().accept(this);
        String temp = newTemp();
        emit(new TACInstruction(
            TACInstruction.Type.ASSIGN,
            "=",
            baseVal + "." + node.getMemberName(),
            null,
            temp
        ));
        return temp;
    }
}
