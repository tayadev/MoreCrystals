package net.taya.morecrystals.crystaltypes;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.taya.morecrystals.CrystalType;

/**
 * Redstone crystal type implementation with redstone-specific properties.
 * These crystals have a reddish color and emit a small amount of redstone signal.
 */
public class RedstoneCrystalType extends CrystalType {

  public RedstoneCrystalType() {
    super("redstone");
  }
  
  @Override
  protected BlockBehaviour.Properties customizeProperties(BlockBehaviour.Properties defaultProperties) {
    return defaultProperties
        .mapColor(MapColor.COLOR_RED);
  }
}
