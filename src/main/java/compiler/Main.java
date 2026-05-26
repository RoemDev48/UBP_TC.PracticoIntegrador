package compiler;

import compiler.ast.ASTBuilder;
import compiler.ast.ASTNode;
import compiler.parser.CPPSubsetLexer;
import compiler.parser.CPPSubsetParser;
import compiler.semantic.SemanticAnalyzer;
import compiler.tac.TACGenerator;
import compiler.tac.TACOptimizer;
import compiler.tac.TACInstruction;
import compiler.utils.CompilerErrorListener;
import compiler.utils.ConsoleColor;
import compiler.visitor.ASTPrinter;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Clase principal que actúa como punto de entrada para el compilador.
 * Ejecuta el análisis léxico, sintáctico y semántico (Fase 1 y 2).
 */
public class Main {

    public static void main(String[] args) {
        if (args.length < 1) {
            ConsoleColor.printRed("[ERROR CRÍTICO] Uso correcto: java compiler.Main <archivo_fuente.cpp>");
            System.exit(1);
        }

        String sourceFilePath = args[0];
        if (!Files.exists(Paths.get(sourceFilePath))) {
            ConsoleColor.printRed("[ERROR CRÍTICO] El archivo '" + sourceFilePath + "' no existe.");
            System.exit(1);
        }

        ConsoleColor.printCyan("Iniciando compilación del archivo: " + sourceFilePath + "\n");

        try {
            // 1. Inicializar el analizador léxico (Lexer)
            CPPSubsetLexer lexer = new CPPSubsetLexer(CharStreams.fromFileName(sourceFilePath));
            
            // Adjuntar detector de errores personalizado al lexer
            CompilerErrorListener errorListener = new CompilerErrorListener();
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);

            // Obtener el flujo de tokens y cargarlo
            CommonTokenStream tokenStream = new CommonTokenStream(lexer);
            tokenStream.fill();
            List<Token> tokens = tokenStream.getTokens();

            // 2. Mostrar la tabla de tokens en consola
            printTokenTable(tokens, lexer);

            // 3. Inicializar el analizador sintáctico (Parser) con el flujo de tokens
            CPPSubsetParser parser = new CPPSubsetParser(tokenStream);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);

            // Ejecutar el parseo a partir de la regla raíz "program"
            CPPSubsetParser.ProgramContext programCtx = parser.program();

            // Verificar si ocurrieron errores críticos en esta fase
            if (errorListener.hasErrors()) {
                ConsoleColor.printRed("\n[ERROR CRÍTICO] La compilación falló en la Fase 1: Análisis Léxico/Sintáctico.");
                System.exit(1);
            } else {
                ConsoleColor.printGreen("\n[ÉXITO] Fase 1 completada. Análisis léxico y sintáctico exitoso (0 errores).");
            }

            // 4. Construcción del Árbol de Sintaxis Abstracta (AST)
            ConsoleColor.printCyan("\nConstruyendo el Árbol de Sintaxis Abstracta (AST)...");
            ASTBuilder astBuilder = new ASTBuilder();
            ASTNode astRoot = astBuilder.visit(programCtx);

            // 5. Mostrar la representación gráfica del AST
            ConsoleColor.printCyan("\n======================================================================");
            ConsoleColor.printCyan("                  ÁRBOL DE SINTAXIS ABSTRACTA (AST)                   ");
            ConsoleColor.printCyan("======================================================================");
            ASTPrinter astPrinter = new ASTPrinter();
            System.out.println(astRoot.accept(astPrinter));
            ConsoleColor.printCyan("======================================================================\n");

            ConsoleColor.printGreen("[ÉXITO] Árbol de Sintaxis Abstracta (AST) generado correctamente.");

            // 6. Análisis Semántico (Fase 2)
            ConsoleColor.printCyan("\nIniciando Análisis Semántico (Fase 2)...");
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
            astRoot.accept(semanticAnalyzer);

            // Imprimir advertencias (Warnings)
            if (semanticAnalyzer.hasWarnings()) {
                ConsoleColor.printYellow("\n[WARNINGS DETECTADOS]:");
                for (String warning : semanticAnalyzer.getWarnings()) {
                    ConsoleColor.printYellow("  " + warning);
                }
            }

            // Imprimir errores (Errors)
            if (semanticAnalyzer.hasErrors()) {
                ConsoleColor.printRed("\n[ERRORES CRÍTICOS DETECTADOS]:");
                for (String error : semanticAnalyzer.getErrors()) {
                    ConsoleColor.printRed("  " + error);
                }
                ConsoleColor.printRed("\n[ERROR CRÍTICO] La compilación falló en la Fase 2: Análisis Semántico.");
                System.exit(1);
            } else {
                ConsoleColor.printGreen("\n[ÉXITO] Fase 2 completada. Análisis semántico exitoso (0 errores).");
            }

            // 7. Generación de Código Intermedio (Fase 3)
            ConsoleColor.printCyan("\nIniciando Generación de Código Intermedio (Fase 3)...");
            TACGenerator tacGenerator = new TACGenerator();
            astRoot.accept(tacGenerator);
            String tacCode = tacGenerator.getTACCode();
            List<TACInstruction> originalInstructions = tacGenerator.getInstructions();

            // Guardar el código intermedio (TAC) en un archivo de salida
            String tacOutputFilePath = sourceFilePath.substring(0, sourceFilePath.lastIndexOf('.')) + ".tac";
            Files.writeString(Paths.get(tacOutputFilePath), tacCode);
            ConsoleColor.printGreen("\n[ÉXITO] Fase 3 completada. Código de tres direcciones (TAC) generado con éxito.");
            ConsoleColor.printGreen("Archivo guardado en: " + tacOutputFilePath);

            // Imprimir el TAC generado en la consola para retroalimentación
            ConsoleColor.printCyan("\n======================================================================");
            ConsoleColor.printCyan("                   CÓDIGO DE TRES DIRECCIONES (TAC)                   ");
            ConsoleColor.printCyan("======================================================================");
            System.out.print(tacCode);
            ConsoleColor.printCyan("======================================================================\n");

            // 8. Optimización de Código Intermedio (Fase 4)
            ConsoleColor.printCyan("Iniciando Optimización de Código Intermedio (Fase 4)...");
            List<TACInstruction> optimizedInstructions = TACOptimizer.optimize(originalInstructions);
            
            // Construir el string del código optimizado
            StringBuilder optSb = new StringBuilder();
            for (TACInstruction inst : optimizedInstructions) {
                optSb.append(inst.toString()).append("\n");
            }
            String optTacCode = optSb.toString();

            // Guardar el código optimizado en un archivo de salida
            String optOutputFilePath = sourceFilePath.substring(0, sourceFilePath.lastIndexOf('.')) + ".opt.tac";
            Files.writeString(Paths.get(optOutputFilePath), optTacCode);
            ConsoleColor.printGreen("\n[ÉXITO] Fase 4 completada. Código de tres direcciones optimizado (OPT-TAC) con éxito.");
            ConsoleColor.printGreen("Archivo optimizado guardado en: " + optOutputFilePath);

            // Imprimir el TAC optimizado en la consola
            ConsoleColor.printCyan("\n======================================================================");
            ConsoleColor.printCyan("             CÓDIGO DE TRES DIRECCIONES OPTIMIZADO (TAC)              ");
            ConsoleColor.printCyan("======================================================================");
            System.out.print(optTacCode);
            ConsoleColor.printCyan("======================================================================\n");
            
        } catch (IOException e) {
            ConsoleColor.printRed("[ERROR CRÍTICO] Error al leer el archivo fuente: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            ConsoleColor.printRed("[ERROR CRÍTICO] Error inesperado en el compilador: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Imprime una tabla bien formateada de los tokens encontrados en el archivo.
     */
    private static void printTokenTable(List<Token> tokens, CPPSubsetLexer lexer) {
        ConsoleColor.printCyan("======================================================================");
        ConsoleColor.printCyan("                      TABLA DE TOKENS (LEXER)                         ");
        ConsoleColor.printCyan("======================================================================");
        System.out.printf("%-20s | %-30s | %-6s | %-6s\n", "TIPO DE TOKEN", "LEXEMA (TEXTO)", "LÍNEA", "COLUMNA");
        System.out.println("----------------------------------------------------------------------");
        
        for (Token token : tokens) {
            if (token.getType() == Token.EOF) continue;
            
            String typeName = lexer.getVocabulary().getSymbolicName(token.getType());
            if (typeName == null) {
                typeName = "LITERAL_TEXTO";
            }
            
            // Limpiar saltos de línea para mostrar de forma segura en la tabla
            String lexeme = token.getText()
                                 .replace("\n", "\\n")
                                 .replace("\r", "\\r")
                                 .replace("\t", "\\t");
            
            // Truncar lexemas muy largos para que no rompan la tabla
            if (lexeme.length() > 27) {
                lexeme = lexeme.substring(0, 24) + "...";
            }

            System.out.printf("%-20s | %-30s | %-6d | %-6d\n", 
                              typeName, 
                              "\"" + lexeme + "\"", 
                              token.getLine(), 
                              token.getCharPositionInLine());
        }
        ConsoleColor.printCyan("======================================================================\n");
    }
}
