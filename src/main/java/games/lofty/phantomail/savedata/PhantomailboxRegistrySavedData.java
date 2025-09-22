package games.lofty.phantomail.savedata;

import games.lofty.phantomail.Phantomail;
import games.lofty.phantomail.block.entity.PhantomailboxBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PhantomailboxRegistrySavedData extends SavedData
{
    public static final String LIST_OF_ALL_PHANTOMAILBOX_UUIDS = Phantomail.MOD_ID + ":" + "list_of_all_phantomailbox_uuids";
    public String listOfAllPhantomailboxUUIDs = "";

    //this is a very bad way to implement this, but I can understand it and implement it with my limited knowledge of the api
    public static final String DELIVERY_KEY_SLOT_0_DETAILS = Phantomail.MOD_ID + ":" + "deliverydetails_slot_0";
    public static final String DELIVERY_KEY_SLOT_0_ITEM = Phantomail.MOD_ID + ":" + "deliveryitem_slot_0";
    public ItemStack DELIVERY_QUEUE_ITEM_SLOT_0 = ItemStack.EMPTY;
    public String DELIVERY_DETAILS_ITEM_SLOT_0 = "-|-|-";

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

        return tag;
    }

    //TODO - improve this later
    private String mergeUUID(String uuid, String bigFancyList)
    {
        ArrayList<String> uuids = new ArrayList<>(Arrays.asList(bigFancyList.split("\\|")));
        boolean found = false;
        int x;
        int n = uuids.toArray().length;
        for(x=0; x<n; ++x)
        {
            if(Objects.equals(uuids.get(x), uuid))
            {
                found = true;
                break;
            }
        }
        if(!found)
            uuids.add(uuid);
        String result = String.join("|", uuids);
        if(Objects.equals(result.substring(0,1),"|"))
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
        ArrayList<String> uuids = new ArrayList<>(Arrays.asList(bigFancyList.split("\\|")));
        ArrayList<String> unmerged = new ArrayList<>();
        boolean found = false;
        int x;
        int n = uuids.toArray().length;
        for(x=0; x<n; ++x)
        {
            String entry = uuids.get(x);
            System.out.println("->" + entry);
            if(Objects.equals(entry, uuid))
            {
                System.out.println("X");
                continue;
            }
            else 
            {
                System.out.println("O");
                unmerged.add(entry);
            }
        }
        String result = String.join("|", unmerged);
        if(Objects.equals(result.substring(0,1),"|"))
        {
            if (result.length() > 1)
            {
                result = result.substring(1);
            }
        }
        return result;
    }

    public void unregisterUUID(PhantomailboxBlockEntity be)
    {
        listOfAllPhantomailboxUUIDs = unmergeUUID(be.PhantomailboxDeliveryUUID, listOfAllPhantomailboxUUIDs);
        this.setDirty();
    }

    public void registerUUID(PhantomailboxBlockEntity be)
    {
        listOfAllPhantomailboxUUIDs = mergeUUID(be.PhantomailboxDeliveryUUID, listOfAllPhantomailboxUUIDs);
        this.setDirty();
    }
}