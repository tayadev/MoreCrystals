package net.taya.morecrystals;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.taya.morecrystals.blocks.BuddingCrystalBlock;
import net.taya.morecrystals.blocks.CrystalBlock;
import net.taya.morecrystals.blocks.CrystalBudBlock;

/**
 * Base class for all crystal types. Each crystal type represents a set of related crystal blocks
 * (buds, clusters, etc.)
 */
public abstract class CrystalType {
  // Track all registered crystal types
  public static final List<CrystalType> REGISTRY = new ArrayList<>();

  // Fields that every crystal type has
  public final String name;
  public final DeferredBlock<Block> flawlessBuddingBlock;
  public final DeferredBlock<Block> flawedBuddingBlock;
  public final DeferredBlock<Block> chippedBuddingBlock;
  public final DeferredBlock<Block> damagedBuddingBlock;
  public final DeferredBlock<Block> crystalBlock;
  public final DeferredBlock<Block> smallBudBlock;
  public final DeferredBlock<Block> mediumBudBlock;
  public final DeferredBlock<Block> largeBudBlock;
  public final DeferredBlock<Block> clusterBlock;

  /**
   * Constructor for CrystalType
   *
   * @param name The type name (e.g., "diamond", "redstone")
   */
  protected CrystalType(String name) {
    this.name = name;

    // Register blocks for this crystal type
    this.smallBudBlock = registerBudBlock("small", CrystalBudBlock.GrowthStage.SMALL);
    this.mediumBudBlock = registerBudBlock("medium", CrystalBudBlock.GrowthStage.MEDIUM);
    this.largeBudBlock = registerBudBlock("large", CrystalBudBlock.GrowthStage.LARGE);
    this.clusterBlock = registerBudBlock("", CrystalBudBlock.GrowthStage.CLUSTER);

    // Register the base crystal block (non-budding)
    this.crystalBlock =
        MoreCrystals.BLOCKS.register(
            name + "_block", () -> new CrystalBlock(CrystalBlock.createStandardProperties()));

    // Register different quality budding crystal blocks
    this.flawlessBuddingBlock =
        registerBuddingCrystalBlock(BuddingCrystalBlock.BuddingQuality.FLAWLESS);
    this.flawedBuddingBlock =
        registerBuddingCrystalBlock(BuddingCrystalBlock.BuddingQuality.FLAWED);
    this.chippedBuddingBlock =
        registerBuddingCrystalBlock(BuddingCrystalBlock.BuddingQuality.CHIPPED);
    this.damagedBuddingBlock =
        registerBuddingCrystalBlock(BuddingCrystalBlock.BuddingQuality.DAMAGED);

    // Register all block items
    registerBlockItems();

    // Register this crystal type in the registry
    REGISTRY.add(this);

    MoreCrystals.LOGGER.info("Registered crystal type: {}", name);
  }

  /** Register all block items for this crystal type */
  private void registerBlockItems() {
    // Register base crystal block item
    registerBlockItem(name + "_block", crystalBlock);

    // Register budding block items
    registerBlockItem("flawless_budding_" + name, flawlessBuddingBlock);
    registerBlockItem("flawed_budding_" + name, flawedBuddingBlock);
    registerBlockItem("chipped_budding_" + name, chippedBuddingBlock);
    registerBlockItem("damaged_budding_" + name, damagedBuddingBlock);

    // Register bud block items
    registerBlockItem("small_" + name + "_bud", smallBudBlock);
    registerBlockItem("medium_" + name + "_bud", mediumBudBlock);
    registerBlockItem("large_" + name + "_bud", largeBudBlock);
    registerBlockItem(name + "_cluster", clusterBlock);
  }

  /**
   * Helper method to register a block item
   *
   * @param blockId The block ID
   * @param deferredBlock The deferred block
   */
  private void registerBlockItem(String blockId, DeferredBlock<Block> deferredBlock) {
    MoreCrystals.ITEMS.registerSimpleBlockItem(blockId, deferredBlock);
  }

  /**
   * Override this method to customize block properties for this crystal type. This allows crystal
   * types to have unique characteristics.
   *
   * @param defaultProperties The default properties to build upon
   * @return Modified properties for this crystal type
   */
  protected BlockBehaviour.Properties customizeProperties(
      BlockBehaviour.Properties defaultProperties) {
    // By default, no customizations are applied
    return defaultProperties;
  }

  /** Helper method to register a budding crystal block with specific quality */
  private DeferredBlock<Block> registerBuddingCrystalBlock(
      BuddingCrystalBlock.BuddingQuality quality) {
    String blockId = quality.getPrefix() + "_budding_" + name;

    // Start with standard properties and allow crystal type to customize
    BlockBehaviour.Properties properties =
        customizeProperties(
            BlockBehaviour.Properties.of().randomTicks().strength(1.5F).sound(SoundType.AMETHYST));

    DeferredBlock<Block> buddingBlock =
        MoreCrystals.BLOCKS.register(
            blockId, () -> new BuddingCrystalBlock(properties, this, quality));

    return buddingBlock;
  }

  /** Helper method to register a bud block for a crystal type */
  private DeferredBlock<Block> registerBudBlock(
      String sizePrefix, CrystalBudBlock.GrowthStage stage) {
    String blockId = sizePrefix.isEmpty() ? name + "_cluster" : sizePrefix + "_" + name + "_bud";

    // Allow crystal types to customize properties
    BlockBehaviour.Properties properties = customizeProperties(BlockBehaviour.Properties.of());

    return MoreCrystals.BLOCKS.register(blockId, () -> new CrystalBudBlock(properties, stage));
  }

  /**
   * Add crystal blocks to creative tabs
   *
   * @param event BuildCreativeModeTabContentsEvent
   */
  public static void addItemsToCreativeTabs(BuildCreativeModeTabContentsEvent event) {
    if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
      // Add all blocks to the building blocks tab
      for (CrystalType type : REGISTRY) {
        addAllBlocksToTab(event, type);
      }
    } else if (event.getTabKey() == CreativeModeTabs.NATURAL_BLOCKS) {
      // Add only natural-looking blocks to the natural blocks tab
      for (CrystalType type : REGISTRY) {
        addNaturalBlocksToTab(event, type);
      }
    }
  }

  /**
   * Adds all blocks of a crystal type to a creative tab
   *
   * @param event The tab content event
   * @param type The crystal type
   */
  private static void addAllBlocksToTab(BuildCreativeModeTabContentsEvent event, CrystalType type) {
    event.accept(type.flawlessBuddingBlock);
    event.accept(type.flawedBuddingBlock);
    event.accept(type.chippedBuddingBlock);
    event.accept(type.damagedBuddingBlock);
    event.accept(type.crystalBlock);
    event.accept(type.smallBudBlock);
    event.accept(type.mediumBudBlock);
    event.accept(type.largeBudBlock);
    event.accept(type.clusterBlock);
  }

  /**
   * Adds only natural-looking blocks of a crystal type to a creative tab
   *
   * @param event The tab content event
   * @param type The crystal type
   */
  private static void addNaturalBlocksToTab(
      BuildCreativeModeTabContentsEvent event, CrystalType type) {
    event.accept(type.flawlessBuddingBlock);
    event.accept(type.crystalBlock);
    event.accept(type.clusterBlock);
  }
}
