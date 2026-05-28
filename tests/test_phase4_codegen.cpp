// Caso de prueba completo para generación de código MIPS (Fase 4)
// Soporta estructuras, miembros, punteros a estructuras, desreferenciación y aritmética

struct Point {
    int x;
    int y;
};

int inicializarPunto(Point* p, int vx, int vy) {
    (*p).x = vx;
    (*p).y = vy;
    return 0;
}

int main() {
    Point pt;
    
    // Inicializar llamando a función pasando puntero &pt
    inicializarPunto(&pt, 10, 20);

    // Acceso y escritura directa sobre miembros
    pt.x = pt.x + 5;
    pt.y = pt.y - 3;

    // Leer valor de miembros
    int val_x = pt.x;
    int val_y = pt.y;

    return val_x + val_y;
}
