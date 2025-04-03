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
  public final String crystalType;
  public final BuddingQuality quality;

  public enum BuddingQuality {
    FLAWLESS, // Highest quality, never deteriorates
    FLAWED, // High quality, can deteriorate to CHIPPED
    CHIPPED, // Medium quality, can deteriorate to DAMAGED
    DAMAGED // Lowest quality, can deteriorate into base crystal block
  }

  public BuddingCrystalBlock(
      BlockBehaviour.Properties properties, String crystalType, BuddingQuality quality) {
    super(properties);
    this.crystalType = crystalType;
    this.quality = quality;
  }

  @Override
  public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    // Check for growth
    if (random.nextInt(Config.growthChance) == 0) {
      Direction direction = Direction.values()[random.nextInt(Direction.values().length)];
      BlockPos growPos = pos.relative(direction);
      BlockState targetState = level.getBlockState(growPos);
      Block targetBlock = targetState.getBlock();

      // Get the growth stages for this crystal type
      CrystalRegistry.CrystalSet crystalSet = CrystalRegistry.getCrystalSet(crystalType);
      if (crystalSet == null) {
        return;
      }

      List<DeferredBlock<Block>> growthStages =
          List.of(
              crystalSet.smallBudBlock,
              crystalSet.mediumBudBlock,
              crystalSet.largeBudBlock,
              crystalSet.clusterBlock);

      // Handle the case where the target position is air (initialize new bud)
      if (targetState.isAir()) {
        level.setBlock(
            growPos,
            growthStages
                .get(0)
                .get()
                .defaultBlockState()
                .setValue(CrystalBudBlock.FACING, direction),
            3);

        // Check for deterioration (except for FLAWLESS quality)
        if (quality != BuddingQuality.FLAWLESS && random.nextInt(Config.deteriorateChance) == 0) {
          deteriorateBlock(level, pos, crystalSet);
        }

        return;
      }

      // Handle growth progression for existing buds
      for (int i = 0; i < growthStages.size() - 1; i++) {
        if (targetBlock == growthStages.get(i).get()) {
          // Get the next growth stage
          DeferredBlock<Block> nextStage = growthStages.get(i + 1);
          level.setBlock(
              growPos,
              nextStage
                  .get()
                  .defaultBlockState()
                  .setValue(CrystalBudBlock.FACING, targetState.getValue(CrystalBudBlock.FACING)),
              3);

          // Check for deterioration (except for FLAWLESS quality)
          if (quality != BuddingQuality.FLAWLESS && random.nextInt(Config.deteriorateChance) == 0) {
            deteriorateBlock(level, pos, crystalSet);
          }

          break;
        }
      }
    }
  }

  // Helper method to handle block deterioration by replacing this block with lower quality or base
  // block
  private void deteriorateBlock(
      ServerLevel level, BlockPos pos, CrystalRegistry.CrystalSet crystalSet) {
    switch (quality) {
      case FLAWED:
        // Deteriorate from FLAWED to CHIPPED
        level.setBlock(pos, crystalSet.chippedBuddingBlock.get().defaultBlockState(), 3);
        break;
      case CHIPPED:
        // Deteriorate from CHIPPED to DAMAGED
        level.setBlock(pos, crystalSet.damagedBuddingBlock.get().defaultBlockState(), 3);
        break;
      case DAMAGED:
        // Deteriorate from DAMAGED to non-budding crystal block
        level.setBlock(pos, crystalSet.crystalBlock.get().defaultBlockState(), 3);
        break;
      default:
        // FLAWLESS is handled separately
        break;
    }
  }
}
