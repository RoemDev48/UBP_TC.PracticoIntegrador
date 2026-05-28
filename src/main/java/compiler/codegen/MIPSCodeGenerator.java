package compiler.codegen;

import compiler.ast.VarDeclNode;
import compiler.semantic.Type;
import compiler.tac.TACInstruction;
import java.util.*;

/**
 * Traduce Código de Tres Direcciones (TAC) a Código Ensamblador MIPS32 real.
 * Soporta estructuras, punteros, pasaje de parámetros, control de flujo
 * y usa los mapeos de registros físicos y derrames calculados previamente.
 */
public class MIPSCodeGenerator {

    private final List<TACInstruction> instructions;
    private final Map<String, String> varToReg;
    private final Map<String, Integer> spillOffsets;
    private final Map<String, List<VarDeclNode>> structRegistry;
    private final Map<String, String> varTypes; // Guarda los tipos declarados para resolver structs
    private final Map<String, List<String>> functionParams; // Guarda la lista ordenada de parámetros por función
    private final int frameSize;

    private final StringBuilder dataSection;
    private final StringBuilder textSection;
    private int stringLabelCounter = 0;
    private final Map<String, String> stringLabels;

    public MIPSCodeGenerator(List<TACInstruction> instructions, 
                             Map<String, String> varToReg, 
                             Map<String, Integer> spillOffsets, 
                             Map<String, List<VarDeclNode>> structRegistry, 
                             Map<String, String> varTypes, 
                             Map<String, List<String>> functionParams,
                             int frameSize) {
        this.instructions = instructions;
        this.varToReg = varToReg;
        this.spillOffsets = spillOffsets;
        this.structRegistry = structRegistry;
        this.varTypes = varTypes;
        this.functionParams = functionParams;
        this.frameSize = frameSize;

        this.dataSection = new StringBuilder(".data\n");
        this.textSection = new StringBuilder(".text\n.globl main\n");
        this.stringLabels = new HashMap<>();

        // Registrar newline constante para impresiones si fuera necesario
        dataSection.append("newline: .asciiz \"\\n\"\n");

        // Propagar tipos de temporales en el TAC
        propagateTemporalTypes();
    }

    private void propagateTemporalTypes() {
        for (TACInstruction inst : instructions) {
            switch (inst.getType()) {
                case ASSIGN: {
                    String src = getBaseVar(inst.getArg1());
                    String dest = getBaseVar(inst.getResult());
                    String srcMember = getMemberName(inst.getArg1());
                    String destMember = getMemberName(inst.getResult());
                    
                    if (srcMember == null && destMember == null) {
                        String type = varTypes.get(src);
                        if (type != null) {
                            varTypes.put(dest, type);
                        }
                    } else if (srcMember != null) {
                        String typeStr = varTypes.get(src);
                        if (typeStr != null) {
                            if (typeStr.endsWith("*")) {
                                typeStr = typeStr.substring(0, typeStr.length() - 1);
                            }
                            List<VarDeclNode> members = structRegistry.get(typeStr);
                            if (members != null) {
                                for (VarDeclNode member : members) {
                                    if (member.getIdentifier().equals(srcMember)) {
                                        String mType = member.getType() + (member.getPointerDepth() > 0 ? "*".repeat(member.getPointerDepth()) : "");
                                        varTypes.put(dest, mType);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
                case UNARY_OP: {
                    String src = getBaseVar(inst.getArg1());
                    String dest = getBaseVar(inst.getResult());
                    
                    if (inst.getOperator().equals("&")) {
                        String type = varTypes.get(src);
                        if (type != null) {
                            varTypes.put(dest, type + "*");
                        }
                    } else if (inst.getOperator().equals("*")) {
                        String type = varTypes.get(src);
                        if (type != null && type.endsWith("*")) {
                            varTypes.put(dest, type.substring(0, type.length() - 1));
                        }
                    } else {
                        String type = varTypes.get(src);
                        if (type != null) {
                            varTypes.put(dest, type);
                        }
                    }
                    break;
                }
                case BINARY_OP: {
                    String dest = getBaseVar(inst.getResult());
                    varTypes.put(dest, "int");
                    break;
                }
                case CALL: {
                    String dest = getBaseVar(inst.getResult());
                    if (dest != null && !dest.isEmpty()) {
                        varTypes.put(dest, "int");
                    }
                    break;
                }
                default:
                    break;
            }
        }
    }

    private int getTypeSize(Type type) {
        if (type.isPointer()) return 4;
        if (type.isArray()) {
            return type.getArraySize() * getTypeSize(type.getElementType());
        }
        switch (type.getKind()) {
            case INT:
            case CHAR:
            case BOOL:
                return 4;
            case DOUBLE:
                return 8;
            case STRUCT:
                return getStructSize(type.getStructName());
            default:
                return 4;
        }
    }

    private int getStructSize(String structName) {
        List<VarDeclNode> members = structRegistry.get(structName);
        if (members == null) return 0;
        int size = 0;
        for (VarDeclNode member : members) {
            Type mType = Type.fromString(member.getType());
            Type fullType = new Type(mType.getKind(), mType.getStructName(), member.getPointerDepth(), member.isArray(), member.getArraySize());
            size += getTypeSize(fullType);
        }
        return size;
    }

    private int getMemberOffset(String structName, String memberName) {
        List<VarDeclNode> members = structRegistry.get(structName);
        if (members == null) return 0;
        int offset = 0;
        for (VarDeclNode member : members) {
            if (member.getIdentifier().equals(memberName)) {
                return offset;
            }
            Type mType = Type.fromString(member.getType());
            Type fullType = new Type(mType.getKind(), mType.getStructName(), member.getPointerDepth(), member.isArray(), member.getArraySize());
            offset += getTypeSize(fullType);
        }
        return 0;
    }

    private String getBaseVar(String val) {
        if (val == null || val.isEmpty()) return null;
        int dotIdx = val.indexOf('.');
        if (dotIdx != -1) {
            return val.substring(0, dotIdx);
        }
        int arrIdx = val.indexOf('[');
        if (arrIdx != -1) {
            return val.substring(0, arrIdx);
        }
        return val;
    }

    private String getMemberName(String val) {
        if (val == null) return null;
        int dotIdx = val.indexOf('.');
        if (dotIdx != -1) {
            return val.substring(dotIdx + 1);
        }
        return null;
    }

    private boolean isConstant(String val) {
        if (val == null || val.isEmpty()) return false;
        if (val.startsWith("\"") && val.endsWith("\"")) return true;
        if (val.startsWith("'") && val.endsWith("'")) return true;
        return Character.isDigit(val.charAt(0)) || val.equals("true") || val.equals("false") || val.startsWith("-") && val.length() > 1 && Character.isDigit(val.charAt(1));
    }

    /**
     * Genera la etiqueta en .data para una constante de string y la almacena.
     */
    private String getStringLabel(String str) {
        if (stringLabels.containsKey(str)) {
            return stringLabels.get(str);
        }
        String label = "str_" + stringLabelCounter++;
        dataSection.append(label).append(": .asciiz ").append(str).append("\n");
        stringLabels.put(str, label);
        return label;
    }

    /**
     * Carga el valor de una variable, temporal o constante de TAC en un registro físico de MIPS.
     */
    private String loadVal(String val, String destReg) {
        if (isConstant(val)) {
            if (val.startsWith("\"")) {
                String label = getStringLabel(val);
                textSection.append("    la ").append(destReg).append(", ").append(label).append("\n");
            } else if (val.equals("true")) {
                textSection.append("    li ").append(destReg).append(", 1\n");
            } else if (val.equals("false")) {
                textSection.append("    li ").append(destReg).append(", 0\n");
            } else {
                textSection.append("    li ").append(destReg).append(", ").append(val).append("\n");
            }
            return destReg;
        }

        String base = getBaseVar(val);
        String member = getMemberName(val);

        // Si la variable base está en un registro físico
        if (varToReg.containsKey(base)) {
            String reg = varToReg.get(base);
            if (member == null) {
                // Variable simple en registro
                if (!reg.equals(destReg)) {
                    textSection.append("    move ").append(destReg).append(", ").append(reg).append("\n");
                }
                return destReg;
            } else {
                // Acceso a miembro sobre base en registro (ej: ptr.x donde ptr es puntero en registro)
                String typeStr = varTypes.get(base);
                if (typeStr != null && typeStr.endsWith("*")) {
                    typeStr = typeStr.substring(0, typeStr.length() - 1);
                }
                int offset = getMemberOffset(typeStr, member);
                textSection.append("    lw ").append(destReg).append(", ").append(offset).append("(").append(reg).append(")\n");
                return destReg;
            }
        }

        // Si la variable base está en pila (Spill o Variable Local)
        if (spillOffsets.containsKey(base)) {
            int baseOffset = spillOffsets.get(base);
            if (member == null) {
                textSection.append("    lw ").append(destReg).append(", ").append(baseOffset).append("($fp)\n");
            } else {
                // Miembro de struct en pila (ej: p.x)
                String typeStr = varTypes.get(base);
                int offset = getMemberOffset(typeStr, member);
                int totalOffset = baseOffset - offset; // La pila crece hacia abajo
                textSection.append("    lw ").append(destReg).append(", ").append(totalOffset).append("($fp)\n");
            }
            return destReg;
        }

        // Si es una variable global o no asignada, por defecto la colocamos en pila dinámicamente si es posible
        textSection.append("    # ERROR: Variable local '").append(val).append("' no inicializada o no mapeada.\n");
        return destReg;
    }

    /**
     * Guarda el valor contenido en un registro físico de MIPS en la variable o temporal correspondiente de TAC.
     */
    private void storeVal(String srcReg, String val) {
        String base = getBaseVar(val);
        String member = getMemberName(val);

        // Si la variable base está en un registro físico
        if (varToReg.containsKey(base)) {
            String reg = varToReg.get(base);
            if (member == null) {
                if (!reg.equals(srcReg)) {
                    textSection.append("    move ").append(reg).append(", ").append(srcReg).append("\n");
                }
            } else {
                // Guardar en miembro sobre puntero en registro (ej: ptr.x = val)
                String typeStr = varTypes.get(base);
                if (typeStr != null && typeStr.endsWith("*")) {
                    typeStr = typeStr.substring(0, typeStr.length() - 1);
                }
                int offset = getMemberOffset(typeStr, member);
                textSection.append("    sw ").append(srcReg).append(", ").append(offset).append("(").append(reg).append(")\n");
            }
            return;
        }

        // Si la variable base está en pila
        if (spillOffsets.containsKey(base)) {
            int baseOffset = spillOffsets.get(base);
            if (member == null) {
                textSection.append("    sw ").append(srcReg).append(", ").append(baseOffset).append("($fp)\n");
            } else {
                // Miembro de struct en pila (ej: p.x = val)
                String typeStr = varTypes.get(base);
                int offset = getMemberOffset(typeStr, member);
                int totalOffset = baseOffset - offset;
                textSection.append("    sw ").append(srcReg).append(", ").append(totalOffset).append("($fp)\n");
            }
            return;
        }

        textSection.append("    # ERROR: Variable local '").append(val).append("' no inicializada en store.\n");
    }

    /**
     * Traduce todas las instrucciones TAC al flujo MIPS32.
     */
    public void generate() {
        int paramCount = 0;

        for (TACInstruction inst : instructions) {
            textSection.append("\n    # ").append(inst.toString().trim()).append("\n");

            switch (inst.getType()) {
                case LABEL:
                    textSection.append(inst.getArg1()).append(":\n");
                    break;

                case FUNC_START:
                    String funcName = inst.getArg1();
                    textSection.append("\n").append(funcName).append(":\n");
                    // Prólogo de la función
                    textSection.append("    addiu $sp, $sp, -").append(frameSize).append("\n");
                    textSection.append("    sw $ra, 4($sp)\n");
                    textSection.append("    sw $fp, 0($sp)\n");
                    textSection.append("    move $fp, $sp\n");

                    // Inicializar parámetros al inicio de la función
                    if (functionParams.containsKey(funcName)) {
                        List<String> params = functionParams.get(funcName);
                        for (int i = 0; i < params.size(); i++) {
                            String pName = params.get(i);
                            if (i < 4) {
                                // Parámetro en registro $a0-$a3
                                storeVal("$a" + i, pName);
                            } else {
                                // Parámetro en pila del llamador
                                int offset = 8 + (params.size() - 1 - i) * 4;
                                textSection.append("    lw $v0, ").append(offset).append("($fp)\n");
                                storeVal("$v0", pName);
                            }
                        }
                    }
                    break;

                case FUNC_END:
                    // Epílogo de la función
                    textSection.append("    move $sp, $fp\n");
                    textSection.append("    lw $fp, 0($sp)\n");
                    textSection.append("    lw $ra, 4($sp)\n");
                    textSection.append("    addiu $sp, $sp, ").append(frameSize).append("\n");
                    textSection.append("    jr $ra\n");
                    break;

                case ASSIGN:
                    loadVal(inst.getArg1(), "$v0");
                    storeVal("$v0", inst.getResult());
                    break;

                case BINARY_OP:
                    loadVal(inst.getArg1(), "$t0");
                    loadVal(inst.getArg2(), "$t1");
                    
                    switch (inst.getOperator()) {
                        case "+":
                            textSection.append("    add $v0, $t0, $t1\n");
                            break;
                        case "-":
                            textSection.append("    sub $v0, $t0, $t1\n");
                            break;
                        case "*":
                            textSection.append("    mul $v0, $t0, $t1\n");
                            break;
                        case "/":
                            textSection.append("    div $t0, $t1\n");
                            textSection.append("    mflo $v0\n");
                            break;
                        case "%":
                            textSection.append("    div $t0, $t1\n");
                            textSection.append("    mfhi $v0\n");
                            break;
                        case "==":
                            textSection.append("    seq $v0, $t0, $t1\n");
                            break;
                        case "!=":
                            textSection.append("    sne $v0, $t0, $t1\n");
                            break;
                        case "<":
                            textSection.append("    slt $v0, $t0, $t1\n");
                            break;
                        case ">":
                            textSection.append("    sgt $v0, $t0, $t1\n");
                            break;
                        case "<=":
                            textSection.append("    sle $v0, $t0, $t1\n");
                            break;
                        case ">=":
                            textSection.append("    sge $v0, $t0, $t1\n");
                            break;
                        case "&&":
                            textSection.append("    and $v0, $t0, $t1\n");
                            break;
                        case "||":
                            textSection.append("    or $v0, $t0, $t1\n");
                            break;
                        default:
                            textSection.append("    # Operador desconocido: ").append(inst.getOperator()).append("\n");
                            break;
                    }
                    storeVal("$v0", inst.getResult());
                    break;

                case UNARY_OP:
                    loadVal(inst.getArg1(), "$t0");
                    
                    if (inst.getOperator().equals("-")) {
                        textSection.append("    neg $v0, $t0\n");
                    } else if (inst.getOperator().equals("!")) {
                        textSection.append("    not $v0, $t0\n");
                    } else if (inst.getOperator().equals("&")) {
                        // Obtener la dirección física de la variable (en pila)
                        String base = getBaseVar(inst.getArg1());
                        if (spillOffsets.containsKey(base)) {
                            int offset = spillOffsets.get(base);
                            textSection.append("    addiu $v0, $fp, ").append(offset).append("\n");
                        } else {
                            textSection.append("    # ERROR: No se puede obtener dirección de variable en registro.\n");
                        }
                    } else if (inst.getOperator().equals("*")) {
                        // Desreferenciar: si el tipo base del puntero es un struct, no cargamos el valor (no cabe en un registro),
                        // simplemente propagamos la dirección base.
                        String baseVar = getBaseVar(inst.getArg1());
                        String typeStr = varTypes.get(baseVar);
                        boolean isStructPointer = false;
                        if (typeStr != null && typeStr.endsWith("*")) {
                            String targetType = typeStr.substring(0, typeStr.length() - 1);
                            if (structRegistry.containsKey(targetType)) {
                                isStructPointer = true;
                            }
                        }
                        
                        if (isStructPointer) {
                            textSection.append("    move $v0, $t0\n");
                        } else {
                            textSection.append("    lw $v0, 0($t0)\n");
                        }
                    } else {
                        textSection.append("    move $v0, $t0\n");
                    }
                    storeVal("$v0", inst.getResult());
                    break;

                case GOTO:
                    textSection.append("    j ").append(inst.getResult()).append("\n");
                    break;

                case IF_FALSE_GOTO:
                    loadVal(inst.getArg1(), "$t0");
                    textSection.append("    beqz $t0, ").append(inst.getResult()).append("\n");
                    break;

                case PARAM:
                    // En MIPS, pasaremos los primeros 4 parámetros en registros $a0-$a3
                    if (paramCount < 4) {
                        loadVal(inst.getArg1(), "$a" + paramCount);
                    } else {
                        // El resto de parámetros se empujan a la pila
                        loadVal(inst.getArg1(), "$t0");
                        textSection.append("    addiu $sp, $sp, -4\n");
                        textSection.append("    sw $t0, 0($sp)\n");
                    }
                    paramCount++;
                    break;

                case CALL:
                    textSection.append("    jal ").append(inst.getArg1()).append("\n");
                    
                    // Restaurar la pila si empujamos argumentos
                    int argsCount = Integer.parseInt(inst.getArg2());
                    if (argsCount > 4) {
                        int stackRestore = (argsCount - 4) * 4;
                        textSection.append("    addiu $sp, $sp, ").append(stackRestore).append("\n");
                    }
                    
                    // Si la función retorna un valor, se encuentra en $v0
                    if (inst.getResult() != null && !inst.getResult().isEmpty()) {
                        storeVal("$v0", inst.getResult());
                    }
                    
                    paramCount = 0; // Reiniciar contador para la siguiente llamada
                    break;

                case RETURN:
                    if (inst.getArg1() != null && !inst.getArg1().isEmpty()) {
                        loadVal(inst.getArg1(), "$v0");
                    }
                    // Saltar al epílogo
                    textSection.append("    move $sp, $fp\n");
                    textSection.append("    lw $fp, 0($sp)\n");
                    textSection.append("    lw $ra, 4($sp)\n");
                    textSection.append("    addiu $sp, $sp, ").append(frameSize).append("\n");
                    textSection.append("    jr $ra\n");
                    break;

                default:
                    break;
            }
        }
    }

    public String getMIPSCode() {
        return dataSection.toString() + "\n" + textSection.toString();
    }
}
