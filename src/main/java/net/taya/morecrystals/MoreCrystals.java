package net.taya.morecrystals;

import com.mojang.logging.LogUtils;
import java.util.Arrays;
import java.util.List;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(MoreCrystals.MODID)
public class MoreCrystals {
  public static final String MODID = "morecrystals";
  public static final Logger LOGGER = LogUtils.getLogger();

  public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
  public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

  public static final List<String> CRYSTAL_TYPES = Arrays.asList("diamond", "redstone");

  public MoreCrystals(IEventBus modEventBus, ModContainer modContainer) {
    for (String crystalType : CRYSTAL_TYPES) {
      CrystalRegistry.registerCrystalType(crystalType);
    }
    LOGGER.info("Registered {} crystal types", CRYSTAL_TYPES.size());

    BLOCKS.register(modEventBus);
    ITEMS.register(modEventBus);

    modEventBus.addListener(this::commonSetup);

    NeoForge.EVENT_BUS.register(this);

    modEventBus.addListener(this::addCreative);

    modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
  }

  private void commonSetup(final FMLCommonSetupEvent event) {
    LOGGER.info("MoreCrystals: Common setup");
  }

  private void addCreative(BuildCreativeModeTabContentsEvent event) {
    CrystalRegistry.addItemsToCreativeTabs(event);
  }

  @SubscribeEvent
  public void onServerStarting(ServerStartingEvent event) {
    LOGGER.info("MoreCrystals: Server starting");
  }

  @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
  public static class ClientModEvents {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
      LOGGER.info("MoreCrystals: Client setup");
    }
  }
}
