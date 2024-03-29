package com.inkar.betterthansquids.block;

import javax.annotation.Nullable;

import com.inkar.betterthansquids.block.state.properties.BetterThanSquidsBlockStateProperties;
import com.inkar.betterthansquids.block.state.properties.PanelType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import static java.lang.Math.abs;

public class PanelBlock extends Block implements SimpleWaterloggedBlock {
    public static final EnumProperty<PanelType> TYPE = BetterThanSquidsBlockStateProperties.PANEL_TYPE;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected static final VoxelShape NORTH_AABB = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16.0D, 8.0D);
    protected static final VoxelShape SOUTH_AABB = Block.box(0.0D, 0.0D, 8.0D, 16.0D, 16.0D, 16.0D);
    protected static final VoxelShape EAST_AABB = Block.box(0.0D, 0.0D, 0.0D, 8.0D, 16.0D, 16.0D);
    protected static final VoxelShape WEST_AABB = Block.box(8.0D, 0.0D, 0.0D, 16.0D, 16.0D, 16.0D);

    public PanelBlock(BlockBehaviour.Properties p_56359_) {
        super(p_56359_);
        this.registerDefaultState(this.defaultBlockState().setValue(TYPE, PanelType.NORTH).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_56388_) {
        p_56388_.add(TYPE, WATERLOGGED);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext p_56361_)
    {
        BlockPos blockpos = p_56361_.getClickedPos();
        BlockState blockstate = p_56361_.getLevel().getBlockState(blockpos);

        if (blockstate.is(this)) return blockstate.setValue(TYPE, PanelType.DOUBLE).setValue(WATERLOGGED, Boolean.valueOf(false));
        else
        {
            FluidState fluidstate = p_56361_.getLevel().getFluidState(blockpos);
            BlockState blockstate1 = this.defaultBlockState().setValue(TYPE, PanelType.NORTH).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
            Direction direction = p_56361_.getClickedFace();

            double x_pos = (p_56361_.getClickLocation().x - (double)blockpos.getX()) - 0.5D;
            double z_pos = (p_56361_.getClickLocation().z - (double)blockpos.getZ()) - 0.5D;

            switch (direction)
            {
                case UP, DOWN ->
                {
                    if (abs(x_pos) > abs(z_pos))
                    {
                        if (x_pos > 0) return blockstate1.setValue(TYPE, PanelType.WEST);
                        else return blockstate1.setValue(TYPE, PanelType.EAST);
                    }
                    else
                    {
                        if (z_pos > 0) return blockstate1.setValue(TYPE, PanelType.SOUTH);
                        else return blockstate1.setValue(TYPE, PanelType.NORTH);
                    }
                }
                case NORTH ->
                        {
                            if (x_pos > 0.1666D) return blockstate1.setValue(TYPE, PanelType.WEST);
                            else if (x_pos < -0.1666D) return blockstate1.setValue(TYPE, PanelType.EAST);
                            else return blockstate1.setValue(TYPE, PanelType.SOUTH);
                        }
                case EAST ->
                        {
                            if (z_pos > 0.1666D) return blockstate1.setValue(TYPE, PanelType.SOUTH);
                            else if (z_pos < -0.1666D) return blockstate1.setValue(TYPE, PanelType.NORTH);
                            else return blockstate1.setValue(TYPE, PanelType.EAST);
                        }
                case WEST ->
                        {
                            if (z_pos > 0.1666D) return blockstate1.setValue(TYPE, PanelType.SOUTH);
                            else if (z_pos < -0.1666D) return blockstate1.setValue(TYPE, PanelType.NORTH);
                            else return blockstate1.setValue(TYPE, PanelType.WEST);
                        }
                default ->
                        {
                            if (x_pos > 0.1666D) return blockstate1.setValue(TYPE, PanelType.WEST);
                            else if (x_pos < -0.1666D) return blockstate1.setValue(TYPE, PanelType.EAST);
                            else return blockstate1.setValue(TYPE, PanelType.NORTH);
                        }
            }
        }
    }

    public boolean placeLiquid(LevelAccessor p_56368_, BlockPos p_56369_, BlockState p_56370_, FluidState p_56371_) {
        return p_56370_.getValue(TYPE) != PanelType.DOUBLE ? SimpleWaterloggedBlock.super.placeLiquid(p_56368_, p_56369_, p_56370_, p_56371_) : false;
    }

    public boolean canPlaceLiquid(BlockGetter p_56363_, BlockPos p_56364_, BlockState p_56365_, Fluid p_56366_) {
        return p_56365_.getValue(TYPE) != PanelType.DOUBLE ? SimpleWaterloggedBlock.super.canPlaceLiquid(p_56363_, p_56364_, p_56365_, p_56366_) : false;
    }

    public boolean useShapeForLightOcclusion(BlockState p_56395_) {
        return p_56395_.getValue(TYPE) != PanelType.DOUBLE;
    }

    public VoxelShape getShape(BlockState p_56390_, BlockGetter p_56391_, BlockPos p_56392_, CollisionContext p_56393_) {
        PanelType panelType = p_56390_.getValue(TYPE);
        switch(panelType) {
            case DOUBLE:
                return Shapes.block();
            case SOUTH:
                return SOUTH_AABB;
            case EAST:
                return EAST_AABB;
            case WEST:
                return WEST_AABB;
            default:
                return NORTH_AABB;
        }
    }

    public boolean canBeReplaced(BlockState p_56373_, BlockPlaceContext p_56374_) {
        ItemStack itemstack = p_56374_.getItemInHand();
        PanelType panelType = p_56373_.getValue(TYPE);

        // Check for double panel, and if this panel is of the same type
        if (panelType != PanelType.DOUBLE && itemstack.is(this.asItem()))
        {
            if (p_56374_.replacingClickedOnBlock())
            {
                if (p_56374_.getPlayer().isSecondaryUseActive()) return false;

                Direction direction = p_56374_.getClickedFace();

                // Check for each direction
                switch (panelType)
                {
                    case SOUTH: return direction == Direction.NORTH;
                    case EAST: return direction == Direction.EAST;
                    case WEST: return direction == Direction.WEST;
                    default: return direction == Direction.SOUTH;
                }
            }
            else return true;
        }
        else return false;
    }

    public FluidState getFluidState(BlockState p_56397_) {
        return p_56397_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_56397_);
    }

    public BlockState updateShape(BlockState p_56381_, Direction p_56382_, BlockState p_56383_, LevelAccessor p_56384_, BlockPos p_56385_, BlockPos p_56386_) {
        if (p_56381_.getValue(WATERLOGGED)) {
            p_56384_.getLiquidTicks().scheduleTick(p_56385_, Fluids.WATER, Fluids.WATER.getTickDelay(p_56384_));
        }

        return super.updateShape(p_56381_, p_56382_, p_56383_, p_56384_, p_56385_, p_56386_);
    }

    public boolean isPathfindable(BlockState p_56376_, BlockGetter p_56377_, BlockPos p_56378_, PathComputationType p_56379_) {
        switch(p_56379_) {
            case LAND:
                return false;
            case WATER:
                return p_56377_.getFluidState(p_56378_).is(FluidTags.WATER);
            case AIR:
                return false;
            default:
                return false;
        }
    }
}