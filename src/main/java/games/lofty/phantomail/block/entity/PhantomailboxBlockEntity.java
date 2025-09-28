package games.lofty.phantomail.block.entity;

import games.lofty.phantomail.entity.ModEntities;
import games.lofty.phantomail.entity.custom.PhantomailCourierEntity;
import games.lofty.phantomail.savedata.PhantomailboxRegistrySavedData;
import games.lofty.phantomail.screen.custom.PhantomailboxMenu;
import games.lofty.phantomail.util.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class PhantomailboxBlockEntity extends BlockEntity implements MenuProvider {
    public PhantomailboxBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PHANTOMAILBOX_BE.get(), pos, blockState);
    }

    //This is false by default and set to true each time the game reloads the blockentity and syncs saved data stuff
    public boolean hasInitializedSavedData = false;

    //TODO - fire a redstone pulse when mail is sent or received, according to the GUI settings

    public static final String DEFAULT_UUID = "00000000-0000-0000-0000-000000000000";

    //this uuid is how the server identifies a mailbox. this should allow compatibility with mods like Carry On
    public String PhantomailboxDeliveryUUID = DEFAULT_UUID;

    //TODO - this display string is what is shown in the GUI when players are choosing a delivery address
    public String PhantomailboxDisplayAddress = "My mailbox";

    /// Ticks of redstone signal output remaining
    public int remainingActivationTicks = 0;

    //whether or not we've spawned a mail delivery mob of some kind
    public boolean courierEnRoute = false;

    public static boolean hasPendingCourier(PhantomailboxBlockEntity phantomailboxBlockEntity)
    {return phantomailboxBlockEntity.courierEnRoute;}

    public void setPendingCourier(boolean b)
    {courierEnRoute = b;}

    //TODO - save this to block entity data
    public int courierEnRouteCountdown = 0;
    public void setPendingCourierDeferred()
    {courierEnRouteCountdown = 20;}

    public static final int TOTAL_SLOTS = 6;
    public static final int SLOT_STAMP = 0;
    public static final int SLOT_OUTGOING = 1;
    public static final int SLOT_INCOMING_0 = 2;
    public static final int SLOT_INCOMING_1 = 3;
    public static final int SLOT_INCOMING_2 = 4;
    public static final int SLOT_INCOMING_3 = 5;

    @Nullable
    /// Specifically for other blocks interacting with the mailbox (?)
    public IItemHandler getItemHandler(@Nullable Direction side)
    {
        if( side == null )
            return null;

        return inventoryAutomated;
    }

    /// This provides some overrides for blocks interacting with the mailbox which don't affect player interactions
    public final ItemStackHandler inventoryAutomated = new ItemStackHandler(TOTAL_SLOTS)
    {
        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate)
        {
            if (slot == PhantomailboxBlockEntity.SLOT_STAMP)
            {
                if(stack.is(ModTags.Items.PHANTOMAIL_VALID_POSTAGE))
                    return inventory.insertItem(slot, stack, simulate);
            }
            else if (slot == PhantomailboxBlockEntity.SLOT_OUTGOING)
            {
                return inventory.insertItem(slot, stack, simulate);
            }
            return stack;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate)
        {
            if (slot == PhantomailboxBlockEntity.SLOT_STAMP)
                return ItemStack.EMPTY;
            else if(slot == PhantomailboxBlockEntity.SLOT_OUTGOING)
                return ItemStack.EMPTY;
            else
                return inventory.extractItem(slot, amount, simulate);
        }
    };

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

    public void emitRedstoneSignal(boolean io)
    {
        int v = 0;
        if(io)
            v = 15;

        level.setBlock(getBlockPos(), getBlockState().setValue(BlockStateProperties.POWER, v), 3);
        level.updateNeighborsAt(getBlockPos(),getBlockState().getBlock());
        remainingActivationTicks = 20;
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
        tag.putString("uuid",PhantomailboxDeliveryUUID);
        tag.putInt("remainingActivationTicks",remainingActivationTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        PhantomailboxDeliveryUUID = tag.getString("uuid");
        remainingActivationTicks = tag.getInt("remainingActivationTicks");
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
            if(PhantomailCourierEntity.shouldAbandonSortieDueToTimeConstraints(level) == false)
            {
                //verifying whether the mailbox is accessible from the sky will be handled by the courier itself
                return true;
            }
        }
        return false;
    }

    /// returns true if there is an item in the queue to be delivered to this mailbox
    private static boolean hasPendingMail(PhantomailboxBlockEntity phantomailboxBlockEntity)
    {
        PhantomailboxRegistrySavedData prsd = PhantomailboxRegistrySavedData.fromMailbox(phantomailboxBlockEntity);
        int slot = prsd.getFirstPendingIndexAddressedToUUID(phantomailboxBlockEntity.PhantomailboxDeliveryUUID);
        return (slot != PhantomailboxRegistrySavedData.NO_SLOTS_AVAILABLE);
    }

    //returns true if all of the environmental, block placement, and inventory conditions necessary to invite a courier have been met
    private static boolean shouldReasonablyInviteCourier(PhantomailboxBlockEntity phantomailboxBlockEntity)
    {
        boolean sendingOutgoing = canReasonablySendOutgoingMail(phantomailboxBlockEntity);
        boolean canReceiveInbound = canReasonablyReceiveInboundMail(phantomailboxBlockEntity);
        boolean pendingMail = hasPendingMail(phantomailboxBlockEntity);
        boolean alreadyOnTheWay = hasPendingCourier(phantomailboxBlockEntity);

        //invite a new courier if there isn't already one en route. egress outbound mail and ingress pending mail if space is available
        boolean result = (!alreadyOnTheWay) && (sendingOutgoing || (canReceiveInbound && pendingMail));
        if(result)
        {
            if(sendingOutgoing)
                System.out.println("Sending outgoing");
            if((canReceiveInbound && pendingMail))
                System.out.println("Receiving incoming");
        }
        return result;
    }

    private static void inviteCourier(Level level, PhantomailboxBlockEntity phantomailboxBlockEntity)
    {
        //set flag for a courier being on the way
        phantomailboxBlockEntity.setPendingCourier(true);

        //if we are attempting to send outgoing mail, attempt to reserve an outbound slot
        if(canReasonablySendOutgoingMail(phantomailboxBlockEntity))
        {
            PhantomailboxRegistrySavedData prsd = PhantomailboxRegistrySavedData.fromMailbox(phantomailboxBlockEntity);
            int req = prsd.requestPendingMailSlot(phantomailboxBlockEntity.PhantomailboxDeliveryUUID);
            if(req != PhantomailboxRegistrySavedData.NO_SLOTS_AVAILABLE)
            {
                prsd.updateDeliveryDetails(req, phantomailboxBlockEntity.PhantomailboxDeliveryUUID, "?", PhantomailboxRegistrySavedData.DELIVERY_STATE_COURIER_PICKING_UP);
            }
        }

        //spawn the mob
        var courier = new PhantomailCourierEntity(ModEntities.PHANTOMAIL_COURIER.get(), level);
        courier.lastKnownTargetMailboxPos = phantomailboxBlockEntity.getBlockPos();
        courier.setPos(phantomailboxBlockEntity.getBlockPos().getX(), phantomailboxBlockEntity.getBlockPos().getY() + 32, phantomailboxBlockEntity.getBlockPos().getZ());
        level.addFreshEntity(courier);

        //create particles to indicate that a courier is inbound
        //TODO - display particles. requires renderer?

        //TODO - choose or create a more appropriate sound
        level.playSound(null, phantomailboxBlockEntity.getBlockPos(), SoundEvents.FOX_SCREECH, SoundSource.BLOCKS, 1f, 1f);
    }

    //returns true if inventory conditions are met to send outgoing mail
    private static boolean canReasonablySendOutgoingMail(PhantomailboxBlockEntity phantomailboxBlockEntity)
    {
        //is there space in the queue?
        PhantomailboxRegistrySavedData prsd = PhantomailboxRegistrySavedData.fromMailbox(phantomailboxBlockEntity);
        if(prsd.getNextAvailableSlot() != PhantomailboxRegistrySavedData.NO_SLOTS_AVAILABLE)
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
        }
        return false;
    }

    //returns the number of unoccupied/available inbound mail slots
    public static int getAvailableInboundMailSlots(PhantomailboxBlockEntity phantomailboxBlockEntity)
    {
        int ret = 0;
        for(int x = PhantomailboxBlockEntity.SLOT_INCOMING_0; x <= PhantomailboxBlockEntity.SLOT_INCOMING_3; ++x)
        {
            if(phantomailboxBlockEntity.inventory.getStackInSlot(x).getCount() <= 0)
                ++ret;
        }
        return ret;
    }

    //returns the next available unoccupied slot
    public static final int NO_SLOTS_AVAILABLE = -1;
    public static int getNextOpenSlot(PhantomailboxBlockEntity phantomailboxBlockEntity)
    {
        for(int x = PhantomailboxBlockEntity.SLOT_INCOMING_0; x <= PhantomailboxBlockEntity.SLOT_INCOMING_3; ++x)
        {
            if(phantomailboxBlockEntity.inventory.getStackInSlot(x).getCount() <= 0)
                return x;
        }
        return NO_SLOTS_AVAILABLE;
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

    private boolean uuidInitialized = false;
    private void initializeUUID()
    {
        if(uuidInitialized)
            return;
        uuidInitialized = true;
        if(Objects.equals(PhantomailboxDeliveryUUID, DEFAULT_UUID))
            PhantomailboxDeliveryUUID = UUID.randomUUID().toString();
    }

    public void unregisterUUID()
    {
        if(PhantomailboxDeliveryUUID != DEFAULT_UUID)
        {
            PhantomailboxRegistrySavedData prsd = PhantomailboxRegistrySavedData.fromMailbox(this);
            prsd.unregisterUUID(this);
        }
    }

    //every tick, each phantomailbox attempts to invite a courier if necessary
    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t)
    {
        if(level.isClientSide() == false)
        {
            //figure out which mailbox it is
            if (level.getBlockEntity(blockPos) instanceof PhantomailboxBlockEntity phantomailboxBlockEntity)
            {
                //update redstone pulse countdown
                if(phantomailboxBlockEntity.remainingActivationTicks > 0)
                {
                    phantomailboxBlockEntity.remainingActivationTicks -= 1;
                    if(phantomailboxBlockEntity.remainingActivationTicks <= 0)
                        phantomailboxBlockEntity.emitRedstoneSignal(false);
                }
                //if the mailbox is ticking for the first time, assign it a new uuid (or load its saved uuid)
                phantomailboxBlockEntity.initializeUUID();
                if(phantomailboxBlockEntity.hasInitializedSavedData == false)
                {
                    phantomailboxBlockEntity.hasInitializedSavedData = true;
                    PhantomailboxRegistrySavedData prsd = PhantomailboxRegistrySavedData.fromMailbox(phantomailboxBlockEntity);
                    prsd.registerUUID(phantomailboxBlockEntity);
                    //System.out.println(prsd.listOfAllPhantomailboxUUIDs);
                }

                //TODO - when new couriers are implemented this might need to be reevaluated
                //if we can't deliver mail right now, we can assume that we're not expecting a courier anymore
                if(PhantomailCourierEntity.shouldAbandonSortieDueToTimeConstraints(level) == true)
                    phantomailboxBlockEntity.setPendingCourier(false);

                //handle a short delay after couriers pick up or drop off before requesting more couriers
                if(phantomailboxBlockEntity.courierEnRouteCountdown > 0)
                {
                    phantomailboxBlockEntity.courierEnRouteCountdown -= 1;
                    if(phantomailboxBlockEntity.courierEnRouteCountdown == 0)
                    {
                        phantomailboxBlockEntity.setPendingCourier(false);
                    }
                }

                //TODO - update this when multiple courier types are implemented
                if (canReasonablyInviteCourier(level, phantomailboxBlockEntity) && shouldReasonablyInviteCourier(phantomailboxBlockEntity))
                {
                    if( phantomailboxBlockEntity.courierEnRoute == false )
                    {
                        inviteCourier(level, phantomailboxBlockEntity);
                    }
                }

                //TODO - periodically clear stale items that may have ended up in the queue, such as slots we requested that didn't resolve
            }
        }
    }
}
