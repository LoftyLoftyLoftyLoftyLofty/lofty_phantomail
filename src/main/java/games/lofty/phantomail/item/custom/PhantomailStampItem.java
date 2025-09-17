package games.lofty.phantomail.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class PhantomailStampItem extends Item {
    public PhantomailStampItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemstack = player.getItemInHand(usedHand);
        player.openItemGui(itemstack, usedHand);
        player.awardStat(Stats.ITEM_USED.get(this));

        if(level.isClientSide()) {
            System.out.println("ITEM HAS BEEN USED. THIS IS CLIENTSIDE DEBUG");
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
