// Caso de prueba para errores semánticos en Estructuras (struct) - Fase 3

struct Point {
    int x;
    int y;
};

int main() {
    // 1. Uso de tipo estructurado inexistente (debe fallar)
    // UnknownStruct s;

    Point p;
    
    // 2. Acceso a miembro inexistente en la estructura (debe reportar error)
    p.z = 100;

    // 3. Acceso a miembro sobre una variable que no es estructura (debe reportar error)
    int n = 5;
    n.x = 10;

    // 4. Asignación incompatible con el tipo del miembro
    // Asignar un puntero a int a un entero normal, o tipos incompatibles (si aplica)
    int* ptr = &n;
    p.x = ptr; // Asignación incompatible: int* a int

    // 5. Advertencia de variable no inicializada (p.y no fue inicializado al leerse)
    int val = p.y; 

    return 0;
}
