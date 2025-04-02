package net.taya.morecrystals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredBlock;

// Represents a generic budding crystal block, which represents a budding crystal (like amethyst is) that when ticked can grow, or advance the growth of an attached crystal bud block
public class BuddingCrystalBlock extends Block {
  // Removed the static final GROWTH_CHANCE constant as we'll now use the config value
  private final DeferredBlock<Block> smallBudBlock;
  private final DeferredBlock<Block> mediumBudBlock;
  private final DeferredBlock<Block> largeBudBlock;
  private final DeferredBlock<Block> clusterBlock;

  public BuddingCrystalBlock(
      BlockBehaviour.Properties properties,
      DeferredBlock<Block> smallBudBlock,
      DeferredBlock<Block> mediumBudBlock,
      DeferredBlock<Block> largeBudBlock,
      DeferredBlock<Block> clusterBlock) {
    super(properties);
    this.smallBudBlock = smallBudBlock;
    this.mediumBudBlock = mediumBudBlock;
    this.largeBudBlock = largeBudBlock;
    this.clusterBlock = clusterBlock;
  }

  @Override
  public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    // Use the growth chance from the config instead of a hard-coded value
    if (random.nextInt(Config.growthChance) == 0) {
      Direction direction = Direction.values()[random.nextInt(Direction.values().length)];
      BlockPos growPos = pos.relative(direction);
      BlockState targetState = level.getBlockState(growPos);
      Block targetBlock = targetState.getBlock();

      // Check if the position is air for new growth
      if (targetState.isAir()) {
        level.setBlock(growPos, smallBudBlock.get().defaultBlockState().setValue(
            CrystalBudBlock.FACING, direction), 3);
      }
      // Check if we can advance growth stages
      else if (targetBlock == smallBudBlock.get()) {
        level.setBlock(growPos, mediumBudBlock.get().defaultBlockState().setValue(
            CrystalBudBlock.FACING, targetState.getValue(CrystalBudBlock.FACING)), 3);
      } else if (targetBlock == mediumBudBlock.get()) {
        level.setBlock(growPos, largeBudBlock.get().defaultBlockState().setValue(
            CrystalBudBlock.FACING, targetState.getValue(CrystalBudBlock.FACING)), 3);
      } else if (targetBlock == largeBudBlock.get()) {
        level.setBlock(growPos, clusterBlock.get().defaultBlockState().setValue(
            CrystalBudBlock.FACING, targetState.getValue(CrystalBudBlock.FACING)), 3);
      }
    }
  }
}
