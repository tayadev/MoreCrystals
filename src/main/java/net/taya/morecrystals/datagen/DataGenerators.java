package net.taya.morecrystals.datagen;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.taya.morecrystals.MoreCrystals;

@EventBusSubscriber(modid = MoreCrystals.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataGenerators {

  @SubscribeEvent
  public static void gatherData(GatherDataEvent event) {
    DataGenerator generator = event.getGenerator();
    PackOutput packOutput = generator.getPackOutput();
    ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

    generator.addProvider(
        event.includeClient(), new ModelsProvider(packOutput, existingFileHelper));
    generator.addProvider(event.includeClient(), new LangProvider(packOutput));
  }
}
