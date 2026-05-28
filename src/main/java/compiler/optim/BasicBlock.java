package compiler.optim;

import compiler.tac.TACInstruction;
import java.util.*;

/**
 * Representa un Bloque Básico en el Grafo de Flujo de Control (CFG).
 * Contiene una secuencia lineal de instrucciones TAC y proporciona un
 * conjunto de optimizaciones locales que se ejecutan hasta alcanzar un punto fijo.
 */
public class BasicBlock {

    private final String id;
    private final List<TACInstruction> instructions;
    private final List<BasicBlock> predecessors;
    private final List<BasicBlock> successors;

    public BasicBlock(String id) {
        this.id = id;
        this.instructions = new ArrayList<>();
        this.predecessors = new ArrayList<>();
        this.successors = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public List<TACInstruction> getInstructions() {
        return instructions;
    }

    public List<BasicBlock> getPredecessors() {
        return predecessors;
    }

    public List<BasicBlock> getSuccessors() {
        return successors;
    }

    public void addInstruction(TACInstruction inst) {
        instructions.add(inst);
    }

    public void addSuccessor(BasicBlock succ) {
        if (!successors.contains(succ)) {
            successors.add(succ);
            succ.predecessors.add(this);
        }
    }

    private boolean isTemporal(String varName) {
        if (varName == null || varName.isEmpty()) return false;
        return varName.startsWith("t") && Character.isDigit(varName.charAt(1));
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

    /**
     * Corre todas las optimizaciones locales en el bloque básico hasta alcanzar un punto fijo.
     */
    public boolean optimize() {
        boolean changed = false;
        boolean localChanged;
        int iterations = 0;
        
        do {
            localChanged = false;
            
            // 1. Propagación de constantes, Plegado de constantes, Simplificaciones algebraicas y Strength reduction
            if (runConstantAndAlgebraicOpt()) localChanged = true;
            
            // 2. Eliminación de Subexpresiones Comunes (CSE Local)
            if (runCSE()) localChanged = true;
            
            // 3. Eliminación de Código Muerto (DCE Local)
            if (runDCE()) localChanged = true;
            
            if (localChanged) changed = true;
            iterations++;
        } while (localChanged && iterations < 20); // Prevenir bucles infinitos accidentales
        
        return changed;
    }

    /**
     * Realiza Plegado/Propagación de constantes, Simplificación algebraica y Strength reduction.
     */
    private boolean runConstantAndAlgebraicOpt() {
        boolean changed = false;
        Map<String, String> constMap = new HashMap<>();

        for (int i = 0; i < instructions.size(); i++) {
            TACInstruction inst = instructions.get(i);
            
            // Si hay alguna escritura a través de puntero o estructura (ej: t0.x = vy o *p = vy),
            // invalidamos cualquier variable con alias de memoria de constMap para ser conservadores.
            if (inst.getType() == TACInstruction.Type.ASSIGN && (inst.getResult().contains(".") || inst.getResult().contains("*"))) {
                constMap.clear(); // Limpieza conservadora de alias de memoria
                continue;
            }

            // Propagar constantes en los argumentos leídos de la instrucción
            String arg1 = inst.getArg1();
            String arg2 = inst.getArg2();
            boolean propArg1 = false;
            boolean propArg2 = false;

            if (arg1 != null && constMap.containsKey(arg1)) {
                // No propagar si es una operación de dirección (&a)
                if (!(inst.getType() == TACInstruction.Type.UNARY_OP && "&".equals(inst.getOperator()))) {
                    arg1 = constMap.get(arg1);
                    propArg1 = true;
                }
            }
            if (arg2 != null && constMap.containsKey(arg2)) {
                arg2 = constMap.get(arg2);
                propArg2 = true;
            }

            if (propArg1 || propArg2) {
                inst = new TACInstruction(inst.getType(), inst.getOperator(), arg1, arg2, inst.getResult());
                instructions.set(i, inst);
                changed = true;
            }

            // Realizar optimizaciones específicas según el tipo de instrucción
            if (inst.getType() == TACInstruction.Type.ASSIGN) {
                String src = inst.getArg1();
                String dest = inst.getResult();

                if (isConstant(src) && !dest.contains(".") && !dest.contains("*")) {
                    constMap.put(dest, src);
                } else {
                    constMap.remove(dest);
                }
            } 
            else if (inst.getType() == TACInstruction.Type.BINARY_OP) {
                String op = inst.getOperator();
                String dest = inst.getResult();

                // 1. Plegado de constantes (Constant Folding)
                if (isConstant(arg1) && isConstant(arg2)) {
                    try {
                        int val1 = Integer.parseInt(arg1);
                        int val2 = Integer.parseInt(arg2);
                        int resultVal = 0;
                        boolean evalOk = true;

                        switch (op) {
                            case "+": resultVal = val1 + val2; break;
                            case "-": resultVal = val1 - val2; break;
                            case "*": resultVal = val1 * val2; break;
                            case "/": 
                                if (val2 != 0) resultVal = val1 / val2; 
                                else evalOk = false;
                                break;
                            case "%":
                                if (val2 != 0) resultVal = val1 % val2;
                                else evalOk = false;
                                break;
                            case "==": resultVal = (val1 == val2) ? 1 : 0; break;
                            case "!=": resultVal = (val1 != val2) ? 1 : 0; break;
                            case "<": resultVal = (val1 < val2) ? 1 : 0; break;
                            case ">": resultVal = (val1 > val2) ? 1 : 0; break;
                            case "<=": resultVal = (val1 <= val2) ? 1 : 0; break;
                            case ">=": resultVal = (val1 >= val2) ? 1 : 0; break;
                            default: evalOk = false;
                        }

                        if (evalOk) {
                            inst = new TACInstruction(TACInstruction.Type.ASSIGN, null, String.valueOf(resultVal), null, dest);
                            instructions.set(i, inst);
                            constMap.put(dest, String.valueOf(resultVal));
                            changed = true;
                            continue;
                        }
                    } catch (NumberFormatException ignored) {}
                }

                // 2. Simplificaciones Algebraicas e Strength Reduction
                boolean simplified = false;
                if (isConstant(arg2)) {
                    int val2 = 0;
                    try { val2 = Integer.parseInt(arg2); } catch (NumberFormatException ignored) {}

                    if (op.equals("+") && val2 == 0) {
                        // x + 0 -> x
                        inst = new TACInstruction(TACInstruction.Type.ASSIGN, null, arg1, null, dest);
                        simplified = true;
                    } else if (op.equals("-") && val2 == 0) {
                        // x - 0 -> x
                        inst = new TACInstruction(TACInstruction.Type.ASSIGN, null, arg1, null, dest);
                        simplified = true;
                    } else if (op.equals("*") && val2 == 1) {
                        // x * 1 -> x
                        inst = new TACInstruction(TACInstruction.Type.ASSIGN, null, arg1, null, dest);
                        simplified = true;
                    } else if (op.equals("*") && val2 == 0) {
                        // x * 0 -> 0
                        inst = new TACInstruction(TACInstruction.Type.ASSIGN, null, "0", null, dest);
                        simplified = true;
                    } else if (op.equals("*") && val2 == 2) {
                        // Strength Reduction: x * 2 -> x + x
                        inst = new TACInstruction(TACInstruction.Type.BINARY_OP, "+", arg1, arg1, dest);
                        simplified = true;
                    }
                } 
                else if (isConstant(arg1)) {
                    int val1 = 0;
                    try { val1 = Integer.parseInt(arg1); } catch (NumberFormatException ignored) {}

                    if (op.equals("+") && val1 == 0) {
                        // 0 + x -> x
                        inst = new TACInstruction(TACInstruction.Type.ASSIGN, null, arg2, null, dest);
                        simplified = true;
                    } else if (op.equals("*") && val1 == 1) {
                        // 1 * x -> x
                        inst = new TACInstruction(TACInstruction.Type.ASSIGN, null, arg2, null, dest);
                        simplified = true;
                    } else if (op.equals("*") && val1 == 0) {
                        // 0 * x -> 0
                        inst = new TACInstruction(TACInstruction.Type.ASSIGN, null, "0", null, dest);
                        simplified = true;
                    } else if (op.equals("*") && val1 == 2) {
                        // Strength Reduction: 2 * x -> x + x
                        inst = new TACInstruction(TACInstruction.Type.BINARY_OP, "+", arg2, arg2, dest);
                        simplified = true;
                    }
                }

                if (simplified) {
                    instructions.set(i, inst);
                    constMap.remove(dest);
                    changed = true;
                } else {
                    constMap.remove(dest);
                }
            } else {
                if (inst.getResult() != null) {
                    constMap.remove(getBaseVar(inst.getResult()));
                }
            }
        }
        return changed;
    }

    /**
     * Realiza la Eliminación de Subexpresiones Comunes (CSE Local).
     */
    private boolean runCSE() {
        boolean changed = false;
        // Expresión string (ej: "a + b") -> temporal de destino que contiene el resultado (ej: "t0")
        Map<String, String> expressions = new HashMap<>();

        for (int i = 0; i < instructions.size(); i++) {
            TACInstruction inst = instructions.get(i);

            // Invalidar expresiones si hay escrituras a punteros
            if (inst.getType() == TACInstruction.Type.ASSIGN && (inst.getResult().contains(".") || inst.getResult().contains("*"))) {
                expressions.clear();
                continue;
            }

            if (inst.getType() == TACInstruction.Type.BINARY_OP) {
                String op = inst.getOperator();
                String arg1 = inst.getArg1();
                String arg2 = inst.getArg2();
                String dest = inst.getResult();

                // Representación única de la expresión binaria
                String exprKey = arg1 + " " + op + " " + arg2;
                
                // CSE simétrico para operadores conmutativos (ej: a + b == b + a)
                String symExprKey = null;
                if (op.equals("+") || op.equals("*") || op.equals("==") || op.equals("!=")) {
                    symExprKey = arg2 + " " + op + " " + arg1;
                }

                if (expressions.containsKey(exprKey)) {
                    String prevTemp = expressions.get(exprKey);
                    // Reemplazar la operación binaria redundante por una asignación simple
                    inst = new TACInstruction(TACInstruction.Type.ASSIGN, null, prevTemp, null, dest);
                    instructions.set(i, inst);
                    changed = true;
                } 
                else if (symExprKey != null && expressions.containsKey(symExprKey)) {
                    String prevTemp = expressions.get(symExprKey);
                    inst = new TACInstruction(TACInstruction.Type.ASSIGN, null, prevTemp, null, dest);
                    instructions.set(i, inst);
                    changed = true;
                } 
                else {
                    // Invalidar expresiones previas que contengan la variable de destino que se va a redefinir
                    invalidateVariable(expressions, dest);
                    expressions.put(exprKey, dest);
                }
            } 
            else if (inst.getResult() != null) {
                // Si la instrucción escribe a una variable, invalidamos expresiones relacionadas
                invalidateVariable(expressions, getBaseVar(inst.getResult()));
            }
        }
        return changed;
    }

    private void invalidateVariable(Map<String, String> expressions, String varName) {
        if (varName == null) return;
        Iterator<Map.Entry<String, String>> it = expressions.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, String> entry = it.next();
            String expr = entry.getKey();
            String val = entry.getValue();
            if (expr.contains(varName) || val.equals(varName)) {
                it.remove();
            }
        }
    }

    /**
     * Realiza la Eliminación de Código Muerto (DCE Local).
     */
    private boolean runDCE() {
        boolean changed = false;
        Set<String> liveVars = new HashSet<>();

        // Por defecto en DCE local, asumimos que todas las variables declaradas del usuario están vivas al final
        // del bloque básico para evitar eliminar código intermedio del usuario de forma incorrecta.
        // Solo eliminamos temporales generados por el compilador (t0, t1, ...) que no tengan lecturas posteriores.
        for (int i = instructions.size() - 1; i >= 0; i--) {
            TACInstruction inst = instructions.get(i);
            
            // Si la instrucción define un temporal local inactivo, la eliminamos
            String dest = getBaseVar(inst.getResult());
            if (dest != null && isTemporal(dest) && !liveVars.contains(dest)) {
                // Exclusiones: No borrar llamadas a funciones (ya que tienen efectos secundarios importantes)
                if (inst.getType() != TACInstruction.Type.CALL) {
                    instructions.remove(i);
                    changed = true;
                    continue;
                }
            }

            // Si no se eliminó la instrucción, actualizar el conjunto de variables vivas
            if (dest != null) {
                liveVars.remove(dest);
            }
            String arg1 = getBaseVar(inst.getArg1());
            String arg2 = getBaseVar(inst.getArg2());
            if (arg1 != null) liveVars.add(arg1);
            if (arg2 != null) liveVars.add(arg2);
        }
        return changed;
    }

    private boolean isConstant(String val) {
        if (val == null || val.isEmpty()) return false;
        if (val.startsWith("\"") && val.endsWith("\"")) return false;
        if (val.startsWith("'") && val.endsWith("'")) return false;
        return Character.isDigit(val.charAt(0)) || val.equals("true") || val.equals("false") || val.startsWith("-") && val.length() > 1 && Character.isDigit(val.charAt(1));
    }
}
