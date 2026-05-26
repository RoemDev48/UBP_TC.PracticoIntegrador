// Prueba semántica con errores y warnings intencionales

double calcular(int x, int x) { // ERROR: Parámetro duplicado 'x'
    return 1.0;
}

void procesar() {
    int a = 5;
    return a; // ERROR: Función void no debe retornar valor
}

int main() {
    int x = 10;
    int x = 20; // ERROR: Redeclaración de 'x'
    
    y = 50; // ERROR: Variable 'y' no declarada
    
    double d = 3.14;
    int valor = d; // WARNING: Coerción implícita de double a int (pérdida de precisión)
    
    int flag = 'A';
    if (flag) {
        break; // ERROR: break fuera de un bucle
    }
    
    // Llamada a función inexistente
    int res = multiplicar(5, 5); // ERROR: Función 'multiplicar' no declarada
    
    // Llamada con número incorrecto de argumentos
    double r = calcular(10); // ERROR: Número incorrecto de argumentos (esperaba 2, recibió 1)
    
    return 3.14; // WARNING: Coerción implícita de double a int en el retorno
}
