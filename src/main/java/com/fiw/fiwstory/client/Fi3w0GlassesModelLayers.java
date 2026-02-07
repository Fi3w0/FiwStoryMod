package com.fiw.fiwstory.client;

import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.util.Identifier;

public class Fi3w0GlassesModelLayers {
	public static final EntityModelLayer FI3W0_GLASSES = new EntityModelLayer(new Identifier("fiwstory", "fi3w0_glasses"), "main");
	
	public static void registerModels() {
		// Model registration will be handled by the renderer initialization
	}
	
	public static TexturedModelData getFi3w0GlassesTexturedModelData() {
		return Fi3w0GlassesModel.getTexturedModelData();
	}
}