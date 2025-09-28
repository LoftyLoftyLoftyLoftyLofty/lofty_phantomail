package games.lofty.phantomail.entity.custom;

import games.lofty.phantomail.block.entity.PhantomailboxBlockEntity;
import games.lofty.phantomail.savedata.PhantomailboxRegistrySavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class PhantomailCourierEntity extends FlyingMob
{
    //These constants indicate the window of time on a server each night when couriers can be active
    /// Tick at which light level hits 4, during night time in the overworld
    public static final int TIME_DARKEST_NIGHT = 13670;
    /// Tick at which light level begins to rise, approaching dawn in the overworld
    public static final int TIME_ABANDON_SORTIE = 22331;

    public final AnimationState glideAnimationState = new AnimationState();
    public final AnimationState flapAnimationState = new AnimationState();

    public static AttributeSupplier.Builder createAttributes()
    {
        //these 3 attributes must be specified or the mob won't spawn ingame
        return Phantom.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 10)
                .add(Attributes.MOVEMENT_SPEED, 1F)
                .add(Attributes.FOLLOW_RANGE, 16F)
                .add(Attributes.FLYING_SPEED, 1F);
    }

    public PhantomailCourierEntity(EntityType<? extends FlyingMob> entityType, Level level)
    {
        super(entityType, level);

        //do not despawn the courier unless correct conditions are met
        this.setPersistenceRequired();

        //disable pathfinding for everything
        for(int x=0;x<PathType.values().length;++x)
            this.setPathfindingMalus(Arrays.stream(PathType.values()).toList().get(x), -1.0F);
        //...except these:
        this.setPathfindingMalus(PathType.OPEN, 0.0F);
        this.setPathfindingMalus(PathType.BREACH, 0.0F);
        this.setPathfindingMalus(PathType.WALKABLE, 0.0F);
        this.setPathfindingMalus(PathType.DOOR_OPEN, 0.0F);
        this.setPathfindingMalus(PathType.RAIL, 0.0F);
        this.setPathfindingMalus(PathType.UNPASSABLE_RAIL, 0.0F);
        this.setPathfindingMalus(PathType.POWDER_SNOW, 0.0F);
        this.setPathfindingMalus(PathType.WATER_BORDER, 0.0F);

        //set xp for murdering the courier
        this.xpReward = 5;

        this.moveControl = new FlyingMoveControl(this, 10, false);
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {return false; }

    @Override
    public SoundSource getSoundSource() { return SoundSource.NEUTRAL; }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.PHANTOM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.0F;
    }

    @Override
    public boolean canAttackType(EntityType<?> type) {
        return true;
    }

    @Override
    public void aiStep()
    {
        //ignite in the sunlight
        if (this.isAlive() && this.isSunBurnTick()) {
            this.igniteForSeconds(8.0F);
        }
        super.aiStep();
    }

    @Override
    protected void registerGoals()
    {
        //goal system not necessary
        this.removeFreeWill();
    }

    public static final boolean enableDebug = true;
    private void debugMe(String d)
    {
        if(enableDebug)
        {
            if(!this.level().isClientSide())
                System.out.println("(S): " + d);
            else
                System.out.println("(C): " + d);
        }
    }

    public void populateNextDeliveryItemFromQueue(PhantomailboxBlockEntity phantomailboxBlockEntity)
    {
        if(level().isClientSide == false)
        {
            debugMe("Populating next delivery item from queue...");
            //if the mailbox we're targeting has an initialized uuid
            if (!Objects.equals(phantomailboxBlockEntity.PhantomailboxDeliveryUUID, PhantomailboxBlockEntity.DEFAULT_UUID))
            {
                debugMe("mailbox uuid is initialized");
                //get the queue
                PhantomailboxRegistrySavedData prsd = PhantomailboxRegistrySavedData.fromMailbox(phantomailboxBlockEntity);

                //find the first item in queue for this mailbox
                int slot = prsd.getFirstPendingIndexAddressedToUUID(phantomailboxBlockEntity.PhantomailboxDeliveryUUID);
                if (slot != PhantomailboxRegistrySavedData.NO_SLOTS_AVAILABLE)
                {
                    debugMe("first pending slot = " + String.valueOf(slot));
                    //TODO - treat this differently if unsafe item transport is disabled

                    //grab the item from the queue
                    ItemStack pendingItem = prsd.getPendingItemFromSlot(slot);
                    currentlyHeldDeliveryItem = pendingItem.copy();
                    prsd.clearItemFromSlot(slot);

                    ArrayList<String> details = prsd.getDetailsFromSlot(slot);

                    //assign delivery data to the courier for pushing updates to the queue later
                    currentlyHandlingDeliverySlotIndex = slot;
                    currentlyHandlingDeliveryUUIDSender = details.get(PhantomailboxRegistrySavedData.DETAILS_INDEX_UUID_FROM);
                    currentlyHandlingDeliveryUUIDReceiver = details.get(PhantomailboxRegistrySavedData.DETAILS_INDEX_UUID_TO);

                    //update the delivery status to be in-progress
                    prsd.updateDeliveryDetails(slot,currentlyHandlingDeliveryUUIDSender,currentlyHandlingDeliveryUUIDReceiver,PhantomailboxRegistrySavedData.DELIVERY_STATE_COURIER_DROPPING_OFF);
                }
                else debugMe("Nothing to deliver to this mailbox");
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound)
    {
        super.addAdditionalSaveData(compound);

        //serialize the spawn position
        compound.putInt("spawnPositionX", spawnPosition.getX());
        compound.putInt("spawnPositionY", spawnPosition.getY());
        compound.putInt("spawnPositionZ", spawnPosition.getZ());

        //serialize the last known target mailbox position
        compound.putInt("lastKnownTargetMailboxPositionX", lastKnownTargetMailboxPos.getX());
        compound.putInt("lastKnownTargetMailboxPositionY", lastKnownTargetMailboxPos.getY());
        compound.putInt("lastKnownTargetMailboxPositionZ", lastKnownTargetMailboxPos.getZ());

        //serialize held items
        compound.put("currentlyHeldDeliveryItem", currentlyHeldDeliveryItem.saveOptional(this.registryAccess()));

        //serialize the current delivery queue slot this courier is responsible for
        compound.putInt("currentlyHandlingDeliverySlotIndex", currentlyHandlingDeliverySlotIndex);

        //sender/receiver uuids
        compound.putString("currentlyHandlingDeliveryUUIDSender", currentlyHandlingDeliveryUUIDSender);
        compound.putString("currentlyHandlingDeliveryUUIDReceiver", currentlyHandlingDeliveryUUIDReceiver);

        //current behavior state
        compound.putInt("phantomailCourierCurrentBehaviorState", phantomailCourierCurrentBehaviorState);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound)
    {
        super.readAdditionalSaveData(compound);

        //deserialize the spawn position
        spawnPosition = new BlockPos(
                compound.getInt("spawnPositionX"),
                compound.getInt("spawnPositionY"),
                compound.getInt("spawnPositionZ")
        );

        //deserialize the last known target mailbox position
        lastKnownTargetMailboxPos = new BlockPos(
                compound.getInt("lastKnownTargetMailboxPositionX"),
                compound.getInt("lastKnownTargetMailboxPositionY"),
                compound.getInt("lastKnownTargetMailboxPositionZ")
        );

        //deserialize held items
        currentlyHeldDeliveryItem = ItemStack.parseOptional(this.registryAccess(),compound.getCompound("currentlyHeldDeliveryItem"));
        //deserialize the current delivery queue slot this courier is responsible for
        currentlyHandlingDeliverySlotIndex = compound.getInt("currentlyHandlingDeliverySlotIndex");

        //sender/receiver uuids
        currentlyHandlingDeliveryUUIDSender = compound.getString("currentlyHandlingDeliveryUUIDSender");
        currentlyHandlingDeliveryUUIDReceiver = compound.getString("currentlyHandlingDeliveryUUIDReceiver");

        //current behavior state
        phantomailCourierCurrentBehaviorState = compound.getInt("phantomailCourierCurrentBehaviorState");
    }

    //--------------------------------------------------------------------------
    //COURIER DATA THAT IS PRESERVED WHEN THE COURIER IS UNLOADED:
    //--------------------------------------------------------------------------

    /// The position that the courier was spawned at, also the position it returns to after delivering mail before it can despawn.
    public BlockPos spawnPosition = null;

    /// The position of the mailbox that the courier wants to visit.
    public BlockPos lastKnownTargetMailboxPos = null;

    /// The current behavior state of the courier.
    public int phantomailCourierCurrentBehaviorState = 0;
    //state constants
    public static final int PHANTOMAIL_COURIER_BEHAVIOR_STATE_INIT = 0;
    public static final int PHANTOMAIL_COURIER_BEHAVIOR_STATE_INGRESS = 1;
    public static final int PHANTOMAIL_COURIER_BEHAVIOR_STATE_INTERACT_MAILBOX = 2;
    public static final int PHANTOMAIL_COURIER_BEHAVIOR_STATE_EGRESS = 3;
    public static final int PHANTOMAIL_COURIER_BEHAVIOR_STATE_DESPAWN = 4;

    /// The ItemStack to be delivered to a mailbox - couriers hold one at a time
    public ItemStack currentlyHeldDeliveryItem = ItemStack.EMPTY;
    public int currentlyHandlingDeliverySlotIndex = PhantomailboxRegistrySavedData.NO_SLOTS_AVAILABLE;
    public String currentlyHandlingDeliveryUUIDSender = PhantomailboxBlockEntity.DEFAULT_UUID;
    public String currentlyHandlingDeliveryUUIDReceiver = PhantomailboxBlockEntity.DEFAULT_UUID;

    //--------------------------------------------------------------------------
    // COURIER DATA THAT IS NOT SAVED WHEN THE COURIER IS UNLOADED:
    //--------------------------------------------------------------------------

    /// This is used to give the courier pathfinding directions.
    FlyingPathNavigation phantomailCourierPathNavigation = null;

    /// If we fail to pathfind successfully, every other attempt will add random offsets to the desired destination, in an attempt to force new route calculations.
    boolean phantomailCourierPathNavigationRandomized = false;//true if we're randomizing around to get a better path

    /// This value steadily increases as pathfinding attempts fail, resulting in a larger search radius for a valid path.
    double pathDesperationRadius = 1.5;//steadily increasing search radius for random alternate destinations to try to force a valid path to the target

    /// returns true if a delivery needs to be abandoned because the sun is coming
    public static boolean shouldAbandonSortieDueToTimeConstraints(Level level)
    {return (level.getDayTime() < TIME_DARKEST_NIGHT) || (level.getDayTime() > TIME_ABANDON_SORTIE);}

    public static Vec3 rotateVector(Vec3 v, Vec3 k, double angle)
    {
        Vec3 kNormalized = k.normalize();
        double cosTheta = Math.cos(angle);
        double sinTheta = Math.sin(angle);
        Vec3 term1 = v.scale(cosTheta);
        Vec3 term2 = kNormalized.cross(v).scale(sinTheta);
        Vec3 term3 = kNormalized.scale(kNormalized.dot(v) * (1 - cosTheta));
        return term1.add(term2).add(term3);
    }

    Vec3 steadyForwardVelocity()
    {
        return rotateVector(new Vec3(1,0,0), new Vec3(0,1,0), Math.PI * getYRot() / 180);
    }

    //TODO - this isn't working and seems to make the pathfinder abort what it's doing

    //it would be cool to have the courier circle in above the mailbox but that polish may have to wait until later
    //for the sake of shipping the mod at all

    public void nudgeForward()
    {
        Vec3 v = steadyForwardVelocity();
        Vec3 cur = getDeltaMovement();
        Vec3 flat = new Vec3(cur.x, 0, cur.z);
        double curFlat = flat.length();
        System.out.println("VX: " + flat.toString());
        System.out.println("XR: " + String.valueOf(getXRot()));
        System.out.println("YR: " + String.valueOf(getYRot()));
        System.out.println("CF: " + String.valueOf(curFlat));

        //normal movement with the flying movecontroller is between 0.14 and 0.17 on average, so we nudge forward gently here to 0.18
        double desiredFactor = 0.18 - curFlat;
        if(desiredFactor < 0)
            desiredFactor = 0;

        //not sure why this has to be a negative value to make the phantom move forward
        addDeltaMovement(v.scale(0.1 * desiredFactor));
    }

    /// Functions that get performed during the initialization tick of the courier
    private void handleStateMachineInit(PhantomailboxBlockEntity targetMailbox)
    {
        //System.out.println("ENTER INIT Sender / Receiver: " + currentlyHandlingDeliveryUUIDSender + " / " + currentlyHandlingDeliveryUUIDReceiver);

        //on our initialization tick, update our saved spawn position
        spawnPosition = this.blockPosition();

        //if we were spawned without a target mailbox being attached (spawn egg) then make an attempt to find a nearby mailbox
        if (targetMailbox == null)
        {
            //TODO - expand this to search a 3x3 chunk range
            //TODO - sort results by distance and choose the closest mailbox
            var chunk = level().getChunkAt(blockPosition());
            var relevantEntities = chunk.getBlockEntitiesPos().toArray();
            for (int x = 0; x < relevantEntities.length; ++x)
            {
                if (chunk.getBlockEntity((BlockPos) relevantEntities[x]) instanceof PhantomailboxBlockEntity phantomailboxBlockEntity)
                {
                    targetMailbox = phantomailboxBlockEntity;
                    lastKnownTargetMailboxPos = (BlockPos) relevantEntities[x];
                    break;
                }
            }
        }

        if (targetMailbox == null)
        {
            //if we don't have a target mailbox after searching for one, there's nothing for us to do, so we can just leave
            phantomailCourierCurrentBehaviorState = PHANTOMAIL_COURIER_BEHAVIOR_STATE_EGRESS;
        }
        else
        {
            //move forward with approaching the mailbox
            phantomailCourierCurrentBehaviorState = PHANTOMAIL_COURIER_BEHAVIOR_STATE_INGRESS;

            //if there are any items in the queue that need to be delivered to this particular mailbox, grab the first one
            populateNextDeliveryItemFromQueue(targetMailbox);
        }
    }

    private void handleStateMachineIngress(PhantomailboxBlockEntity targetMailbox)
    {
        //System.out.println("ENTER INGRESS Sender / Receiver: " + currentlyHandlingDeliveryUUIDSender + " / " + currentlyHandlingDeliveryUUIDReceiver);
        //nudgeForward();

        //if we don't have a target mailbox, abandon sortie
        if ((targetMailbox == null) || shouldAbandonSortieDueToTimeConstraints(this.level()))
        {
            phantomailCourierCurrentBehaviorState = PHANTOMAIL_COURIER_BEHAVIOR_STATE_EGRESS;
            phantomailCourierPathNavigation = null;
        }
        else
        {
            //if we haven't tried to build a path yet, start trying
            if (phantomailCourierPathNavigation == null)
            {
                phantomailCourierPathNavigation = new FlyingPathNavigation(this, level());
                phantomailCourierPathNavigation.setCanOpenDoors(false);
                phantomailCourierPathNavigation.setCanFloat(false);
                phantomailCourierPathNavigation.setCanPassDoors(true);
                phantomailCourierPathNavigation.setMaxVisitedNodesMultiplier(10.0F);
                phantomailCourierPathNavigation.moveTo(
                        (double) targetMailbox.getBlockPos().getX(),
                        (double) targetMailbox.getBlockPos().getY(),
                        (double) targetMailbox.getBlockPos().getZ(),
                        0,
                        this.getAttributeValue(Attributes.FLYING_SPEED)
                );
            }
            //if we've already started a pathfinding job...
            else
            {
                if (phantomailCourierPathNavigation.isInProgress())
                {
                    //debugMe("(Arrival) Awaiting path result...");
                    phantomailCourierPathNavigation.tick();
                }

                //...and it's finished
                if (phantomailCourierPathNavigation.isDone())
                {
                    //...and it succeeded
                    if (phantomailCourierPathNavigation.getPath() != null)
                    {
                        //...reasonably close to the goal
                        if (this.blockPosition().distManhattan(lastKnownTargetMailboxPos) <= 1)
                        {
                            phantomailCourierCurrentBehaviorState = PHANTOMAIL_COURIER_BEHAVIOR_STATE_INTERACT_MAILBOX;
                        }
                        //...not close enough, repath
                        else
                        {
                            if (phantomailCourierPathNavigationRandomized == false)
                            {
                                //try a randomized position this time - we flip back and forth between choosing random offsets toward the destination and choosing the actual destination
                                phantomailCourierPathNavigationRandomized = true;

                                //calculate a random offset in the general direction of the target
                                BlockPos delta = new BlockPos(
                                        lastKnownTargetMailboxPos.getX() - this.blockPosition().getX(),
                                        lastKnownTargetMailboxPos.getY() - this.blockPosition().getY(),
                                        lastKnownTargetMailboxPos.getZ() - this.blockPosition().getZ()
                                );

                                //steadily increase our desperation search radius until we are either successful or need to egress
                                pathDesperationRadius += 1.5;
                                phantomailCourierPathNavigation.moveTo(
                                        (double) this.blockPosition().getX() + ((random.nextFloat() * pathDesperationRadius) - (pathDesperationRadius * 0.5)) + delta.getX(),
                                        (double) this.blockPosition().getY() + ((random.nextFloat() * pathDesperationRadius) - (pathDesperationRadius * 0.5)) + delta.getY(),
                                        (double) this.blockPosition().getZ() + ((random.nextFloat() * pathDesperationRadius) - (pathDesperationRadius * 0.5)) + delta.getZ(),
                                        2,
                                        this.getAttributeValue(Attributes.FLYING_SPEED)
                                );
                            } else
                            {
                                phantomailCourierPathNavigation = null;
                                phantomailCourierPathNavigationRandomized = false;
                            }
                        }
                    }
                    //path creation failed
                    else
                    {
                        //scenario where the path failed because we're already at the target (awkward)
                        if (this.blockPosition().distManhattan(lastKnownTargetMailboxPos) <= 1)
                        {
                            phantomailCourierCurrentBehaviorState = PHANTOMAIL_COURIER_BEHAVIOR_STATE_INTERACT_MAILBOX;
                        }
                        //scenario where path creation failed for some other reason: abandon sortie
                        else
                        {
                            phantomailCourierCurrentBehaviorState = PHANTOMAIL_COURIER_BEHAVIOR_STATE_EGRESS;
                            phantomailCourierPathNavigation = null;
                        }
                    }
                }
            }
        }
    }
    private void handleStateMachineInteract(PhantomailboxBlockEntity targetMailbox)
    {
        //System.out.println("ENTER INTERACT Sender / Receiver: " + currentlyHandlingDeliveryUUIDSender + " / " + currentlyHandlingDeliveryUUIDReceiver);

        if ((targetMailbox == null) || shouldAbandonSortieDueToTimeConstraints(this.level()))
        {
            phantomailCourierCurrentBehaviorState = PHANTOMAIL_COURIER_BEHAVIOR_STATE_EGRESS;
            phantomailCourierPathNavigation = null;
        }
        else
        {
            debugMe("(Interact) Beginning interaction logic...");
            //get our savedata
            PhantomailboxRegistrySavedData prsd = PhantomailboxRegistrySavedData.fromMailbox(targetMailbox);

            //drop off goods belonging to this mailbox
            if (currentlyHeldDeliveryItem.isEmpty() == false)
            {
                debugMe("(Interact) Attempting to deliver an item...");
                //is there room in the mailbox?
                if(PhantomailboxBlockEntity.getAvailableInboundMailSlots(targetMailbox) > 0)
                {
                    debugMe("(Interact) There is room in the mailbox for the item");
                    //deliver the item successfully
                    int insertSlot = PhantomailboxBlockEntity.getNextOpenSlot(targetMailbox);
                    targetMailbox.inventory.setStackInSlot(insertSlot,currentlyHeldDeliveryItem.copy());
                    currentlyHeldDeliveryItem = ItemStack.EMPTY;

                    //clear the delivery queue entry
                    prsd.deliveredSuccessfully(currentlyHandlingDeliverySlotIndex);

                    //clear the slot too
                    currentlyHandlingDeliverySlotIndex = PhantomailboxRegistrySavedData.NO_SLOTS_AVAILABLE;

                    //tell the mailbox to pulse
                    targetMailbox.emitRedstoneSignal(true);
                }
                else debugMe("(Interact) No room in mailbox for incoming item");
            }
            else debugMe("(Interact) No items to deliver");

            //the courier can only hold one thing at a time. if we were blocked from delivering an item, don't overwrite it with an outgoing item because we have to return it to the queue
            if(currentlyHeldDeliveryItem.isEmpty())
            {
                //pick up goods stored in this mailbox only if we can reserve a delivery queue slot for them
                debugMe("(Interact) Checking outgoing mail conditions");

                //grab the receiver's uuid from the stamp
                ItemStack stamp = targetMailbox.inventory.getStackInSlot(PhantomailboxBlockEntity.SLOT_STAMP);

                //verify we have an outbound item
                ItemStack outgoing = targetMailbox.inventory.getStackInSlot(PhantomailboxBlockEntity.SLOT_OUTGOING);

                //only handle outgoing mail if we have a stamp and an outbound item
                //TODO verify the stamp is addressed here
                if ((stamp.isEmpty() == false) && (outgoing.isEmpty() == false))
                {
                    debugMe("(Interact) Stamp and outbound item are present");

                    int reqSlot = prsd.requestPendingMailSlot(targetMailbox.PhantomailboxDeliveryUUID);
                    if (reqSlot != PhantomailboxRegistrySavedData.NO_SLOTS_AVAILABLE)
                    {
                        debugMe("(Interact) requested slot is available to handle a reservation: " + String.valueOf(reqSlot));

                        //TODO - TEST DATA, NEEDS FIX
                        String infoToUUID = targetMailbox.PhantomailboxDeliveryUUID;

                        //grab the sender's uuid from the mailbox
                        String infoFromUUID = targetMailbox.PhantomailboxDeliveryUUID;

                        //give the item to the courier - the courier needs to despawn holding the item before it transfers to the system for pending delivery

                        //TODO if unsafe item transport is disabled, just put the item in the system for delivery now
                        if(false)
                        {
                        }
                        else
                        {
                            currentlyHandlingDeliverySlotIndex = reqSlot;
                            currentlyHeldDeliveryItem = outgoing.copy();
                            currentlyHandlingDeliveryUUIDSender = infoFromUUID;
                            currentlyHandlingDeliveryUUIDReceiver = infoToUUID;
                        }

                        debugMe("(Interact) Clearing inventory slots in the mailbox");

                        //clear the slot in the mailbox
                        targetMailbox.inventory.setStackInSlot(PhantomailboxBlockEntity.SLOT_OUTGOING, ItemStack.EMPTY);//working

                        //update the delivery details
                        //TODO - infoToUUID should come from reading component tags on the stamp
                        prsd.updateDeliveryDetails(reqSlot, infoFromUUID, infoToUUID, PhantomailboxRegistrySavedData.DELIVERY_STATE_COURIER_PICKING_UP);

                        //remove the stamp
                        targetMailbox.inventory.setStackInSlot(PhantomailboxBlockEntity.SLOT_STAMP, ItemStack.EMPTY);//working

                        //tell the mailbox to pulse
                        targetMailbox.emitRedstoneSignal(true);
                    }
                    else
                    {
                        debugMe("(Interact) ...but no system slots are available for this delivery");
                        debugMe("currentlyHandlingDeliverySlotIndex: " + String.valueOf(currentlyHandlingDeliverySlotIndex));
                        debugMe("currentlyHandlingDeliveryUUIDSender: " + currentlyHandlingDeliveryUUIDSender);
                        debugMe("currentlyHandlingDeliveryUUIDReceiver: " + currentlyHandlingDeliveryUUIDReceiver);
                    }
                }
                else
                {
                    //TODO: if we have reserved a delivery slot in the queue, un-reserve it
                    //identified by sender = us, receiver = "?"
                }
            }
            debugMe("(Interact) Moving to egress state");
            //return to the sky
            phantomailCourierCurrentBehaviorState = PHANTOMAIL_COURIER_BEHAVIOR_STATE_EGRESS;
            phantomailCourierPathNavigation = null;
        }
    }
    private void handleStateMachineEgress(PhantomailboxBlockEntity targetMailbox)
    {
        //System.out.println("ENTER EGRESS Sender / Receiver: " + currentlyHandlingDeliveryUUIDSender + " / " + currentlyHandlingDeliveryUUIDReceiver);

        //if we haven't tried to build a path yet, start trying
        if (phantomailCourierPathNavigation == null)
        {
            debugMe("(Egress) Building path from " + blockPosition().toString() + " to " + spawnPosition.toString());
            phantomailCourierPathNavigation = new FlyingPathNavigation(this, level());
            phantomailCourierPathNavigation.setCanOpenDoors(false);
            phantomailCourierPathNavigation.setCanFloat(false);
            phantomailCourierPathNavigation.setCanPassDoors(true);
            phantomailCourierPathNavigation.setMaxVisitedNodesMultiplier(10.0F);
            phantomailCourierPathNavigation.moveTo(
                    (double) spawnPosition.getX(),
                    (double) spawnPosition.getY(),
                    (double) spawnPosition.getZ(),
                    0,
                    this.getAttributeValue(Attributes.FLYING_SPEED)
            );
        }
        //if we've already started a pathfinding job...
        else
        {
            if (phantomailCourierPathNavigation.isInProgress())
            {
                //debugMe("(Arrival) Awaiting path result...");
                phantomailCourierPathNavigation.tick();
            }

            if (phantomailCourierPathNavigation.isStuck())
            {
                debugMe("(Egress) Stuck");
            }

            //...and it's finished
            if (phantomailCourierPathNavigation.isDone())
            {
                debugMe("(Egress) Done");
                //...and it succeeded
                if (phantomailCourierPathNavigation.getPath() != null)
                {
                    debugMe("(Egress) Path not null");
                    if ((this.blockPosition().distManhattan(spawnPosition) <= 5) && (level().canSeeSky(this.blockPosition())))
                    {
                        debugMe("(Egress) Path arrived at correct block");
                        phantomailCourierCurrentBehaviorState = PHANTOMAIL_COURIER_BEHAVIOR_STATE_DESPAWN;
                    }
                    else
                    {
                        debugMe("(Egress) Path arrived at incorrect block");
                        if (phantomailCourierPathNavigationRandomized == false)
                        {
                            //try a randomized position this time - we flip back and forth between choosing random offsets toward the destination and choosing the actual destination
                            phantomailCourierPathNavigationRandomized = true;

                            //steadily increase our desperation search radius until we are either successful or need to egress
                            pathDesperationRadius += 1.5;
                            phantomailCourierPathNavigation.moveTo(
                                    (double) this.blockPosition().getX() + ((random.nextFloat() * pathDesperationRadius) - (pathDesperationRadius * 0.5)),
                                    (double) this.blockPosition().getY() + ((random.nextFloat() * pathDesperationRadius) - (pathDesperationRadius * 0.5)),
                                    (double) this.blockPosition().getZ() + ((random.nextFloat() * pathDesperationRadius) - (pathDesperationRadius * 0.5)),
                                    2,
                                    this.getAttributeValue(Attributes.FLYING_SPEED)
                            );
                        }
                        else
                        {
                            phantomailCourierPathNavigation = null;
                            phantomailCourierPathNavigationRandomized = false;
                        }
                    }
                }
                else
                {
                    //keep trying to leave
                    debugMe("(Egress) Path is null");
                    phantomailCourierPathNavigation = null;
                }
            }
        }
    }
    private void handleStateMachineDespawn(PhantomailboxBlockEntity targetMailbox)
    {
        //System.out.println("ENTER DESPAWN Sender / Receiver: " + currentlyHandlingDeliveryUUIDSender + " / " + currentlyHandlingDeliveryUUIDReceiver);
        //get our savedata
        PhantomailboxRegistrySavedData prsd = PhantomailboxRegistrySavedData.fromMailbox(targetMailbox);

        //if we are holding an item, put it back into the system for redelivery later
        if(currentlyHandlingDeliverySlotIndex != PhantomailboxBlockEntity.NO_SLOTS_AVAILABLE)
        {
            debugMe("returning item to slot: " + String.valueOf(currentlyHandlingDeliverySlotIndex));
            if(currentlyHeldDeliveryItem.isEmpty() == false)
            {
                //TODO fix this nonsense
                if (currentlyHandlingDeliverySlotIndex == 0)
                    prsd.DELIVERY_QUEUE_ITEM_SLOT_0 = currentlyHeldDeliveryItem;
                else if (currentlyHandlingDeliverySlotIndex == 1)
                    prsd.DELIVERY_QUEUE_ITEM_SLOT_1 = currentlyHeldDeliveryItem;
                else if (currentlyHandlingDeliverySlotIndex == 2)
                    prsd.DELIVERY_QUEUE_ITEM_SLOT_2 = currentlyHeldDeliveryItem;
                else if (currentlyHandlingDeliverySlotIndex == 3)
                    prsd.DELIVERY_QUEUE_ITEM_SLOT_3 = currentlyHeldDeliveryItem;
                else if (currentlyHandlingDeliverySlotIndex == 4)
                    prsd.DELIVERY_QUEUE_ITEM_SLOT_4 = currentlyHeldDeliveryItem;
                else if (currentlyHandlingDeliverySlotIndex == 5)
                    prsd.DELIVERY_QUEUE_ITEM_SLOT_5 = currentlyHeldDeliveryItem;
                else if (currentlyHandlingDeliverySlotIndex == 6)
                    prsd.DELIVERY_QUEUE_ITEM_SLOT_6 = currentlyHeldDeliveryItem;
                else if (currentlyHandlingDeliverySlotIndex == 7)
                    prsd.DELIVERY_QUEUE_ITEM_SLOT_7 = currentlyHeldDeliveryItem;
                else if (currentlyHandlingDeliverySlotIndex == 8)
                    prsd.DELIVERY_QUEUE_ITEM_SLOT_8 = currentlyHeldDeliveryItem;
                else if (currentlyHandlingDeliverySlotIndex == 9)
                    prsd.DELIVERY_QUEUE_ITEM_SLOT_9 = currentlyHeldDeliveryItem;

                if(targetMailbox != null)
                {
                    System.out.println("Sender / Receiver: " + currentlyHandlingDeliveryUUIDSender + " / " + currentlyHandlingDeliveryUUIDReceiver);
                    prsd.updateDeliveryDetails(
                            currentlyHandlingDeliverySlotIndex,
                            currentlyHandlingDeliveryUUIDSender,
                            currentlyHandlingDeliveryUUIDReceiver,
                            PhantomailboxRegistrySavedData.DELIVERY_STATE_PENDING_MAIL//we just despawned, so we're depositing this item into the system in the pending delivery state
                    );
                }
            }
        }

        if(targetMailbox != null)
            targetMailbox.setPendingCourierDeferred();

        playSound(SoundEvents.FOX_SCREECH);
        this.remove(RemovalReason.DISCARDED);
        return;
    }

    @Override
    public void tick()
    {
        super.tick();

        //TODO - add some glide velocity. rotations are represented as an angle on an axis instead of something sane i guess
        //this.addDeltaMovement(new Vec3(0.125,0,0));

        //animations
        if(level().isClientSide)
        {
            //if the courier has velocity, flap wings
            if((moveControl.getWantedX() > 0) || (moveControl.getWantedY() > 0) || (moveControl.getWantedZ() > 0))
            {
                flapAnimationState.startIfStopped(level().getBlockTicks().count());
                glideAnimationState.stop();
            }
            else
            {
                glideAnimationState.startIfStopped(level().getBlockTicks().count());
                flapAnimationState.stop();
            }
        }

        //we only care about handling courier behavior on the server side
        if(!level().isClientSide)
        {
            //get the target mailbox blockentity
            PhantomailboxBlockEntity targetMailbox = null;

            //if the mailbox that spawned us registered its position with us, use that to find it because a blockpos won't null out if a mailbox is destroyed
            if (lastKnownTargetMailboxPos != null)
            {
                if (level().getBlockEntity(lastKnownTargetMailboxPos) instanceof PhantomailboxBlockEntity p)
                    targetMailbox = p;
            }

            //we expect to be in this state for a single tick
            if (phantomailCourierCurrentBehaviorState == PHANTOMAIL_COURIER_BEHAVIOR_STATE_INIT)
                handleStateMachineInit(targetMailbox);

            //if we are arriving, circle around in the sky until we have a valid path to the mailbox
            else if (phantomailCourierCurrentBehaviorState == PHANTOMAIL_COURIER_BEHAVIOR_STATE_INGRESS)
                handleStateMachineIngress(targetMailbox);

            //if we are interacting with the mailbox, do that
            else if (phantomailCourierCurrentBehaviorState == PHANTOMAIL_COURIER_BEHAVIOR_STATE_INTERACT_MAILBOX)
                handleStateMachineInteract(targetMailbox);

            //if we are exiting, do that
            else if (phantomailCourierCurrentBehaviorState == PHANTOMAIL_COURIER_BEHAVIOR_STATE_EGRESS)
                handleStateMachineEgress(targetMailbox);

            //if we have made it back to the sky, despawn
            else if (phantomailCourierCurrentBehaviorState == PHANTOMAIL_COURIER_BEHAVIOR_STATE_DESPAWN)
                handleStateMachineDespawn(targetMailbox);
        }
    }
}
