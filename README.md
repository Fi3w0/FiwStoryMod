# Fiw Story Mod

[![Minecraft](https://img.shields.io/badge/Minecraft-1.20.1-green.svg)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-0.92.7-blue.svg)](https://fabricmc.net/)
[![Mod Version](https://img.shields.io/badge/Version-1.3.33-orange.svg)](https://github.com/Fi3w0/FiwStoryMod/releases)
[![License](https://img.shields.io/badge/License-All%20Rights%20Reserved-red.svg)](LICENSE)

**Fiw Story** is a comprehensive Minecraft mod that introduces a deep progression system centered around corruption, divine artifacts, and powerful abilities. Journey through a world of gods, ancient powers, and mysterious artifacts as you balance corruption and purity.

## ✨ Features

### 🎮 Core Systems

#### **Corruption System**
- **5 Corruption Levels** with progressive effects
- **Corruption Sources**: Artifacts, combat, exploration
- **Purification Mechanics**: Crystals, rituals, items
- **Immunity System**: Temporary protection from corruption
- **Visual Indicators**: Particle effects, HUD elements

#### **Artifact Collection**
- **Legendary Items**: Each with unique lore and abilities
- **Soulbound System**: Keep artifacts through death
- **Progressive Unlocking**: Discover artifacts through gameplay
- **Custom Models & Textures**: High-quality 3D models

### ⚔️ Combat & Abilities

#### **Cursed Spear of Fi3w0**
- **Dash Riptide**: 1.5s cooldown dash ability
  - Click right to dash in look direction
  - Cooldown reduction on enemy hits (-0.5s)
  - Corruption boost: +0.6 strength, +3 blocks range at 100% corruption
  - Visual trail effects during dash

- **World Barrage**: 10s cooldown ultimate ability
  - Shift + Right Click for 12 rotating slashes
  - Teleportation to target location
  - PvP balanced: Reduced damage against players
  - Kill rewards: Regeneration II, Strength II, Speed I
  - Requires Fi3w0 Glasses to activate

#### **Fi3w0 Glasses**
- **Custom 3D Model**: Wearable artifact
- **Ability Enabler**: Required for World Barrage
- **Visual Effects**: Special rendering when equipped

### 🏰 Dimension & Exploration

#### **Timeless Void Dimension**
- **Custom Dimension**: Infinite void with floating islands
- **Void Teleportation**: `/void` command access
- **Particle Effects**: Custom void particles
- **Safe Environment**: No mobs, peaceful exploration

### 🛡️ Artifact Arsenal

#### **Egyptian Artifacts**
- **Pharaoh's Dagger**: Ancient ceremonial blade
- **Scarab Artifact**: Symbol of rebirth and protection
- **Pharaoh's Ring**: Royal authority and power

#### **Divine & Chaotic Artifacts**
- **Fallen God's Heart**: Divine power source
- **Chaos Gem**: Unstable chaotic energy
- **Blood Gem**: Life force manipulation
- **Espada del Caos**: Sword of chaos and destruction

#### **Utility Artifacts**
- **Philosopher's Stone**: Alchemical transformation
- **Temporal Structure**: Time manipulation
- **Healing Rune**: Restoration magic
- **Pure Crystal**: Corruption purification

## 🚀 Installation

### Requirements
- **Minecraft**: 1.20.1
- **Fabric Loader**: 0.18.3+
- **Fabric API**: 0.92.7+
- **Java**: 21

### Installation Steps
1. Install [Fabric Loader](https://fabricmc.net/use/) for Minecraft 1.20.1
2. Download [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api)
3. Download the latest Fiw Story mod from [Releases](https://github.com/Fi3w0/FiwStoryMod/releases)
4. Place the `.jar` file in your `mods` folder
5. Launch Minecraft with Fabric profile

## 📖 Usage & Commands

### In-Game Commands
- `/corruption [player]` - Check corruption level
- `/immunity [player] [duration]` - Grant corruption immunity
- `/void` - Teleport to Timeless Void dimension
- `/bind` - Soulbind current held item

### Gameplay Tips
1. **Start Slow**: Begin with low-corruption artifacts
2. **Balance Corruption**: Use purification items regularly
3. **Master Abilities**: Practice dash and barrage timing
4. **Explore Dimensions**: Discover hidden artifacts in the Timeless Void
5. **PvP Strategy**: Use reduced-damage abilities against players

## 🛠️ Development

### Building from Source
```bash
# Clone the repository
git clone https://github.com/Fi3w0/FiwStoryMod.git

# Build the mod
./gradlew build

# Output will be in build/libs/
```

### Project Structure
```
src/main/java/com/fiw/fiwstory/
├── item/              # Custom items and artifacts
├── event/             # Event handlers
├── data/              # Persistent data systems
├── effect/            # Status effects
├── command/           # Custom commands
├── dimension/         # Custom dimensions
├── client/            # Client-side rendering
└── lib/               # Utility libraries
```

### Dependencies
- **Fabric Loom**: 1.5-SNAPSHOT
- **Fabric API**: 0.92.7+1.20.1
- **Yarn Mappings**: 1.20.1+build.10

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Code Style
- Follow existing code conventions
- Add comments for complex logic
- Include JavaDoc for public methods
- Test changes thoroughly

## 📄 License

All Rights Reserved. See [LICENSE](LICENSE) for details.

## 🙏 Credits

### Development
- **Fi3w0** - Project Lead & Main Developer
- **DeepSeek V3.2 API with OpenCode Agents** - AI-assisted development and optimization

### Special Thanks
- Minecraft Fabric community for excellent documentation
- All testers and contributors
- The modding community for inspiration

## 🔗 Links

- **GitHub**: [https://github.com/Fi3w0/FiwStoryMod](https://github.com/Fi3w0/FiwStoryMod)
- **Issues**: [Report Bugs](https://github.com/Fi3w0/FiwStoryMod/issues)
- **Discussions**: [Community Forum](https://github.com/Fi3w0/FiwStoryMod/discussions)

## 📊 Version History

### v1.3.33 (Current)
- Fixed cursed spear texture JSON corruption
- Restored original texture from v1.3.26
- Maintained model scale and position adjustments
- Improved ability cooldown messaging

### v1.3.0+
- Added World Barrage ability system
- Implemented Fi3w0 Glasses requirement
- Added PvP balance adjustments
- Enhanced visual effects

### v1.0.0 - v1.2.0
- Core corruption system implementation
- Basic artifact collection
- Soulbound system
- Dimension creation
- Command framework

---

**Enjoy your journey through the world of Fiw Story! May your corruption be balanced and your artifacts powerful.**