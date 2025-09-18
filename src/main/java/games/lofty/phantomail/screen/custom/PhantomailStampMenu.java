package games.lofty.phantomail.screen.custom;

import games.lofty.phantomail.screen.ModMenuTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class PhantomailStampMenu extends AbstractContainerMenu {


    public PhantomailStampMenu(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    public PhantomailStampMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf registryFriendlyByteBuf)
    {
        this(ModMenuTypes.PHANTOMAIL_STAMP_MENU.get(), containerId);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }
}
