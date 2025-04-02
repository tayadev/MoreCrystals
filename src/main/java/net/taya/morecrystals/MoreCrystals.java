package net.taya.morecrystals;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.world.item.CreativeModeTabs;
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

import java.util.Arrays;
import java.util.List;

@Mod(MoreCrystals.MODID)
public class MoreCrystals {
        public static final String MODID = "morecrystals";
        public static final Logger LOGGER = LogUtils.getLogger();

        public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
        public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
        
        public static final List<String> CRYSTAL_TYPES = Arrays.asList(
            "diamond",
            "redstone"
        );

        public MoreCrystals(IEventBus modEventBus, ModContainer modContainer) {
                // Register all crystal types first, before registering the DeferredRegisters
                for (String crystalType : CRYSTAL_TYPES) {
                    CrystalRegistry.registerCrystalType(crystalType);
                }
                LOGGER.info("Registered {} crystal types", CRYSTAL_TYPES.size());
                
                // Now register the DeferredRegisters with the mod event bus
                BLOCKS.register(modEventBus);
                ITEMS.register(modEventBus);
                
                modEventBus.addListener(this::commonSetup);

                NeoForge.EVENT_BUS.register(this);

                modEventBus.addListener(this::addCreative);

                modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        }

        private void commonSetup(final FMLCommonSetupEvent event) {
                // Some common setup code
                LOGGER.info("MoreCrystals: Common setup");
        }

        // Add crystal items to the appropriate creative tabs
        private void addCreative(BuildCreativeModeTabContentsEvent event) {
                if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
                        // Add all crystal blocks to the building blocks tab
                        for (String crystalType : CRYSTAL_TYPES) {
                                CrystalRegistry.CrystalSet crystalSet = CrystalRegistry.getCrystalSet(crystalType);
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
                        // Add all crystal blocks to the natural blocks tab too
                        for (String crystalType : CRYSTAL_TYPES) {
                                CrystalRegistry.CrystalSet crystalSet = CrystalRegistry.getCrystalSet(crystalType);
                                if (crystalSet != null) {
                                        event.accept(crystalSet.getBuddingBlock());
                                        event.accept(crystalSet.getClusterBlock());
                                }
                        }
                }
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
