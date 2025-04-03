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
          
  // Properties for crystal deterioration
  private static final ModConfigSpec.IntValue DETERIORATE_CHANCE =
      BUILDER
          .comment(
              "The chance (1 in X) that a budding crystal will deteriorate after growing a bud.",
              "Higher numbers mean slower deterioration. Default: 8")
          .defineInRange("deteriorateChance", 8, 1, 100);

  public static final ModConfigSpec SPEC = BUILDER.build();

  public static int growthChance = 5;
  public static int deteriorateChance = 8;

  @SubscribeEvent
  public static void onLoad(final ModConfigEvent event) {
    growthChance = GROWTH_CHANCE.get();
    deteriorateChance = DETERIORATE_CHANCE.get();
    MoreCrystals.LOGGER.info("Loaded growth chance config: 1 in {}", growthChance);
    MoreCrystals.LOGGER.info("Loaded deteriorate chance config: 1 in {}", deteriorateChance);
  }
}
