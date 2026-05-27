// Caso de prueba exitoso para Soporte de Estructuras (struct) - Fase 3

struct Point {
    int x;
    int y;
};

struct Vector {
    Point start;
    Point end;
};

int main() {
    // 1. Declaración e inicialización de estructura simple
    Point p1;
    p1.x = 10;
    p1.y = 20;

    // 2. Operadores de incremento y decremento en miembros
    p1.x++;
    ++p1.y;

    // 3. Estructuras anidadas
    Vector v;
    v.start.x = 1;
    v.start.y = 2;
    v.end.x = p1.x;
    v.end.y = p1.y;

    // 4. Puntero a estructura y acceso por desreferenciación (*ptr).member
    Point* ptr = &p1;
    (*ptr).x = 100;
    int val = (*ptr).x + (*ptr).y;

    return 0;
}
