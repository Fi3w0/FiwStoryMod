# Fiw Story Mod

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-0.92.7-blue.svg)](https://fabricmc.net/)
[![Versión](https://img.shields.io/badge/Versión-2.1.2-orange.svg)](https://github.com/Fi3w0/FiwStoryMod)
[![Idioma](https://img.shields.io/badge/Idioma-Español-red.svg)](https://github.com/Fi3w0/FiwStoryMod)
[![Licencia](https://img.shields.io/badge/Licencia-CC--BY--NC--SA--4.0-lightgrey.svg)](LICENSE)

Mod personal creado para el SMP de Fi3w0. Añade artefactos legendarios con habilidades únicas, un sistema de corrupción narrativo, comandos de administración y lore exclusivo del servidor.

> **Nota:** Este mod está diseñado específicamente para el contexto de un servidor SMP privado. Mucho del contenido solo tiene sentido dentro de ese lore. Puedes usarlo libremente, pero sin soporte técnico.

---

## Requisitos

| Dependencia | Versión |
|---|---|
| Minecraft | 1.20.1 |
| Fabric Loader | 0.18.3+ |
| Fabric API | 0.92.7+1.20.1 |
| Trinkets | 3.7.1+ |
| Java | 21 |

---

## Instalación

1. Instalar [Fabric Loader](https://fabricmc.net/use/) para Minecraft 1.20.1
2. Descargar [Fabric API](https://modrinth.com/mod/fabric-api)
3. Descargar [Trinkets](https://modrinth.com/mod/trinkets)
4. Clonar el repositorio y compilar con `./gradlew build`
5. Copiar el `.jar` de `build/libs/` a la carpeta `mods`

```bash
git clone https://github.com/Fi3w0/FiwStoryMod.git
cd FiwStoryMod
./gradlew build
```

---

## Contenido

### Artefactos del Faraón

Artefactos legendarios pertenecientes al lore del Dios Faraón. Se equipan en slots de trinket (offhand o Trinkets API).

| Artefacto | Habilidad pasiva | Atributos |
|---|---|---|
| **Escarabajo del Faraón** | Bendición Solar — regenera 0.5♥ cada 3s de día | +1 ATK, +0.5 vel. ataque, +1♥, +5% vel. |
| **Daga del Faraón** | — | +4 ATK, +5% vel., +5 suerte (arma mainhand) |
| **Anillo del Faraón** | Fortuna Divina — Luck I permanente | +4 armadura, +2 toughness, +10 suerte |
| **Gema del Caos** | Caos Latente — efecto aleatorio cada 30s | +4% vel., +10% KB resist., +1.5 ATK |
| **Gema de Sangre Divina** | Robo de Vida — 15% lifesteal (CD: 2s) | +2♥, +2 armadura, +2 ATK, +10% KB resist. |
| **Corazón de Dios Caído** | Resistencia I + Fuerza I — drena -1♥ max cada 20s | Sistema HP dinámico |
| **Piedra Filosófica** | Transmutación — Luck I + 1-2 XP cada 30s | — |
| **Estructura Atemporal** | Colapso Temporal — Haste III + Speed II 8s (CD: 60s) | +10% vel. |
| **Escarabajo** | Escudo de Arena — Absorción II al recibir daño (CD: 20s) | +4 armadura, +2 toughness, +20% KB resist. |

### Anillos de Cobre

| Artefacto | Habilidad | Atributos |
|---|---|---|
| **Amuleto de Cobre Corroído** | Descarga eléctrica en cadena si hay 3+ mobs cerca (c/5s) | +5% vel., +1 ATK, -1 armadura |
| **Amuleto de Cobre Despertado** | Descarga mejorada + reacción a rayos durante tormenta | +7% vel., +2 ATK, +1 armadura |

### Artefactos Skyxern

| Artefacto | Rareza | Habilidades |
|---|---|---|
| **Núcleo Astral** | Épico | Zero Step (doble salto + -20% caída), Bendición del Horizonte auto c/42s |
| **Skyxern Legacy Fragment** | Legendario | Ascenso del Legado (Speed I + Strength I, CD: 90s), curación pasiva c/200 ticks, -30% caída |

### Artefactos Tecnológicos

| Artefacto | Habilidad |
|---|---|
| **GD42 Quantum** | Quantum Anchor — ancla cuántica temporal |
| **MK88 Tablet** | Emergency Recovery Module — recuperación de emergencia |
| **Bronze Axiom-7** | Combustion Core — ignición al golpear |

### Armas

| Arma | Tipo | Habilidades |
|---|---|---|
| **Lanza Maldita de Fi3w0** | Lanza legendaria | Dash Riptide (CD: 1.5s), World Barrage — 20 arc slashes en 4s, **7 dmg mágico c/u** (ignora armadura), Wither II 5s al final (requiere Gafas, CD: 10s) |
| **Espada del Caos** | Espada legendaria | Aura de Caos — Strength II + Slowness en área (CD: 60s); Arc Slash — barrido 180° 8 dmg (CD: 8s) |
| **Espada Mgshtraklar** | Espada legendaria | Blood Steal — lifesteal 10% pasivo en mainhand (CD: 2s); Crimson Slash — 3 garras de energía 10 dmg c/u + explosión final (CD: 10s); anti-duelo con Gema de Sangre |
| **Hoja Atemporal** | Espada mítica | Acceso al Vacío, indestructible |
| **Tomo Mágico** | Grimorio | Desmantelar — slash 10 bloques **20 dmg mágico** (ignora armadura, CD: 3s); Marca Corrupta — debuff armadura + 10% daño extra (CD: 12s); Dominio Fracturado — refleja 30% dmg 3s (CD: 18s) |

### Armadura

| Armadura | Habilidades |
|---|---|
| **Gafas de Fi3w0** | +35% vel., +4 armadura, +3 toughness, sinergia con la Lanza |

### Artefactos de Lore / Diosa

| Item | Descripción |
|---|---|
| **Flor de la Diosa** | Juicio de la naturaleza, protección de emergencia |
| **Piedra de Hielo** | Aura de escarcha y ralentización |
| **Piedra Filosófica Mejorada** | Versión avanzada con generación de mineral |

### Economía del Servidor

| Item | Descripción |
|---|---|
| **Moneda de Skyxern** | Mineral de origen desconocido, moneda universal por acuerdo tácito. Stackable ×64. |

### Items de Sistema

| Item | Función |
|---|---|
| **Cristal Puro** | Ingrediente de purificación |
| **Mezcla Pura** | Reduce niveles de corrupción |
| **Runa de Curación** | Elimina corrupción nivel 1 |
| **Cristal Corrupto** | Aplica corrupción permanente |
| **Sangre Divina** | Poción de curación poderosa |
| **Carne Corrompida** | Alimento — Resistencia II pero Hambre + Náusea |

---

## Sistemas

### Sistema de Corrupción

Progresión narrativa de 5 niveles que afecta al jugador con efectos visuales, partículas y penalizaciones mecánicas. Se acumula usando artefactos y se purifica con cristales y rituales.

### Sistema Trinkets

Todos los artefactos son compatibles con la [Trinkets API](https://modrinth.com/mod/trinkets), que permite equiparlos en slots dedicados sin ocupar el inventario principal.

### Sistema de Bind (Soulbound)

Los artefactos pueden vincularse a un jugador específico. Un artefacto vinculado no puede ser usado por otro jugador y aplica penalizaciones si lo intenta.

### Sistema Anti-Duelo (Blood)

La **Espada Mgshtraklar** y la **Gema de Sangre Divina** son incompatibles. Si un jugador tiene ambos items en cualquier ubicación (inventario, offhand, trinkets, ender chest) recibe 1♥ de daño mágico cada 2 segundos hasta retirar uno de ellos.

### Dimensión Timeless Void

Dimensión personalizada — vacío infinito con islas flotantes. Sin mobs, ambiente pacífico, pensado para contexto de lore.

---

## Comandos

| Comando | Descripción | Permiso |
|---|---|---|
| `/corruption [jugador]` | Ver nivel de corrupción | Op |
| `/immunity [jugador] [duración]` | Dar inmunidad temporal | Op |
| `/void` | Teletransporte a Timeless Void | Op |
| `/bind` | Vincular el item actualmente sostenido | Op |

---

## Compilar desde fuente

```bash
git clone https://github.com/Fi3w0/FiwStoryMod.git
cd FiwStoryMod
./gradlew build
# El .jar estará en build/libs/
```

---

## Estructura del proyecto

```
src/main/java/com/fiw/fiwstory/
├── item/           # Items, artefactos y BaseArtifactItem
├── event/          # Manejadores de eventos del servidor
├── data/           # Datos persistentes (corrupción, hearts, inmunidad)
├── effect/         # Efectos de estado personalizados
├── command/        # Comandos de administración
├── dimension/      # Dimensión Timeless Void
├── client/         # Renderizado y efectos cliente
└── lib/            # FiwLib — utilidades compartidas
```

---

## Avisos legales

### Texturas
Las texturas son de terceros. No tengo derechos sobre ellas. Uso exclusivamente personal y no comercial. Si eres el autor de alguna textura y deseas que la retire, contáctame.

### Código
Desarrollado por Fi3w0 con asistencia de [Claude Sonnet 4.6](https://anthropic.com) (Anthropic). El código es de autoría conjunta — las ideas, diseño de mecánicas y lore son de Fi3w0; la implementación técnica fue desarrollada en colaboración con IA.

---

## Licencia

Este proyecto está bajo la licencia [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International](LICENSE).

**Puedes:** clonar, modificar y distribuir con atribución, para uso no comercial, bajo la misma licencia.
**No puedes:** usar con fines comerciales ni redistribuir sin atribución.

---

## Créditos

- **Fi3w0** — Diseño, lore, mecánicas y dirección del proyecto
- **Claude Sonnet 4.6** (Anthropic) — Asistencia en implementación del código
- **Comunidad Fabric** — Documentación y herramientas
- **Autores de texturas** — Recursos visuales

---

## Enlaces

- **GitHub:** [Fi3w0/FiwStoryMod](https://github.com/Fi3w0/FiwStoryMod)
- **GitLab:** [fi3w0/FiwStoryMod](https://gitlab.com/fi3w0/FiwStoryMod)
- **Issues:** [Reportar un bug](https://github.com/Fi3w0/FiwStoryMod/issues)
