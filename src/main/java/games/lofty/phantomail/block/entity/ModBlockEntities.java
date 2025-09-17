package games.lofty.phantomail.block.entity;

import games.lofty.phantomail.Phantomail;
import games.lofty.phantomail.block.ModBlocks;
import games.lofty.phantomail.block.custom.PhantomailboxBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Phantomail.MOD_ID);

    public static final Supplier<BlockEntityType<PhantomailboxBlockEntity>> PHANTOMAILBOX_BE =
            BLOCK_ENTITIES.register("phantomailbox_be", () -> BlockEntityType.Builder.of(
                    PhantomailboxBlockEntity::new, ModBlocks.PHANTOMAILBOX.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
