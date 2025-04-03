package net.taya.morecrystals;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.taya.morecrystals.crystaltypes.*;
import org.slf4j.Logger;

@Mod(MoreCrystals.MODID)
public class MoreCrystals {
  public static final String MODID = "morecrystals";
  public static final Logger LOGGER = LogUtils.getLogger();

  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

  public MoreCrystals(IEventBus modEventBus, ModContainer modContainer) {
    // Register blocks and items
    BLOCKS.register(modEventBus);
    ITEMS.register(modEventBus);

    new DiamondCrystalType();
    new RedstoneCrystalType();

    modEventBus.addListener(this::addCreative);

    // Register config
    modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
  }

  private void addCreative(BuildCreativeModeTabContentsEvent event) {
    CrystalType.addItemsToCreativeTabs(event);
  }
}
