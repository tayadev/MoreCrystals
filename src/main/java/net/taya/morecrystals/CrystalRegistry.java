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

    // Register the budding crystal block itself
    DeferredBlock<Block> buddingCrystal =
        MoreCrystals.BLOCKS.register(
            "budding_" + crystalType,
            () ->
                new BuddingCrystalBlock(
                    BlockBehaviour.Properties.of()
                        .randomTicks()
                        .strength(1.5F)
                        .sound(SoundType.AMETHYST),
                    smallBud,
                    mediumBud,
                    largeBud,
                    cluster));

    // Register the block items
    MoreCrystals.ITEMS.registerSimpleBlockItem("budding_" + crystalType, buddingCrystal);
    MoreCrystals.ITEMS.registerSimpleBlockItem("small_" + crystalType + "_bud", smallBud);
    MoreCrystals.ITEMS.registerSimpleBlockItem("medium_" + crystalType + "_bud", mediumBud);
    MoreCrystals.ITEMS.registerSimpleBlockItem("large_" + crystalType + "_bud", largeBud);
    MoreCrystals.ITEMS.registerSimpleBlockItem(crystalType + "_cluster", cluster);

    // Store the crystal set in the registry
    CrystalSet crystalSet = new CrystalSet(buddingCrystal, smallBud, mediumBud, largeBud, cluster);
    CRYSTAL_VARIANTS.put(crystalType, crystalSet);

    MoreCrystals.LOGGER.info("Registered crystal type: {}", crystalType);
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
          event.accept(crystalSet.getBuddingBlock());
          event.accept(crystalSet.getSmallBudBlock());
          event.accept(crystalSet.getMediumBudBlock());
          event.accept(crystalSet.getLargeBudBlock());
          event.accept(crystalSet.getClusterBlock());
        }
      }
    }

    if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
      for (String crystalType : getRegisteredTypes()) {
        CrystalSet crystalSet = getCrystalSet(crystalType);
        if (crystalSet != null) {
          event.accept(crystalSet.getBuddingBlock());
          event.accept(crystalSet.getClusterBlock());
        }
      }
    }
  }

  /** Class to hold references to the blocks that make up a crystal type */
  public static class CrystalSet {
    private final DeferredBlock<Block> buddingBlock;
    private final DeferredBlock<Block> smallBudBlock;
    private final DeferredBlock<Block> mediumBudBlock;
    private final DeferredBlock<Block> largeBudBlock;
    private final DeferredBlock<Block> clusterBlock;

    public CrystalSet(
        DeferredBlock<Block> buddingBlock,
        DeferredBlock<Block> smallBudBlock,
        DeferredBlock<Block> mediumBudBlock,
        DeferredBlock<Block> largeBudBlock,
        DeferredBlock<Block> clusterBlock) {
      this.buddingBlock = buddingBlock;
      this.smallBudBlock = smallBudBlock;
      this.mediumBudBlock = mediumBudBlock;
      this.largeBudBlock = largeBudBlock;
      this.clusterBlock = clusterBlock;
    }

    public DeferredBlock<Block> getBuddingBlock() {
      return buddingBlock;
    }

    public DeferredBlock<Block> getSmallBudBlock() {
      return smallBudBlock;
    }

    public DeferredBlock<Block> getMediumBudBlock() {
      return mediumBudBlock;
    }

    public DeferredBlock<Block> getLargeBudBlock() {
      return largeBudBlock;
    }

    public DeferredBlock<Block> getClusterBlock() {
      return clusterBlock;
    }
  }
}
