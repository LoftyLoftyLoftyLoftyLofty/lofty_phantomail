package games.lofty.phantomail.entity.custom;

import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.level.Level;

public class PhantomailCourierEntity extends Phantom
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

    public PhantomailCourierEntity(EntityType<? extends Phantom> entityType, Level level)
    {
        super(entityType, level);
        this.setPersistenceRequired();
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    protected void registerGoals()
    {
        //no goals for this courier
    }

    private void setupAnimationStates()
    {
        //if we are rising, flap, else glide

    }

    @Override
    public void tick()
    {
        super.tick();

        //if we are arriving, circle around in the sky
        //if we can get to the mailbox, get to it
        //if we are interacting with the mailbox, do that
        //if we are exiting, do that
        //if we have made it back to the sky, despawn
    }
}
