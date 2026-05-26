package compiler.tac;

import java.util.*;

/**
 * Optimizador de Código de Tres Direcciones (TAC).
 * Aplica exactamente tres técnicas de optimización en un bucle iterativo hasta alcanzar convergencia:
 * 1. Propagación de Constantes (con Plegado de Constantes).
 * 2. Simplificación de Expresiones (identidades algebraicas).
 * 3. Eliminación de Código Muerto (instrucciones inalcanzables y temporales inútiles).
 */
public class TACOptimizer {

    /**
     * Aplica optimizaciones iterativas al TAC original hasta que ya no haya más cambios.
     */
    public static List<TACInstruction> optimize(List<TACInstruction> originalInstructions) {
        List<TACInstruction> current = new ArrayList<>(originalInstructions);
        boolean changed;
        
        do {
            int previousSize = current.size();
            String previousCode = getCodeRepresentation(current);
            
            // 1. Propagación y plegado de constantes
            current = constantPropagationAndFolding(current);
            
            // 2. Simplificación de expresiones (identidades algebraicas)
            current = algebraicSimplification(current);
            
            // 3. Eliminación de código muerto
            current = deadCodeElimination(current);
            
            String currentCode = getCodeRepresentation(current);
            changed = (current.size() != previousSize) || !currentCode.equals(previousCode);
            
        } while (changed);

        return current;
    }

    private static String getCodeRepresentation(List<TACInstruction> instructions) {
        StringBuilder sb = new StringBuilder();
        for (TACInstruction inst : instructions) {
            sb.append(inst.toString()).append("\n");
        }
        return sb.toString();
    }

    /**
     * 1. Propagación de Constantes y Plegado de Constantes.
     */
    private static List<TACInstruction> constantPropagationAndFolding(List<TACInstruction> instructions) {
        List<TACInstruction> result = new ArrayList<>();
        Map<String, String> constantMap = new HashMap<>();

        for (TACInstruction inst : instructions) {
            TACInstruction.Type type = inst.getType();
            
            // Al cambiar de función, limpiamos el mapa de constantes de variables locales
            if (type == TACInstruction.Type.FUNC_START) {
                constantMap.clear();
            }

            // Si es un Label, invalidamos todas las variables no temporales ya que cambian las condiciones de flujo
            if (type == TACInstruction.Type.LABEL) {
                constantMap.keySet().removeIf(k -> !k.startsWith("t"));
            }

            String op = inst.getOperator();
            String res = inst.getResult();

            // Propagar constantes en argumentos (evitando hacerlo en el argumento de &)
            String arg1 = inst.getArg1();
            boolean isAddressOf = type == TACInstruction.Type.UNARY_OP && op != null && op.equals("&");
            if (arg1 != null && constantMap.containsKey(arg1) && !isAddressOf) {
                arg1 = constantMap.get(arg1);
            }
            String arg2 = inst.getArg2();
            if (arg2 != null && constantMap.containsKey(arg2)) {
                arg2 = constantMap.get(arg2);
            }

            // Plegado de constantes si ambos argumentos se convirtieron en literales constantes
            if (type == TACInstruction.Type.BINARY_OP && isLiteral(arg1) && isLiteral(arg2)) {
                String folded = foldBinary(arg1, op, arg2);
                if (folded != null) {
                    // Reemplazar operación binaria por asignación simple
                    TACInstruction foldedInst = new TACInstruction(
                        TACInstruction.Type.ASSIGN, 
                        "=", 
                        folded, 
                        null, 
                        res
                    );
                    result.add(foldedInst);
                    constantMap.put(res, folded);
                    continue;
                }
            }

            if (type == TACInstruction.Type.UNARY_OP && isLiteral(arg1)) {
                String folded = foldUnary(op, arg1);
                if (folded != null) {
                    TACInstruction foldedInst = new TACInstruction(
                        TACInstruction.Type.ASSIGN, 
                        "=", 
                        folded, 
                        null, 
                        res
                    );
                    result.add(foldedInst);
                    constantMap.put(res, folded);
                    continue;
                }
            }

            // Mantener registro en asignaciones directas
            if (type == TACInstruction.Type.ASSIGN && isLiteral(arg1) && res != null && !res.startsWith("*") && !res.contains("[")) {
                constantMap.put(res, arg1);
            } else if (res != null && !res.isEmpty()) {
                // Si la variable de destino se modifica por otra operación no constante, se invalida
                constantMap.remove(res);
            }

            boolean isIndirectStore = res != null && (res.startsWith("*") || res.contains("["));
            if (isIndirectStore || type == TACInstruction.Type.CALL) {
                constantMap.keySet().removeIf(k -> !k.startsWith("t"));
            }

            // Crear la instrucción propagada
            result.add(new TACInstruction(type, op, arg1, arg2, res));
        }

        return result;
    }

    /**
     * 2. Simplificación de Expresiones (Identidades algebraicas).
     */
    private static List<TACInstruction> algebraicSimplification(List<TACInstruction> instructions) {
        List<TACInstruction> result = new ArrayList<>();

        for (TACInstruction inst : instructions) {
            if (inst.getType() == TACInstruction.Type.BINARY_OP) {
                String arg1 = inst.getArg1();
                String arg2 = inst.getArg2();
                String op = inst.getOperator();
                String res = inst.getResult();

                // Identidades de suma: x + 0 = x, 0 + x = x
                if (op.equals("+")) {
                    if (arg1.equals("0")) {
                        result.add(new TACInstruction(TACInstruction.Type.ASSIGN, "=", arg2, null, res));
                        continue;
                    }
                    if (arg2.equals("0")) {
                        result.add(new TACInstruction(TACInstruction.Type.ASSIGN, "=", arg1, null, res));
                        continue;
                    }
                }
                
                // Identidades de resta: x - 0 = x
                if (op.equals("-")) {
                    if (arg2.equals("0")) {
                        result.add(new TACInstruction(TACInstruction.Type.ASSIGN, "=", arg1, null, res));
                        continue;
                    }
                }

                // Identidades de multiplicación: x * 1 = x, 1 * x = x, x * 0 = 0, 0 * x = 0
                if (op.equals("*")) {
                    if (arg1.equals("1")) {
                        result.add(new TACInstruction(TACInstruction.Type.ASSIGN, "=", arg2, null, res));
                        continue;
                    }
                    if (arg2.equals("1")) {
                        result.add(new TACInstruction(TACInstruction.Type.ASSIGN, "=", arg1, null, res));
                        continue;
                    }
                    if (arg1.equals("0") || arg2.equals("0")) {
                        result.add(new TACInstruction(TACInstruction.Type.ASSIGN, "=", "0", null, res));
                        continue;
                    }
                }
            }
            result.add(inst);
        }

        return result;
    }

    /**
     * 3. Eliminación de Código Muerto (Código inalcanzable y variables temporales inútiles).
     */
    private static List<TACInstruction> deadCodeElimination(List<TACInstruction> instructions) {
        List<TACInstruction> reachable = new ArrayList<>();
        boolean isUnreachable = false;

        // Fase 3.1: Eliminación de código inalcanzable (después de goto o return incondicionales)
        for (TACInstruction inst : instructions) {
            TACInstruction.Type type = inst.getType();

            if (type == TACInstruction.Type.LABEL || 
                type == TACInstruction.Type.FUNC_START || 
                type == TACInstruction.Type.FUNC_END) {
                isUnreachable = false; // El código vuelve a ser alcanzable mediante saltos a esta etiqueta
            }

            if (!isUnreachable) {
                reachable.add(inst);
            }

            if (type == TACInstruction.Type.GOTO || type == TACInstruction.Type.RETURN) {
                isUnreachable = true; // El código posterior hasta una etiqueta es inalcanzable
            }
        }

        // Fase 3.2: Eliminación de asignaciones a variables temporales ('tX') nunca leídas posteriormente.
        Set<String> usedVariables = new HashSet<>();
        for (TACInstruction inst : reachable) {
            String arg1 = inst.getArg1();
            if (arg1 != null && !isLiteral(arg1)) {
                usedVariables.add(arg1);
            }
            String arg2 = inst.getArg2();
            if (arg2 != null && !isLiteral(arg2)) {
                usedVariables.add(arg2);
            }
            // En saltos condicionales, la etiqueta de destino también se considera usada
            if (inst.getType() == TACInstruction.Type.IF_FALSE_GOTO || inst.getType() == TACInstruction.Type.GOTO) {
                usedVariables.add(inst.getResult());
            }
        }

        List<TACInstruction> finalInstructions = new ArrayList<>();
        for (TACInstruction inst : reachable) {
            String res = inst.getResult();
            // Si es un temporal generado por el compilador (t0, t1...) y nunca se usa, eliminamos su asignación
            if (res != null && res.startsWith("t") && !usedVariables.contains(res)) {
                TACInstruction.Type type = inst.getType();
                // No eliminar llamadas a funciones ya que producen efectos colaterales (pero sí el temporal de retorno)
                if (type == TACInstruction.Type.CALL) {
                    finalInstructions.add(new TACInstruction(
                        TACInstruction.Type.CALL, 
                        inst.getOperator(), 
                        inst.getArg1(), 
                        inst.getArg2(), 
                        null
                    ));
                }
                // Cualquier otra instrucción (binary, unary, assign) se descarta por completo
                continue;
            }
            finalInstructions.add(inst);
        }

        return finalInstructions;
    }

    /**
     * Auxiliar para comprobar si una cadena es un literal constante (numérico o carácter).
     */
    private static boolean isLiteral(String str) {
        if (str == null) return false;
        if (str.startsWith("'") && str.endsWith("'")) return true; // char literal
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Plegado constante binario.
     */
    private static String foldBinary(String left, String op, String right) {
        try {
            boolean isDouble = left.contains(".") || right.contains(".");
            if (isDouble) {
                double l = Double.parseDouble(left);
                double r = Double.parseDouble(right);
                switch (op) {
                    case "+": return String.valueOf(l + r);
                    case "-": return String.valueOf(l - r);
                    case "*": return String.valueOf(l * r);
                    case "/": return r != 0 ? String.valueOf(l / r) : "0.0";
                    case "==": return l == r ? "1" : "0";
                    case "!=": return l != r ? "1" : "0";
                    case "<": return l < r ? "1" : "0";
                    case ">": return l > r ? "1" : "0";
                    case "<=": return l <= r ? "1" : "0";
                    case ">=": return l >= r ? "1" : "0";
                }
            } else {
                int l = Integer.parseInt(left);
                int r = Integer.parseInt(right);
                switch (op) {
                    case "+": return String.valueOf(l + r);
                    case "-": return String.valueOf(l - r);
                    case "*": return String.valueOf(l * r);
                    case "/": return r != 0 ? String.valueOf(l / r) : "0";
                    case "%": return r != 0 ? String.valueOf(l % r) : "0";
                    case "==": return l == r ? "1" : "0";
                    case "!=": return l != r ? "1" : "0";
                    case "<": return l < r ? "1" : "0";
                    case ">": return l > r ? "1" : "0";
                    case "<=": return l <= r ? "1" : "0";
                    case ">=": return l >= r ? "1" : "0";
                }
            }
        } catch (NumberFormatException e) {
            if (left.startsWith("'") && left.endsWith("'") && right.startsWith("'") && right.endsWith("'")) {
                char l = left.charAt(1);
                char r = right.charAt(1);
                switch (op) {
                    case "==": return l == r ? "1" : "0";
                    case "!=": return l != r ? "1" : "0";
                }
            }
        }
        return null;
    }

    /**
     * Plegado constante unario.
     */
    private static String foldUnary(String op, String val) {
        try {
            if (op.equals("!")) {
                double v = Double.parseDouble(val);
                return v == 0 ? "1" : "0";
            }
            if (op.equals("-")) {
                if (val.contains(".")) {
                    return String.valueOf(-Double.parseDouble(val));
                } else {
                    return String.valueOf(-Integer.parseInt(val));
                }
            }
            if (op.equals("+")) {
                return val;
            }
        } catch (NumberFormatException e) {
            // Ignorar fallos de formato
        }
        return null;
    }
}
