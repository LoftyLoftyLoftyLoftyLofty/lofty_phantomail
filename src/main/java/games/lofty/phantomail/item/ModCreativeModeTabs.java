package games.lofty.phantomail.item;

import games.lofty.phantomail.Phantomail;
import games.lofty.phantomail.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Phantomail.MOD_ID);

    public static final Supplier<CreativeModeTab> PHANTOMAIL_ITEMS_TAB = CREATIVE_MODE_TAB.register("phantomail_items_tab",
            () -> CreativeModeTab.builder()
                    .icon(()-> new ItemStack(ModItems.PHANTOMAIL_BUNDLE.get()))
                    .title(Component.translatable("creativetab.loftyphantomail.items"))
                    //.withTabsBefore(ResourceLocation.fromNamespaceAndPath(Phantomail.MOD_ID, "phantomail_items_tab"))
                    .displayItems(((itemDisplayParameters, output) -> {
                        output.accept(ModItems.PHANTOMAIL_BUNDLE);
                        output.accept(ModItems.PHANTOMAIL_STAMP);
                        output.accept(ModBlocks.PHANTOMAILBOX);
                    }))
                    .build());

    public static void register(IEventBus eventBus){
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
