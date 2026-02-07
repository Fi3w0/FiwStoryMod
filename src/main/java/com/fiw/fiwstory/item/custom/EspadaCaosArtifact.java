package com.fiw.fiwstory.item.custom;

import com.fiw.fiwstory.item.BaseArtifactItem;
import com.fiw.fiwstory.item.BaseArtifactSwordItem;
import com.fiw.fiwstory.lib.FiwEffects;
import com.fiw.fiwstory.lib.FiwNBT;
import com.fiw.fiwstory.lib.FiwUtils;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.Ingredient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class EspadaCaosArtifact extends BaseArtifactSwordItem {
	
	private static final int COOLDOWN_MS = 60000; // 1 minuto
	private static final int AURA_DURATION_TICKS = 60; // 3 segundos (20 ticks/segundo)
	private static final int STRENGTH_DURATION_TICKS = 200; // 10 segundos
	
	// Material personalizado para la espada con 3000 de durabilidad
	private static final ToolMaterial ESPADA_CAOS_MATERIAL = new ToolMaterial() {
		@Override
		public int getDurability() {
			return 3000;
		}
		
		@Override
		public float getMiningSpeedMultiplier() {
			return 1.5f;
		}
		
		@Override
		public float getAttackDamage() {
			return 0f; // El daño se define en el constructor de SwordItem
		}
		
		@Override
		public int getMiningLevel() {
			return 3;
		}
		
		@Override
		public int getEnchantability() {
			return 10;
		}
		
		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.ofItems(com.fiw.fiwstory.item.ModItems.CORRUPTED_CRYSTAL);
		}
	};

	public EspadaCaosArtifact(ToolMaterial toolMaterial, int attackDamage, float attackSpeed, Settings settings) {
		// Pasar los parámetros al constructor de BaseArtifactSwordItem
		super(ESPADA_CAOS_MATERIAL, attackDamage, attackSpeed, 
			  BaseArtifactItem.ArtifactType.WEAPON,
			  BaseArtifactItem.ArtifactRarity.LEGENDARY,
			  5, // Tasa de corrupción por uso
			  0, // Usos infinitos
			  settings.maxDamage(3000));
	}
	
	public String getArtifactDisplayName() {
		return "Espada del Caos";
	}
	
	public String getArtifactDescription() {
		return "Artefacto del mundo pasado - Paradoja temporal";
	}
	
	public List<String> getArtifactFeatures() {
		return Arrays.asList(
			"Sensación de no pertenecer a este mundo",
			"Energía caótica contenida",
			"Poder destructivo descontrolado"
		);
	}
	
	public String getArtifactQuote() {
		return "El caos siempre encuentra su camino";
	}
	
	@Override
	public void onArtifactUse(World world, PlayerEntity player, ItemStack stack, Hand hand) {
		if (!world.isClient()) {
			// Aplicar efectos al jugador
			player.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, STRENGTH_DURATION_TICKS, 1, false, false)); // Fuerza II
			
			// Aplicar efectos a enemigos cercanos
			List<LivingEntity> nearbyEntities = world.getEntitiesByClass(LivingEntity.class, 
				player.getBoundingBox().expand(5.0), 
				entity -> entity != player && entity.isAlive());
			
			for (LivingEntity entity : nearbyEntities) {
				// Lentitud durante 3 segundos
				entity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, AURA_DURATION_TICKS, 1, false, false));
				
				// Efecto visual de partículas rojas
				FiwEffects.spawnParticlesAroundEntity(entity, 
					net.minecraft.particle.ParticleTypes.FLAME, 10, 1.5);
			}
			
			// Cooldown del sistema de artefactos
			FiwNBT.setCooldown(stack, "ability", COOLDOWN_MS);
			
			// Cooldown de Minecraft (para compatibilidad)
			player.getItemCooldownManager().set(this, 1200); // 60 segundos en ticks
			
			// Efectos visuales para el jugador (aura roja)
			FiwEffects.spawnExplosionParticles(world, player.getPos(), 
				net.minecraft.particle.ParticleTypes.FLAME, 50, 3.0);
			FiwEffects.playSoundAtEntity(player, net.minecraft.sound.SoundEvents.ENTITY_WITHER_SHOOT, 
				1.0f, 0.7f);
			
			// Mensaje de activación
			player.sendMessage(Text.literal("§c§l¡AURA DEL CAOS ACTIVADA!§r").formatted(Formatting.RED, Formatting.BOLD), true);
		}
	}
	
	@Override
	public void inventoryTick(ItemStack stack, World world, net.minecraft.entity.Entity entity, int slot, boolean selected) {
		super.inventoryTick(stack, world, entity, slot, selected);
		
		if (!world.isClient() && entity instanceof PlayerEntity player && selected) {
			// Partículas caóticas mientras se sostiene
			if (world.getTime() % 15 == 0) { // Cada 0.75 segundos
				FiwEffects.spawnParticlesAroundEntity(player, 
					net.minecraft.particle.ParticleTypes.SOUL_FIRE_FLAME, 2, 1.0);
			}
		}
	}
	
	// ========== MÉTODOS DE SWORDITEM OVERRIDES ==========
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getStackInHand(hand);
		
		if (world.isClient()) {
			return TypedActionResult.success(stack, true);
		}
		
		// Verificar cooldown
		if (!FiwNBT.isCooldownOver(stack, "ability")) {
			long remaining = FiwNBT.getCooldownRemaining(stack, "ability");
			FiwUtils.sendErrorMessage(player, "Habilidad en cooldown: " + 
				FiwUtils.formatTimeSeconds(remaining / 1000.0));
			return TypedActionResult.fail(stack);
		}
		
		// Ejecutar habilidad
		onArtifactUse(world, player, stack, hand);
		
		// Registrar uso
		FiwNBT.incrementUses(stack);
		FiwNBT.setLong(stack, FiwNBT.LAST_USED, System.currentTimeMillis());
		
		return TypedActionResult.success(stack, false);
	}
	
	@Override
	public boolean isEnchantable(ItemStack stack) {
		return true;
	}
	
	@Override
	public int getEnchantability() {
		return 10; // Encantabilidad media
	}
	
	@Override
	public boolean canRepair(ItemStack stack, ItemStack ingredient) {
		// Se repara con cursed crystal como la lanza
		return ingredient.getItem() == com.fiw.fiwstory.item.ModItems.CORRUPTED_CRYSTAL;
	}
	
	// ========== TOOLTIP PERSONALIZADO ==========
	
	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		// Título en rojo como la gema de caos
		tooltip.add(Text.literal("«Espada del Caos»").formatted(Formatting.DARK_RED, Formatting.BOLD));
		tooltip.add(Text.literal("Artefacto del mundo pasado - Paradoja temporal").formatted(Formatting.GRAY, Formatting.ITALIC));
		tooltip.add(Text.literal(""));
		
		tooltip.add(Text.literal("§5§oUn recuerdo del pasado§r").formatted(Formatting.DARK_PURPLE));
		tooltip.add(Text.literal("§7• Sensación de no pertenecer a este mundo§r").formatted(Formatting.GRAY));
		tooltip.add(Text.literal("§7• Energía caótica contenida§r").formatted(Formatting.GRAY));
		tooltip.add(Text.literal("§7• Poder destructivo descontrolado§r").formatted(Formatting.GRAY));
		tooltip.add(Text.literal(""));
		
		// Información de cooldown (solo si está en cooldown)
		long cooldownRemaining = FiwNBT.getCooldownRemaining(stack, "ability");
		if (cooldownRemaining > 0) {
			String cooldownText = "Cooldown: " + FiwUtils.formatTimeSeconds(cooldownRemaining / 1000.0);
			tooltip.add(Text.literal("§7" + cooldownText + "§r").formatted(Formatting.GRAY));
			tooltip.add(Text.literal(""));
		}
		
		// Cita misteriosa en rojo como la gema
		tooltip.add(Text.literal("§c§l¡ITEM DE LORE IMPORTANTE!§r").formatted(Formatting.RED, Formatting.BOLD));
		tooltip.add(Text.literal("§8«El caos siempre encuentra su camino»§r").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
	}
	
	@Override
	public boolean hasGlint(ItemStack stack) {
		// Los artefactos legendarios siempre tienen glint
		return true;
	}
}