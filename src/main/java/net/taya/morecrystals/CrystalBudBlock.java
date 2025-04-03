package net.taya.morecrystals;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CrystalBudBlock extends Block implements SimpleWaterloggedBlock {
  public static final DirectionProperty FACING = BlockStateProperties.FACING;
  public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

  private final GrowthStage growthStage;

  public enum GrowthStage {
    SMALL(1, 1, 6, 6),
    MEDIUM(2, 2, 8, 8),
    LARGE(3, 3, 10, 10),
    CLUSTER(4, 5, 12, 12);

    private final int level;
    private final int lightEmission;
    private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);

    GrowthStage(int level, int lightEmission, int width, int height) {
      this.level = level;
      this.lightEmission = lightEmission;

      double inset = (16.0D - width) / 2.0D;


      shapes.put(Direction.UP, Block.box(inset, 0.0D, inset, 16.0D - inset, height, 16.0D - inset));
      shapes.put(
          Direction.DOWN,
          Block.box(inset, 16.0D - height, inset, 16.0D - inset, 16.0D, 16.0D - inset));
      shapes.put(
          Direction.NORTH,
          Block.box(inset, inset, 16.0D - height, 16.0D - inset, 16.0D - inset, 16.0D));
      shapes.put(
          Direction.SOUTH, Block.box(inset, inset, 0.0D, 16.0D - inset, 16.0D - inset, height));
      shapes.put(
          Direction.EAST, Block.box(0.0D, inset, inset, height, 16.0D - inset, 16.0D - inset));
      shapes.put(
          Direction.WEST,
          Block.box(16.0D - height, inset, inset, 16.0D, 16.0D - inset, 16.0D - inset));
    }

    public VoxelShape getShape(Direction direction) {
      return shapes.get(direction);
    }

    public int getLevel() {
      return level;
    }

    public int getLightEmission() {
      return lightEmission;
    }
  }

  public CrystalBudBlock(BlockBehaviour.Properties properties, GrowthStage growthStage) {
    super(
        properties
            .noOcclusion()
            .lightLevel(state -> growthStage.getLightEmission())
            .sound(SoundType.AMETHYST_CLUSTER)
            .strength(1.5F));

    this.growthStage = growthStage;
    this.registerDefaultState(
        this.stateDefinition
            .any()
            .setValue(FACING, Direction.UP)
            .setValue(WATERLOGGED, Boolean.FALSE));
  }

  @Override
  protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
    builder.add(FACING, WATERLOGGED);
  }

  @Override
  public @NotNull VoxelShape getShape(
      BlockState state,
      @NotNull BlockGetter level,
      @NotNull BlockPos pos,
      @NotNull CollisionContext context) {
    return growthStage.getShape(state.getValue(FACING));
  }

  @Nullable
  @Override
  public BlockState getStateForPlacement(BlockPlaceContext context) {
    Direction direction = context.getClickedFace();
    FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());

    return canSupportAt(context.getLevel(), context.getClickedPos(), direction)
        ? this.defaultBlockState()
            .setValue(FACING, direction)
            .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER)
        : null;
  }

  @Override
  public @NotNull BlockState updateShape(
      BlockState state,
      @NotNull Direction direction,
      @NotNull BlockState neighborState,
      @NotNull LevelAccessor level,
      @NotNull BlockPos pos,
      @NotNull BlockPos neighborPos) {
    if (state.getValue(WATERLOGGED)) {
      level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
    }

    if (direction == state.getValue(FACING).getOpposite()
        && !canSupportAt(level, pos, state.getValue(FACING))) {
      return Blocks.AIR.defaultBlockState();
    }

    return state;
  }

  private boolean canSupportAt(LevelReader level, BlockPos pos, Direction direction) {
    BlockPos supportPos = pos.relative(direction.getOpposite());
    return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, direction);
  }

  @Override
  public @NotNull FluidState getFluidState(BlockState state) {
    return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
  }

  @Override
  public @NotNull PushReaction getPistonPushReaction(@NotNull BlockState state) {
    return PushReaction.DESTROY;
  }
}
