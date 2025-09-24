package games.lofty.phantomail.entity.client;

import games.lofty.phantomail.Phantomail;
import games.lofty.phantomail.entity.custom.PhantomailCourierEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PhantomailCourierEyesLayer<T extends PhantomailCourierEntity> extends EyesLayer<T, PhantomailCourierModel<T>>
{
    private static final RenderType PHANTOM_EYES = RenderType.eyes(ResourceLocation.fromNamespaceAndPath(Phantomail.MOD_ID,"textures/entity/phantomailcourier/phantomailcourier_eyes.png"));

    public PhantomailCourierEyesLayer(RenderLayerParent<T, PhantomailCourierModel<T>> p_117342_) {
        super(p_117342_);
    }
    @Override
    public RenderType renderType() {
        return PHANTOM_EYES;
    }
}
