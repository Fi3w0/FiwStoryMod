# Dockerfile para probar Fiw Story Mod con dimensión Timeless Void
FROM itzg/minecraft-server:java17

# Configurar memoria (2GB como solicitado)
ENV MEMORY=2G
ENV TYPE=FABRIC
ENV VERSION=1.20.1
ENV FABRIC_LOADER_VERSION=0.18.3

# Configuración para testing
ENV MODE=creative
ENV DIFFICULTY=peaceful
ENV SPAWN_PROTECTION=0
ENV ENABLE_COMMAND_BLOCK=true
ENV OP_PERMISSION_LEVEL=4
ENV EULA=TRUE

# Puerto de Minecraft
EXPOSE 25565

# Copiar nuestro mod compilado (versión 1.2.6 con dimensión Timeless Void)
COPY build/libs/fiwstory-1.2.6.jar /data/mods/

# Health check
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD mc-health