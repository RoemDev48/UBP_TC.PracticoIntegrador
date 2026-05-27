// Caso de prueba para errores semánticos de la Fase 2

int main() {
    // 1. Intentar hacer ++ sobre un valor constante (no es L-Value)
    int a = 5++; // ERROR: El operando del operador '++' debe ser una dirección de memoria modificable (L-Value).
    
    // 2. Intentar hacer ++ sobre un tipo no numérico (ej. string)
    string s = "test";
    s--; // ERROR: El operando del operador '--' debe ser de tipo numérico o puntero.
    
    // 3. Asignación incompatible de string a int
    int num = "un string"; // ERROR: Tipos incompatibles en la inicialización de 'num'. No se puede convertir string a int.
    
    return 0;
}
