package com.fiw.fiwstory.item.custom;

import com.fiw.fiwstory.effect.CorruptionStatusEffect;
import com.fiw.fiwstory.item.BaseArtifactItem;
import com.fiw.fiwstory.item.BaseArtifactSwordItem;
import com.fiw.fiwstory.item.ModItems;
import com.fiw.fiwstory.lib.FiwEffects;
import com.fiw.fiwstory.lib.FiwNBT;
import com.fiw.fiwstory.lib.FiwUtils;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import com.google.common.collect.Multimap;

public class CursedSpearOfFi3w0 extends BaseArtifactSwordItem {
	
	private static final int COOLDOWN_MS = 20000; // 20 segundos para habilidad especial
	private static final int CHARGE_TIME = 60; // 3 segundos para carga máxima (60 ticks)
	private static final int RIPTIDE_COOLDOWN_MS = 1500; // 1.5 segundos para Riptide
	private static final int WORLD_BARRAGE_COOLDOWN_MS = 10000; // 10 segundos para World Barrage
	private static final int ERROR_MESSAGE_COOLDOWN_MS = 5000; // 5 segundos para mensajes de error
	private static final String SHARED_COOLDOWN_TYPE = "spear_ability"; // Cooldown compartido
	private static final String ERROR_COOLDOWN_TYPE = "error_message"; // Cooldown para mensajes de error
	
	public CursedSpearOfFi3w0(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
		super(toolMaterial, attackDamage, attackSpeed, 
			  BaseArtifactItem.ArtifactType.WEAPON,
			  BaseArtifactItem.ArtifactRarity.LEGENDARY,
			  5, // Tasa de corrupción por uso
			  0, // Usos infinitos
			  settings.maxDamage(4042));
	}
	

	
	// ========== MÉTODOS DE ARTEFACTO ==========
	
	public String getArtifactDisplayName() {
		return "Lanza Devora Almas";
	}
	
	public String getArtifactDescription() {
		return "Forjada en el Vacío Atemporal";
	}
	
	public List<String> getArtifactFeatures() {
		return Arrays.asList(
			"Devora almas divinas",
			"Paradoja temporal encarnada", 
			"Control sobre el End",
			"Habilidad: Devoración"
		);
	}
	
	public String getArtifactQuote() {
		return "El universo siempre aspira al orden";
	}
	
	public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
		if (!world.isClient()) {
			// Efectos sin partículas de poción
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 200, 1, false, false));
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 200, 0, false, false));
			
			// Cooldown del sistema de artefactos
			FiwNBT.setCooldown(stack, "ability", COOLDOWN_MS);
			
			// Cooldown de Minecraft (para compatibilidad)
			player.getItemCooldownManager().set(this, 400);
			
			// Efectos visuales
			FiwEffects.spawnExplosionParticles(world, player.getPos(), 
				net.minecraft.particle.ParticleTypes.SOUL_FIRE_FLAME, 25, 2.0);
			FiwEffects.playSoundAtEntity(player, net.minecraft.sound.SoundEvents.ENTITY_WITHER_SHOOT, 
				0.8f, 0.5f);
		}
	}
	
	// ========== SISTEMA DE ATAQUES CARGADOS ==========
	
	@Override
	public int getMaxUseTime(ItemStack stack) {
		return CHARGE_TIME; // 3 segundos máximo
	}
	
	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.SPEAR; // Animación de lanza
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		
		// Verificar cooldown compartido
		if (FiwNBT.isOnCooldown(stack, SHARED_COOLDOWN_TYPE)) {
			long remaining = FiwNBT.getCooldownRemaining(stack, SHARED_COOLDOWN_TYPE);
			sendErrorMessageWithCooldown(player, stack, "§cHabilidad en cooldown: " + (remaining / 1000.0) + "s§r");
			return TypedActionResult.fail(stack);
		}
		
		// Verificar si se presiona Shift + Click derecho para World Barrage (con gafas)
		if (player.isSneaking()) {
			if (hasFi3w0Glasses(player)) {
				boolean activated = activateWorldBarrage(player, stack);
				if (activated) {
					return TypedActionResult.success(stack);
				} else {
					return TypedActionResult.fail(stack);
				}
			} else {
				sendErrorMessageWithCooldown(player, stack, "§cRequieres las gafas de Fi3w0 para World Barrage§r");
				return TypedActionResult.fail(stack);
			}
		}
		
		// Click derecho normal = Dash estilo Riptide
		boolean dashActivated = activateRiptideDash(player, stack);
		if (dashActivated) {
			return TypedActionResult.success(stack);
		}
		
		return TypedActionResult.fail(stack);
	}
	
	@Override
	public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingTicks) {
		if (!(user instanceof PlayerEntity player)) return;
		
		int chargeTime = this.getMaxUseTime(stack) - remainingTicks;
		float chargePercent = chargeTime / (float) CHARGE_TIME;
		
		// Efectos visuales de carga
		if (!world.isClient()) {
			FiwEffects.spawnChargeParticles(player, chargePercent);
		}
		
		// Determinar nivel de ataque cargado
		if (chargePercent > 0.66f) {
			// Nivel 3: Ataque definitivo (+100% daño, área)
			performUltimateAttack(player, stack, chargePercent);
		} else if (chargePercent > 0.33f) {
			// Nivel 2: Ataque cargado (+50% daño, knockback)
			performChargedAttack(player, stack, chargePercent, false);
		}
		// Nivel 1: Ataque normal (no hacer nada especial)
	}
	
	@Override
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		if (!(user instanceof PlayerEntity player)) return;
		
		int chargeTime = this.getMaxUseTime(stack) - remainingUseTicks;
		float chargePercent = chargeTime / (float) CHARGE_TIME;
		
		// Efectos visuales durante la carga
		if (!world.isClient() && world.getTime() % 5 == 0) {
			FiwEffects.spawnChargeParticles(player, chargePercent);
		}
		
		// Actualizar nivel de carga en NBT
		FiwNBT.setFloat(stack, FiwNBT.CHARGE_LEVEL, chargePercent);
	}
	
	// ========== ATAQUES CARGADOS ==========
	
	private void performChargedAttack(PlayerEntity player, ItemStack stack, float chargePercent, boolean isUltimate) {
		World world = player.getWorld();
		
		// Calcular daño base con buffos
		float baseDamage = 10.0f; // Daño base de la lanza
		float totalDamage = calculateTotalDamage(player, stack, baseDamage, chargePercent, isUltimate);
		
		// Verificar si tiene gafas equipadas
		boolean hasGlasses = hasFi3w0Glasses(player);
		
		// Efectos visuales
		if (!world.isClient()) {
			if (isUltimate) {
				FiwEffects.spawnUltimateAttackEffect(player, hasGlasses);
			} else {
				FiwEffects.spawnChargedAttackEffect(player, chargePercent, hasGlasses);
			}
		}
		
		// Aplicar daño a entidades en frente
		applyDamageInCone(player, totalDamage, isUltimate ? 4.0 : 3.0, isUltimate, hasGlasses);
		
		// Cooldown después del ataque
		if (isUltimate) {
			player.getItemCooldownManager().set(this, 40); // 2 segundos
		}
		
		// Incrementar kills si mató algo (para buffos)
		// Esto se maneja en el evento de daño
	}
	
	private void performUltimateAttack(PlayerEntity player, ItemStack stack, float chargePercent) {
		performChargedAttack(player, stack, chargePercent, true);
		
		// Efectos adicionales para ataque definitivo
		World world = player.getWorld();
		
		// Empuje masivo
		Vec3d look = player.getRotationVec(1.0f);
		player.addVelocity(look.x * 2.0, 0.5, look.z * 2.0);
		
		// Buffo temporal después del ataque
		if (!world.isClient()) {
			player.addStatusEffect(new StatusEffectInstance(
				StatusEffects.RESISTANCE, 100, 1, false, false
			));
		}
	}
	
	// ========== CÁLCULO DE DAÑO ==========
	
	private float calculateTotalDamage(PlayerEntity player, ItemStack stack, float baseDamage, 
									  float chargeMultiplier, boolean isUltimate) {
		// Multiplicador por carga
		float chargeBonus = isUltimate ? 2.0f : 1.5f; // +100% o +50%
		
		// Buffos del sistema NBT
		float corruptionBuff = FiwNBT.getCorruptionDamageBuff(stack);
		float killBuff = FiwNBT.getKillDamageBuff(stack);
		
		// Buffo por End
		float endBuff = player.getWorld().getRegistryKey() == World.END ? 0.15f : 0.0f;
		
		// Buffo por gafas
		float glassesBuff = hasFi3w0Glasses(player) ? 0.25f : 0.0f;
		
		// Cálculo final
		float totalMultiplier = chargeBonus * (1.0f + corruptionBuff + killBuff + endBuff + glassesBuff);
		return baseDamage * totalMultiplier;
	}
	
	private void applyDamageInCone(PlayerEntity player, float damage, double range, 
								  boolean isUltimate, boolean hasGlasses) {
		World world = player.getWorld();
		Vec3d playerPos = player.getPos();
		Vec3d look = player.getRotationVec(1.0f);
		
		// Ajustar rango con gafas
		double actualRange = hasGlasses ? range * 1.5 : range;
		
		// Área de efecto más grande para ataque definitivo
		double area = isUltimate ? 2.0 : 1.0;
		
		// Buscar entidades en cono
		List<LivingEntity> entities = world.getEntitiesByClass(
			LivingEntity.class,
			new Box(
				playerPos.x - actualRange, playerPos.y - 2, playerPos.z - actualRange,
				playerPos.x + actualRange, playerPos.y + 3, playerPos.z + actualRange
			),
			entity -> entity != player && entity.isAlive()
		);
		
		for (LivingEntity entity : entities) {
			Vec3d toEntity = entity.getPos().subtract(playerPos).normalize();
			double dot = look.dotProduct(toEntity);
			
			// Verificar si está dentro del cono (45 grados)
			if (dot > 0.7) { // ~45 grados
				double distance = entity.getPos().distanceTo(playerPos);
				if (distance <= actualRange) {
					// Aplicar daño
					entity.damage(player.getDamageSources().playerAttack(player), damage);
					
					// Empuje
					Vec3d knockback = toEntity.multiply(isUltimate ? 2.0 : 1.0);
					entity.addVelocity(knockback.x, 0.5, knockback.z);
					
					// Efectos adicionales para ataque definitivo
					if (isUltimate && !world.isClient()) {
						entity.addStatusEffect(new StatusEffectInstance(
							StatusEffects.WITHER, 100, 1, false, false
						));
					}
				}
			}
		}
	}
	
	// ========== SISTEMA DE BUFFOS EN TIEMPO REAL ==========
	
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		
		if (!world.isClient() && entity instanceof PlayerEntity player && selected) {
			// 1. Actualizar todos los buffos
			FiwNBT.updateAllBuffs(stack, player);
			
			// 2. Aplicar buffos por corrupción
			int corruptionLevel = CorruptionStatusEffect.getPlayerCorruptionLevel(player);
			updateCorruptionBuffs(stack, corruptionLevel);
			
			// 3. Aplicar buffos por dimensión (End)
			updateDimensionBuffs(stack, world.getRegistryKey());
			
			// 4. Efectos visuales para buffos activos
			spawnBuffParticles(player, stack);
			
			// 5. Efectos corruptos si no tiene inmunidad
			if (!hasImmunity(player)) {
				int itemCorruption = FiwNBT.getInt(stack, FiwNBT.CORRUPTION_LEVEL, 0);
				FiwEffects.applyCorruptionEffects(player, itemCorruption / 20); // Escalar a nivel 0-5
				
				// Partículas mientras se sostiene
				if (world.getTime() % 10 == 0) {
					FiwEffects.spawnParticlesAroundEntity(player, 
						net.minecraft.particle.ParticleTypes.SMOKE, 3, 1.0);
				}
			}
		}
	}
	
	private void updateCorruptionBuffs(ItemStack stack, int corruptionLevel) {
		if (corruptionLevel <= 0) {
			FiwNBT.setCorruptionDamageBuff(stack, 0.0f);
			FiwNBT.setCorruptionSpeedBuff(stack, 0.0f);
			return;
		}
		
		// Escalado con nivel de corrupción
		float damageBuff = 0.0f;
		float speedBuff = 0.0f;
		
		if (corruptionLevel >= 1) {
			damageBuff = 0.10f; // +10%
			speedBuff = 0.05f;  // +5%
		}
		if (corruptionLevel >= 3) {
			damageBuff = 0.20f; // +20%
			speedBuff = 0.10f;  // +10%
		}
		if (corruptionLevel >= 5) {
			damageBuff = 0.30f; // +30%
			speedBuff = 0.15f;  // +15%
		}
		
		FiwNBT.setCorruptionDamageBuff(stack, damageBuff);
		FiwNBT.setCorruptionSpeedBuff(stack, speedBuff);
	}
	
	private void updateDimensionBuffs(ItemStack stack, net.minecraft.registry.RegistryKey<World> dimension) {
		float endBuff = (dimension == World.END) ? 0.15f : 0.0f;
		FiwNBT.setEndDamageBuff(stack, endBuff);
	}
	
	private void spawnBuffParticles(PlayerEntity player, ItemStack stack) {
		// Partículas por corrupción
		float corruptionBuff = FiwNBT.getCorruptionDamageBuff(stack);
		if (corruptionBuff > 0) {
			FiwEffects.spawnBuffParticles(player, "corruption", corruptionBuff * 3);
		}
		
		// Partículas por kills
		float killBuff = FiwNBT.getKillDamageBuff(stack);
		if (killBuff > 0) {
			FiwEffects.spawnBuffParticles(player, "kill", killBuff * 4);
		}
		
		// Partículas por End
		float endBuff = FiwNBT.getEndDamageBuff(stack);
		if (endBuff > 0) {
			FiwEffects.spawnBuffParticles(player, "end", 1.0f);
		}
	}
	
	// ========== UTILIDADES ==========
	
	private boolean hasFi3w0Glasses(PlayerEntity player) {
		return player.getInventory().armor.get(3).getItem() == ModItems.FI3W0_GLASSES;
	}
	
	protected boolean hasImmunity(PlayerEntity player) {
		if (player.getServer() != null) {
			com.fiw.fiwstory.data.ImmunityData data = com.fiw.fiwstory.data.ImmunityData.getServerState(player.getServer());
			return data.isPlayerImmune(player.getUuid());
		}
		return false;
	}
	
	// ========== MÉTODOS DE SWORDITEM OVERRIDES ==========
	
	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		if (attacker instanceof PlayerEntity player) {
			// Verificar si se mató a un jugador
			if (!target.isAlive() && target instanceof PlayerEntity) {
				// Incrementar kills recientes para buffos
				FiwNBT.incrementRecentKills(stack);
				
				// Aplicar buffos por matar jugador
				applyPlayerKillBuffos(player);
			}
			
			// Aplicar corrupción por uso
			FiwNBT.incrementUses(stack);
			FiwNBT.setInt(stack, FiwNBT.CORRUPTION_LEVEL, 
				Math.min(100, FiwNBT.getInt(stack, FiwNBT.CORRUPTION_LEVEL, 0) + getBaseCorruptionRate()));
			
			// Reducir cooldown del Riptide al golpear
			reduceRiptideCooldown(player, stack);
		}
		return super.postHit(stack, target, attacker);
	}
	
	/**
	 * Envía un mensaje de error con cooldown de 5 segundos para evitar spam.
	 */
	private void sendErrorMessageWithCooldown(PlayerEntity player, ItemStack stack, String message) {
		// Verificar cooldown de mensajes de error
		if (FiwNBT.isOnCooldown(stack, ERROR_COOLDOWN_TYPE)) {
			return; // No enviar mensaje si está en cooldown
		}
		
		// Enviar mensaje
		player.sendMessage(Text.literal(message), false);
		
		// Aplicar cooldown de 5 segundos
		FiwNBT.setCooldown(stack, ERROR_COOLDOWN_TYPE, ERROR_MESSAGE_COOLDOWN_MS);
	}
	
	/**
	 * Crea un trail visual durante el dash Riptide.
	 */
	private void spawnRiptideTrail(PlayerEntity player, Vec3d direction, int corruptionLevel) {
		World world = player.getWorld();
		Vec3d playerPos = player.getPos();
		
		// Color del trail basado en corrupción
		net.minecraft.particle.ParticleEffect trailParticle = corruptionLevel >= 100 
			? net.minecraft.particle.ParticleTypes.SOUL_FIRE_FLAME // Rojo/negro al 100% corrupción
			: net.minecraft.particle.ParticleTypes.DRAGON_BREATH; // Púrpura normal
		
		// Crear trail detrás del jugador
		Vec3d trailStart = playerPos.subtract(direction.multiply(1.5));
		
		// Spawnear partículas en línea
		for (int i = 0; i < 8; i++) {
			double progress = i / 7.0;
			Vec3d trailPos = trailStart.add(direction.multiply(progress * 3.0));
			
			// Partículas principales del trail
			FiwEffects.spawnParticlesAtPosition(world, trailPos, 
				trailParticle, 3, 0.2);
			
			// Partículas de chispas
			FiwEffects.spawnParticlesAtPosition(world, trailPos, 
				net.minecraft.particle.ParticleTypes.ELECTRIC_SPARK, 1, 0.1);
			
			// Partículas de humo en los extremos
			if (i == 0 || i == 7) {
				FiwEffects.spawnParticlesAtPosition(world, trailPos, 
					net.minecraft.particle.ParticleTypes.SMOKE, 2, 0.15);
			}
		}
		
		// Efecto de estela continua (se ejecutará por unos ticks)
		// En una implementación completa, usaríamos un sistema de ticks
	}
	
	/**
	 * Obtiene la dirección del dash (siempre dirección de mirada).
	 */
	private Vec3d getDashDirection(PlayerEntity player) {
		// Siempre dirección de mirada del jugador
		return player.getRotationVec(1.0f);
	}
	
	/**
	 * Reduce el cooldown del Riptide al golpear enemigos.
	 */
	private void reduceRiptideCooldown(PlayerEntity player, ItemStack stack) {
		long currentCooldown = FiwNBT.getCooldownRemaining(stack, SHARED_COOLDOWN_TYPE);
		if (currentCooldown > 0) {
			// Reducir 0.5 segundos (500ms)
			long newCooldown = Math.max(0, currentCooldown - 500);
			
			// Actualizar cooldown en NBT
			if (newCooldown > 0) {
				FiwNBT.setLong(stack, FiwNBT.COOLDOWN_END + "_" + SHARED_COOLDOWN_TYPE, 
					System.currentTimeMillis() + newCooldown);
			} else {
				// Si el cooldown llegó a 0, limpiarlo
				FiwNBT.setLong(stack, FiwNBT.COOLDOWN_END + "_" + SHARED_COOLDOWN_TYPE, 0);
			}
			
			// Actualizar cooldown de Minecraft
			int ticksRemaining = (int) Math.ceil(newCooldown / 50.0);
			player.getItemCooldownManager().set(this, ticksRemaining);
			
			// Efecto visual de reducción de cooldown
			FiwEffects.spawnParticlesAtPosition(player.getWorld(), player.getPos(),
				net.minecraft.particle.ParticleTypes.HAPPY_VILLAGER, 3, 0.1);
		}
	}
	
	/**
	 * Aplica buffos por matar a un jugador con la lanza.
	 */
	private void applyPlayerKillBuffos(PlayerEntity player) {
		// Regeneración II por 2 minutos (2400 ticks)
		player.addStatusEffect(new StatusEffectInstance(
			StatusEffects.REGENERATION, 2400, 1, false, true
		));
		
		// Fuerza II por 5 minutos (6000 ticks)
		player.addStatusEffect(new StatusEffectInstance(
			StatusEffects.STRENGTH, 6000, 1, false, true
		));
		
		// Velocidad I por 10 minutos (12000 ticks)
		player.addStatusEffect(new StatusEffectInstance(
			StatusEffects.SPEED, 12000, 0, false, true
		));
		
		// Mensaje de feedback
		player.sendMessage(Text.literal("§d§l¡Jugador eliminado!§r §aBuffos aplicados:§r"), false);
		player.sendMessage(Text.literal("§a• Regeneración II (2 minutos)§r"), false);
		player.sendMessage(Text.literal("§a• Fuerza II (5 minutos)§r"), false);
		player.sendMessage(Text.literal("§a• Velocidad I (10 minutos)§r"), false);
		
		// Efectos visuales
		FiwEffects.spawnParticlesAroundEntity(player, 
			net.minecraft.particle.ParticleTypes.TOTEM_OF_UNDYING, 50, 2.0);
		FiwEffects.playSoundAtEntity(player, 
			net.minecraft.sound.SoundEvents.ENTITY_PLAYER_LEVELUP, 1.0f, 0.8f);
	}
	
	@Override
	public boolean isEnchantable(ItemStack stack) {
		return true;
	}
	
	@Override
	public int getEnchantability() {
		return 15;
	}
	
	@Override
	public boolean canRepair(ItemStack stack, ItemStack ingredient) {
		return ingredient.getItem() == ModItems.CORRUPTED_CRYSTAL;
	}
	
	// ========== TOOLTIP PERSONALIZADO (LORE ONLY) ==========
	
	/**
	 * Activa el dash estilo Riptide (nivel 1, sin necesidad de agua).
	 */
	public boolean activateRiptideDash(PlayerEntity player, ItemStack stack) {
		World world = player.getWorld();
		
		// Verificar cooldown compartido (ya se verificó en use(), pero por seguridad)
		if (FiwNBT.isOnCooldown(stack, SHARED_COOLDOWN_TYPE)) {
			return false; // Ya se mostró mensaje en use()
		}
		
		// Calcular dirección del dash (multidireccional XYZ)
		Vec3d direction = getDashDirection(player).normalize();
		
		// Fuerza base del dash (similar a Riptide nivel 1, pero sin necesidad de agua/lluvia)
		double baseStrength = 1.8;
		
		// Verificar nivel de corrupción para boost
		int corruptionLevel = FiwNBT.getInt(stack, FiwNBT.CORRUPTION_LEVEL, 0);
		double corruptionBoost = corruptionLevel >= 100 ? 0.6 : 0.0; // +0.6 de fuerza al 100% corrupción
		
		double strength = baseStrength + corruptionBoost;
		
		// Aplicar impulso en la dirección deseada
		player.addVelocity(
			direction.x * strength,
			Math.min(direction.y * strength + 0.3, 0.8), // Limitado hacia arriba
			direction.z * strength
		);
		
		// Prevenir daño por caída durante el dash
		player.fallDistance = 0;
		
		// Efectos visuales y sonoros
		if (!world.isClient()) {
			// Trail visual durante el dash
			spawnRiptideTrail(player, direction, corruptionLevel);
			
			// Partículas de tridente (como Riptide)
			FiwEffects.spawnParticlesAroundEntity(player, 
				net.minecraft.particle.ParticleTypes.CLOUD, 25, 1.5);
			
			// Partículas de burbujas (efecto acuático)
			FiwEffects.spawnParticlesAroundEntity(player, 
				net.minecraft.particle.ParticleTypes.BUBBLE, 15, 1.0);
			
			// Sonido de tridente
			FiwEffects.playSoundAtEntity(player, 
				net.minecraft.sound.SoundEvents.ITEM_TRIDENT_RIPTIDE_1, 1.0f, 1.2f);
			
			// Efecto de velocidad durante el dash
			player.addStatusEffect(new StatusEffectInstance(
				StatusEffects.SPEED, 30, 1, false, false // 1.5 segundos de velocidad II
			));
			
			// Aplicar daño por colisión a entidades en el camino
			applyDashCollisionDamage(player, stack);
		}
		
		// Aplicar cooldown compartido (1.5 segundos para Riptide)
		FiwNBT.setCooldown(stack, SHARED_COOLDOWN_TYPE, RIPTIDE_COOLDOWN_MS);
		player.getItemCooldownManager().set(this, 30); // 1.5 segundos en ticks (20 ticks = 1 segundo)
		
		return true;
	}
	
	/**
	 * Aplica daño a entidades en el camino del dash.
	 */
	private void applyDashCollisionDamage(PlayerEntity player, ItemStack stack) {
		World world = player.getWorld();
		Vec3d playerPos = player.getPos();
		Vec3d look = player.getRotationVec(1.0f);
		
		// Verificar nivel de corrupción para boost
		int corruptionLevel = FiwNBT.getInt(stack, FiwNBT.CORRUPTION_LEVEL, 0);
		
		// Área de efecto del dash (cono adelante)
		double baseRange = 4.0;
		double corruptionRangeBoost = corruptionLevel >= 100 ? 3.0 : 0.0; // +3 bloques al 100% corrupción
		
		double range = baseRange + corruptionRangeBoost;
		Box dashBox = new Box(
			playerPos.x - range, playerPos.y - 1, playerPos.z - range,
			playerPos.x + range, playerPos.y + 2, playerPos.z + range
		);
		
		List<LivingEntity> entities = world.getEntitiesByClass(
			LivingEntity.class,
			dashBox,
			entity -> entity != player && entity.isAlive()
		);
		
		for (LivingEntity entity : entities) {
			Vec3d toEntity = entity.getPos().subtract(playerPos).normalize();
			double dot = look.dotProduct(toEntity);
			
			// Verificar si está en la dirección del dash
			if (dot > 0.5) {
				// Daño del dash (balanceado para PvP)
				float damage = entity instanceof PlayerEntity ? 3.0f : 5.0f;
				boolean wasAlive = entity.isAlive();
				entity.damage(player.getDamageSources().playerAttack(player), damage);
				
				// Verificar si se mató con el dash
				if (!entity.isAlive() && wasAlive && entity instanceof PlayerEntity) {
					applyPlayerKillBuffos(player);
				}
				
				// Reducir cooldown del Riptide al golpear (0.5 segundos)
				reduceRiptideCooldown(player, stack);
				
				// Knockback
				Vec3d knockback = toEntity.multiply(0.5).add(0, 0.3, 0);
				entity.addVelocity(knockback.x, knockback.y, knockback.z);
				
				// Efectos visuales de hit
				FiwEffects.spawnParticlesAtPosition(world, entity.getPos(), 
					net.minecraft.particle.ParticleTypes.CRIT, 10, 0.2);
			}
		}
	}
	
	/**
	 * Activa la habilidad World Barrage (requiere gafas de Fi3w0 equipadas).
	 */
	public boolean activateWorldBarrage(PlayerEntity player, ItemStack stack) {
		World world = player.getWorld();
		
		// Verificar cooldown compartido (ya se verificó en use(), pero por seguridad)
		if (FiwNBT.isOnCooldown(stack, SHARED_COOLDOWN_TYPE)) {
			return false; // Ya se mostró mensaje en use()
		}
		
		// Verificar gafas equipadas
		if (!hasFi3w0Glasses(player)) {
			sendErrorMessageWithCooldown(player, stack, "§cRequieres las gafas de Fi3w0 equipadas§r");
			return false;
		}
		
		// Buscar objetivo
		LivingEntity target = findTargetForWorldBarrage(player);
		if (target == null) {
			sendErrorMessageWithCooldown(player, stack, "§cNo hay objetivo válido§r");
			return false;
		}
		
		// Ejecutar World Barrage
		FiwEffects.executeWorldBarrage(player, target);
		
		// Aplicar cooldown compartido (10 segundos para World Barrage)
		FiwNBT.setCooldown(stack, SHARED_COOLDOWN_TYPE, WORLD_BARRAGE_COOLDOWN_MS);
		player.getItemCooldownManager().set(this, 200); // 10 segundos en ticks de Minecraft
		
		// Efectos visuales iniciales
		if (!world.isClient()) {
			FiwEffects.spawnExplosionParticles(world, player.getPos(), 
				net.minecraft.particle.ParticleTypes.SOUL_FIRE_FLAME, 30, 3.0);
			FiwEffects.playSoundAtEntity(player, net.minecraft.sound.SoundEvents.ENTITY_WITHER_SPAWN, 
				1.5f, 0.3f);
		}
		
		return true;
	}
	
	/**
	 * Encuentra un objetivo válido para World Barrage.
	 * Ahora funciona con jugadores también.
	 */
	private LivingEntity findTargetForWorldBarrage(PlayerEntity player) {
		World world = player.getWorld();
		Vec3d playerPos = player.getPos();
		Vec3d look = player.getRotationVec(1.0f);
		
		// Buscar entidades en un cono frente al jugador (incluyendo jugadores)
		List<LivingEntity> entities = world.getEntitiesByClass(
			LivingEntity.class,
			new Box(
				playerPos.x - 15, playerPos.y - 5, playerPos.z - 15,
				playerPos.x + 15, playerPos.y + 5, playerPos.z + 15
			),
			entity -> entity != player && entity.isAlive()
		);
		
		// Encontrar el objetivo más cercano en la dirección de mirada
		LivingEntity bestTarget = null;
		double bestDot = 0.7; // Mínimo 45 grados de alineación
		
		for (LivingEntity entity : entities) {
			Vec3d toEntity = entity.getPos().subtract(playerPos).normalize();
			double dot = look.dotProduct(toEntity);
			
			if (dot > bestDot) {
				bestDot = dot;
				bestTarget = entity;
			}
		}
		
		return bestTarget;
	}
	
	@Override
	public Text getName(ItemStack stack) {
		// Nombre morado en menú creativo/inventario
		return Text.translatable(this.getTranslationKey(stack)).formatted(Formatting.DARK_PURPLE);
	}
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		// Solo lore, sin estadísticas detalladas
		tooltip.add(FiwUtils.formatArtifactName(getArtifactDisplayName()));
		tooltip.add(FiwUtils.formatArtifactDescription(getArtifactDescription()));
		tooltip.add(Text.literal(""));
		
		tooltip.add(FiwUtils.formatArtifactType(
			getArtifactType().getDisplayName() + " • " + getArtifactRarity().getDisplayName()));
		
		for (String feature : getArtifactFeatures()) {
			tooltip.add(FiwUtils.formatArtifactFeature(feature));
		}
		
		tooltip.add(Text.literal(""));
		
		// Información de bind (solo si está vinculado)
		if (FiwNBT.isBound(stack)) {
			tooltip.add(Text.literal("§6§oVinculado a su dueño§r").formatted(Formatting.GOLD, Formatting.ITALIC));
			tooltip.add(Text.literal(""));
		}
		
		// Información de corrupción (sutil)
		int corruptionLevel = FiwNBT.getInt(stack, FiwNBT.CORRUPTION_LEVEL, 0);
		if (corruptionLevel > 0) {
			String corruptionText = "Corrupción: " + corruptionLevel + "%";
			Formatting corruptionColor = corruptionLevel < 30 ? Formatting.YELLOW : 
										corruptionLevel < 70 ? Formatting.RED : Formatting.DARK_RED;
			tooltip.add(Text.literal("§7" + corruptionText + "§r").formatted(corruptionColor));
			tooltip.add(Text.literal(""));
		}
		
		// Cita misteriosa
		tooltip.add(FiwUtils.formatArtifactQuote(getArtifactQuote()));
		
		// Indicador sutil de carga actual
		float chargeLevel = FiwNBT.getFloat(stack, FiwNBT.CHARGE_LEVEL, 0);
		if (chargeLevel > 0 && context.isAdvanced()) {
			tooltip.add(Text.literal("§8[Carga: " + (int)(chargeLevel * 100) + "%]§r").formatted(Formatting.DARK_GRAY));
		}
	}
	
	@Override
	public boolean hasGlint(ItemStack stack) {
		return true;
	}
}