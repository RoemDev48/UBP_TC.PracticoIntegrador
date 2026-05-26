# Script para ejecutar el compilador

# Definir la ruta de la JDK local
$java = "C:\Program Files\Java\jdk-21.0.11\bin\java.exe"

if ($args.Length -lt 1) {
    Write-Host "Uso: .\run.ps1 <archivo_prueba.cpp>" -ForegroundColor Red
    exit 1
}

$sourceFile = $args[0]

# Ejecutar el compilador
& $java -cp "bin;lib/*" compiler.Main $sourceFile
