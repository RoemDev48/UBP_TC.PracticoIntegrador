package compiler.optim;

import compiler.tac.TACInstruction;
import java.util.*;

/**
 * Representa el Grafo de Flujo de Control (CFG) para las funciones del programa.
 * Segmenta la lista lineal completa de instrucciones TAC por función,
 * construye el CFG formal para cada una, realiza las optimizaciones locales
 * e interbloques, y vuelve a linealizar el TAC resultante.
 */
public class ControlFlowGraph {

    private final List<TACInstruction> originalInstructions;

    public ControlFlowGraph(List<TACInstruction> originalInstructions) {
        this.originalInstructions = originalInstructions;
    }

    /**
     * Procesa la lista completa de instrucciones, las optimiza dividiéndolas
     * por función en CFGs individuales y retorna la lista optimizada resultante.
     */
    public List<TACInstruction> optimize() {
        List<TACInstruction> optimizedProgram = new ArrayList<>();
        int n = originalInstructions.size();
        int i = 0;

        while (i < n) {
            TACInstruction inst = originalInstructions.get(i);
            
            if (inst.getType() == TACInstruction.Type.FUNC_START) {
                // Capturar el bloque completo de la función
                List<TACInstruction> funcInstructions = new ArrayList<>();
                funcInstructions.add(inst);
                i++;
                
                while (i < n && originalInstructions.get(i).getType() != TACInstruction.Type.FUNC_END) {
                    funcInstructions.add(originalInstructions.get(i));
                    i++;
                }
                
                if (i < n) {
                    funcInstructions.add(originalInstructions.get(i)); // Añadir FUNC_END
                    i++;
                }

                // Construir, optimizar y linealizar el CFG de esta función
                List<TACInstruction> optimizedFunc = optimizeFunction(funcInstructions);
                optimizedProgram.addAll(optimizedFunc);
            } else {
                // Instrucciones globales fuera de funciones (si existieran)
                optimizedProgram.add(inst);
                i++;
            }
        }

        return optimizedProgram;
    }

    /**
     * Construye, optimiza y linealiza el CFG para una única función.
     */
    private List<TACInstruction> optimizeFunction(List<TACInstruction> funcInsts) {
        int n = funcInsts.size();
        if (n == 0) return funcInsts;

        // 1. Identificar líderes
        Set<Integer> leaders = new TreeSet<>();
        leaders.add(0); // Regla 1: La primera instrucción es líder

        // Mapear etiquetas a índices
        Map<String, Integer> labelMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            TACInstruction inst = funcInsts.get(i);
            if (inst.getType() == TACInstruction.Type.LABEL) {
                labelMap.put(inst.getArg1(), i);
            }
        }

        for (int i = 0; i < n; i++) {
            TACInstruction inst = funcInsts.get(i);
            
            if (inst.getType() == TACInstruction.Type.GOTO || inst.getType() == TACInstruction.Type.IF_FALSE_GOTO) {
                // Regla 2: El destino de un salto es líder
                Integer target = labelMap.get(inst.getResult());
                if (target != null) {
                    leaders.add(target);
                }
                // Regla 3: La instrucción inmediatamente posterior a un salto es líder
                if (i + 1 < n) {
                    leaders.add(i + 1);
                }
            }
        }

        // Convertir líderes a lista ordenada
        List<Integer> leaderList = new ArrayList<>(leaders);
        List<BasicBlock> blocks = new ArrayList<>();
        Map<Integer, BasicBlock> leaderToBlock = new HashMap<>();

        // 2. Particionar en bloques básicos
        for (int j = 0; j < leaderList.size(); j++) {
            int start = leaderList.get(j);
            int end = (j + 1 < leaderList.size()) ? leaderList.get(j + 1) : n;

            BasicBlock block = new BasicBlock("B" + j);
            for (int k = start; k < end; k++) {
                block.addInstruction(funcInsts.get(k));
            }
            blocks.add(block);
            leaderToBlock.put(start, block);
        }

        // Mapear el líder del bloque por su primera instrucción si es una etiqueta
        Map<String, BasicBlock> labelToBlock = new HashMap<>();
        for (BasicBlock block : blocks) {
            if (!block.getInstructions().isEmpty()) {
                TACInstruction first = block.getInstructions().get(0);
                if (first.getType() == TACInstruction.Type.LABEL) {
                    labelToBlock.put(first.getArg1(), block);
                }
            }
        }

        // 3. Enlazar aristas predecesoras y sucesoras
        for (int j = 0; j < blocks.size(); j++) {
            BasicBlock block = blocks.get(j);
            if (block.getInstructions().isEmpty()) continue;

            TACInstruction last = block.getInstructions().get(block.getInstructions().size() - 1);

            if (last.getType() == TACInstruction.Type.GOTO) {
                BasicBlock target = labelToBlock.get(last.getResult());
                if (target != null) {
                    block.addSuccessor(target);
                }
            } else if (last.getType() == TACInstruction.Type.IF_FALSE_GOTO) {
                BasicBlock target = labelToBlock.get(last.getResult());
                if (target != null) {
                    block.addSuccessor(target);
                }
                if (j + 1 < blocks.size()) {
                    block.addSuccessor(blocks.get(j + 1));
                }
            } else if (last.getType() == TACInstruction.Type.RETURN || last.getType() == TACInstruction.Type.FUNC_END) {
                // Fin de flujo
            } else {
                // Caída secuencial (fall-through)
                if (j + 1 < blocks.size()) {
                    block.addSuccessor(blocks.get(j + 1));
                }
            }
        }

        // 4. Imprimir logs informativos de los bloques básicos en consola
        System.out.println("CFG de función '" + funcInsts.get(0).getArg1() + "':");
        for (BasicBlock b : blocks) {
            StringBuilder succs = new StringBuilder();
            for (BasicBlock s : b.getSuccessors()) {
                succs.append(s.getId()).append(" ");
            }
            System.out.println("  Bloque " + b.getId() + " [Líneas TAC: " + b.getInstructions().size() + "] -> Sucesores: " + succs.toString());
        }

        // 5. Ejecutar optimizaciones locales en cada bloque básico
        boolean changed = true;
        int pass = 0;
        while (changed && pass < 5) {
            changed = false;
            for (BasicBlock block : blocks) {
                if (block.optimize()) {
                    changed = true;
                }
            }
            pass++;
        }

        // 6. Linealizar los bloques de vuelta a una lista plana de TAC
        List<TACInstruction> linearized = new ArrayList<>();
        for (BasicBlock block : blocks) {
            linearized.addAll(block.getInstructions());
        }

        return linearized;
    }
}
