package net.taya.morecrystals;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Configuration settings for the More Crystals mod. */
@EventBusSubscriber(modid = MoreCrystals.MODID, bus = EventBusSubscriber.Bus.MOD)
public class Config {
  private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

  // Default values
  private static final int DEFAULT_GROWTH_CHANCE = 5;
  private static final int DEFAULT_DETERIORATE_CHANCE = 8;

  // Properties for crystal growth
  private static final ModConfigSpec.IntValue GROWTH_CHANCE =
      BUILDER
          .comment(
              "The chance (1 in X) that a budding crystal will attempt to grow during a random tick.",
              "Higher numbers mean slower growth. Default: " + DEFAULT_GROWTH_CHANCE)
          .defineInRange("growthChance", DEFAULT_GROWTH_CHANCE, 1, 100);

  // Properties for crystal deterioration
  private static final ModConfigSpec.IntValue DETERIORATE_CHANCE =
      BUILDER
          .comment(
              "The chance (1 in X) that a budding crystal will deteriorate after growing a bud.",
              "Higher numbers mean slower deterioration. Default: " + DEFAULT_DETERIORATE_CHANCE)
          .defineInRange("deteriorateChance", DEFAULT_DETERIORATE_CHANCE, 1, 100);

  public static final ModConfigSpec SPEC = BUILDER.build();

  // Runtime values - initialized with defaults and updated when config is loaded
  public static int growthChance = DEFAULT_GROWTH_CHANCE;
  public static int deteriorateChance = DEFAULT_DETERIORATE_CHANCE;

  @SubscribeEvent
  public static void onLoad(final ModConfigEvent event) {
    growthChance = GROWTH_CHANCE.get();
    deteriorateChance = DETERIORATE_CHANCE.get();
    MoreCrystals.LOGGER.info("Loaded growth chance config: 1 in {}", growthChance);
    MoreCrystals.LOGGER.info("Loaded deteriorate chance config: 1 in {}", deteriorateChance);
  }
}
