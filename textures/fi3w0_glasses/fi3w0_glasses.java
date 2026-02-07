// Made with Blockbench 5.0.7
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class CustomModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "custommodel"), "main");
	private final ModelPart Fi3w0glasses;

	public CustomModel(ModelPart root) {
		this.Fi3w0glasses = root.getChild("Fi3w0glasses");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Fi3w0glasses = partdefinition.addOrReplaceChild("Fi3w0glasses", CubeListBuilder.create().texOffs(0, 8).addBox(-1.0F, -5.0F, -4.5F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-5.0F, -5.0F, -4.5F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(0, 4).addBox(-5.0F, -5.0F, -4.5F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(4, 4).addBox(5.0F, -5.0F, -4.5F, 0.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 2).addBox(1.0F, -5.0F, -4.5F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 16, 16);
	}

	@Override
	public void setupAnim(Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		Fi3w0glasses.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}