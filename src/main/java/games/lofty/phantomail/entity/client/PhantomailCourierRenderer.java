package games.lofty.phantomail.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import games.lofty.phantomail.Phantomail;
import games.lofty.phantomail.entity.custom.PhantomailCourierEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.PhantomEyesLayer;
import net.minecraft.resources.ResourceLocation;

public class PhantomailCourierRenderer extends MobRenderer<PhantomailCourierEntity, PhantomailCourierModel<PhantomailCourierEntity>>
{

    public PhantomailCourierRenderer(EntityRendererProvider.Context context)
    {
        super(context, new PhantomailCourierModel<>(context.bakeLayer(PhantomailCourierModel.LAYER_LOCATION)), 1);
        this.addLayer(new PhantomailCourierEyesLayer<>(this));
    }

    @Override
    public ResourceLocation getTextureLocation(PhantomailCourierEntity entity)
    {
        return ResourceLocation.fromNamespaceAndPath(Phantomail.MOD_ID,"textures/entity/phantomailcourier/phantomailcourier.png");
    }

    @Override
    public void render(PhantomailCourierEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
}
