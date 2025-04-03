package net.taya.morecrystals.blocks;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

/**
 * Represents a basic, non-budding crystal block. This is what the damaged budding crystal blocks
 * deteriorate into.
 */
public class CrystalBlock extends Block {

  public CrystalBlock(BlockBehaviour.Properties properties) {
    super(properties);
  }

  public static BlockBehaviour.Properties createStandardProperties() {
    return BlockBehaviour.Properties.of().strength(1.5F).sound(SoundType.AMETHYST);
  }
}
