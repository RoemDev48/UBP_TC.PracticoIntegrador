// Caso de prueba para operadores ++ y -- (Fase 2)

int main() {
    int x = 5;
    int y = ++x; // Prefijo: x se convierte en 6, y recibe 6
    
    int a = 10;
    int b = a++; // Postfijo: a se convierte en 11, b recibe 10
    
    int arr[5];
    arr[0] = 100;
    int c = ++arr[0]; // Prefijo sobre elemento de arreglo: arr[0] -> 101, c -> 101
    int d = arr[0]--; // Postfijo sobre elemento de arreglo: arr[0] -> 100, d -> 101
    
    int val = 42;
    int* ptr = &val;
    int e = ++(*ptr); // Prefijo sobre desreferenciación: val -> 43, e -> 43
    
    return 0;
}
