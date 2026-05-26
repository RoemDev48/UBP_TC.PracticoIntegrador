# Script de compilación del compilador de subconjunto de C++

# Definir la ruta de la JDK local
$javaBin = "C:\Program Files\Java\jdk-21.0.11\bin"
$java = "$javaBin\java.exe"
$javac = "$javaBin\javac.exe"

Write-Host "=========================================" -ForegroundColor Cyan
Write-Host " Iniciando compilación del Compilador C++" -ForegroundColor Cyan
Write-Host "=========================================" -ForegroundColor Cyan

# Crear directorios necesarios
New-Item -ItemType Directory -Force -Path "src/main/java/compiler/parser" | Out-Null
New-Item -ItemType Directory -Force -Path "bin" | Out-Null

Write-Host "[1/3] Generando analizador léxico y sintáctico con ANTLR4..." -ForegroundColor Yellow
& $java -jar "lib/antlr-4.13.1-complete.jar" -package compiler.parser -visitor -no-listener -o "src/main/java/compiler/parser" "src/main/antlr/CPPSubset.g4"

if ($LASTEXITCODE -ne 0) {
    Write-Error "[ERROR] Falló la generación de clases de ANTLR4."
    exit $LASTEXITCODE
}
Write-Host "      Generación de ANTLR4 exitosa." -ForegroundColor Green

Write-Host "[2/3] Compilando código fuente Java..." -ForegroundColor Yellow
# Buscar recursivamente todos los archivos .java
$javaFiles = Get-ChildItem -Path "src/main/java" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }

& $javac -cp "lib/*;src/main/java" -d "bin" $javaFiles

if ($LASTEXITCODE -ne 0) {
    Write-Error "[ERROR] Falló la compilación del código Java."
    exit $LASTEXITCODE
}
Write-Host "      Compilación de Java exitosa." -ForegroundColor Green

Write-Host "[3/3] Proceso finalizado. El compilador está listo para ejecutarse." -ForegroundColor Green
Write-Host "=========================================" -ForegroundColor Cyan
