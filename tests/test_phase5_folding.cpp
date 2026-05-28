// Caso de prueba para Constant Folding, Algebraic Simplifications y Strength Reduction - Fase 5

int main() {
    int a = 5;
    int b = 10;
    int c = a + b; // Constant Folding: 5 + 10 -> 15
    int d = c + 0; // Algebraic Simplification: c + 0 -> c
    int e = d * 1; // Algebraic Simplification: d * 1 -> d
    int f = e * 2; // Strength Reduction: e * 2 -> e + e
    return f;
}
