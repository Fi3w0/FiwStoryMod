package com.fiw.fiwstory.item;

import com.fiw.fiwstory.FiwstoryMod;
import com.fiw.fiwstory.item.custom.BloodGemArtifact;
import com.fiw.fiwstory.item.custom.ChaosGemArtifact;
import com.fiw.fiwstory.item.custom.CorruptedCrystal;
import com.fiw.fiwstory.item.custom.CursedSpearOfFi3w0;
import com.fiw.fiwstory.item.custom.EscarabajoArtifact;
import com.fiw.fiwstory.item.custom.EspadaCaosArtifact;
import com.fiw.fiwstory.item.custom.FallenGodHeartArtifact;
import com.fiw.fiwstory.item.custom.TimelessBladeArtifact;
import com.fiw.fiwstory.item.custom.Fi3w0GlassesArmor;
import com.fiw.fiwstory.item.custom.PharaohDaggerArtifact;
import com.fiw.fiwstory.item.custom.PharaohRingArtifact;
import com.fiw.fiwstory.item.custom.PharaohScarabArtifact;
import com.fiw.fiwstory.item.custom.TemporalStructureArtifact;
import com.fiw.fiwstory.item.custom.PhilosopherStoneArtifact;
import com.fiw.fiwstory.item.custom.PhilosopherStoneUpgradedArtifact;
import com.fiw.fiwstory.item.custom.FallenGodHeartArtifact;
import com.fiw.fiwstory.item.custom.PureCrystalItem;
import com.fiw.fiwstory.item.custom.PureMixItem;
import com.fiw.fiwstory.item.custom.HealingRuneItem;
import com.fiw.fiwstory.item.custom.DivineBloodItem;
import com.fiw.fiwstory.item.custom.CorrodedCopperRingArtifact;
import com.fiw.fiwstory.item.custom.PlainCopperRingArtifact;
import com.fiw.fiwstory.item.custom.GoddessFlowerArtifact;
import com.fiw.fiwstory.item.custom.MagicTomeItem;
import com.fiw.fiwstory.item.custom.CorruptedMeatItem;
import com.fiw.fiwstory.item.custom.FrostStoneArtifact;
import com.fiw.fiwstory.item.custom.GD42QuantumArtifact;
import com.fiw.fiwstory.item.custom.MK88TabletArtifact;
import com.fiw.fiwstory.item.custom.BronzeAxiomArtifact;
import com.fiw.fiwstory.item.custom.SkyxernStoneArtifact;
import com.fiw.fiwstory.item.custom.SkyxernLegacyArtifact;
import com.fiw.fiwstory.item.custom.SkyxernCoinItem;
import com.fiw.fiwstory.item.custom.EspadaMgshtraklar;
import com.fiw.fiwstory.item.custom.EspadaElficaArtifact;
import com.fiw.fiwstory.item.custom.HachaRelampagoArtifact;
import com.fiw.fiwstory.item.custom.EspadaFrostmornArtifact;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
	public static final Item CURSED_SPEAR_OF_FI3W0 = registerItem("cursed_spear_of_fi3w0",
			new CursedSpearOfFi3w0(ToolMaterials.NETHERITE, 6, -2.4f, new Item.Settings().maxCount(1).fireproof()));
	
	public static final Item CORRUPTED_CRYSTAL = registerItem("corrupted_crystal",
			new CorruptedCrystal(new Item.Settings().maxCount(16)));
	
	public static final Item PHARAOH_SCARAB_ARTIFACT = registerItem("pharaoh_scarab_artifact",
			new PharaohScarabArtifact(new Item.Settings().maxCount(1).fireproof()));
	
	public static final Item CHAOS_GEM_ARTIFACT = registerItem("chaos_gem_artifact",
			new ChaosGemArtifact(new Item.Settings().maxCount(1).fireproof()));
	
	public static final Item BLOOD_GEM_ARTIFACT = registerItem("blood_gem_artifact",
			new BloodGemArtifact(new Item.Settings().maxCount(1).fireproof()));
	
	public static final Item PHARAOH_RING_ARTIFACT = registerItem("pharaoh_ring_artifact",
			new PharaohRingArtifact(new Item.Settings().maxCount(1).fireproof()));
	
	public static final Item TEMPORAL_STRUCTURE_ARTIFACT = registerItem("temporal_structure_artifact",
			new TemporalStructureArtifact(new Item.Settings().maxCount(1).fireproof()));
	
	public static final Item FI3W0_GLASSES = registerItem("fi3w0_glasses",
			new Fi3w0GlassesArmor(ArmorItem.Type.HELMET, new Item.Settings().maxCount(1).fireproof()));
	
	public static final Item PHILOSOPHER_STONE_ARTIFACT = registerItem("philosopher_stone_artifact",
			new PhilosopherStoneArtifact(new Item.Settings().maxCount(1).fireproof()));
	
	public static final Item PHILOSOPHER_STONE_UPGRADED_ARTIFACT = registerItem("philosopher_stone_upgraded_artifact",
			new PhilosopherStoneUpgradedArtifact(new Item.Settings().maxCount(1).fireproof()));
	
	public static final Item FALLEN_GOD_HEART_ARTIFACT = registerItem("fallen_god_heart_artifact",
			new FallenGodHeartArtifact(new Item.Settings().maxCount(1).fireproof()));
	
	public static final Item PURE_CRYSTAL = registerItem("pure_crystal",
			new PureCrystalItem(new Item.Settings().maxCount(16)));
	
	public static final Item PURE_MIX = registerItem("pure_mix",
			new PureMixItem(new Item.Settings().maxCount(16)));
	
	public static final Item HEALING_RUNE = registerItem("healing_rune",
			new HealingRuneItem(new Item.Settings().maxCount(1).fireproof()));
	
	public static final Item PHARAOH_DAGGER_ARTIFACT = registerItem("pharaoh_dagger_artifact",
			new PharaohDaggerArtifact(new Item.Settings().maxCount(1).fireproof()));

	public static final Item ESCARABAJO_ARTIFACT = registerItem("escarabajo_artifact",
			new EscarabajoArtifact(new Item.Settings().maxCount(1).fireproof()));

	public static final Item ESPADA_CAOS_ARTIFACT = registerItem("espada_caos_artifact",
			new EspadaCaosArtifact(null, 9, -2.4f, new Item.Settings().maxCount(1).fireproof()));

	public static final Item TIMELESS_BLADE_ARTIFACT = registerItem("timeless_blade_artifact",
			new TimelessBladeArtifact(null, 10, -2.4f, new Item.Settings().maxCount(1).fireproof()));
	
	public static final Item DIVINE_BLOOD = registerItem("divine_blood",
			new DivineBloodItem(new Item.Settings().maxCount(42).fireproof()));

	// === Recuerdos del Pasado ===
	public static final Item ARTIFACT_CORRODED_COPPER_RING = registerItem("artifact_corroded_copper_ring",
			new CorrodedCopperRingArtifact(new Item.Settings().maxCount(1).fireproof()));

	public static final Item ARTIFACT_PLAIN_COPPER_RING = registerItem("artifact_plain_copper_ring",
			new PlainCopperRingArtifact(new Item.Settings().maxCount(1).fireproof()));

	public static final Item ARTIFACT_GODDESS_FLOWER = registerItem("artifact_goddess_flower",
			new GoddessFlowerArtifact(new Item.Settings().maxCount(1).fireproof()));

	public static final Item TOMO_MAGICO = registerItem("tomo_magico",
			new MagicTomeItem(new Item.Settings().maxCount(1).fireproof()));

	public static final Item CARNE_CORRUPTA = registerItem("carne_corrupta",
			new CorruptedMeatItem(new Item.Settings().maxCount(64)));

	public static final Item FROST_STONE_ARTIFACT = registerItem("frost_stone_artifact",
			new FrostStoneArtifact(new Item.Settings().maxCount(1).fireproof()));

	public static final Item GD42_QUANTUM = registerItem("gd42_quantum",
			new GD42QuantumArtifact(new Item.Settings().maxCount(1).fireproof()));

	public static final Item MK88_TABLET = registerItem("mk88_tablet",
			new MK88TabletArtifact(new Item.Settings().maxCount(1).fireproof()));

	public static final Item BRONZE_AXIOM_ARTIFACT = registerItem("bronze_axiom_artifact",
			new BronzeAxiomArtifact(new Item.Settings().maxCount(1).fireproof()));

	public static final Item SKYXERN_STONE_ARTIFACT = registerItem("skyxern_stone_artifact",
			new SkyxernStoneArtifact(new Item.Settings().maxCount(1).fireproof()));

	public static final Item SKYXERN_LEGACY_ARTIFACT = registerItem("skyxern_legacy_artifact",
			new SkyxernLegacyArtifact(new Item.Settings().maxCount(1).fireproof()));

	// === Espada Mgshtraklar ===
	public static final Item ESPADA_MGSHTRAKLAR = registerItem("espada_mgshtraklar",
			new EspadaMgshtraklar(null, 5, -2.4f, new Item.Settings().maxCount(1).fireproof()));

	// === Armas v2.1.3 ===
	public static final Item ESPADA_ELFICA = registerItem("espada_elfica",
			new EspadaElficaArtifact(null, 10, -2.5f, new Item.Settings().maxCount(1).fireproof()));

	public static final Item HACHA_RELAMPAGO = registerItem("hacha_relampago",
			new HachaRelampagoArtifact(null, 8, -2.6f, new Item.Settings().maxCount(1).fireproof()));

	public static final Item ESPADA_FROSTMORN = registerItem("espada_frostmorn",
			new EspadaFrostmornArtifact(null, 10, -2.4f, new Item.Settings().maxCount(1).fireproof()));

	// === Moneda del servidor ===
	public static final Item SKYXERN_COIN = registerItem("skyxern_coin",
			new SkyxernCoinItem(new Item.Settings().maxCount(64)));

	private static Item registerItem(String name, Item item) {
		return Registry.register(Registries.ITEM, new Identifier(FiwstoryMod.MOD_ID, name), item);
	}

	public static void registerModItems() {
		FiwstoryMod.LOGGER.info("Registering mod items for " + FiwstoryMod.MOD_ID);
	}
}