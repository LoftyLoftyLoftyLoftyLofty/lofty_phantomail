package games.lofty.phantomail.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import games.lofty.phantomail.Phantomail;
import games.lofty.phantomail.entity.custom.PhantomailCourierEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class PhantomailCourierModel<T extends PhantomailCourierEntity> extends HierarchicalModel<T>
{
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Phantomail.MOD_ID,"phantomailcourier"),"main");
    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart wingL;
    private final ModelPart wingR;

    public PhantomailCourierModel(ModelPart root) {
        this.body = root.getChild("Body");
        this.head = this.body.getChild("Head");
        this.wingL = this.body.getChild("WingL");
        this.wingR = this.body.getChild("WingR");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(0, 8).addBox(-6.0F, -4.0F, -9.0F, 5.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(3, 20).addBox(-5.0F, -3.0F, 0.0F, 3.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(4, 29).addBox(-4.0F, -2.0F, 6.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(0, 54).addBox(-5.0F, -1.0F, -7.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, 24.0F, 6.0F));

        PartDefinition Head = Body.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -3.0F, -13.0F, 7.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(0, 61).addBox(-2.0F, -3.0F, -14.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(0, 61).addBox(-7.0F, -3.0F, -14.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition WingL = Body.addOrReplaceChild("WingL", CubeListBuilder.create().texOffs(23, 12).addBox(-1.0F, -4.0F, -9.0F, 6.0F, 2.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(16, 24).addBox(5.0F, -4.0F, -9.0F, 13.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition WingR = Body.addOrReplaceChild("WingR", CubeListBuilder.create().texOffs(23, 12).mirror().addBox(4.0F, -4.0F, -9.0F, 6.0F, 2.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false)
                .texOffs(16, 24).mirror().addBox(-9.0F, -4.0F, -9.0F, 13.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-16.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(PhantomailCourierEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.root().getAllParts().forEach(ModelPart::resetPose);
        this.animate(entity.flapAnimationState, PhantomailCourierAnimations.ANIM_PHANTOMAILCOURIER_GLIDE, ageInTicks, 1);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, color);
    }

    @Override
    public ModelPart root()
    {
        return body;
    }
}