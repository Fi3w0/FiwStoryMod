package com.fiw.fiwstory.client;

import net.minecraft.client.model.*;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;

public class Fi3w0GlassesModel<T extends Entity> extends EntityModel<T> {
	private final ModelPart Fi3w0glasses;

	public Fi3w0GlassesModel(ModelPart root) {
		this.Fi3w0glasses = root.getChild("Fi3w0glasses");
	}

	public static TexturedModelData getTexturedModelData() {
		ModelData modelData = new ModelData();
		ModelPartData modelPartData = modelData.getRoot();

		ModelPartData Fi3w0glasses = modelPartData.addChild("Fi3w0glasses", ModelPartBuilder.create()
			.uv(0, 8).cuboid(-1.0F, -5.0F, -4.5F, 2.0F, 1.0F, 0.0F, new Dilation(0.0F))
			.uv(0, 0).cuboid(-5.0F, -5.0F, -4.5F, 4.0F, 2.0F, 0.0F, new Dilation(0.0F))
			.uv(0, 4).cuboid(-5.0F, -5.0F, -4.5F, 0.0F, 2.0F, 2.0F, new Dilation(0.0F))
			.uv(4, 4).cuboid(5.0F, -5.0F, -4.5F, 0.0F, 2.0F, 2.0F, new Dilation(0.0F))
			.uv(0, 2).cuboid(1.0F, -5.0F, -4.5F, 4.0F, 2.0F, 0.0F, new Dilation(0.0F)),
			ModelTransform.pivot(0.0F, 0.0F, 0.0F));

		return TexturedModelData.of(modelData, 16, 16);
	}

	@Override
	public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		// No animation needed - rotation will be handled by the renderer
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
		Fi3w0glasses.render(matrices, vertexConsumer, light, overlay, red, green, blue, alpha);
	}
}