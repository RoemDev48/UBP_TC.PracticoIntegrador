// Prueba semántica de warnings: inicialización y código muerto

int test_ret() {
    return 1;
    int a = 10; // WARNING: Código inalcanzable (código muerto) detectado.
}

int main() {
    int x; 
    int y = x + 5; // WARNING: La variable 'x' podría no estar inicializada al usarse.
    
    int* ptr;
    int* val = &x; // OK: &x no lee el valor de x, no debe dar warning.
    
    int a;
    int b;
    int c = 1;
    
    if (c == 1) {
        a = 10;
        b = 20;
    } else {
        a = 15;
    }
    
    int z = a; // OK: 'a' se inicializó en ambas ramas del if.
    int w = b; // WARNING: 'b' podría no estar inicializada al usarse (no se inicializó en el else).
    
    return 0;
}
