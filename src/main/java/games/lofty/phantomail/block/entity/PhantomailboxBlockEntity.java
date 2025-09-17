package games.lofty.phantomail.block.entity;

import games.lofty.phantomail.screen.custom.PhantomailboxMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class PhantomailboxBlockEntity extends BlockEntity implements MenuProvider {
    public PhantomailboxBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PHANTOMAILBOX_BE.get(), pos, blockState);
    }

    public static final int TOTAL_SLOTS = 6;
    public static final int SLOT_STAMP = 0;
    public static final int SLOT_OUTGOING = 1;
    public static final int SLOT_INCOMING_0 = 2;
    public static final int SLOT_INCOMING_1 = 3;
    public static final int SLOT_INCOMING_2 = 4;
    public static final int SLOT_INCOMING_3 = 5;

    public final ItemStackHandler inventory = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            //only allow one stamp in the stamp slot
            if( slot == 0 )
                return 1;

            //otherwise normal stack limit behavior
            return super.getStackLimit(slot, stack);
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public void clearContents()
    {
        int x = TOTAL_SLOTS;
        while((~--x)!=0)
        {
            inventory.setStackInSlot(x, ItemStack.EMPTY);
        }
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(inventory.getSlots());
        int x = TOTAL_SLOTS;
        while((~--x)!=0)
        {
            inv.setItem(x,inventory.getStackInSlot(x));
        }
        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory",inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Phantomailbox");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new PhantomailboxMenu(containerId, playerInventory, this);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
