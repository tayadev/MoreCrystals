package net.taya.morecrystals.datagen;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.taya.morecrystals.CrystalType;
import net.taya.morecrystals.MoreCrystals;
import net.taya.morecrystals.blocks.BuddingCrystalBlock;
import net.taya.morecrystals.blocks.CrystalBudBlock;

public class ModelsProvider extends BlockStateProvider {
  public ModelsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
    super(output, MoreCrystals.MODID, existingFileHelper);
  }

  @Override
  protected void registerStatesAndModels() {
    // Use the getAllTypes() method to get all registered crystal types
    for (CrystalType type : CrystalType.REGISTRY) {
      registerCrystalVariant(type);
    }
  }

  /**
   * Registers all block models, blockstates, and item models for a crystal variant
   *
   * @param type The crystal type
   */
  private void registerCrystalVariant(CrystalType type) {
    // Create models for different budding crystal qualities
    createBuddingCrystalBlockModel(type, BuddingCrystalBlock.BuddingQuality.FLAWLESS);
    createBuddingCrystalBlockModel(type, BuddingCrystalBlock.BuddingQuality.FLAWED);
    createBuddingCrystalBlockModel(type, BuddingCrystalBlock.BuddingQuality.CHIPPED);
    createBuddingCrystalBlockModel(type, BuddingCrystalBlock.BuddingQuality.DAMAGED);

    // Create model for the base crystal block
    createCrystalBlockModel(type);

    // Create bud models
    createCrystalBudModels(type, "small", CrystalBudBlock.GrowthStage.SMALL);
    createCrystalBudModels(type, "medium", CrystalBudBlock.GrowthStage.MEDIUM);
    createCrystalBudModels(type, "large", CrystalBudBlock.GrowthStage.LARGE);
    createCrystalBudModels(type, "", CrystalBudBlock.GrowthStage.CLUSTER);
  }

  /** Creates model for the crystal block (non-budding) */
  private void createCrystalBlockModel(CrystalType type) {
    String crystalType = type.name;
    String blockId = crystalType + "_block";
    DeferredBlock<Block> deferredBlock = type.crystalBlock;
    Block block = deferredBlock.get();

    // Use crystal type subfolder for textures
    ResourceLocation textureLocation = modLoc("block/" + crystalType + "/crystal_block");
    BlockModelBuilder modelBuilder = models().cubeAll(blockId, textureLocation);
    simpleBlock(block, modelBuilder);
    simpleBlockItem(block, modelBuilder);
  }

  /** Creates models for the budding crystal block with specified quality */
  private void createBuddingCrystalBlockModel(
      CrystalType type, BuddingCrystalBlock.BuddingQuality quality) {
    String crystalType = type.name;
    String qualityPrefix = quality.getPrefix();
    String blockId = qualityPrefix + "_budding_" + crystalType;

    // Get the block directly from the crystal type based on quality
    DeferredBlock<Block> deferredBlock = getBuddingBlockForQuality(type, quality);
    if (deferredBlock == null) {
      MoreCrystals.LOGGER.error("Failed to get budding block for quality: {}", quality);
      return;
    }

    Block block = deferredBlock.get();

    // Use crystal type subfolder and simplified names for textures
    ResourceLocation textureLocation =
        modLoc("block/" + crystalType + "/" + qualityPrefix + "_budding");
    BlockModelBuilder modelBuilder = models().cubeAll(blockId, textureLocation);

    // For budding quality blocks, we will use the same model for all states
    getVariantBuilder(block)
        .forAllStates(state -> ConfiguredModel.builder().modelFile(modelBuilder).build());

    simpleBlockItem(block, modelBuilder);
  }

  /**
   * Helper method to get the appropriate budding block for a quality
   *
   * @param type The crystal type
   * @param quality The budding quality
   * @return The deferred block for the quality, or null if unknown quality
   */
  private DeferredBlock<Block> getBuddingBlockForQuality(
      CrystalType type, BuddingCrystalBlock.BuddingQuality quality) {
    return switch (quality) {
      case FLAWLESS -> type.flawlessBuddingBlock;
      case FLAWED -> type.flawedBuddingBlock;
      case CHIPPED -> type.chippedBuddingBlock;
      case DAMAGED -> type.damagedBuddingBlock;
      default -> null;
    };
  }

  /** Creates models for crystal bud blocks at a specific growth stage */
  private void createCrystalBudModels(
      CrystalType type, String sizePrefix, CrystalBudBlock.GrowthStage stage) {
    String crystalType = type.name;
    String blockId =
        sizePrefix.isEmpty() ? crystalType + "_cluster" : sizePrefix + "_" + crystalType + "_bud";

    // Get the bud block based on growth stage
    DeferredBlock<Block> deferredBlock = getBudBlockForStage(type, stage);
    if (deferredBlock == null) {
      MoreCrystals.LOGGER.error("Failed to get bud block for stage: {}", stage);
      return;
    }

    Block block = deferredBlock.get();

    ModelFile crossModel = createCrossBudModel(crystalType, blockId, stage, sizePrefix);

    getVariantBuilder(block)
        .forAllStates(
            state -> {
              Direction dir = state.getValue(CrystalBudBlock.FACING);

              // For cross models, we only need rotational data for non-UP faces
              if (dir == Direction.UP) {
                return ConfiguredModel.builder().modelFile(crossModel).build();
              } else {
                // For other directions, we need to determine appropriate rotations
                return ConfiguredModel.builder()
                    .modelFile(crossModel)
                    .rotationX(dir == Direction.DOWN ? 180 : 90)
                    .rotationY(getYRotationForDirection(dir))
                    .build();
              }
            });

    itemModels().getBuilder(blockId).parent(crossModel);
  }

  /**
   * Helper method to get the appropriate bud block for a growth stage
   *
   * @param type The crystal type
   * @param stage The growth stage
   * @return The deferred block for the stage, or null if unknown stage
   */
  private DeferredBlock<Block> getBudBlockForStage(
      CrystalType type, CrystalBudBlock.GrowthStage stage) {
    return switch (stage) {
      case SMALL -> type.smallBudBlock;
      case MEDIUM -> type.mediumBudBlock;
      case LARGE -> type.largeBudBlock;
      case CLUSTER -> type.clusterBlock;
      default -> null;
    };
  }

  /**
   * Creates a cross model for a crystal bud
   *
   * @return The model file for the cross type bud
   */
  private ModelFile createCrossBudModel(
      String crystalType, String blockId, CrystalBudBlock.GrowthStage stage, String sizePrefix) {
    // Use crystal type subfolder and simplified names for textures
    String budType = sizePrefix.isEmpty() ? "cluster" : sizePrefix + "_bud";
    ResourceLocation texture = modLoc("block/" + crystalType + "/" + budType);

    return models().cross(blockId, texture).renderType("cutout");
  }

  /** Returns the Y rotation angle for a horizontal direction */
  private int getYRotationForDirection(Direction dir) {
    return switch (dir) {
      case SOUTH -> 180;
      case WEST -> 270;
      case EAST -> 90;
      default -> 0; // NORTH or other directions
    };
  }
}
