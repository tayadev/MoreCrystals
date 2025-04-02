package net.taya.morecrystals.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;
import net.taya.morecrystals.MoreCrystals;

public class LangProvider extends LanguageProvider {
    
    public LangProvider(PackOutput output) {
        super(output, MoreCrystals.MODID, "en_us");
    }
    
    @Override
    protected void addTranslations() {
        // Add mod name and description
        add("itemGroup." + MoreCrystals.MODID, "More Crystals");
        
        // Add translations for all registered crystal types
        for (String crystalType : MoreCrystals.CRYSTAL_TYPES) {
            addCrystalTranslations(crystalType);
        }
    }
    
    /**
     * Add translations for all blocks related to a crystal type
     */
    private void addCrystalTranslations(String crystalType) {
        // Format the crystal name to be capitalized nicely (e.g., "diamond" → "Diamond")
        String formattedName = formatName(crystalType);
        
        // Add translations for all blocks related to this crystal type
        add("block.morecrystals.budding_" + crystalType, "Budding " + formattedName);
        add("block.morecrystals.small_" + crystalType + "_bud", "Small " + formattedName + " Bud");
        add("block.morecrystals.medium_" + crystalType + "_bud", "Medium " + formattedName + " Bud");
        add("block.morecrystals.large_" + crystalType + "_bud", "Large " + formattedName + " Bud");
        add("block.morecrystals." + crystalType + "_cluster", formattedName + " Cluster");
    }
    
    /**
     * Format a name to be capitalized nicely
     */
    private String formatName(String name) {
        if (name.isEmpty()) return "";
        
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }
}