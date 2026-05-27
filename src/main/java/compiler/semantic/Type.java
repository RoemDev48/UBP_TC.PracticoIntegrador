package compiler.semantic;

import java.util.Objects;

/**
 * Representa un tipo de dato en el compilador.
 * Soporta tipos básicos, punteros (indirección múltiple) y arreglos de tamaño fijo.
 */
public class Type {
    public enum Kind {
        INT,
        DOUBLE,
        CHAR,
        BOOL,
        VOID,
        ERROR,
        STRING,
        STRUCT
    }

    private final Kind kind;
    private final String structName;
    private final int pointerDepth;
    private final boolean isArray;
    private final int arraySize;

    // Constantes predefinidas para tipos escalares simples
    public static final Type INT = new Type(Kind.INT, 0, false, 0);
    public static final Type DOUBLE = new Type(Kind.DOUBLE, 0, false, 0);
    public static final Type CHAR = new Type(Kind.CHAR, 0, false, 0);
    public static final Type BOOL = new Type(Kind.BOOL, 0, false, 0);
    public static final Type VOID = new Type(Kind.VOID, 0, false, 0);
    public static final Type ERROR = new Type(Kind.ERROR, 0, false, 0);
    public static final Type STRING = new Type(Kind.STRING, 0, false, 0);

    public Type(Kind kind, int pointerDepth, boolean isArray, int arraySize) {
        this(kind, null, pointerDepth, isArray, arraySize);
    }

    public Type(Kind kind, String structName, int pointerDepth, boolean isArray, int arraySize) {
        this.kind = kind;
        this.structName = structName;
        this.pointerDepth = pointerDepth;
        this.isArray = isArray;
        this.arraySize = arraySize;
    }

    public Kind getKind() {
        return kind;
    }

    public String getStructName() {
        return structName;
    }

    public int getPointerDepth() {
        return pointerDepth;
    }

    public boolean isArray() {
        return isArray;
    }

    public int getArraySize() {
        return arraySize;
    }

    public boolean isPointer() {
        return pointerDepth > 0;
    }

    public boolean isScalar() {
        return !isPointer() && !isArray;
    }

    public boolean isNumeric() {
        return isScalar() && (kind == Kind.INT || kind == Kind.DOUBLE || kind == Kind.CHAR || kind == Kind.BOOL);
    }

    /**
     * Retorna un tipo puntero a este tipo.
     */
    public Type makePointer() {
        if (kind == Kind.ERROR) return ERROR;
        return new Type(kind, structName, pointerDepth + 1, false, 0);
    }

    public Type dereference() {
        if (kind == Kind.ERROR) return ERROR;
        if (pointerDepth <= 0) return ERROR;
        return new Type(kind, structName, pointerDepth - 1, false, 0);
    }

    public Type makeArray(int size) {
        if (kind == Kind.ERROR) return ERROR;
        return new Type(kind, structName, pointerDepth, true, size);
    }

    public Type getElementType() {
        if (kind == Kind.ERROR) return ERROR;
        if (!isArray) return ERROR;
        return new Type(kind, structName, pointerDepth, false, 0);
    }

    /**
     * Convierte una cadena de texto de la gramática a su tipo básico correspondiente.
     */
    public static Type fromString(String typeStr) {
        if (typeStr == null) return ERROR;
        switch (typeStr) {
            case "int": return INT;
            case "double": return DOUBLE;
            case "char": return CHAR;
            case "bool": return BOOL;
            case "void": return VOID;
            case "string": return STRING;
            default:
                // Es un nombre de struct
                return new Type(Kind.STRUCT, typeStr, 0, false, 0);
        }
    }

    /**
     * Verifica la compatibilidad para asignaciones o pase de parámetros.
     */
    public static boolean isCompatible(Type target, Type source) {
        if (target.kind == Kind.ERROR || source.kind == Kind.ERROR) {
            return true; // Evitar cascada de errores
        }

        // Si son exactamente iguales, son compatibles
        if (target.equals(source)) {
            return true;
        }

        // Array-to-pointer decay (un arreglo T[N] se puede asignar o pasar a un puntero T*)
        if (target.isPointer() && source.isArray()) {
            if (target.kind == source.kind && target.pointerDepth == source.pointerDepth + 1) {
                return true;
            }
        }

        // Si son punteros, deben coincidir exactamente en tipo y nivel de indirección
        if (target.isPointer() || source.isPointer()) {
            return false;
        }

        // Si son arreglos, deben coincidir exactamente
        if (target.isArray() || source.isArray()) {
            return false;
        }

        // Conversiones implícitas entre tipos numéricos escalares
        if (target.isNumeric() && source.isNumeric()) {
            // Permitir widening (INT/CHAR/BOOL -> DOUBLE)
            if (target.kind == Kind.DOUBLE) {
                return true;
            }
            // Permitir asignaciones entre enteros (INT <-> CHAR <-> BOOL)
            if (target.kind == Kind.INT || target.kind == Kind.CHAR || target.kind == Kind.BOOL) {
                return true; 
            }
        }

        return false;
    }

    /**
     * Determina si se requiere una advertencia (narrowing o pérdida potencial de precisión).
     */
    public static boolean needsCoercionWarning(Type target, Type source) {
        if (!target.isScalar() || !source.isScalar()) {
            return false;
        }
        // Asignar DOUBLE a INT/CHAR/BOOL puede perder precisión
        if ((target.kind == Kind.INT || target.kind == Kind.CHAR || target.kind == Kind.BOOL) && source.kind == Kind.DOUBLE) {
            return true;
        }
        return false;
    }

    /**
     * Determina el tipo resultante de una operación aritmética binaria.
     */
    public static Type getResultType(Type left, Type right) {
        if (left.kind == Kind.ERROR || right.kind == Kind.ERROR) {
            return ERROR;
        }
        if (left.isPointer() || right.isPointer()) {
            // Aritmética de punteros limitada (p + i o i + p da tipo puntero)
            if (left.isPointer() && (right.kind == Kind.INT || right.kind == Kind.CHAR)) {
                return left;
            }
            if (right.isPointer() && (left.kind == Kind.INT || left.kind == Kind.CHAR)) {
                return right;
            }
            return ERROR;
        }
        if (left.kind == Kind.DOUBLE || right.kind == Kind.DOUBLE) {
            return DOUBLE;
        }
        if (left.kind == Kind.INT || right.kind == Kind.INT) {
            return INT;
        }
        if (left.kind == Kind.CHAR || right.kind == Kind.CHAR) {
            return INT;
        }
        if (left.kind == Kind.BOOL || right.kind == Kind.BOOL) {
            return INT;
        }
        return ERROR;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Type type = (Type) o;
        return pointerDepth == type.pointerDepth &&
               isArray == type.isArray &&
               arraySize == type.arraySize &&
               kind == type.kind &&
               Objects.equals(structName, type.structName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, structName, pointerDepth, isArray, arraySize);
    }

    @Override
    public String toString() {
        if (kind == Kind.ERROR) return "ERROR";
        StringBuilder sb = new StringBuilder();
        if (kind == Kind.STRUCT) {
            sb.append(structName);
        } else {
            sb.append(kind);
        }
        for (int i = 0; i < pointerDepth; i++) {
            sb.append("*");
        }
        if (isArray) {
            sb.append("[").append(arraySize).append("]");
        }
        return sb.toString().toLowerCase();
    }
}
