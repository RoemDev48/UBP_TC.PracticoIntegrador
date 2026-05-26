// Programa de prueba con error de sintaxis intencional (falta un punto y coma y paréntesis no cerrado)

int main() {
    int x = 10
    if (x > 5 {
        x = x + 1;
    }
    return 0;
}
