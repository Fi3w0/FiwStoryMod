package com.fiw.fiwstory;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;

import com.fiw.fiwstory.client.Fi3w0GlassesModelLayers;
import com.fiw.fiwstory.client.Fi3w0GlassesRenderer;
import com.fiw.fiwstory.event.ClientModEvents;
import com.fiw.fiwstory.item.ModItems;

@Environment(EnvType.CLIENT)
public class FiwstoryModClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientModEvents.registerClientEvents();
		
		// Register Fi3w0 Glasses model layer
		EntityModelLayerRegistry.registerModelLayer(Fi3w0GlassesModelLayers.FI3W0_GLASSES, 
			Fi3w0GlassesModelLayers::getFi3w0GlassesTexturedModelData);
		
		// Register Fi3w0 Glasses armor renderer
		ArmorRenderer.register(new Fi3w0GlassesRenderer(), ModItems.FI3W0_GLASSES);
	}
}