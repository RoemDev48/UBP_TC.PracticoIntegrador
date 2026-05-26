package compiler.utils;

/**
 * Utilidad para imprimir texto con colores ANSI en la consola.
 */
public class ConsoleColor {
    // Códigos ANSI para colores
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String CYAN = "\u001B[36m";

    public static void printGreen(String text) {
        System.out.println(GREEN + text + RESET);
    }

    public static void printYellow(String text) {
        System.out.println(YELLOW + text + RESET);
    }

    public static void printRed(String text) {
        System.err.println(RED + text + RESET);
    }

    public static void printCyan(String text) {
        System.out.println(CYAN + text + RESET);
    }

    public static String getGreen(String text) {
        return GREEN + text + RESET;
    }

    public static String getYellow(String text) {
        return YELLOW + text + RESET;
    }

    public static String getRed(String text) {
        return RED + text + RESET;
    }

    public static String getCyan(String text) {
        return CYAN + text + RESET;
    }
}
