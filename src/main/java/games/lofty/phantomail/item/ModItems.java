package games.lofty.phantomail.item;

import games.lofty.phantomail.Phantomail;
import games.lofty.phantomail.item.custom.PhantomailStampItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Phantomail.MOD_ID);

    public static final DeferredItem<Item> PHANTOMAIL_STAMP = ITEMS.register("phantomailstamp",
            () -> new PhantomailStampItem(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
