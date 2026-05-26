// Prueba de declaración de funciones, llamadas y retornos

double calcularArea(double radio) {
    double pi = 3.141592;
    return pi * radio * radio;
}

int factorial(int n) {
    if (n <= 1) {
        return 1;
    }
    return n * factorial(n - 1);
}

void procesar() {
    int x = 5;
    int f = factorial(x);
    double area = calcularArea(10.0);
}

int main() {
    procesar();
    return 0;
}
