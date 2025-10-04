package games.lofty.phantomail.block.custom;

import com.mojang.serialization.MapCodec;
import games.lofty.phantomail.block.entity.ModBlockEntities;
import games.lofty.phantomail.block.entity.PhantomailboxBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.HorizontalDirectionalBlock.FACING;

public class PhantomailboxBlock extends BaseEntityBlock {

    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public static final VoxelShape SHAPE = Block.box( 2, 0, 2, 14, 13, 14 );
    public static final MapCodec<PhantomailboxBlock> CODEC = simpleCodec(PhantomailboxBlock::new);

    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return SHAPE;
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter blockAccess, BlockPos pos, Direction side)
    {
        return blockState.getValue(POWER);
    }

    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter level, BlockPos pos, Direction direction)
    {
        return blockState.getValue(POWER);
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
                //drop anything in the blockentity's inventory
                phantomailboxBlockEntity.drops();

                //unregister the uuid from the list of mailboxes
                phantomailboxBlockEntity.unregisterUUID();

                //i think this is for redstone but not sure
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

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(POWER);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction)
    {
        return true;
        //return super.canConnectRedstone(state, level, pos, direction);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(POWER, 0);
    }
    /**
     * Returns the blockstate with the given rotation from the passed blockstate. If inapplicable, returns the passed blockstate.
     */
    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    /**
     * Returns the blockstate with the given mirror of the passed blockstate. If inapplicable, returns the passed blockstate.
     */
    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}
