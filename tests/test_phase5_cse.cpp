// Caso de prueba dinámico para Eliminación de Subexpresiones Comunes (CSE Local) - Fase 5
// Usa parámetros de función para evitar plegado de constantes y forzar la optimización CSE

int calcularCse(int a, int b) {
    int x = a + b; // Primera evaluación
    int y = a + b; // CSE Local redundante: debe reusar el temporal de x
    int z = y + 2;
    return z;
}

int main() {
    int r = calcularCse(5, 10);
    return 0;
}
