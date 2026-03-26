package com.fiw.fiwstory;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fiw.fiwstory.command.BindCommand;
import com.fiw.fiwstory.command.CorruptionCommand;
import com.fiw.fiwstory.command.ImmunityCommand;
import com.fiw.fiwstory.command.VoidCommand;
import dev.emi.trinkets.api.Trinket;
import dev.emi.trinkets.api.TrinketsApi;
import com.fiw.fiwstory.effect.ModStatusEffects;
import com.fiw.fiwstory.event.AmethystDropEvent;
import com.fiw.fiwstory.event.CorruptionPreventionEvent;
import com.fiw.fiwstory.event.CorruptionTrackingEvent;
import com.fiw.fiwstory.event.ModEvents;
import com.fiw.fiwstory.event.PhilosopherStoneEvents;
import com.fiw.fiwstory.item.custom.MagicTomeItem;
import com.fiw.fiwstory.item.custom.MK88TabletArtifact;
import com.fiw.fiwstory.item.custom.BronzeAxiomArtifact;
import com.fiw.fiwstory.item.custom.SkyxernStoneArtifact;
import com.fiw.fiwstory.item.custom.SkyxernLegacyArtifact;
import com.fiw.fiwstory.item.custom.EspadaMgshtraklar;
import com.fiw.fiwstory.item.custom.BloodGemArtifact;
import com.fiw.fiwstory.item.custom.EscarabajoArtifact;
import com.fiw.fiwstory.item.custom.EspadaElficaArtifact;
import com.fiw.fiwstory.item.custom.HachaRelampagoArtifact;
import com.fiw.fiwstory.item.custom.EspadaFrostmornArtifact;
import com.fiw.fiwstory.item.custom.HojaDeLaPurgaArtifact;
import com.fiw.fiwstory.item.custom.PufferfishArtifact;
import com.fiw.fiwstory.event.SoulboundDeathHandler;
import com.fiw.fiwstory.item.ModItems;
import com.fiw.fiwstory.particles.VoidParticles;

public class FiwstoryMod implements ModInitializer {
	public static final String MOD_ID = "fiwstory";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final ItemGroup FIWSTORY_GROUP = FabricItemGroup.builder()
			.icon(() -> new ItemStack(ModItems.CORRUPTED_CRYSTAL))
			.displayName(Text.translatable("itemgroup.fiwstory.main"))
			.entries((context, entries) -> {
				entries.add(ModItems.CORRUPTED_CRYSTAL);
				entries.add(ModItems.CURSED_SPEAR_OF_FI3W0);
				entries.add(ModItems.PHARAOH_SCARAB_ARTIFACT);
				entries.add(ModItems.CHAOS_GEM_ARTIFACT);
				entries.add(ModItems.BLOOD_GEM_ARTIFACT);
				entries.add(ModItems.PHARAOH_RING_ARTIFACT);
				entries.add(ModItems.TEMPORAL_STRUCTURE_ARTIFACT);
				entries.add(ModItems.FI3W0_GLASSES);
				entries.add(ModItems.PHILOSOPHER_STONE_ARTIFACT);
				entries.add(ModItems.PHILOSOPHER_STONE_UPGRADED_ARTIFACT);
				entries.add(ModItems.FALLEN_GOD_HEART_ARTIFACT);
				entries.add(ModItems.PURE_CRYSTAL);
				entries.add(ModItems.PURE_MIX);
				entries.add(ModItems.HEALING_RUNE);
				entries.add(ModItems.PHARAOH_DAGGER_ARTIFACT);
				entries.add(ModItems.ESCARABAJO_ARTIFACT);
				entries.add(ModItems.ESPADA_CAOS_ARTIFACT);
				entries.add(ModItems.TIMELESS_BLADE_ARTIFACT);
				entries.add(ModItems.DIVINE_BLOOD);
			// Recuerdos del Pasado
			entries.add(ModItems.ARTIFACT_CORRODED_COPPER_RING);
			entries.add(ModItems.ARTIFACT_PLAIN_COPPER_RING);
			entries.add(ModItems.ARTIFACT_GODDESS_FLOWER);
			entries.add(ModItems.TOMO_MAGICO);
			entries.add(ModItems.CARNE_CORRUPTA);
			entries.add(ModItems.FROST_STONE_ARTIFACT);
			entries.add(ModItems.GD42_QUANTUM);
			entries.add(ModItems.MK88_TABLET);
			entries.add(ModItems.BRONZE_AXIOM_ARTIFACT);
		entries.add(ModItems.SKYXERN_STONE_ARTIFACT);
		entries.add(ModItems.SKYXERN_LEGACY_ARTIFACT);
		entries.add(ModItems.PUFFERFISH_ARTIFACT);
		entries.add(ModItems.ESPADA_MGSHTRAKLAR);
		entries.add(ModItems.ESPADA_ELFICA);
		entries.add(ModItems.HACHA_RELAMPAGO);
		entries.add(ModItems.ESPADA_FROSTMORN);
		entries.add(ModItems.HOJA_DE_LA_PURGA);
		entries.add(ModItems.SKYXERN_COIN);
			})
			.build();

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Fiw Story Mod v2.1.3");

		ModItems.registerModItems();
		Registry.register(Registries.ITEM_GROUP, new Identifier(MOD_ID, "main"), FIWSTORY_GROUP);

		// Registrar trinkets
		registerTrinket(ModItems.PHARAOH_SCARAB_ARTIFACT);
		registerTrinket(ModItems.CHAOS_GEM_ARTIFACT);
		registerTrinket(ModItems.BLOOD_GEM_ARTIFACT);
		registerTrinket(ModItems.PHARAOH_RING_ARTIFACT);
		registerTrinket(ModItems.TEMPORAL_STRUCTURE_ARTIFACT);
		registerTrinket(ModItems.PHILOSOPHER_STONE_ARTIFACT);
		registerTrinket(ModItems.PHILOSOPHER_STONE_UPGRADED_ARTIFACT);
		registerTrinket(ModItems.FALLEN_GOD_HEART_ARTIFACT);
		registerTrinket(ModItems.ESCARABAJO_ARTIFACT);
		registerTrinket(ModItems.ARTIFACT_CORRODED_COPPER_RING);
		registerTrinket(ModItems.ARTIFACT_PLAIN_COPPER_RING);
		registerTrinket(ModItems.ARTIFACT_GODDESS_FLOWER);
		registerTrinket(ModItems.FROST_STONE_ARTIFACT);
		registerTrinket(ModItems.GD42_QUANTUM);
		registerTrinket(ModItems.MK88_TABLET);
		registerTrinket(ModItems.BRONZE_AXIOM_ARTIFACT);
		registerTrinket(ModItems.SKYXERN_STONE_ARTIFACT);
		registerTrinket(ModItems.SKYXERN_LEGACY_ARTIFACT);
		registerTrinket(ModItems.PUFFERFISH_ARTIFACT);

		// Registrar items que causan corrupción
		com.fiw.fiwstory.data.CorruptionData.registerCorruptItem(ModItems.CURSED_SPEAR_OF_FI3W0);
		com.fiw.fiwstory.data.CorruptionData.registerCorruptItem(ModItems.CORRUPTED_CRYSTAL);
		com.fiw.fiwstory.data.CorruptionData.registerCorruptItem(ModItems.FI3W0_GLASSES);
		
		// Registrar efectos de estado
		ModStatusEffects.registerStatusEffects();
		
		// Registrar eventos de daño del Tomo Mágico y nuevos artefactos
		MagicTomeItem.registerDamageEvents();
		MK88TabletArtifact.registerDamageEvents();
		BronzeAxiomArtifact.registerDamageEvents();
		SkyxernStoneArtifact.registerDamageEvents();
		SkyxernLegacyArtifact.registerDamageEvents();
		BloodGemArtifact.registerDamageEvents();
		EspadaMgshtraklar.registerDamageEvents();
		EscarabajoArtifact.registerDamageEvents();
		HachaRelampagoArtifact.registerDamageEvents();
		PufferfishArtifact.registerDamageEvents();
		HojaDeLaPurgaArtifact.registerDamageEvents();

		ModEvents.registerServerEvents();
		AmethystDropEvent.register();
		PhilosopherStoneEvents.registerEvents();
		CorruptionTrackingEvent.registerEvents();
		CorruptionPreventionEvent.registerEvents();
		
		// Registrar sistema Soulbound
		SoulboundDeathHandler.registerEvents();
		
		// Registrar sistema de Void
		// VoidEvents.register() se llama automáticamente a través de los eventos
		
		// Registrar partículas del Void
		VoidParticles.register();
		
		// Registrar comandos
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
			ImmunityCommand.register(dispatcher, registryAccess, environment);
			BindCommand.register(dispatcher, registryAccess, environment);
			CorruptionCommand.register(dispatcher, registryAccess, environment);
			VoidCommand.register(dispatcher, registryAccess, environment);
		});

		LOGGER.info("Fiw Story Mod initialized successfully!");
		LOGGER.info("Timeless Void dimension system ready");
		LOGGER.info("Commands: /v enter, /v leave, /v whitelist");
		LOGGER.info("Timeless Blade can access Void (10s cooldown)");
	}

	private static void registerTrinket(Item item) {
		if (item instanceof Trinket trinket) {
			TrinketsApi.registerTrinket(item, trinket);
		}
	}
}