package net.taya.morecrystals.blocks;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.taya.morecrystals.Config;
import net.taya.morecrystals.CrystalType;

/**
 * Represents a budding crystal block that can grow crystal buds on adjacent faces during random ticks.
 * 
 * This block has multiple quality levels that determine its durability:
 * - FLAWLESS: Never deteriorates, will grow buds indefinitely
 * - FLAWED: Can deteriorate to CHIPPED after growing buds
 * - CHIPPED: Can deteriorate to DAMAGED after growing buds
 * - DAMAGED: Can deteriorate to a normal crystal block after growing buds
 * 
 * The growth and deterioration chances are configurable via the mod's config.
 */
public class BuddingCrystalBlock extends Block {
  /** The crystal type this budding block belongs to */
  public final CrystalType crystalType;
  
  /** The quality/durability level of this budding block */
  public final BuddingQuality quality;

  /**
   * Defines the different quality levels of budding crystal blocks,
   * which determine how they deteriorate over time.
   */
  public enum BuddingQuality {
    FLAWLESS("flawless"), // Highest quality, never deteriorates
    FLAWED("flawed"),     // High quality, can deteriorate to CHIPPED
    CHIPPED("chipped"),   // Medium quality, can deteriorate to DAMAGED
    DAMAGED("damaged");   // Lowest quality, can deteriorate into base crystal block

    private final String prefix;

    BuddingQuality(String prefix) {
      this.prefix = prefix;
    }

    /**
     * Gets the name prefix used for this quality in block IDs
     * 
     * @return The prefix string (e.g., "flawless", "damaged")
     */
    public String getPrefix() {
      return prefix;
    }

    /**
     * Looks up a BuddingQuality by its prefix string
     * 
     * @param prefix The prefix to search for
     * @return The matching quality, or FLAWLESS as default if not found
     */
    public static BuddingQuality fromPrefix(String prefix) {
      for (BuddingQuality quality : values()) {
        if (quality.prefix.equals(prefix)) {
          return quality;
        }
      }
      return FLAWLESS; // Default to highest quality if not found
    }
  }

  /**
   * Creates a new budding crystal block
   * 
   * @param properties Block behavior properties
   * @param crystalType The crystal type this block belongs to
   * @param quality The quality/durability level of this block
   */
  public BuddingCrystalBlock(
      BlockBehaviour.Properties properties, CrystalType crystalType, BuddingQuality quality) {
    super(properties);
    this.crystalType = crystalType;
    this.quality = quality;
  }

  @Override
  public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    // Check if growth should occur based on config chance
    if (random.nextInt(Config.growthChance) != 0) {
      return;
    }

    // Select random direction for growth
    Direction direction = Direction.values()[random.nextInt(Direction.values().length)];
    BlockPos growPos = pos.relative(direction);
    BlockState targetState = level.getBlockState(growPos);
    
    // Try to grow a crystal at the target position
    boolean grew = growCrystal(level, growPos, targetState, direction);
    
    // If growth occurred, check for deterioration based on quality
    if (grew && quality != BuddingQuality.FLAWLESS && random.nextInt(Config.deteriorateChance) == 0) {
      deteriorateBlock(level, pos);
    }
  }
  
  /**
   * Gets the ordered list of crystal growth stages
   * 
   * @return List of crystal bud blocks in order of growth progression
   */
  private List<DeferredBlock<Block>> getGrowthStages() {
    return List.of(
        crystalType.smallBudBlock,
        crystalType.mediumBudBlock,
        crystalType.largeBudBlock,
        crystalType.clusterBlock);
  }
  
  /**
   * Attempts to advance the growth stage of an existing crystal bud
   * 
   * @param level The server level
   * @param growPos The position of the bud
   * @param targetState The current block state at the position
   * @param targetBlock The block at the position
   * @return true if growth advanced, false otherwise
   */
  private boolean advanceExistingBud(ServerLevel level, BlockPos growPos, BlockState targetState, Block targetBlock) {
    List<DeferredBlock<Block>> growthStages = getGrowthStages();
            
    // Check each growth stage (except the final one) for possible advancement
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
        return true;
      }
    }
    
    return false;
  }
  
  /**
   * Attempts to grow a crystal at the target position
   * 
   * @param level The server level
   * @param growPos The position to grow at
   * @param targetState The current block state at the position
   * @param direction The direction of growth
   * @return true if growth occurred, false otherwise
   */
  private boolean growCrystal(ServerLevel level, BlockPos growPos, BlockState targetState, Direction direction) {
    // Handle the case where the target position is air (initialize new bud)
    if (targetState.isAir()) {
      level.setBlock(
          growPos,
          crystalType.smallBudBlock
              .get()
              .defaultBlockState()
              .setValue(CrystalBudBlock.FACING, direction),
          3);
      return true;
    }
    
    // Handle growth progression for existing buds
    Block targetBlock = targetState.getBlock();
    return advanceExistingBud(level, growPos, targetState, targetBlock);
  }

  // Helper method to handle block deterioration by replacing this block with lower quality or base
  // block
  private void deteriorateBlock(ServerLevel level, BlockPos pos) {
    // Get the next deteriorated block state based on current quality
    BlockState deterioratedState = switch (quality) {
      case FLAWED -> crystalType.chippedBuddingBlock.get().defaultBlockState();
      case CHIPPED -> crystalType.damagedBuddingBlock.get().defaultBlockState();
      case DAMAGED -> crystalType.crystalBlock.get().defaultBlockState();
      default -> null; // FLAWLESS never deteriorates
    };
    
    if (deterioratedState != null) {
      level.setBlock(pos, deterioratedState, 3);
    }
  }
}
