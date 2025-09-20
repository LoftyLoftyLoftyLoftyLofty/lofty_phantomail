package games.lofty.phantomail.item.custom;

import games.lofty.phantomail.record.ModPayloads;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import net.neoforged.neoforge.network.PacketDistributor;

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

        if(level.isClientSide())
        {
            System.out.println("Sending a packet to the server to request the stamp GUI...");
            PacketDistributor.sendToServer(new ModPayloads.PhantomailRequestStampGUIPacket(0, 0, "00000000-0000-0000-0000-000000000000"));
        }
        else
        {
            //System.out.println("ITEM HAS BEEN USED. THIS IS SERVERSIDE DEBUG");
        }

        return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
    }
}
