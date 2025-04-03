package net.taya.morecrystals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;

/** Registry for managing crystal types and their related blocks. */
public class CrystalRegistry {
  private static final Map<String, CrystalSet> CRYSTAL_VARIANTS = new HashMap<>();

  /**
   * Register a new crystal type with the registry
   *
   * @param crystalType The name of the crystal type (e.g., "diamond", "ruby")
   */
  public static void registerCrystalType(String crystalType) {
    if (CRYSTAL_VARIANTS.containsKey(crystalType)) {
      MoreCrystals.LOGGER.warn("Crystal type '{}' is already registered, skipping", crystalType);
      return;
    }

    // Create blocks for this crystal type
    DeferredBlock<Block> smallBud =
        registerBudBlock(crystalType, "small", CrystalBudBlock.GrowthStage.SMALL);
    DeferredBlock<Block> mediumBud =
        registerBudBlock(crystalType, "medium", CrystalBudBlock.GrowthStage.MEDIUM);
    DeferredBlock<Block> largeBud =
        registerBudBlock(crystalType, "large", CrystalBudBlock.GrowthStage.LARGE);
    DeferredBlock<Block> cluster =
        registerBudBlock(crystalType, "", CrystalBudBlock.GrowthStage.CLUSTER);

    // Register the base crystal block (non-budding)
    DeferredBlock<Block> crystalBlock =
        MoreCrystals.BLOCKS.register(
            crystalType + "_block",
            () -> new CrystalBlock(CrystalBlock.createStandardProperties()));

    // Register different quality budding crystal blocks
    DeferredBlock<Block> flawlessBuddingCrystal =
        registerBuddingCrystalBlock(crystalType, "flawless");
    DeferredBlock<Block> flawedBuddingCrystal = registerBuddingCrystalBlock(crystalType, "flawed");
    DeferredBlock<Block> chippedBuddingCrystal =
        registerBuddingCrystalBlock(crystalType, "chipped");
    DeferredBlock<Block> damagedBuddingCrystal =
        registerBuddingCrystalBlock(crystalType, "damaged");

    // Register the block items
    MoreCrystals.ITEMS.registerSimpleBlockItem(crystalType + "_block", crystalBlock);
    MoreCrystals.ITEMS.registerSimpleBlockItem(
        "flawless_budding_" + crystalType, flawlessBuddingCrystal);
    MoreCrystals.ITEMS.registerSimpleBlockItem(
        "flawed_budding_" + crystalType, flawedBuddingCrystal);
    MoreCrystals.ITEMS.registerSimpleBlockItem(
        "chipped_budding_" + crystalType, chippedBuddingCrystal);
    MoreCrystals.ITEMS.registerSimpleBlockItem(
        "damaged_budding_" + crystalType, damagedBuddingCrystal);
    MoreCrystals.ITEMS.registerSimpleBlockItem("small_" + crystalType + "_bud", smallBud);
    MoreCrystals.ITEMS.registerSimpleBlockItem("medium_" + crystalType + "_bud", mediumBud);
    MoreCrystals.ITEMS.registerSimpleBlockItem("large_" + crystalType + "_bud", largeBud);
    MoreCrystals.ITEMS.registerSimpleBlockItem(crystalType + "_cluster", cluster);

    // Store the crystal set in the registry
    CrystalSet crystalSet =
        new CrystalSet(
            flawlessBuddingCrystal,
            flawedBuddingCrystal,
            chippedBuddingCrystal,
            damagedBuddingCrystal,
            crystalBlock,
            smallBud,
            mediumBud,
            largeBud,
            cluster);
    CRYSTAL_VARIANTS.put(crystalType, crystalSet);

    MoreCrystals.LOGGER.info("Registered crystal type: {}", crystalType);
  }

  /** Helper method to register a budding crystal block with specific quality */
  private static DeferredBlock<Block> registerBuddingCrystalBlock(
      String crystalType, String qualityPrefix) {

    String blockId = qualityPrefix + "_budding_" + crystalType;

    DeferredBlock<Block> buddingBlock =
        MoreCrystals.BLOCKS.register(
            blockId,
            () -> {
              BuddingCrystalBlock.BuddingQuality quality;
              switch (qualityPrefix) {
                case "flawless":
                  quality = BuddingCrystalBlock.BuddingQuality.FLAWLESS;
                  break;
                case "flawed":
                  quality = BuddingCrystalBlock.BuddingQuality.FLAWED;
                  break;
                case "chipped":
                  quality = BuddingCrystalBlock.BuddingQuality.CHIPPED;
                  break;
                case "damaged":
                  quality = BuddingCrystalBlock.BuddingQuality.DAMAGED;
                  break;
                default:
                  quality = BuddingCrystalBlock.BuddingQuality.FLAWLESS;
                  break;
              }

              return new BuddingCrystalBlock(
                  BlockBehaviour.Properties.of()
                      .randomTicks()
                      .strength(1.5F)
                      .sound(SoundType.AMETHYST),
                  crystalType,
                  quality);
            });

    return buddingBlock;
  }

  /** Helper method to register a bud block for a crystal type */
  private static DeferredBlock<Block> registerBudBlock(
      String crystalType, String sizePrefix, CrystalBudBlock.GrowthStage stage) {
    String blockId =
        sizePrefix.isEmpty() ? crystalType + "_cluster" : sizePrefix + "_" + crystalType + "_bud";

    return MoreCrystals.BLOCKS.register(
        blockId, () -> new CrystalBudBlock(BlockBehaviour.Properties.of(), stage));
  }

  /**
   * Get a crystal set by its type name
   *
   * @param crystalType The crystal type name
   * @return The CrystalSet for the given type, or null if not found
   */
  public static CrystalSet getCrystalSet(String crystalType) {
    return CRYSTAL_VARIANTS.get(crystalType);
  }

  /**
   * Get a list of all registered crystal types
   *
   * @return List of crystal type names
   */
  public static List<String> getRegisteredTypes() {
    return new ArrayList<>(CRYSTAL_VARIANTS.keySet());
  }

  /**
   * Add crystal blocks to the appropriate creative mode tabs
   *
   * @param event BuildCreativeModeTabContentsEvent
   */
  public static void addItemsToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
    if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
      for (String crystalType : getRegisteredTypes()) {
        CrystalSet crystalSet = getCrystalSet(crystalType);
        if (crystalSet != null) {
          event.accept(crystalSet.flawlessBuddingBlock);
          event.accept(crystalSet.flawedBuddingBlock);
          event.accept(crystalSet.chippedBuddingBlock);
          event.accept(crystalSet.damagedBuddingBlock);
          event.accept(crystalSet.crystalBlock);
          event.accept(crystalSet.smallBudBlock);
          event.accept(crystalSet.mediumBudBlock);
          event.accept(crystalSet.largeBudBlock);
          event.accept(crystalSet.clusterBlock);
        }
      }
    }

    if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
      for (String crystalType : getRegisteredTypes()) {
        CrystalSet crystalSet = getCrystalSet(crystalType);
        if (crystalSet != null) {
          event.accept(crystalSet.flawlessBuddingBlock);
          event.accept(crystalSet.crystalBlock);
          event.accept(crystalSet.clusterBlock);
        }
      }
    }
  }

  /**
   * Get the next lower quality budding block for deterioration
   *
   * @param currentBlock The current block that is deteriorating
   * @return The next lower quality block, or null if none found
   */
  public static Block getNextLowerQualityBlock(BuddingCrystalBlock currentBlock) {
    String crystalType = currentBlock.crystalType;
    BuddingCrystalBlock.BuddingQuality quality = currentBlock.quality;

    CrystalSet crystalSet = getCrystalSet(crystalType);
    if (crystalSet == null) {
      return null;
    }

    // Return the appropriate lower quality block based on current quality
    switch (quality) {
      case FLAWED:
        return crystalSet.chippedBuddingBlock.get();
      case CHIPPED:
        return crystalSet.damagedBuddingBlock.get();
      case DAMAGED:
        return crystalSet.crystalBlock.get();
      default:
        return null;
    }
  }

  /** Class to hold references to the blocks that make up a crystal type */
  public static class CrystalSet {
    public final DeferredBlock<Block> flawlessBuddingBlock;
    public final DeferredBlock<Block> flawedBuddingBlock;
    public final DeferredBlock<Block> chippedBuddingBlock;
    public final DeferredBlock<Block> damagedBuddingBlock;
    public final DeferredBlock<Block> crystalBlock;
    public final DeferredBlock<Block> smallBudBlock;
    public final DeferredBlock<Block> mediumBudBlock;
    public final DeferredBlock<Block> largeBudBlock;
    public final DeferredBlock<Block> clusterBlock;

    public CrystalSet(
        DeferredBlock<Block> flawlessBuddingBlock,
        DeferredBlock<Block> flawedBuddingBlock,
        DeferredBlock<Block> chippedBuddingBlock,
        DeferredBlock<Block> damagedBuddingBlock,
        DeferredBlock<Block> crystalBlock,
        DeferredBlock<Block> smallBudBlock,
        DeferredBlock<Block> mediumBudBlock,
        DeferredBlock<Block> largeBudBlock,
        DeferredBlock<Block> clusterBlock) {
      this.flawlessBuddingBlock = flawlessBuddingBlock;
      this.flawedBuddingBlock = flawedBuddingBlock;
      this.chippedBuddingBlock = chippedBuddingBlock;
      this.damagedBuddingBlock = damagedBuddingBlock;
      this.crystalBlock = crystalBlock;
      this.smallBudBlock = smallBudBlock;
      this.mediumBudBlock = mediumBudBlock;
      this.largeBudBlock = largeBudBlock;
      this.clusterBlock = clusterBlock;
    }

    // For compatibility with existing code
    public DeferredBlock<Block> getBuddingBlock() {
      return flawlessBuddingBlock;
    }
  }
}
