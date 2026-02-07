#!/bin/bash
# Script para probar el mod Fiw Story

echo "=== Probando Fiw Story Mod v1.0.6 ==="
echo ""

# Compilar el mod
echo "1. Compilando el mod..."
./gradlew build

if [ $? -eq 0 ]; then
    echo "✅ Compilación exitosa"
    
    # Verificar archivos generados
    echo ""
    echo "2. Verificando archivos generados..."
    
    JAR_FILE=$(find build/libs -name "*.jar" | grep -v sources | grep -v dev | head -1)
    if [ -f "$JAR_FILE" ]; then
        echo "✅ JAR generado: $JAR_FILE"
        echo "   Tamaño: $(du -h "$JAR_FILE" | cut -f1)"
    else
        echo "❌ No se encontró el archivo JAR"
        exit 1
    fi
    
    # Verificar modelos JSON
    echo ""
    echo "3. Verificando modelos JSON..."
    
    MODELS=(
        "pharaoh_ring_artifact.json"
        "temporal_structure_artifact.json" 
        "fi3w0_glasses.json"
    )
    
    for model in "${MODELS[@]}"; do
        if [ -f "src/main/resources/assets/fiwstory/models/item/$model" ]; then
            echo "✅ Modelo $model encontrado"
        else
            echo "❌ Modelo $model NO encontrado"
        fi
    done
    
    # Verificar texturas
    echo ""
    echo "4. Verificando texturas..."
    
    TEXTURES=(
        "artifact_ring.png"
        "artifact_light.png"
        "artifact_light.png.mcmeta"
        "fi3w0_glasses.png"
    )
    
    for texture in "${TEXTURES[@]}"; do
        if [ -f "src/main/resources/assets/fiwstory/textures/item/$texture" ]; then
            echo "✅ Textura $texture encontrada"
        else
            echo "❌ Textura $texture NO encontrada"
        fi
    done
    
    # Verificar clases Java
    echo ""
    echo "5. Verificando clases Java..."
    
    CLASSES=(
        "PharaohRingArtifact.java"
        "TemporalStructureArtifact.java"
        "Fi3w0GlassesArmor.java"
    )
    
    for class in "${CLASSES[@]}"; do
        if [ -f "src/main/java/com/fiw/fiwstory/item/custom/$class" ]; then
            echo "✅ Clase $class encontrada"
        else
            echo "❌ Clase $class NO encontrada"
        fi
    done
    
    echo ""
    echo "=== Resumen ==="
    echo "Mod: Fiw Story v1.0.6"
    echo "Total ítems: 8"
    echo "  - Lanza Devora Almas"
    echo "  - Cristal Corrupto"
    echo "  - Escarabajo del Faraón"
    echo "  - Gema del Lord de Caos"
    echo "  - Gema de Sangre Divina"
    echo "  - Anillo del Faraón"
    echo "  - Estructura Atemporal"
    echo "  - Gafas de Fi3w0"
    echo ""
    echo "✅ Mod listo para usar"
    echo ""
    echo "Para ejecutar el cliente:"
    echo "  ./gradlew runClient"
    echo ""
    echo "Para usar Docker:"
    echo "  docker build -t fiwstory-mod ."
    echo "  docker-compose up"
    
else
    echo "❌ Error en la compilación"
    exit 1
fi