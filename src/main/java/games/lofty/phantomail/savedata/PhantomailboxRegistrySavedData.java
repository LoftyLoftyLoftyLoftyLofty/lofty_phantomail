package games.lofty.phantomail.savedata;

import games.lofty.phantomail.Phantomail;
import games.lofty.phantomail.block.entity.PhantomailboxBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PhantomailboxRegistrySavedData extends SavedData
{
    public static final String LIST_OF_ALL_PHANTOMAILBOX_UUIDS = Phantomail.MOD_ID + ":" + "list_of_all_phantomailbox_uuids";
    public String listOfAllPhantomailboxUUIDs = "";//strided, uuid|dimension|name

    public static final String UNIT_SEPARATOR = "␟";
    public static final String RECORD_SEPARATOR = "␞";

    public static final int DELIVERY_STATE_UNKNOWN = -1;
    public static final int DELIVERY_STATE_COURIER_PICKING_UP = 0;
    public static final int DELIVERY_STATE_PENDING_MAIL = 1;
    public static final int DELIVERY_STATE_COURIER_DROPPING_OFF = 2;

    public static final String DELIVERY_DETAILS_DEFAULT_DATA = "-" + UNIT_SEPARATOR + "-" + UNIT_SEPARATOR + "-";
    public static final String DELIVERY_DETAILS_DEFAULT_DATA_RESERVED = "-" + UNIT_SEPARATOR + "-" + UNIT_SEPARATOR + DELIVERY_STATE_COURIER_PICKING_UP;

    public static final int DETAILS_INDEX_UUID_TO = 0;
    public static final int DETAILS_INDEX_UUID_FROM = 1;
    public static final int DETAILS_INDEX_STATUS = 2;

    //this is a very bad way to implement this, but I can understand it and implement it with my limited knowledge of the api
    public static final String DELIVERY_KEY_SLOT_0_DETAILS = Phantomail.MOD_ID + ":" + "deliverydetails_slot_0";
    public static final String DELIVERY_KEY_SLOT_0_ITEM = Phantomail.MOD_ID + ":" + "deliveryitem_slot_0";
    public ItemStack DELIVERY_QUEUE_ITEM_SLOT_0 = ItemStack.EMPTY;
    public String DELIVERY_DETAILS_ITEM_SLOT_0 = DELIVERY_DETAILS_DEFAULT_DATA;

    public static final String DELIVERY_KEY_SLOT_1_DETAILS = Phantomail.MOD_ID + ":" + "deliverydetails_slot_1";
    public static final String DELIVERY_KEY_SLOT_1_ITEM = Phantomail.MOD_ID + ":" + "deliveryitem_slot_1";
    public ItemStack DELIVERY_QUEUE_ITEM_SLOT_1 = ItemStack.EMPTY;
    public String DELIVERY_DETAILS_ITEM_SLOT_1 = DELIVERY_DETAILS_DEFAULT_DATA;

    public static final String DELIVERY_KEY_SLOT_2_DETAILS = Phantomail.MOD_ID + ":" + "deliverydetails_slot_2";
    public static final String DELIVERY_KEY_SLOT_2_ITEM = Phantomail.MOD_ID + ":" + "deliveryitem_slot_2";
    public ItemStack DELIVERY_QUEUE_ITEM_SLOT_2 = ItemStack.EMPTY;
    public String DELIVERY_DETAILS_ITEM_SLOT_2 = DELIVERY_DETAILS_DEFAULT_DATA;

    public static final String DELIVERY_KEY_SLOT_3_DETAILS = Phantomail.MOD_ID + ":" + "deliverydetails_slot_3";
    public static final String DELIVERY_KEY_SLOT_3_ITEM = Phantomail.MOD_ID + ":" + "deliveryitem_slot_3";
    public ItemStack DELIVERY_QUEUE_ITEM_SLOT_3 = ItemStack.EMPTY;
    public String DELIVERY_DETAILS_ITEM_SLOT_3 = DELIVERY_DETAILS_DEFAULT_DATA;

    public static final String DELIVERY_KEY_SLOT_4_DETAILS = Phantomail.MOD_ID + ":" + "deliverydetails_slot_4";
    public static final String DELIVERY_KEY_SLOT_4_ITEM = Phantomail.MOD_ID + ":" + "deliveryitem_slot_4";
    public ItemStack DELIVERY_QUEUE_ITEM_SLOT_4 = ItemStack.EMPTY;
    public String DELIVERY_DETAILS_ITEM_SLOT_4 = DELIVERY_DETAILS_DEFAULT_DATA;

    public static final String DELIVERY_KEY_SLOT_5_DETAILS = Phantomail.MOD_ID + ":" + "deliverydetails_slot_5";
    public static final String DELIVERY_KEY_SLOT_5_ITEM = Phantomail.MOD_ID + ":" + "deliveryitem_slot_5";
    public ItemStack DELIVERY_QUEUE_ITEM_SLOT_5 = ItemStack.EMPTY;
    public String DELIVERY_DETAILS_ITEM_SLOT_5 = DELIVERY_DETAILS_DEFAULT_DATA;

    public static final String DELIVERY_KEY_SLOT_6_DETAILS = Phantomail.MOD_ID + ":" + "deliverydetails_slot_6";
    public static final String DELIVERY_KEY_SLOT_6_ITEM = Phantomail.MOD_ID + ":" + "deliveryitem_slot_6";
    public ItemStack DELIVERY_QUEUE_ITEM_SLOT_6 = ItemStack.EMPTY;
    public String DELIVERY_DETAILS_ITEM_SLOT_6 = DELIVERY_DETAILS_DEFAULT_DATA;

    public static final String DELIVERY_KEY_SLOT_7_DETAILS = Phantomail.MOD_ID + ":" + "deliverydetails_slot_7";
    public static final String DELIVERY_KEY_SLOT_7_ITEM = Phantomail.MOD_ID + ":" + "deliveryitem_slot_7";
    public ItemStack DELIVERY_QUEUE_ITEM_SLOT_7 = ItemStack.EMPTY;
    public String DELIVERY_DETAILS_ITEM_SLOT_7 = DELIVERY_DETAILS_DEFAULT_DATA;

    public static final String DELIVERY_KEY_SLOT_8_DETAILS = Phantomail.MOD_ID + ":" + "deliverydetails_slot_8";
    public static final String DELIVERY_KEY_SLOT_8_ITEM = Phantomail.MOD_ID + ":" + "deliveryitem_slot_8";
    public ItemStack DELIVERY_QUEUE_ITEM_SLOT_8 = ItemStack.EMPTY;
    public String DELIVERY_DETAILS_ITEM_SLOT_8 = DELIVERY_DETAILS_DEFAULT_DATA;

    public static final String DELIVERY_KEY_SLOT_9_DETAILS = Phantomail.MOD_ID + ":" + "deliverydetails_slot_9";
    public static final String DELIVERY_KEY_SLOT_9_ITEM = Phantomail.MOD_ID + ":" + "deliveryitem_slot_9";
    public ItemStack DELIVERY_QUEUE_ITEM_SLOT_9 = ItemStack.EMPTY;
    public String DELIVERY_DETAILS_ITEM_SLOT_9 = DELIVERY_DETAILS_DEFAULT_DATA;

    // Create new instance of saved data
    public static PhantomailboxRegistrySavedData create()
    {
        return new PhantomailboxRegistrySavedData();
    }

    // Load existing instance of saved data
    public static PhantomailboxRegistrySavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider)
    {
        PhantomailboxRegistrySavedData data = PhantomailboxRegistrySavedData.create();
        //load the list of uuids
        data.listOfAllPhantomailboxUUIDs = tag.getString(LIST_OF_ALL_PHANTOMAILBOX_UUIDS);

        //load each pending item slot one by one
        data.DELIVERY_QUEUE_ITEM_SLOT_0 = ItemStack.parseOptional(lookupProvider, tag.getCompound(DELIVERY_KEY_SLOT_0_ITEM));
        data.DELIVERY_DETAILS_ITEM_SLOT_0 = tag.getString(DELIVERY_KEY_SLOT_0_DETAILS);

        data.DELIVERY_QUEUE_ITEM_SLOT_1 = ItemStack.parseOptional(lookupProvider, tag.getCompound(DELIVERY_KEY_SLOT_1_ITEM));
        data.DELIVERY_DETAILS_ITEM_SLOT_1 = tag.getString(DELIVERY_KEY_SLOT_1_DETAILS);

        data.DELIVERY_QUEUE_ITEM_SLOT_2 = ItemStack.parseOptional(lookupProvider, tag.getCompound(DELIVERY_KEY_SLOT_2_ITEM));
        data.DELIVERY_DETAILS_ITEM_SLOT_2 = tag.getString(DELIVERY_KEY_SLOT_2_DETAILS);

        data.DELIVERY_QUEUE_ITEM_SLOT_3 = ItemStack.parseOptional(lookupProvider, tag.getCompound(DELIVERY_KEY_SLOT_3_ITEM));
        data.DELIVERY_DETAILS_ITEM_SLOT_3 = tag.getString(DELIVERY_KEY_SLOT_3_DETAILS);

        data.DELIVERY_QUEUE_ITEM_SLOT_4 = ItemStack.parseOptional(lookupProvider, tag.getCompound(DELIVERY_KEY_SLOT_4_ITEM));
        data.DELIVERY_DETAILS_ITEM_SLOT_4 = tag.getString(DELIVERY_KEY_SLOT_4_DETAILS);

        data.DELIVERY_QUEUE_ITEM_SLOT_5 = ItemStack.parseOptional(lookupProvider, tag.getCompound(DELIVERY_KEY_SLOT_5_ITEM));
        data.DELIVERY_DETAILS_ITEM_SLOT_5 = tag.getString(DELIVERY_KEY_SLOT_5_DETAILS);

        data.DELIVERY_QUEUE_ITEM_SLOT_6 = ItemStack.parseOptional(lookupProvider, tag.getCompound(DELIVERY_KEY_SLOT_6_ITEM));
        data.DELIVERY_DETAILS_ITEM_SLOT_6 = tag.getString(DELIVERY_KEY_SLOT_6_DETAILS);

        data.DELIVERY_QUEUE_ITEM_SLOT_7 = ItemStack.parseOptional(lookupProvider, tag.getCompound(DELIVERY_KEY_SLOT_7_ITEM));
        data.DELIVERY_DETAILS_ITEM_SLOT_7 = tag.getString(DELIVERY_KEY_SLOT_7_DETAILS);

        data.DELIVERY_QUEUE_ITEM_SLOT_8 = ItemStack.parseOptional(lookupProvider, tag.getCompound(DELIVERY_KEY_SLOT_8_ITEM));
        data.DELIVERY_DETAILS_ITEM_SLOT_8 = tag.getString(DELIVERY_KEY_SLOT_8_DETAILS);

        data.DELIVERY_QUEUE_ITEM_SLOT_9 = ItemStack.parseOptional(lookupProvider, tag.getCompound(DELIVERY_KEY_SLOT_9_ITEM));
        data.DELIVERY_DETAILS_ITEM_SLOT_9 = tag.getString(DELIVERY_KEY_SLOT_9_DETAILS);

        return data;
    }

    //grab the level from the mailbox
    public static PhantomailboxRegistrySavedData fromMailbox(PhantomailboxBlockEntity be)
    {
        return be.getLevel().getServer().overworld().getDataStorage().computeIfAbsent(new Factory<>(PhantomailboxRegistrySavedData::create, PhantomailboxRegistrySavedData::load, null), Phantomail.MOD_ID);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries)
    {
        //save the list of mailbox uuids
        tag.putString(LIST_OF_ALL_PHANTOMAILBOX_UUIDS,listOfAllPhantomailboxUUIDs);

        //save the individual pending mail slots one by one
        tag.put(DELIVERY_KEY_SLOT_0_ITEM, DELIVERY_QUEUE_ITEM_SLOT_0.saveOptional(registries));
        tag.putString(DELIVERY_KEY_SLOT_0_DETAILS, DELIVERY_DETAILS_ITEM_SLOT_0);

        tag.put(DELIVERY_KEY_SLOT_1_ITEM, DELIVERY_QUEUE_ITEM_SLOT_1.saveOptional(registries));
        tag.putString(DELIVERY_KEY_SLOT_1_DETAILS, DELIVERY_DETAILS_ITEM_SLOT_1);

        tag.put(DELIVERY_KEY_SLOT_2_ITEM, DELIVERY_QUEUE_ITEM_SLOT_2.saveOptional(registries));
        tag.putString(DELIVERY_KEY_SLOT_2_DETAILS, DELIVERY_DETAILS_ITEM_SLOT_2);

        tag.put(DELIVERY_KEY_SLOT_3_ITEM, DELIVERY_QUEUE_ITEM_SLOT_3.saveOptional(registries));
        tag.putString(DELIVERY_KEY_SLOT_3_DETAILS, DELIVERY_DETAILS_ITEM_SLOT_3);

        tag.put(DELIVERY_KEY_SLOT_4_ITEM, DELIVERY_QUEUE_ITEM_SLOT_4.saveOptional(registries));
        tag.putString(DELIVERY_KEY_SLOT_4_DETAILS, DELIVERY_DETAILS_ITEM_SLOT_4);

        tag.put(DELIVERY_KEY_SLOT_5_ITEM, DELIVERY_QUEUE_ITEM_SLOT_5.saveOptional(registries));
        tag.putString(DELIVERY_KEY_SLOT_5_DETAILS, DELIVERY_DETAILS_ITEM_SLOT_5);

        tag.put(DELIVERY_KEY_SLOT_6_ITEM, DELIVERY_QUEUE_ITEM_SLOT_6.saveOptional(registries));
        tag.putString(DELIVERY_KEY_SLOT_6_DETAILS, DELIVERY_DETAILS_ITEM_SLOT_6);

        tag.put(DELIVERY_KEY_SLOT_7_ITEM, DELIVERY_QUEUE_ITEM_SLOT_7.saveOptional(registries));
        tag.putString(DELIVERY_KEY_SLOT_7_DETAILS, DELIVERY_DETAILS_ITEM_SLOT_7);

        tag.put(DELIVERY_KEY_SLOT_8_ITEM, DELIVERY_QUEUE_ITEM_SLOT_8.saveOptional(registries));
        tag.putString(DELIVERY_KEY_SLOT_8_DETAILS, DELIVERY_DETAILS_ITEM_SLOT_8);

        tag.put(DELIVERY_KEY_SLOT_9_ITEM, DELIVERY_QUEUE_ITEM_SLOT_9.saveOptional(registries));
        tag.putString(DELIVERY_KEY_SLOT_9_DETAILS, DELIVERY_DETAILS_ITEM_SLOT_9);

        return tag;
    }

    public static final int RECORD_INDEX_UUID = 0;
    public static final int RECORD_INDEX_DIMENSION = 1;
    public static final int RECORD_INDEX_NAME = 2;

    public static final int NO_SLOTS_AVAILABLE = -1;
    public int getNextAvailableSlot()
    {
        //TODO - this is a terrible way to do this, fix me
        if(Objects.equals(DELIVERY_DETAILS_ITEM_SLOT_0, DELIVERY_DETAILS_DEFAULT_DATA))
            return 0;

        if(Objects.equals(DELIVERY_DETAILS_ITEM_SLOT_1, DELIVERY_DETAILS_DEFAULT_DATA))
            return 1;

        if(Objects.equals(DELIVERY_DETAILS_ITEM_SLOT_2, DELIVERY_DETAILS_DEFAULT_DATA))
            return 2;

        if(Objects.equals(DELIVERY_DETAILS_ITEM_SLOT_3, DELIVERY_DETAILS_DEFAULT_DATA))
            return 3;

        if(Objects.equals(DELIVERY_DETAILS_ITEM_SLOT_4, DELIVERY_DETAILS_DEFAULT_DATA))
            return 4;

        if(Objects.equals(DELIVERY_DETAILS_ITEM_SLOT_5, DELIVERY_DETAILS_DEFAULT_DATA))
            return 5;

        if(Objects.equals(DELIVERY_DETAILS_ITEM_SLOT_6, DELIVERY_DETAILS_DEFAULT_DATA))
            return 6;

        if(Objects.equals(DELIVERY_DETAILS_ITEM_SLOT_7, DELIVERY_DETAILS_DEFAULT_DATA))
            return 7;

        if(Objects.equals(DELIVERY_DETAILS_ITEM_SLOT_8, DELIVERY_DETAILS_DEFAULT_DATA))
            return 8;

        if(Objects.equals(DELIVERY_DETAILS_ITEM_SLOT_9, DELIVERY_DETAILS_DEFAULT_DATA))
            return 9;

        return NO_SLOTS_AVAILABLE;
    }

    public void updateDeliveryDetails(int slot, String senderUUID, String deliveryUUID, int status)
    {
        String payload = deliveryUUID + UNIT_SEPARATOR + senderUUID + UNIT_SEPARATOR + String.valueOf(status);
        //TODO - this is a very embarrassing implementation and should be corrected
        if(slot == 0)
            DELIVERY_DETAILS_ITEM_SLOT_0 = payload;
        else if(slot == 1)
            DELIVERY_DETAILS_ITEM_SLOT_1 = payload;
        else if(slot == 2)
            DELIVERY_DETAILS_ITEM_SLOT_2 = payload;
        else if(slot == 3)
            DELIVERY_DETAILS_ITEM_SLOT_3 = payload;
        else if(slot == 4)
            DELIVERY_DETAILS_ITEM_SLOT_4 = payload;
        else if(slot == 5)
            DELIVERY_DETAILS_ITEM_SLOT_5 = payload;
        else if(slot == 6)
            DELIVERY_DETAILS_ITEM_SLOT_6 = payload;
        else if(slot == 7)
            DELIVERY_DETAILS_ITEM_SLOT_7 = payload;
        else if(slot == 8)
            DELIVERY_DETAILS_ITEM_SLOT_8 = payload;
        else if(slot == 9)
            DELIVERY_DETAILS_ITEM_SLOT_9 = payload;

        setDirty();
        debugDeliveryQueue();
    }

    public void debugDeliveryQueue()
    {
        System.out.println("---------- DELIVERY QUEUE ----------");
        System.out.println("0: " + DELIVERY_DETAILS_ITEM_SLOT_0);
        System.out.println("1: " + DELIVERY_DETAILS_ITEM_SLOT_1);
        System.out.println("2: " + DELIVERY_DETAILS_ITEM_SLOT_2);
        System.out.println("3: " + DELIVERY_DETAILS_ITEM_SLOT_3);
        System.out.println("4: " + DELIVERY_DETAILS_ITEM_SLOT_4);
        System.out.println("5: " + DELIVERY_DETAILS_ITEM_SLOT_5);
        System.out.println("6: " + DELIVERY_DETAILS_ITEM_SLOT_6);
        System.out.println("7: " + DELIVERY_DETAILS_ITEM_SLOT_7);
        System.out.println("8: " + DELIVERY_DETAILS_ITEM_SLOT_8);
        System.out.println("9: " + DELIVERY_DETAILS_ITEM_SLOT_9);
        System.out.println("------------------------------------");
    }

    //TODO - improve this later
    private String mergeUUID(String uuid, String dimension, String name, String bigFancyList)
    {
        ArrayList<String> uuids = new ArrayList<>(Arrays.asList(bigFancyList.split("\\" + RECORD_SEPARATOR)));
        boolean found = false;
        int x;
        int n = uuids.toArray().length;
        for(x=0; x<n; ++x)
        {
            ArrayList<String> entry = new ArrayList<>(Arrays.asList(uuids.get(x).split("\\" + UNIT_SEPARATOR)));
            if(Objects.equals(entry.get(RECORD_INDEX_UUID), uuid))
            {
                found = true;
                break;
            }
        }
        if(!found)
            uuids.add(uuid + UNIT_SEPARATOR + dimension + UNIT_SEPARATOR + name);
        String result = String.join(RECORD_SEPARATOR, uuids);
        if(Objects.equals(result.substring(0,1),RECORD_SEPARATOR))
        {
            if (result.length() > 1)
            {
                result = result.substring(1);
            }
        }
        return result;
    }

    private String unmergeUUID(String uuid, String bigFancyList)
    {
        ArrayList<String> uuids = new ArrayList<>(Arrays.asList(bigFancyList.split("\\" + RECORD_SEPARATOR)));
        ArrayList<String> unmerged = new ArrayList<>();
        boolean found = false;
        int x;
        int n = uuids.toArray().length;
        for(x=0; x<n; ++x)
        {
            String entryString = uuids.get(x);
            ArrayList<String> entrySplit = new ArrayList<>(Arrays.asList(entryString.split("\\" + UNIT_SEPARATOR)));
            String entryUUID = entrySplit.get(0);
            if(Objects.equals(entryUUID, uuid))
            {
                continue;
            }
            else 
            {
                unmerged.add(entryString);
            }
        }
        String result = String.join(RECORD_SEPARATOR, unmerged);
        if(result.length() > 0)
        {
            if (Objects.equals(result.substring(0, 1), RECORD_SEPARATOR))
            {
                if (result.length() > 1)
                {
                    result = result.substring(1);
                }
            }
            return result;
        }
        else return RECORD_SEPARATOR;
    }

    public void unregisterUUID(PhantomailboxBlockEntity be)
    {
        listOfAllPhantomailboxUUIDs = unmergeUUID(be.PhantomailboxDeliveryUUID, listOfAllPhantomailboxUUIDs);
        this.setDirty();
    }

    public void registerUUID(PhantomailboxBlockEntity be)
    {
        listOfAllPhantomailboxUUIDs = mergeUUID(be.PhantomailboxDeliveryUUID, be.getLevel().dimension().location().toString(), be.PhantomailboxDisplayAddress, listOfAllPhantomailboxUUIDs);
        this.setDirty();
    }

    ///  returns the index of the first piece of mail found in the queue addressed to a particular uuid
    public int getFirstPendingIndexAddressedToUUID(String uuid)
    {
        String[] entries = {
                DELIVERY_DETAILS_ITEM_SLOT_0,
                DELIVERY_DETAILS_ITEM_SLOT_1,
                DELIVERY_DETAILS_ITEM_SLOT_2,
                DELIVERY_DETAILS_ITEM_SLOT_3,
                DELIVERY_DETAILS_ITEM_SLOT_4,
                DELIVERY_DETAILS_ITEM_SLOT_5,
                DELIVERY_DETAILS_ITEM_SLOT_6,
                DELIVERY_DETAILS_ITEM_SLOT_7,
                DELIVERY_DETAILS_ITEM_SLOT_8,
                DELIVERY_DETAILS_ITEM_SLOT_9
        };

        int x;
        int n = entries.length;
        for(x=0;x<n;++x)
        {
            ArrayList<String> details = new ArrayList<>(Arrays.asList(entries[x].split("\\" + UNIT_SEPARATOR)));

            //pull any item from the queue intended for this mailbox
            boolean intendedDeliveryTarget = Objects.equals(details.get(DETAILS_INDEX_UUID_TO), uuid);
            //pull any item from the queue intended for a null mailbox
            boolean nullKey = Objects.equals(details.get(DETAILS_INDEX_UUID_TO),PhantomailboxBlockEntity.DEFAULT_UUID);
            //pull any item from the queue intended for a dead mailbox
            boolean staleMail = !validMailboxUUID(details.get(DETAILS_INDEX_UUID_TO));

            if (intendedDeliveryTarget || nullKey || staleMail)
            {
                try
                {
                    int status = Integer.valueOf(details.get(DETAILS_INDEX_STATUS));
                    if (status == PhantomailboxRegistrySavedData.DELIVERY_STATE_PENDING_MAIL)
                        return x;
                }
                catch(Exception e)
                {
                    continue;
                }
            }
        }
        return NO_SLOTS_AVAILABLE;
    }

    public ItemStack getPendingItemFromSlot(int slot)
    {
        if(slot == 0)
            return DELIVERY_QUEUE_ITEM_SLOT_0;
        else if(slot == 1)
            return DELIVERY_QUEUE_ITEM_SLOT_1;
        else if(slot == 2)
            return DELIVERY_QUEUE_ITEM_SLOT_2;
        else if(slot == 3)
            return DELIVERY_QUEUE_ITEM_SLOT_3;
        else if(slot == 4)
            return DELIVERY_QUEUE_ITEM_SLOT_4;
        else if(slot == 5)
            return DELIVERY_QUEUE_ITEM_SLOT_5;
        else if(slot == 6)
            return DELIVERY_QUEUE_ITEM_SLOT_6;
        else if(slot == 7)
            return DELIVERY_QUEUE_ITEM_SLOT_7;
        else if(slot == 8)
            return DELIVERY_QUEUE_ITEM_SLOT_8;
        else if(slot == 9)
            return DELIVERY_QUEUE_ITEM_SLOT_9;
        return null;
    }

    public void clearItemFromSlot(int slot)
    {
        if(slot == 0)
            DELIVERY_QUEUE_ITEM_SLOT_0 = ItemStack.EMPTY;
        else if(slot == 1)
            DELIVERY_QUEUE_ITEM_SLOT_1 = ItemStack.EMPTY;
        else if(slot == 2)
            DELIVERY_QUEUE_ITEM_SLOT_2 = ItemStack.EMPTY;
        else if(slot == 3)
            DELIVERY_QUEUE_ITEM_SLOT_3 = ItemStack.EMPTY;
        else if(slot == 4)
            DELIVERY_QUEUE_ITEM_SLOT_4 = ItemStack.EMPTY;
        else if(slot == 5)
            DELIVERY_QUEUE_ITEM_SLOT_5 = ItemStack.EMPTY;
        else if(slot == 6)
            DELIVERY_QUEUE_ITEM_SLOT_6 = ItemStack.EMPTY;
        else if(slot == 7)
            DELIVERY_QUEUE_ITEM_SLOT_7 = ItemStack.EMPTY;
        else if(slot == 8)
            DELIVERY_QUEUE_ITEM_SLOT_8 = ItemStack.EMPTY;
        else if(slot == 9)
            DELIVERY_QUEUE_ITEM_SLOT_9 = ItemStack.EMPTY;
        setDirty();
    }

    public int requestPendingMailSlot(@Nullable String uuidOfSendingMailbox)
    {
        String[] details = {
                DELIVERY_DETAILS_ITEM_SLOT_0,
                DELIVERY_DETAILS_ITEM_SLOT_1,
                DELIVERY_DETAILS_ITEM_SLOT_2,
                DELIVERY_DETAILS_ITEM_SLOT_3,
                DELIVERY_DETAILS_ITEM_SLOT_4,
                DELIVERY_DETAILS_ITEM_SLOT_5,
                DELIVERY_DETAILS_ITEM_SLOT_6,
                DELIVERY_DETAILS_ITEM_SLOT_7,
                DELIVERY_DETAILS_ITEM_SLOT_8,
                DELIVERY_DETAILS_ITEM_SLOT_9
        };
        int x;
        int n = details.length;

        //if uuid is provided...
        if(uuidOfSendingMailbox != null)
        {
            //scan through for a pending outbound slot for our uuid with an unknown destination and return that preferentially
            for (x = 0; x < n; ++x)
            {
                ArrayList<String> slotDetails = getDetailsFromSlot(x);
                String slotSenderUUID = slotDetails.get(DETAILS_INDEX_UUID_FROM);
                if (Objects.equals(slotSenderUUID, uuidOfSendingMailbox))
                {
                    return x;
                }
            }
        }

        //otherwise attempt to return an empty slot
        for(x=0;x<n;++x)
        {
            if(Objects.equals(details[x], DELIVERY_DETAILS_DEFAULT_DATA))
            {
                reserveSlot(x);
                return x;
            }
        }
        return NO_SLOTS_AVAILABLE;
    }

    private void reserveSlot(int index)
    {
        if(index == 0)
            DELIVERY_DETAILS_ITEM_SLOT_0 = DELIVERY_DETAILS_DEFAULT_DATA_RESERVED;
        else if(index == 1)
            DELIVERY_DETAILS_ITEM_SLOT_1 = DELIVERY_DETAILS_DEFAULT_DATA_RESERVED;
        else if(index == 2)
            DELIVERY_DETAILS_ITEM_SLOT_2 = DELIVERY_DETAILS_DEFAULT_DATA_RESERVED;
        else if(index == 3)
            DELIVERY_DETAILS_ITEM_SLOT_3 = DELIVERY_DETAILS_DEFAULT_DATA_RESERVED;
        else if(index == 4)
            DELIVERY_DETAILS_ITEM_SLOT_4 = DELIVERY_DETAILS_DEFAULT_DATA_RESERVED;
        else if(index == 5)
            DELIVERY_DETAILS_ITEM_SLOT_5 = DELIVERY_DETAILS_DEFAULT_DATA_RESERVED;
        else if(index == 6)
            DELIVERY_DETAILS_ITEM_SLOT_6 = DELIVERY_DETAILS_DEFAULT_DATA_RESERVED;
        else if(index == 7)
            DELIVERY_DETAILS_ITEM_SLOT_7 = DELIVERY_DETAILS_DEFAULT_DATA_RESERVED;
        else if(index == 8)
            DELIVERY_DETAILS_ITEM_SLOT_8 = DELIVERY_DETAILS_DEFAULT_DATA_RESERVED;
        else if(index == 9)
            DELIVERY_DETAILS_ITEM_SLOT_9 = DELIVERY_DETAILS_DEFAULT_DATA_RESERVED;
        setDirty();
    }

    public void deliveredSuccessfully(int index)
    {
        if(index == 0)
            DELIVERY_DETAILS_ITEM_SLOT_0 = DELIVERY_DETAILS_DEFAULT_DATA;
        else if(index == 1)
            DELIVERY_DETAILS_ITEM_SLOT_1 = DELIVERY_DETAILS_DEFAULT_DATA;
        else if(index == 2)
            DELIVERY_DETAILS_ITEM_SLOT_2 = DELIVERY_DETAILS_DEFAULT_DATA;
        else if(index == 3)
            DELIVERY_DETAILS_ITEM_SLOT_3 = DELIVERY_DETAILS_DEFAULT_DATA;
        else if(index == 4)
            DELIVERY_DETAILS_ITEM_SLOT_4 = DELIVERY_DETAILS_DEFAULT_DATA;
        else if(index == 5)
            DELIVERY_DETAILS_ITEM_SLOT_5 = DELIVERY_DETAILS_DEFAULT_DATA;
        else if(index == 6)
            DELIVERY_DETAILS_ITEM_SLOT_6 = DELIVERY_DETAILS_DEFAULT_DATA;
        else if(index == 7)
            DELIVERY_DETAILS_ITEM_SLOT_7 = DELIVERY_DETAILS_DEFAULT_DATA;
        else if(index == 8)
            DELIVERY_DETAILS_ITEM_SLOT_8 = DELIVERY_DETAILS_DEFAULT_DATA;
        else if(index == 9)
            DELIVERY_DETAILS_ITEM_SLOT_9 = DELIVERY_DETAILS_DEFAULT_DATA;
        setDirty();
        debugDeliveryQueue();
    }

    public ArrayList<String> getDetailsFromSlot(int slot)
    {
        if(slot == 0)
            return new ArrayList<>(Arrays.asList(DELIVERY_DETAILS_ITEM_SLOT_0.split("\\" + UNIT_SEPARATOR)));
        else if(slot == 1)
            return new ArrayList<>(Arrays.asList(DELIVERY_DETAILS_ITEM_SLOT_1.split("\\" + UNIT_SEPARATOR)));
        else if(slot == 2)
            return new ArrayList<>(Arrays.asList(DELIVERY_DETAILS_ITEM_SLOT_2.split("\\" + UNIT_SEPARATOR)));
        else if(slot == 3)
            return new ArrayList<>(Arrays.asList(DELIVERY_DETAILS_ITEM_SLOT_3.split("\\" + UNIT_SEPARATOR)));
        else if(slot == 4)
            return new ArrayList<>(Arrays.asList(DELIVERY_DETAILS_ITEM_SLOT_4.split("\\" + UNIT_SEPARATOR)));
        else if(slot == 5)
            return new ArrayList<>(Arrays.asList(DELIVERY_DETAILS_ITEM_SLOT_5.split("\\" + UNIT_SEPARATOR)));
        else if(slot == 6)
            return new ArrayList<>(Arrays.asList(DELIVERY_DETAILS_ITEM_SLOT_6.split("\\" + UNIT_SEPARATOR)));
        else if(slot == 7)
            return new ArrayList<>(Arrays.asList(DELIVERY_DETAILS_ITEM_SLOT_7.split("\\" + UNIT_SEPARATOR)));
        else if(slot == 8)
            return new ArrayList<>(Arrays.asList(DELIVERY_DETAILS_ITEM_SLOT_8.split("\\" + UNIT_SEPARATOR)));
        else if(slot == 9)
            return new ArrayList<>(Arrays.asList(DELIVERY_DETAILS_ITEM_SLOT_9.split("\\" + UNIT_SEPARATOR)));
        else
            return new ArrayList<>(Arrays.asList(DELIVERY_DETAILS_DEFAULT_DATA.split("\\" + UNIT_SEPARATOR)));
    }

    public boolean validMailboxUUID(String uuid)
    {
        ArrayList<String> uuids = new ArrayList<>(Arrays.asList(listOfAllPhantomailboxUUIDs.split("\\" + RECORD_SEPARATOR)));
        boolean found = false;
        int x;
        int n = uuids.toArray().length;
        for(x=0; x<n; ++x)
        {
            ArrayList<String> entry = new ArrayList<>(Arrays.asList(uuids.get(x).split("\\" + UNIT_SEPARATOR)));
            if(Objects.equals(entry.get(RECORD_INDEX_UUID), uuid))
            {
                found = true;
                break;
            }
        }
        if(!found)
            return false;
        return true;
    }
}