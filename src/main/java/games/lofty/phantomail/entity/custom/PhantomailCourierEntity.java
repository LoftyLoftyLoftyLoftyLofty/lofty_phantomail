package games.lofty.phantomail.entity.custom;

import games.lofty.phantomail.block.custom.PhantomailboxBlock;
import games.lofty.phantomail.block.entity.PhantomailboxBlockEntity;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.PathType;

import java.util.Arrays;

public class PhantomailCourierEntity extends FlyingMob
{
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

    private void setupAnimationStates()
    {
        //if we are rising, flap, else glide
        //if(this.getMoveControl().getWantedY() > 0F)
    }

    public static final boolean enableDebug = true;
    private void debugMe(String d)
    {
        if(enableDebug)
        {
            if(this.level().isClientSide())
                System.out.println("(S): " + d);
            else
                System.out.println("(C): " + d);
        }
    }

    //TODO - save most of this stuff in saveAdditional, load it in loadAdditional
    //spawn position - presumably this is in the sky
    BlockPos spawnPosition;//TODO implement
    //target mailbox
    BlockPos lastKnownTargetMailboxPos = null;//if we can't find a mailbox at this location, something has gone wrong and we need to egress
    //current path navigation variable to get to our desired mailbox or return to the sky
    FlyingPathNavigation phantomailCourierPathNavigation = null;
    boolean phantomailCourierPathNavigationRandomized = false;//true if we're randomizing around to get a better path
    double pathDesperationRadius = 1.5;//steadily increasing search radius for random alternate destinations to try to force a valid path to the target
    //miniature state machine state
    public int phantomailCourierCurrentBehaviorState = 0;
    //how long we've been in our current state
    public int howLongHasThisDeliveryStateBeenRunning = 0;
    //state constants
    public static final int PHANTOMAIL_COURIER_BEHAVIOR_STATE_INIT = 0;
    public static final int PHANTOMAIL_COURIER_BEHAVIOR_STATE_INGRESS = 1;
    public static final int PHANTOMAIL_COURIER_BEHAVIOR_STATE_INTERACT_MAILBOX = 2;
    public static final int PHANTOMAIL_COURIER_BEHAVIOR_STATE_EGRESS = 3;
    public static final int PHANTOMAIL_COURIER_BEHAVIOR_STATE_DESPAWN = 4;

    /// tick at which light level hits 4, during night time in the overworld
    public static final int TIME_DARKEST_NIGHT = 13670;
    /// tick at which light level begins to rise, approaching dawn in the overworld
    public static final int TIME_ABANDON_SORTIE = 22331;
    /// returns true if a delivery needs to be abandoned because the sun is coming
    public boolean shouldAbandonSortieDueToTimeConstraints()
    {return (level().getDayTime() < TIME_DARKEST_NIGHT) || (level().getDayTime() > TIME_ABANDON_SORTIE);}

    @Override
    public void tick()
    {
        super.tick();

        //get the target mailbox blockentity
        PhantomailboxBlockEntity targetMailbox = null;

        //if the mailbox that spawned us registered its position with us, use that to find it
        if(lastKnownTargetMailboxPos != null)
        {
            if (level().getBlockEntity(lastKnownTargetMailboxPos) instanceof PhantomailboxBlockEntity p)
                targetMailbox = p;
        }

        //we expect to be in this state for a single tick
        if(phantomailCourierCurrentBehaviorState == PHANTOMAIL_COURIER_BEHAVIOR_STATE_INIT)
        {
            debugMe("(Init) Initializing...");
            //if we were spawned without a target mailbox being attached (spawn egg) then make an attempt to find a mailbox
            if(targetMailbox == null)
            {
                debugMe("(Init) No target provided. Searching...");

                //TODO - if we have a saved target UUID, attempt to find that mailbox instead
                //TODO - expand this to search a 3x3 chunk range
                //TODO - sort results by distance and choose the closest mailbox
                var chunk = level().getChunkAt(blockPosition());
                var relevantEntities = chunk.getBlockEntitiesPos().toArray();
                debugMe("(Init) BlockEntities in chunk: " + String.valueOf(relevantEntities.length));
                for (int x = 0; x < relevantEntities.length; ++x)
                {
                    if (chunk.getBlockEntity((BlockPos)relevantEntities[x]) instanceof PhantomailboxBlockEntity phantomailboxBlockEntity)
                    {
                        debugMe("(Init) Target acquired");
                        targetMailbox = phantomailboxBlockEntity;
                        lastKnownTargetMailboxPos = (BlockPos)relevantEntities[x];
                        break;
                    }
                }
            }

            //if we don't have a target mailbox, despawn
            if (targetMailbox == null)
            {
                debugMe("(Init) Couldn't find a mailbox. Despawning...");
                phantomailCourierCurrentBehaviorState = PHANTOMAIL_COURIER_BEHAVIOR_STATE_DESPAWN;
            }
            else
            {
                debugMe("(Init) Entering arrival state...");
                phantomailCourierCurrentBehaviorState = PHANTOMAIL_COURIER_BEHAVIOR_STATE_INGRESS;
            }
        }

        //if we are arriving, circle around in the sky until we have a valid path to the mailbox
        else if(phantomailCourierCurrentBehaviorState == PHANTOMAIL_COURIER_BEHAVIOR_STATE_INGRESS)
        {
            //if we don't have a target mailbox, abandon sortie
            if ((targetMailbox == null) || shouldAbandonSortieDueToTimeConstraints())
            {
                debugMe("(Arrival) abandoning sortie");
                phantomailCourierCurrentBehaviorState = PHANTOMAIL_COURIER_BEHAVIOR_STATE_EGRESS;
            }
            else
            {
                //if we haven't tried to build a path yet, start trying
                if (phantomailCourierPathNavigation == null)
                {
                    debugMe("(Arrival) Building path from " + blockPosition().toString() + " to " + targetMailbox.getBlockPos().toString());
                    phantomailCourierPathNavigation = new FlyingPathNavigation(this, level());
                    phantomailCourierPathNavigation.setCanOpenDoors(false);
                    phantomailCourierPathNavigation.setCanFloat(false);
                    phantomailCourierPathNavigation.setCanPassDoors(true);
                    phantomailCourierPathNavigation.setMaxVisitedNodesMultiplier(10.0F);
                    phantomailCourierPathNavigation.moveTo(
                            (double)targetMailbox.getBlockPos().getX(),
                            (double)targetMailbox.getBlockPos().getY(),
                            (double)targetMailbox.getBlockPos().getZ(),
                            0,
                            this.getAttributeValue(Attributes.FLYING_SPEED)
                    );
                }
                //if we've already started a pathfinding job...
                else
                {
                    if( phantomailCourierPathNavigation.isInProgress() )
                    {
                        //debugMe("(Arrival) Awaiting path result...");
                        phantomailCourierPathNavigation.tick();
                    }

                    if(phantomailCourierPathNavigation.isStuck())
                    {
                        debugMe("(Arrival) Stuck");
                    }

                    //...and it's finished
                    if( phantomailCourierPathNavigation.isDone() )
                    {
                        debugMe("(Arrival) Done");
                        //...and it succeeded
                        if(phantomailCourierPathNavigation.getPath() != null)
                        {
                            debugMe("(Arrival) Path not null");
                            if((this.blockPosition().distManhattan(lastKnownTargetMailboxPos) <= 2) && (this.isColliding(lastKnownTargetMailboxPos, level().getBlockState(lastKnownTargetMailboxPos))))
                            {
                                debugMe("(Arrival) Path arrived at correct block");
                                phantomailCourierCurrentBehaviorState = PHANTOMAIL_COURIER_BEHAVIOR_STATE_INTERACT_MAILBOX;
                            }
                            else
                            {
                                debugMe("(Arrival) Path arrived at incorrect block");
                                if( phantomailCourierPathNavigationRandomized == false )
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
                            debugMe("(Arrival) Path is null");
                            //otherwise, abandon sortie
                            phantomailCourierCurrentBehaviorState = PHANTOMAIL_COURIER_BEHAVIOR_STATE_EGRESS;
                        }
                    }
                }
            }
        }

        //if we are interacting with the mailbox, do that
        else if(phantomailCourierCurrentBehaviorState == PHANTOMAIL_COURIER_BEHAVIOR_STATE_INTERACT_MAILBOX)
        {
            if (targetMailbox == null)
                phantomailCourierCurrentBehaviorState = PHANTOMAIL_COURIER_BEHAVIOR_STATE_EGRESS;
            else
            {
                phantomailCourierCurrentBehaviorState = PHANTOMAIL_COURIER_BEHAVIOR_STATE_DESPAWN;
                //drop off goods belonging to this mailbox

                //pick up goods belonging to this mailbox
            }
        }

        //if we are exiting, do that
        else if(phantomailCourierCurrentBehaviorState == PHANTOMAIL_COURIER_BEHAVIOR_STATE_EGRESS)
        {
            phantomailCourierCurrentBehaviorState = PHANTOMAIL_COURIER_BEHAVIOR_STATE_DESPAWN;
        }

        //if we have made it back to the sky, despawn
        else if(phantomailCourierCurrentBehaviorState == PHANTOMAIL_COURIER_BEHAVIOR_STATE_DESPAWN)
        {
            playSound(SoundEvents.FOX_SCREECH);
            this.remove(RemovalReason.DISCARDED);
            return;
        }
    }
}
