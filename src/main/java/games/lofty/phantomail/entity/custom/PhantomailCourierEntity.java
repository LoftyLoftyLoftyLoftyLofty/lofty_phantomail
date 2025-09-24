package games.lofty.phantomail.entity.custom;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
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
                .add(Attributes.MOVEMENT_SPEED, 1)
                .add(Attributes.FOLLOW_RANGE, 16);
    }

    public PhantomailCourierEntity(EntityType<? extends FlyingMob> entityType, Level level)
    {
        super(entityType, level);

        //do not despawn the courier unless correct conditions are met
        this.setPersistenceRequired();

        //disable pathfinding for everything
        for(int x=0;x<PathType.values().length;++x)
            this.setPathfindingMalus(Arrays.stream(PathType.values()).toList().get(x), -1.0F);
        //...except open air
        this.setPathfindingMalus(PathType.OPEN, 0.0F);

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
        //no goals for this courier
        this.removeFreeWill();
    }

    private void setupAnimationStates()
    {
        //if we are rising, flap, else glide

    }

    @Override
    public void tick()
    {
        //how to write excellent Java
        //step 1: ignore hierarchy
        super.tick();

        //if we do not have a mailbox target, try to find one - we can still pick up outgoing mail even if we don't have a parcel
        //if we are arriving, circle around in the sky
        //if we can get to the mailbox, get to it
        //if we are interacting with the mailbox, do that
        //if we are exiting, do that
        //if we have made it back to the sky, despawn
    }
}
