// Caso de prueba avanzado (Fase 6)
// Cubre: Tipos Booleanos, Arreglos de tamaño fijo y Punteros con direccionamiento/desreferenciación.

int main() {
    // 1. Tipos Booleanos
    bool flag = true;
    bool flag2 = false;
    
    // 2. Arreglos y bucles
    int datos[5];
    int i = 0;
    while (i < 5) {
        datos[i] = i * 10;
        i = i + 1;
    }
    
    // 3. Punteros y desreferenciación
    int x = 100;
    int* ptr = &x;
    
    // Modificar x indirectamente a través del puntero
    *ptr = 50;
    
    // Acceder a elemento de arreglo y sumar
    int valor = datos[2]; // valor debe ser 2 * 10 = 20
    int resultado = x + valor; // resultado debe ser 50 + 20 = 70
    
    return resultado;
}
