// Prueba semántica de error de retorno: falta de retorno garantizado en función no-void

int calcular(int x) {
    if (x > 0) {
        return 10;
    }
    // ERROR: La función no-void 'calcular' debe retornar un valor en todos sus caminos de ejecución.
}

int main() {
    int val = calcular(5);
    return 0;
}
