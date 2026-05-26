package compiler.tac;

/**
 * Representa una instrucción de Código de Tres Direcciones (TAC).
 */
public class TACInstruction {
    public enum Type {
        LABEL,          // L0:
        ASSIGN,         // x = y
        BINARY_OP,      // x = y + z
        UNARY_OP,       // x = -y
        GOTO,           // goto L1
        IF_FALSE_GOTO,  // ifFalse x goto L2
        PARAM,          // param x
        CALL,           // t0 = call f, n  o  call f, n
        RETURN,         // return x  o  return
        FUNC_START,     // function f
        FUNC_END        // end function
    }

    private final Type type;
    private final String operator;
    private final String arg1;
    private final String arg2;
    private final String result;

    public TACInstruction(Type type, String operator, String arg1, String arg2, String result) {
        this.type = type;
        this.operator = operator;
        this.arg1 = arg1;
        this.arg2 = arg2;
        this.result = result;
    }

    public Type getType() {
        return type;
    }

    public String getOperator() {
        return operator;
    }

    public String getArg1() {
        return arg1;
    }

    public String getArg2() {
        return arg2;
    }

    public String getResult() {
        return result;
    }

    @Override
    public String toString() {
        switch (type) {
            case LABEL:
                return arg1 + ":";
            case ASSIGN:
                return result + " = " + arg1;
            case BINARY_OP:
                return result + " = " + arg1 + " " + operator + " " + arg2;
            case UNARY_OP:
                return result + " = " + operator + arg1;
            case GOTO:
                return "goto " + result;
            case IF_FALSE_GOTO:
                return "ifFalse " + arg1 + " goto " + result;
            case PARAM:
                return "param " + arg1;
            case CALL:
                if (result != null && !result.isEmpty()) {
                    return result + " = call " + arg1 + ", " + arg2;
                } else {
                    return "call " + arg1 + ", " + arg2;
                }
            case RETURN:
                if (arg1 != null && !arg1.isEmpty()) {
                    return "return " + arg1;
                } else {
                    return "return";
                }
            case FUNC_START:
                return "\nfunction " + arg1;
            case FUNC_END:
                return "end function\n";
            default:
                return "UNKNOWN INSTRUCTION";
        }
    }
}
