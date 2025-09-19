package games.lofty.phantomail.screen.custom;

import games.lofty.phantomail.block.ModBlocks;
import games.lofty.phantomail.block.entity.PhantomailboxBlockEntity;
import games.lofty.phantomail.screen.ModMenuTypes;
import games.lofty.phantomail.util.ModTags;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

public class PhantomailboxMenu extends AbstractContainerMenu {

    //TODO - checkboxes in the GUI for redstone signal alerts when packages are sent or received

    public final PhantomailboxBlockEntity blockEntity;
    private final Level level;

    public PhantomailboxMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public PhantomailboxMenu(int containerId, Inventory inv, BlockEntity blockEntity) {
        super(ModMenuTypes.PHANTOMAILBOX_MENU.get(), containerId);

        this.blockEntity = ((PhantomailboxBlockEntity) blockEntity);
        this.level = inv.player.level();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.addSlot( new SlotItemHandler(this.blockEntity.inventory, PhantomailboxBlockEntity.SLOT_STAMP, 80, 35) {
            @Override
            public boolean mayPlace(ItemStack stack)
            {
                //the PhantomailboxBlockEntity class additionally restricts slot 0 to only allow a single item no matter the stack size
                return stack.is(ModTags.Items.PHANTOMAIL_VALID_POSTAGE);
            }
        });
        this.addSlot( new SlotItemHandler(this.blockEntity.inventory, PhantomailboxBlockEntity.SLOT_OUTGOING, 98, 35));
        this.addSlot( new SlotItemHandler(this.blockEntity.inventory, PhantomailboxBlockEntity.SLOT_INCOMING_0, 80, 53));
        this.addSlot( new SlotItemHandler(this.blockEntity.inventory, PhantomailboxBlockEntity.SLOT_INCOMING_1, 98, 53));
        this.addSlot( new SlotItemHandler(this.blockEntity.inventory, PhantomailboxBlockEntity.SLOT_INCOMING_2, 116, 53));
        this.addSlot( new SlotItemHandler(this.blockEntity.inventory, PhantomailboxBlockEntity.SLOT_INCOMING_3, 134, 53));
    }

    //this is overkill but at least there's no ambiguity as to what the hardcoded numbers mean anymore
    public static final int EXPECTED_PLAYER_INVENTORY_SLOT_BEGIN = 0;
    public static final int EXPECTED_INVENTORY_SIZE = 27;
    public static final int EXPECTED_HOTBAR_SIZE = 9;
    public static final int EXPECTED_PLAYER_INVENTORY_SLOTS = EXPECTED_INVENTORY_SIZE + EXPECTED_HOTBAR_SIZE;
    public static final int SLOT_OFFSET_PHANTOMAILBOX_INVENTORY_BEGIN = EXPECTED_PLAYER_INVENTORY_SLOTS + EXPECTED_PLAYER_INVENTORY_SLOT_BEGIN;
    public static final int SLOT_OFFSET_PHANTOMAILBOX_STAMP = SLOT_OFFSET_PHANTOMAILBOX_INVENTORY_BEGIN + PhantomailboxBlockEntity.SLOT_STAMP;
    public static final int SLOT_OFFSET_PHANTOMAILBOX_OUTGOING = SLOT_OFFSET_PHANTOMAILBOX_INVENTORY_BEGIN + PhantomailboxBlockEntity.SLOT_OUTGOING;
    public static final int SLOT_OFFSET_PHANTOMAILBOX_INCOMING_0 = SLOT_OFFSET_PHANTOMAILBOX_INVENTORY_BEGIN + PhantomailboxBlockEntity.SLOT_INCOMING_0;
    public static final int SLOT_OFFSET_PHANTOMAILBOX_INCOMING_1 = SLOT_OFFSET_PHANTOMAILBOX_INVENTORY_BEGIN + PhantomailboxBlockEntity.SLOT_INCOMING_1;
    public static final int SLOT_OFFSET_PHANTOMAILBOX_INCOMING_2 = SLOT_OFFSET_PHANTOMAILBOX_INVENTORY_BEGIN + PhantomailboxBlockEntity.SLOT_INCOMING_2;
    public static final int SLOT_OFFSET_PHANTOMAILBOX_INCOMING_3 = SLOT_OFFSET_PHANTOMAILBOX_INVENTORY_BEGIN + PhantomailboxBlockEntity.SLOT_INCOMING_3;
    public static final int TOTAL_EXPECTED_AVAILABLE_SLOTS = EXPECTED_PLAYER_INVENTORY_SLOTS + PhantomailboxBlockEntity.TOTAL_SLOTS + EXPECTED_PLAYER_INVENTORY_SLOT_BEGIN;

    @Override
    public ItemStack quickMoveStack(Player player, int pIndex)
    {
        //figure out which slot we're using
        Slot sourceSlot = slots.get(pIndex);
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        //if the slot is empty or null, then return an empty or null result
        if (sourceSlot == null || !sourceSlot.hasItem())
            return ItemStack.EMPTY;

        //System.out.println("pIndex = " + String.valueOf(pIndex));
        //if the slot ranges from 0 to the combined size of the inventory and hotbar, we are inserting
        if (pIndex < EXPECTED_PLAYER_INVENTORY_SLOTS)
        {
            //System.out.println("inserting");
            //whether or not we attempt to put the item in the outgoing mail slot
            boolean tryOutgoing = false;

            //item is a stamp
            if(slots.get(SLOT_OFFSET_PHANTOMAILBOX_STAMP).mayPlace(sourceStack))
            {
                //System.out.println("stamp yes");
                //attempt to place into SLOT_STAMP
                if (!moveItemStackTo(sourceStack, SLOT_OFFSET_PHANTOMAILBOX_STAMP, SLOT_OFFSET_PHANTOMAILBOX_STAMP+1, false))
                {
                    //System.out.println("move unsuccessful");
                    tryOutgoing = true;
                }
                else
                {
                    //System.out.println("move successful");
                    return ItemStack.EMPTY;
                }
            }
            //item is not a stamp, try to mail it
            else
            {
                tryOutgoing = true;
                //System.out.println("stamp no");
            }

            //attempting to put something in the mailbox outgoing slot
            if (tryOutgoing)
            {
                //System.out.println("attempt outgoing");
                //attempt to place into outgoing mail
                if (!moveItemStackTo(sourceStack, SLOT_OFFSET_PHANTOMAILBOX_OUTGOING, SLOT_OFFSET_PHANTOMAILBOX_OUTGOING+1, false))
                {
                    //System.out.println("slot 37 insert fail");
                    return ItemStack.EMPTY;
                }
                else
                {
                    //System.out.println("slot 37 insert success");
                    return ItemStack.EMPTY;
                }
            }
        }

        //indices 36 and above are slots assigned to the mailbox inventory, we are withdrawing
        else if (pIndex < TOTAL_EXPECTED_AVAILABLE_SLOTS)
        {
            //attempt merge back into player inventory
            if (!moveItemStackTo(sourceStack, EXPECTED_PLAYER_INVENTORY_SLOT_BEGIN, EXPECTED_PLAYER_INVENTORY_SLOTS, false))
                return ItemStack.EMPTY;
        }

        //invalid slot index
        else
        {
            return ItemStack.EMPTY;
        }

        //entire stack moved successfully, clear the slot
        if (sourceStack.getCount() == 0)
        {
            sourceSlot.set(ItemStack.EMPTY);
        }
        else
        {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(player, sourceStack);

        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.PHANTOMAILBOX.get());
    }

    private void addPlayerInventory(Inventory playerInventory)
    {
        for(int i = 0; i < 3; ++i) {
            for(int l = 0; l < 9; ++l) {
                this.addSlot( new Slot( playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18 ));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory)
    {
        for(int i = 0; i < 9; ++i)
        {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }
}
