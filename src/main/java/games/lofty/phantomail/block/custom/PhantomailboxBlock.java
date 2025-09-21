package games.lofty.phantomail.block.custom;

import com.mojang.serialization.MapCodec;
import games.lofty.phantomail.block.entity.ModBlockEntities;
import games.lofty.phantomail.block.entity.PhantomailboxBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PhantomailboxBlock extends BaseEntityBlock {

    public static final VoxelShape SHAPE = Block.box( 2, 0, 2, 14, 13, 14 );
    public static final MapCodec<PhantomailboxBlock> CODEC = simpleCodec(PhantomailboxBlock::new);

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPE;
    }

    public PhantomailboxBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PhantomailboxBlockEntity(pos, state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if(state.getBlock() != newState.getBlock())
        {
            if(level.getBlockEntity(pos) instanceof PhantomailboxBlockEntity phantomailboxBlockEntity)
            {
                phantomailboxBlockEntity.drops();
                phantomailboxBlockEntity.unregisterUUID();
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        if( level.getBlockEntity(pos) instanceof PhantomailboxBlockEntity phantomailboxBlockEntity)
        {
            //TODO - figure out why this playSound trigger behaves strangely when level is not clientside
            level.playSound(player, pos, SoundEvents.BARREL_OPEN, SoundSource.BLOCKS);

            if( !level.isClientSide() )
            {
                ((ServerPlayer) player).openMenu(new SimpleMenuProvider(phantomailboxBlockEntity, Component.literal("Phantomailbox")),pos);
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

    @SuppressWarnings("unchecked") // Due to generics, an unchecked cast is necessary here.
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type)
    {
        return type == ModBlockEntities.PHANTOMAILBOX_BE.get() ? (BlockEntityTicker<T>) PhantomailboxBlockEntity::tick : null;
    }
}
