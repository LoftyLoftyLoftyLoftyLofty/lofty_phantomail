package games.lofty.phantomail.screen;

import games.lofty.phantomail.Phantomail;
import games.lofty.phantomail.screen.custom.PhantomailStampMenu;
import games.lofty.phantomail.screen.custom.PhantomailboxMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, Phantomail.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<PhantomailboxMenu>> PHANTOMAILBOX_MENU =
            registerMenuType("phantomailbox_menu", PhantomailboxMenu::new);

    public static final DeferredHolder<MenuType<?>, MenuType<PhantomailStampMenu>> PHANTOMAIL_STAMP_MENU =
            registerMenuType("phantomailstamp_menu", PhantomailStampMenu::new);

    @SuppressWarnings("unchecked")
    private static <T extends AbstractContainerMenu>DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(String name, IContainerFactory factory)
    {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus)
    {
        MENUS.register(eventBus);
    }
}
