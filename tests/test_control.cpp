// Prueba de estructuras de control (if-else, while, for, break, continue)

int main() {
    int x = 10;
    int y = 20;
    
    // Condicional if-else
    if (x < y && x != 0) {
        x = x + 5;
    } else {
        y = y - 5;
    }
    
    // Bucle while
    while (x > 0) {
        x = x - 1;
        if (x == 5) {
            continue;
        }
    }
    
    // Bucle for
    int total = 0;
    for (int i = 0; i < 10; i = i + 1) {
        total = total + i;
        if (total > 30) {
            break;
        }
    }
    
    return total;
}
