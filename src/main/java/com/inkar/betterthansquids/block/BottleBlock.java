package com.inkar.betterthansquids.block;

import com.google.common.collect.ImmutableList;
import com.inkar.betterthansquids.block.state.properties.BetterThanSquidsBlockStateProperties;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.function.ToIntFunction;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class BottleBlock extends AbstractBottleBlock implements SimpleWaterloggedBlock
{
    public static final IntegerProperty BOTTLES = BetterThanSquidsBlockStateProperties.BOTTLES;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape ONE_AABB = Block.box(7.0D, 0.0D, 7.0D, 9.0D, 6.0D, 9.0D);
    private static final VoxelShape TWO_AABB = Block.box(5.0D, 0.0D, 6.0D, 11.0D, 6.0D, 9.0D);
    private static final VoxelShape THREE_AABB = Block.box(5.0D, 0.0D, 6.0D, 10.0D, 6.0D, 11.0D);
    private static final VoxelShape FOUR_AABB = Block.box(5.0D, 0.0D, 5.0D, 11.0D, 6.0D, 10.0D);

    public BottleBlock(BlockBehaviour.Properties p_152801_) {
        super(p_152801_);
        this.registerDefaultState(this.stateDefinition.any().setValue(BOTTLES, Integer.valueOf(1)).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    public InteractionResult use(BlockState p_152822_, Level p_152823_, BlockPos p_152824_, Player p_152825_, InteractionHand p_152826_, BlockHitResult p_152827_) {
        if (p_152825_.getItemInHand(p_152826_).isEmpty() || p_152825_.isSecondaryUseActive()) {
            //grab(p_152825_, p_152822_, p_152823_, p_152824_, this);
            return InteractionResult.sidedSuccess(p_152823_.isClientSide);
        } else {
            return InteractionResult.PASS;
        }
    }

    public boolean canBeReplaced(BlockState p_152814_, BlockPlaceContext p_152815_) {
        return !p_152815_.isSecondaryUseActive() && p_152815_.getItemInHand().getItem() == this.asItem() && p_152814_.getValue(BOTTLES) < 4 ? true : super.canBeReplaced(p_152814_, p_152815_);
    }

    public BlockState getStateForPlacement(BlockPlaceContext p_152803_) {
        BlockState blockstate = p_152803_.getLevel().getBlockState(p_152803_.getClickedPos());
        if (blockstate.is(this)) {
            return blockstate.cycle(BOTTLES);
        } else {
            FluidState fluidstate = p_152803_.getLevel().getFluidState(p_152803_.getClickedPos());
            boolean flag = fluidstate.getType() == Fluids.WATER;
            return super.getStateForPlacement(p_152803_).setValue(WATERLOGGED, Boolean.valueOf(flag));
        }
    }

    public BlockState updateShape(BlockState p_152833_, Direction p_152834_, BlockState p_152835_, LevelAccessor p_152836_, BlockPos p_152837_, BlockPos p_152838_) {
        if (p_152833_.getValue(WATERLOGGED)) {
            p_152836_.getLiquidTicks().scheduleTick(p_152837_, Fluids.WATER, Fluids.WATER.getTickDelay(p_152836_));
        }

        return super.updateShape(p_152833_, p_152834_, p_152835_, p_152836_, p_152837_, p_152838_);
    }

    public FluidState getFluidState(BlockState p_152844_) {
        return p_152844_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_152844_);
    }

    public VoxelShape getShape(BlockState p_152817_, BlockGetter p_152818_, BlockPos p_152819_, CollisionContext p_152820_) {
        switch(p_152817_.getValue(BOTTLES)) {
            case 1:
            default:
                return ONE_AABB;
            case 2:
                return TWO_AABB;
            case 3:
                return THREE_AABB;
            case 4:
                return FOUR_AABB;
        }
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_152840_) {
        p_152840_.add(BOTTLES, WATERLOGGED);
    }

    public boolean placeLiquid(LevelAccessor p_152805_, BlockPos p_152806_, BlockState p_152807_, FluidState p_152808_) {
        if (!p_152807_.getValue(WATERLOGGED) && p_152808_.getType() == Fluids.WATER) {
            BlockState blockstate = p_152807_.setValue(WATERLOGGED, Boolean.valueOf(true));

            p_152805_.setBlock(p_152806_, blockstate, 3);

            p_152805_.getLiquidTicks().scheduleTick(p_152806_, p_152808_.getType(), p_152808_.getType().getTickDelay(p_152805_));
            return true;
        } else {
            return false;
        }
    }

    public boolean canSurvive(BlockState p_152829_, LevelReader p_152830_, BlockPos p_152831_) {
        return Block.canSupportCenter(p_152830_, p_152831_.below(), Direction.UP);
    }

    public VoxelShape getVisualShape(BlockState p_48735_, BlockGetter p_48736_, BlockPos p_48737_, CollisionContext p_48738_) {
        return Shapes.empty();
    }

    public float getShadeBrightness(BlockState p_48731_, BlockGetter p_48732_, BlockPos p_48733_) {
        return 1.0F;
    }

    public boolean propagatesSkylightDown(BlockState p_48740_, BlockGetter p_48741_, BlockPos p_48742_) {
        return true;
    }

//    public static void grab(@Nullable Player p_151900_, BlockState p_151901_, Level p_151902_, BlockPos p_151903_, AbstractBottleBlock bottle)
//    {
//        p_151901_.cycle(BOTTLES);
//        popResource(p_151902_, p_151903_, new ItemStack(bottle));
//        p_151902_.gameEvent(p_151900_, GameEvent.BLOCK_CHANGE, p_151903_);
//    }
}
