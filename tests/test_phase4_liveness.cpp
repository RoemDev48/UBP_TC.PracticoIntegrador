// Caso de prueba dinámico para estresar el codegen y asignador de registros
// Implementa paso de parámetros por pila, liveness dinámico y spilling de registros

int sumarDoce(int v1, int v2, int v3, int v4, int v5, int v6, int v7, int v8, int v9, int v10, int v11, int v12) {
    // Al sumarlos todos de forma dinámica, forzamos que las 12 variables
    // y los temporales asociados se mantengan vivos simultáneamente.
    int sum = v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8 + v9 + v10 + v11 + v12;
    return sum;
}

int main() {
    int result = sumarDoce(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12);
    return 0;
}
