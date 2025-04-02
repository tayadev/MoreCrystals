package net.taya.morecrystals.client;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.taya.morecrystals.CrystalRegistry;
import net.taya.morecrystals.MoreCrystals;

/**
 * Handles registration of render types for mod blocks
 * This is client-side only code
 */
@EventBusSubscriber(modid = MoreCrystals.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class RenderTypeRegistry {

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // Set cutout render type for all crystal buds
            for (String crystalType : CrystalRegistry.getRegisteredTypes()) {
                CrystalRegistry.CrystalSet crystalSet = CrystalRegistry.getCrystalSet(crystalType);
                if (crystalSet != null) {
                    // Apply CUTOUT render type to all bud blocks (small, medium, large, cluster)
                    ItemBlockRenderTypes.setRenderLayer(crystalSet.getSmallBudBlock().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(crystalSet.getMediumBudBlock().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(crystalSet.getLargeBudBlock().get(), RenderType.cutout());
                    ItemBlockRenderTypes.setRenderLayer(crystalSet.getClusterBlock().get(), RenderType.cutout());
                }
            }
            MoreCrystals.LOGGER.info("Registered render types for crystal blocks");
        });
    }
}