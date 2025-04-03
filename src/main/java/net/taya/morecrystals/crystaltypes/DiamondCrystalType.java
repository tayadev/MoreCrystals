package net.taya.morecrystals.crystaltypes;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.taya.morecrystals.CrystalType;

/** 
 * Diamond crystal type implementation with diamond-specific properties.
 * These crystals have a light blue color and higher hardness than standard crystals.
 */
public class DiamondCrystalType extends CrystalType {
  
  public DiamondCrystalType() {
    super("diamond");
  }
  
  @Override
  protected BlockBehaviour.Properties customizeProperties(BlockBehaviour.Properties defaultProperties) {
    return defaultProperties
        .mapColor(MapColor.DIAMOND);
  }
}
