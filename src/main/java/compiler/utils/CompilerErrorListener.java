package compiler.utils;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * Escuchador de errores personalizado para interceptar y reportar errores léxicos
 * y sintácticos de ANTLR con colores y formato descriptivo.
 */
public class CompilerErrorListener extends BaseErrorListener {
    private boolean hasErrors = false;

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, 
                            int line, int charPositionInLine, 
                            String msg, RecognitionException e) {
        hasErrors = true;
        ConsoleColor.printRed("[ERROR CRÍTICO] Error de análisis en la línea " + line + ", columna " + charPositionInLine + ": " + msg);
    }

    public boolean hasErrors() {
        return hasErrors;
    }
}
