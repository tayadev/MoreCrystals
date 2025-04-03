package net.taya.morecrystals;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredBlock;

// Represents a generic budding crystal block, which represents a budding crystal (like amethyst is)
// that when ticked can grow, or advance the growth of an attached crystal bud block
public class BuddingCrystalBlock extends Block {
  private final List<DeferredBlock<Block>> growthStages;

  public BuddingCrystalBlock(
      BlockBehaviour.Properties properties,
      DeferredBlock<Block> smallBudBlock,
      DeferredBlock<Block> mediumBudBlock,
      DeferredBlock<Block> largeBudBlock,
      DeferredBlock<Block> clusterBlock) {
    super(properties);

    this.growthStages = List.of(
        smallBudBlock,
        mediumBudBlock, 
        largeBudBlock, 
        clusterBlock
    );
  }

  @Override
  public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    if (random.nextInt(Config.growthChance) == 0) {
      Direction direction = Direction.values()[random.nextInt(Direction.values().length)];
      BlockPos growPos = pos.relative(direction);
      BlockState targetState = level.getBlockState(growPos);
      Block targetBlock = targetState.getBlock();

      // Handle the case where the target position is air (initialize new bud)
      if (targetState.isAir()) {
        level.setBlock(
            growPos,
            growthStages.get(0).get().defaultBlockState().setValue(CrystalBudBlock.FACING, direction),
            3);
        return;
      }

      // Handle growth progression for existing buds
      for (int i = 0; i < growthStages.size() - 1; i++) {
        if (targetBlock == growthStages.get(i).get()) {
          // Get the next growth stage
          DeferredBlock<Block> nextStage = growthStages.get(i + 1);
          level.setBlock(
              growPos,
              nextStage.get().defaultBlockState()
                  .setValue(CrystalBudBlock.FACING, targetState.getValue(CrystalBudBlock.FACING)),
              3);
          break;
        }
      }
    }
  }
}
