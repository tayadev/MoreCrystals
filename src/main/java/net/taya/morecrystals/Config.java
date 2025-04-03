package net.taya.morecrystals;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = MoreCrystals.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
  private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

  // Properties for crystal growth
  private static final ModConfigSpec.IntValue GROWTH_CHANCE =
      BUILDER
          .comment(
              "The chance (1 in X) that a budding crystal will attempt to grow during a random tick.",
              "Higher numbers mean slower growth. Default: 5")
          .defineInRange("growthChance", 5, 1, 100);

  public static final ModConfigSpec SPEC = BUILDER.build();

  public static int growthChance = 5;

  @SubscribeEvent
  public static void onLoad(final ModConfigEvent event) {
    growthChance = GROWTH_CHANCE.get();
    MoreCrystals.LOGGER.info("Loaded growth chance config: 1 in {}", growthChance);
  }
}
