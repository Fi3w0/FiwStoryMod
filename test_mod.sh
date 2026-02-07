#!/bin/bash
# Script para probar Fiw Story Mod 1.0.9

echo "=== Fiw Story Mod Test Script ==="
echo "Versión: 1.0.9 [BETA]"
echo ""

# 1. Compilar el mod
echo "1. Compilando el mod..."
./gradlew build

if [ $? -eq 0 ]; then
    echo "✅ Compilación exitosa"
    
    # Mostrar información del JAR
    JAR_FILE=$(find build/libs -name "fiwstory-*.jar" | head -1)
    echo "   JAR creado: $JAR_FILE"
    echo "   Tamaño: $(du -h "$JAR_FILE" | cut -f1)"
else
    echo "❌ Error en compilación"
    exit 1
fi

echo ""
echo "2. Verificando archivos importantes..."

# Verificar texturas
echo "   Texturas disponibles:"
find src/main/resources/assets/fiwstory/textures -name "*.png" | wc -l | xargs echo "   - Total:"

# Verificar que existan texturas críticas
CRITICAL_TEXTURES=(
    "src/main/resources/assets/fiwstory/textures/mob_effect/corruption.png"
    "src/main/resources/assets/fiwstory/textures/item/cristal_puro.png"
    "src/main/resources/assets/fiwstory/textures/item/mix_puro.png"
    "src/main/resources/assets/fiwstory/textures/item/cursed_spear_of_fi3w0.png"
    "src/main/resources/assets/fiwstory/textures/item/corrupted_crystal.png"
)

for texture in "${CRITICAL_TEXTURES[@]}"; do
    if [ -f "$texture" ]; then
        echo "   ✅ $(basename "$texture")"
    else
        echo "   ⚠️  FALTA: $(basename "$texture")"
    fi
done

echo ""
echo "3. Comandos de prueba disponibles:"
echo "   ./gradlew runClient    - Ejecutar cliente de prueba"
echo "   ./gradlew build        - Recompilar"
echo "   ./test_mod.sh          - Ejecutar este script"
echo ""
echo "4. Para probar en Docker (2GB RAM):"
echo "   docker build -t fiwstory-test ."
echo "   docker run -p 25565:25565 fiwstory-test"
echo ""
echo "=== Test completado ==="