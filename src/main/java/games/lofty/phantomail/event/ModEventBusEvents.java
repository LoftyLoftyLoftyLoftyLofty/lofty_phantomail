package games.lofty.phantomail.event;

import games.lofty.phantomail.Phantomail;
import games.lofty.phantomail.entity.ModEntities;
import games.lofty.phantomail.entity.client.PhantomailCourierModel;
import games.lofty.phantomail.entity.custom.PhantomailCourierEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = Phantomail.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents
{
    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(PhantomailCourierModel.LAYER_LOCATION, PhantomailCourierModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event)
    {
        event.put(ModEntities.PHANTOMAIL_COURIER.get(), PhantomailCourierEntity.createAttributes().build());
    }

}
