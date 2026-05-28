package compiler.codegen;

import java.util.*;

/**
 * Realiza la asignación de registros físicos utilizando el algoritmo Linear Scan.
 * Mapea variables a registros temporales ($t0-$t9) y guardados ($s0-$s7).
 * Soporta derrame (spilling) ordenado en la pila.
 */
public class RegisterAllocator {

    private final Map<String, LivenessAnalyzer.LiveInterval> intervals;
    private final List<String> freeTRegs;
    private final List<String> freeSRegs;
    private final List<LivenessAnalyzer.LiveInterval> active;
    private final Map<String, String> varToReg;
    private final Map<String, Integer> spillOffsets;
    private int nextSpillOffset;

    public RegisterAllocator(Map<String, LivenessAnalyzer.LiveInterval> intervals, List<compiler.tac.TACInstruction> instructions) {
        this.intervals = intervals;
        this.active = new ArrayList<>();
        this.varToReg = new HashMap<>();
        this.spillOffsets = new HashMap<>();
        this.nextSpillOffset = -4; // Los offsets en la pila comienzan en -4 respecto a $fp

        // Inicializar los pools de registros de MIPS
        this.freeTRegs = new ArrayList<>(Arrays.asList(
            "$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7", "$t8", "$t9"
        ));
        this.freeSRegs = new ArrayList<>(Arrays.asList(
            "$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7"
        ));

        // Escanear variables address-taken
        Set<String> addressTaken = new HashSet<>();
        for (compiler.tac.TACInstruction inst : instructions) {
            if (inst.getType() == compiler.tac.TACInstruction.Type.UNARY_OP && inst.getOperator().equals("&")) {
                String base = getBaseVar(inst.getArg1());
                if (base != null) {
                    addressTaken.add(base);
                }
            }
        }

        for (String var : addressTaken) {
            if (intervals.containsKey(var)) {
                LivenessAnalyzer.LiveInterval current = intervals.get(var);
                current.isSpilled = true;
                current.spillOffset = nextSpillOffset;
                spillOffsets.put(var, nextSpillOffset);
                nextSpillOffset -= 4;
            }
        }
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

    public Map<String, String> getVarToReg() {
        return varToReg;
    }

    public Map<String, Integer> getSpillOffsets() {
        return spillOffsets;
    }

    public int getFrameSize() {
        // Retorna el tamaño total del marco necesario para variables derramadas y locales
        // MIPS requiere alineación de pila a 8 bytes
        int size = Math.abs(nextSpillOffset) + 4;
        if (size % 8 != 0) {
            size += (8 - (size % 8));
        }
        return size;
    }

    private boolean isTemporal(String varName) {
        return varName.startsWith("t") && Character.isDigit(varName.charAt(1));
    }

    /**
     * Remueve de la lista de activos los intervalos que terminan antes de 'start'
     * y devuelve sus registros al pool libre.
     */
    private void expireOldIntervals(int start) {
        Iterator<LivenessAnalyzer.LiveInterval> it = active.iterator();
        while (it.hasNext()) {
            LivenessAnalyzer.LiveInterval act = it.next();
            if (act.end < start) {
                // Devolver registro
                if (act.register != null) {
                    if (act.register.startsWith("$t")) {
                        freeTRegs.add(act.register);
                    } else if (act.register.startsWith("$s")) {
                        freeSRegs.add(act.register);
                    }
                }
                it.remove();
            }
        }
    }

    /**
     * Realiza el derrame de un registro cuando no quedan registros disponibles.
     * Selecciona el intervalo activo o actual que termine más tarde.
     */
    private void spillAtInterval(LivenessAnalyzer.LiveInterval current) {
        // Encontrar el intervalo activo que termina más tarde
        LivenessAnalyzer.LiveInterval candidateToSpill = null;
        int maxEnd = current.end;
        
        for (LivenessAnalyzer.LiveInterval act : active) {
            if (act.end > maxEnd) {
                maxEnd = act.end;
                candidateToSpill = act;
            }
        }

        if (candidateToSpill != null) {
            // Derramar el candidato activo
            candidateToSpill.isSpilled = true;
            candidateToSpill.spillOffset = nextSpillOffset;
            spillOffsets.put(candidateToSpill.varName, nextSpillOffset);
            nextSpillOffset -= 4;

            // Reasignar el registro del candidato derramado al intervalo actual
            current.register = candidateToSpill.register;
            varToReg.put(current.varName, current.register);
            
            candidateToSpill.register = null;
            active.remove(candidateToSpill);
            
            active.add(current);
            // Ordenar activos por fecha de fin ascendente
            active.sort(Comparator.comparingInt(a -> a.end));
        } else {
            // Derramar el intervalo actual
            current.isSpilled = true;
            current.spillOffset = nextSpillOffset;
            spillOffsets.put(current.varName, nextSpillOffset);
            nextSpillOffset -= 4;
        }
    }

    /**
     * Ejecuta el algoritmo de Linear Scan Register Allocation.
     */
    public void allocate() {
        // Ordenar intervalos por su inicio (start) de forma ascendente
        List<LivenessAnalyzer.LiveInterval> unhandled = new ArrayList<>(intervals.values());
        unhandled.sort(Comparator.comparingInt(a -> a.start));

        for (LivenessAnalyzer.LiveInterval current : unhandled) {
            if (current.isSpilled) {
                continue;
            }
            expireOldIntervals(current.start);

            boolean assigned = false;
            if (isTemporal(current.varName)) {
                // Intentar asignar un registro temporal ($t)
                if (!freeTRegs.isEmpty()) {
                    current.register = freeTRegs.remove(0);
                    varToReg.put(current.varName, current.register);
                    active.add(current);
                    assigned = true;
                } else if (!freeSRegs.isEmpty()) {
                    // Si no hay $t pero hay $s libres, usarlo
                    current.register = freeSRegs.remove(0);
                    varToReg.put(current.varName, current.register);
                    active.add(current);
                    assigned = true;
                }
            } else {
                // Intentar asignar un registro guardado ($s)
                if (!freeSRegs.isEmpty()) {
                    current.register = freeSRegs.remove(0);
                    varToReg.put(current.varName, current.register);
                    active.add(current);
                    assigned = true;
                } else if (!freeTRegs.isEmpty()) {
                    // Si no hay $s pero hay $t libres, usarlo
                    current.register = freeTRegs.remove(0);
                    varToReg.put(current.varName, current.register);
                    active.add(current);
                    assigned = true;
                }
            }

            if (!assigned) {
                // Si no hay registros disponibles, realizar spilling
                spillAtInterval(current);
            }

            // Mantener activos ordenados por end
            active.sort(Comparator.comparingInt(a -> a.end));
        }
    }

    public void printAllocationLog() {
        System.out.println("======================================================================");
        System.out.println("                 ASIGNACIÓN DE REGISTROS (LOG)                        ");
        System.out.println("======================================================================");
        for (LivenessAnalyzer.LiveInterval interval : intervals.values()) {
            System.out.println(interval.toString());
        }
        System.out.println("Tamaño total del Stack Frame: " + getFrameSize() + " bytes");
        System.out.println("======================================================================\n");
    }
}
