// Prueba semántica exitosa (0 errores semánticos)

double calcular(int a, double b) {
    // Coerción implícita de int a double al sumar
    double resultado = a + b;
    return resultado;
}

int main() {
    int x = 5;
    double y = 10.5;
    
    // Llamada válida a función
    double res = calcular(x, y);
    
    // Asignación válida y coherente
    int valor = 'Z'; // Conversión implícita char -> int permitida
    
    // Uso correcto de bucle y condicional
    int contador = 0;
    while (contador < 10) {
        contador = contador + 1;
        if (contador == 5) {
            continue;
        }
    }
    
    return 0;
}
