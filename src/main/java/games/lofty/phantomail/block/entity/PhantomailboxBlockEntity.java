package games.lofty.phantomail.block.entity;

import games.lofty.phantomail.screen.custom.PhantomailboxMenu;
import games.lofty.phantomail.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class PhantomailboxBlockEntity extends BlockEntity implements MenuProvider {
    public PhantomailboxBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PHANTOMAILBOX_BE.get(), pos, blockState);
    }
    //TODO - fire a redstone pulse when mail is sent or received, according to the GUI settings

    //TODO - this uuid is how the server identifies a mailbox. this should allow compatibility with mods like Carry On
    protected String PhantomailboxDeliveryUUID = "";

    //TODO - this display string is what is shown in the GUI when players are choosing a delivery address
    protected String PhantomailboxDisplayAddress = "My mailbox";

    //whether or not we've spawned a mail delivery mob of some kind
    protected boolean courierEnRoute = false;

    public static boolean hasPendingCourier(PhantomailboxBlockEntity phantomailboxBlockEntity)
    {return phantomailboxBlockEntity.courierEnRoute;}

    public void setPendingCourier(boolean b)
    {courierEnRoute = b;}


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
            //only allow one stamp in the stamp slot at a time
            if( slot == SLOT_STAMP )
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

    //returns true if mailbox conditions are met to summon a courier
    //TODO this should be updated to return a list of actionable courier types so that we know what our options are (if we can reasonably summon anything at all)
    private static boolean canReasonablyInviteCourier(Level level, PhantomailboxBlockEntity phantomailboxBlockEntity)
    {
        //if we are summoning a courier of type Phantom Courier
        //TODO - if multiple types of couriers are implemented in the future, this will need to be expanded upon
        if(true)
        {
            //if it is nighttime
            if(level.isNight())
            {
                //if the mailbox has sky access
                //TODO - this isn't a good way to check for a clear flight path for an inbound flying courier
                if(level.canSeeSky(phantomailboxBlockEntity.getBlockPos()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean hasPendingMail(PhantomailboxBlockEntity phantomailboxBlockEntity)
    {
        //TODO - the server needs to be able to store outgoing mail in some kind of buffer and the phantomailbox needs to be able to read that buffer
        return false;
    }

    //returns true if all of the environmental, block placement, and inventory conditions necessary to invite a courier have been met
    private static boolean shouldReasonablyInviteCourier(PhantomailboxBlockEntity phantomailboxBlockEntity)
    {
        boolean sendingOutgoing = canReasonablySendOutgoingMail(phantomailboxBlockEntity);
        boolean canReceiveInbound = canReasonablyReceiveInboundMail(phantomailboxBlockEntity);
        boolean pendingMail = hasPendingMail(phantomailboxBlockEntity);
        boolean alreadyOnTheWay = hasPendingCourier(phantomailboxBlockEntity);

        //invite a new courier if there isn't already one en route. egress outbound mail and ingress pending mail if space is available
        return (!alreadyOnTheWay) && (sendingOutgoing || (canReceiveInbound && pendingMail));
    }

    private static void inviteCourier(Level level, PhantomailboxBlockEntity phantomailboxBlockEntity)
    {
        //set flag for a courier being on the way
        phantomailboxBlockEntity.setPendingCourier(true);

        //spawn the mob
        //TODO - spawn the actual mob. need to create the mob first

        //create particles to indicate that a courier is inbound
        //TODO - display particles. requires renderer?

        //System.out.println("INVITING COURIER");

        //TODO - choose or create a more appropriate sound
        level.playSound(null, phantomailboxBlockEntity.getBlockPos(), SoundEvents.FOX_SCREECH, SoundSource.BLOCKS, 1f, 1f);
    }

    //returns true if inventory conditions are met to send outgoing mail
    private static boolean canReasonablySendOutgoingMail(PhantomailboxBlockEntity phantomailboxBlockEntity)
    {
        //look at the stamp slot
        ItemStack stampSlotItemStack = phantomailboxBlockEntity.inventory.getStackInSlot(PhantomailboxBlockEntity.SLOT_STAMP);

        //if the stamp is valid postage
        if (stampSlotItemStack.is(ModTags.Items.PHANTOMAIL_VALID_POSTAGE))
        {
            //if the player has chosen a delivery address
            //if(w.get(ModDataComponents.PHANTOMAIL_CHOSEN_DELIVERY_ADDRESS))
            //TODO - check the DataComponents on the stamp for a chosen delivery address. requires those components being set via GUI on the stamp itself
            if (true)
            {
                //if the outgoing slot is occupied, we are trying to send mail
                ItemStack outgoingSlotItemStack = phantomailboxBlockEntity.inventory.getStackInSlot(PhantomailboxBlockEntity.SLOT_OUTGOING);
                if (outgoingSlotItemStack.getCount() > 0)
                {
                    return true;
                }
            }
        }
        return false;
    }

    //returns the number of unoccupied/available inbound mail slots
    private static int getAvailableInboundMailSlots(PhantomailboxBlockEntity phantomailboxBlockEntity)
    {
        int ret = 0;
        for(int x = PhantomailboxBlockEntity.SLOT_INCOMING_0; x < PhantomailboxBlockEntity.SLOT_INCOMING_3; ++x)
        {
            if(phantomailboxBlockEntity.inventory.getStackInSlot(x).getCount() <= 0)
                ++ret;
        }
        return ret;
    }

    //returns true if inventory conditions are met to receive more mail
    private static boolean canReasonablyReceiveInboundMail(PhantomailboxBlockEntity phantomailboxBlockEntity)
    {
        //if we have available slots
        if(getAvailableInboundMailSlots(phantomailboxBlockEntity) > 0)
        {
            return true;
        }
        return false;
    }

    //every tick, each phantomailbox attempts to invite a courier if necessary
    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t)
    {
        //figure out which mailbox it is
        if(level.getBlockEntity(blockPos) instanceof PhantomailboxBlockEntity phantomailboxBlockEntity)
        {
            //TODO - update this when multiple courier types are implemented
            if(canReasonablyInviteCourier(level, phantomailboxBlockEntity) && shouldReasonablyInviteCourier(phantomailboxBlockEntity))
            {
                inviteCourier(level, phantomailboxBlockEntity);
            }
        }
    }
}
