package com.fiw.fiwstory.client;

import com.fiw.fiwstory.item.ModItems;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class Fi3w0GlassesRenderer implements ArmorRenderer {
	// Textura para el modelo 3D en la cabeza
	private static final Identifier MODEL_TEXTURE = new Identifier("fiwstory", "textures/item/glasses_armor.png");
	private Fi3w0GlassesModel<LivingEntity> model;
	
	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack, LivingEntity entity, EquipmentSlot slot, int light, BipedEntityModel<LivingEntity> contextModel) {
		if (stack.getItem() == ModItems.FI3W0_GLASSES && slot == EquipmentSlot.HEAD) {
			if (model == null) {
				model = new Fi3w0GlassesModel<>(MinecraftClient.getInstance().getEntityModelLoader().getModelPart(Fi3w0GlassesModelLayers.FI3W0_GLASSES));
			}
			
			// Aplicar transformaciones de la cabeza del jugador
			matrices.push();
			contextModel.head.rotate(matrices);
			
			// Ajustar posición para que las gafas estén en frente de la cara
			// En lugar de atrás - bajadas 5-6 píxeles (0.3125-0.375 bloques)
			matrices.translate(0.0F, 0.0625F, 0.0F); // Bajadas desde -0.25 a +0.0625
			
			// Aplicar rotación de cabeza al modelo
			float headYaw = entity.getHeadYaw();
			float headPitch = entity.getPitch();
			model.setAngles(entity, 0, 0, 0, headYaw, headPitch);
			
			// Render con transparencia cyan
			VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(MODEL_TEXTURE));
			
			// Color cyan transparente: R=0.0, G=1.0, B=1.0, Alpha=0.5 (50% transparente - 10% menos)
			model.render(matrices, vertexConsumer, light, 0, 0.0f, 1.0f, 1.0f, 0.5f);
			
			matrices.pop();
		}
	}
}