// Caso de prueba para comprobar que el Análisis de Alias de Memoria evite optimizaciones erróneas - Fase 5

int main() {
    int a = 5;
    int* p = &a;
    *p = 10; // Escribe a través de un puntero en memoria
    int b = a; // No debe optimizarse a 5 porque la escritura en *p pudo haber cambiado el valor de a
    return b;
}
