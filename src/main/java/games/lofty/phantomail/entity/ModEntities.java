package games.lofty.phantomail.entity;

import games.lofty.phantomail.Phantomail;
import games.lofty.phantomail.entity.custom.PhantomailCourierEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModEntities
{
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, Phantomail.MOD_ID);

    public static final Supplier<EntityType<PhantomailCourierEntity>> PHANTOMAIL_COURIER =
            ENTITY_TYPES.register("phantomailcourier", () -> EntityType.Builder.of(PhantomailCourierEntity::new, MobCategory.MISC).build("phantomailcourier"));

    public static void register(IEventBus eventBus)
    {
        ENTITY_TYPES.register(eventBus);
    }
}
