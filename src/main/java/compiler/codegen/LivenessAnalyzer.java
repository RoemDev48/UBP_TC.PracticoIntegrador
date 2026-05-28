package compiler.codegen;

import compiler.tac.TACInstruction;
import java.util.*;

/**
 * Realiza el análisis de vida útil (Liveness Analysis) sobre las instrucciones TAC.
 * Calcula los conjuntos IN y OUT de variables vivas para cada instrucción
 * y computa los intervalos de vida útil de cada variable y temporal.
 */
public class LivenessAnalyzer {

    public static class LiveInterval {
        public final String varName;
        public int start;
        public int end;
        public String register = null;
        public int spillOffset = 0;
        public boolean isSpilled = false;

        public LiveInterval(String varName, int start, int end) {
            this.varName = varName;
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return varName + ": [" + start + ", " + end + "]" + 
                   (register != null ? " -> " + register : "") + 
                   (isSpilled ? " -> SPILL(" + spillOffset + ")" : "");
        }
    }

    private final List<TACInstruction> instructions;
    private final List<Set<String>> def;
    private final List<Set<String>> use;
    private final List<List<Integer>> successors;
    private final List<Set<String>> in;
    private final List<Set<String>> out;

    public LivenessAnalyzer(List<TACInstruction> instructions) {
        this.instructions = instructions;
        int n = instructions.size();
        this.def = new ArrayList<>(n);
        this.use = new ArrayList<>(n);
        this.successors = new ArrayList<>(n);
        this.in = new ArrayList<>(n);
        this.out = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            def.add(new HashSet<>());
            use.add(new HashSet<>());
            successors.add(new ArrayList<>());
            in.add(new HashSet<>());
            out.add(new HashSet<>());
        }
    }

    /**
     * Extrae el identificador base de una variable en TAC (ej: "p.x" -> "p", "t0" -> "t0")
     */
    private String getBaseVar(String val) {
        if (val == null || val.isEmpty()) return null;
        // Evitar literales y constantes de cadena
        if (val.startsWith("\"") && val.endsWith("\"")) return null;
        if (val.startsWith("'") && val.endsWith("'")) return null;
        if (Character.isDigit(val.charAt(0)) || val.equals("true") || val.equals("false")) return null;
        if (val.startsWith("&")) {
            return getBaseVar(val.substring(1));
        }
        if (val.startsWith("*")) {
            return getBaseVar(val.substring(1));
        }
        
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

    private void addUse(int instIdx, String val) {
        String base = getBaseVar(val);
        if (base != null) {
            use.get(instIdx).add(base);
        }
    }

    private void addDef(int instIdx, String val) {
        String base = getBaseVar(val);
        if (base != null) {
            def.get(instIdx).add(base);
        }
    }

    /**
     * Analiza el CFG y llena los conjuntos DEF, USE y sucesores de cada instrucción.
     */
    private void buildCFGAndDefUse() {
        int n = instructions.size();
        
        // Mapear etiquetas a su índice de instrucción
        Map<String, Integer> labelMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            TACInstruction inst = instructions.get(i);
            if (inst.getType() == TACInstruction.Type.LABEL) {
                labelMap.put(inst.getArg1(), i);
            }
        }

        for (int i = 0; i < n; i++) {
            TACInstruction inst = instructions.get(i);
            
            // 1. Determinar USE y DEF según el tipo de instrucción
            switch (inst.getType()) {
                case ASSIGN:
                    addDef(i, inst.getResult());
                    addUse(i, inst.getArg1());
                    break;
                case BINARY_OP:
                    addDef(i, inst.getResult());
                    addUse(i, inst.getArg1());
                    addUse(i, inst.getArg2());
                    break;
                case UNARY_OP:
                    addDef(i, inst.getResult());
                    addUse(i, inst.getArg1());
                    break;
                case IF_FALSE_GOTO:
                    addUse(i, inst.getArg1());
                    break;
                case PARAM:
                    addUse(i, inst.getArg1());
                    break;
                case CALL:
                    if (inst.getResult() != null && !inst.getResult().isEmpty()) {
                        addDef(i, inst.getResult());
                    }
                    break;
                case RETURN:
                    if (inst.getArg1() != null && !inst.getArg1().isEmpty()) {
                        addUse(i, inst.getArg1());
                    }
                    break;
                default:
                    break;
            }

            // 2. Determinar los sucesores de la instrucción
            if (inst.getType() == TACInstruction.Type.GOTO) {
                Integer target = labelMap.get(inst.getResult());
                if (target != null) {
                    successors.get(i).add(target);
                }
            } else if (inst.getType() == TACInstruction.Type.IF_FALSE_GOTO) {
                Integer target = labelMap.get(inst.getResult());
                if (target != null) {
                    successors.get(i).add(target);
                }
                if (i + 1 < n) {
                    successors.get(i).add(i + 1);
                }
            } else if (inst.getType() == TACInstruction.Type.RETURN || inst.getType() == TACInstruction.Type.FUNC_END) {
                // Return y End Function no tienen sucesores dentro de la función actual
            } else {
                if (i + 1 < n) {
                    successors.get(i).add(i + 1);
                }
            }
        }
    }

    /**
     * Resuelve de forma iterativa el análisis de punto fijo de liveness hacia atrás.
     */
    private void solveLivenessEquations() {
        int n = instructions.size();
        boolean changed = true;
        
        while (changed) {
            changed = false;
            for (int i = n - 1; i >= 0; i--) {
                Set<String> oldIn = new HashSet<>(in.get(i));
                Set<String> oldOut = new HashSet<>(out.get(i));

                // OUT[i] = union(IN[s] for s in SUCC[i])
                Set<String> newOut = new HashSet<>();
                for (int succIdx : successors.get(i)) {
                    newOut.addAll(in.get(succIdx));
                }
                out.set(i, newOut);

                // IN[i] = USE[i] union (OUT[i] \ DEF[i])
                Set<String> newIn = new HashSet<>(use.get(i));
                Set<String> outMinusDef = new HashSet<>(newOut);
                outMinusDef.removeAll(def.get(i));
                newIn.addAll(outMinusDef);
                in.set(i, newIn);

                if (!newIn.equals(oldIn) || !newOut.equals(oldOut)) {
                    changed = true;
                }
            }
        }
    }

    /**
     * Calcula los intervalos de vida útil de cada variable basándose en los conjuntos IN/OUT.
     */
    public Map<String, LiveInterval> computeIntervals() {
        buildCFGAndDefUse();
        solveLivenessEquations();

        Map<String, LiveInterval> intervals = new LinkedHashMap<>();
        int n = instructions.size();

        // Primera pasada: Encontrar la primera definición o uso para determinar 'start'
        for (int i = 0; i < n; i++) {
            Set<String> activeVars = new HashSet<>();
            activeVars.addAll(def.get(i));
            activeVars.addAll(use.get(i));
            activeVars.addAll(in.get(i));
            activeVars.addAll(out.get(i));

            for (String var : activeVars) {
                if (!intervals.containsKey(var)) {
                    intervals.put(var, new LiveInterval(var, i, i));
                } else {
                    intervals.get(var).end = i;
                }
            }
        }

        return intervals;
    }
}
