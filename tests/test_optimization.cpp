// Prueba de optimizaciones (Fase 4)
// Cubre: Propagación de constantes, simplificación de expresiones y eliminación de código muerto

int main() {
    int a = 10;
    int b = 20;
    
    // 1. Propagación y Plegado:
    // a + b -> 10 + 20 -> 30
    int c = a + b; 
    
    // 2. Identidades Algebraicas:
    // c * 1 -> c (30)
    // d + 0 -> d (30)
    int d = c * 1;
    int e = d + 0;
    
    // Multiplicación por cero:
    // e * 0 -> 0
    int f = e * 0;
    
    // 3. Código inalcanzable (Dead Code):
    return f;
    
    // Esto es código inalcanzable y debe ser completamente eliminado:
    int x = 999;
    int y = x + 5;
    x = y * 0;
}
