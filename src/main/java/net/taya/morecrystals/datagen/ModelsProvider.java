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
import net.taya.morecrystals.CrystalBudBlock;
import net.taya.morecrystals.CrystalRegistry;
import net.taya.morecrystals.MoreCrystals;

public class ModelsProvider extends BlockStateProvider {
  public ModelsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
    super(output, MoreCrystals.MODID, existingFileHelper);
  }

  @Override
  protected void registerStatesAndModels() {
    for (String crystalType : CrystalRegistry.getRegisteredTypes()) {
      registerCrystalVariant(crystalType);
    }
  }

  /**
   * Registers all block models, blockstates, and item models for a crystal variant
   *
   * @param crystalType The type of crystal (e.g., "diamond")
   */
  private void registerCrystalVariant(String crystalType) {
    createBuddingCrystalBlockModel(crystalType);

    createCrystalBudModels(crystalType, "small", CrystalBudBlock.GrowthStage.SMALL);
    createCrystalBudModels(crystalType, "medium", CrystalBudBlock.GrowthStage.MEDIUM);
    createCrystalBudModels(crystalType, "large", CrystalBudBlock.GrowthStage.LARGE);
    createCrystalBudModels(crystalType, "", CrystalBudBlock.GrowthStage.CLUSTER);
  }

  /** Creates models for the budding crystal block */
  private void createBuddingCrystalBlockModel(String crystalType) {
    String blockId = "budding_" + crystalType;
    CrystalRegistry.CrystalSet crystalSet = CrystalRegistry.getCrystalSet(crystalType);

    if (crystalSet != null) {
      DeferredBlock<Block> deferredBlock = crystalSet.getBuddingBlock();
      Block block = deferredBlock.get();

      BlockModelBuilder modelBuilder = models().cubeAll(blockId, modLoc("block/" + blockId));
      simpleBlock(block, modelBuilder);
      simpleBlockItem(block, modelBuilder);
    }
  }

  /** Creates models for crystal bud blocks at a specific growth stage */
  private void createCrystalBudModels(
      String crystalType, String sizePrefix, CrystalBudBlock.GrowthStage stage) {
    String blockId =
        sizePrefix.isEmpty() ? crystalType + "_cluster" : sizePrefix + "_" + crystalType + "_bud";

    CrystalRegistry.CrystalSet crystalSet = CrystalRegistry.getCrystalSet(crystalType);

    if (crystalSet != null) {
      DeferredBlock<Block> deferredBlock;

      switch (stage) {
        case SMALL -> deferredBlock = crystalSet.getSmallBudBlock();
        case MEDIUM -> deferredBlock = crystalSet.getMediumBudBlock();
        case LARGE -> deferredBlock = crystalSet.getLargeBudBlock();
        case CLUSTER -> deferredBlock = crystalSet.getClusterBlock();
        default -> {
          return;
        }
      }

      Block block = deferredBlock.get();

      ModelFile crossModel = createCrossBudModel(crystalType, blockId, stage);

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
  }

  /**
   * Creates a cross model for a crystal bud
   *
   * @return The model file for the cross type bud
   */
  private ModelFile createCrossBudModel(
      String crystalType, String blockId, CrystalBudBlock.GrowthStage stage) {
    ResourceLocation texture = modLoc("block/" + blockId);

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
