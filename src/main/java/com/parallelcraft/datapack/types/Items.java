package com.parallelcraft.datapack.types;

import com.parallelcraft.datapack.Main;
import com.parallelcraft.datapack.reflection.MRes;
import com.parallelcraft.datapack.reflection.ReflectionHelper;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author extremeCrazyCoder
 */
public class Items {
    private static final String OUTPUT_PATH = "world/items";
    
    public static final String REGISTRY_NAME = "ITEM_REGISTRY";
    private static final String CREATIVE_MODE_PATH = "net.minecraft.world.item.CreativeModeTab";
    
    public static void generateDatapackPart() throws Exception {
        System.out.println("Generating ITEMS part");
        
        MRes registry = ReflectionHelper.getRegistry(REGISTRY_NAME);
        MRes enchReg = ReflectionHelper.getRegistry(Enchantments.REGISTRY_NAME);
        MRes enchantments = enchReg.i("entrySet");
        
        JSONArray resultAll = new JSONArray();
        MRes entries = registry.i("entrySet");
        for(MRes entry : entries) {
            MRes loc = entry.i("getKey").i("location");
            String name = loc.i("getPath").aStr();
            
            JSONObject result = new JSONObject();
            result.put("name", name);
            
            MRes val = entry.i("getValue");
            result.put("id", registry.i("getId", val).aI());
            
            JSONObject element = new JSONObject();
            String categoryName = ReflectionHelper.c(CREATIVE_MODE_PATH).findName(val.f("category"));
            if(categoryName != null) {
                if(categoryName.contains("Icon()")) {
                    ReflectionHelper.c(CREATIVE_MODE_PATH).findName(val.f("category"));
                }
                element.put("category", categoryName.substring(4));
            }
            element.put("rarity", val.f("rarity").get().toString());
            element.put("maxStackSize", val.f("maxStackSize").aI());
            element.put("maxDamage", val.f("maxDamage").aI());
            element.put("isFireResistant", val.f("isFireResistant").aB());
            element.put("craftingRemainingItem", registry.i("getId", val.f("craftingRemainingItem")).aI());
            element.put("enchantmentValue", val.i("getEnchantmentValue").aI());
            
            List<Integer> possEnch = new ArrayList<>();
            for(MRes enchantment : enchantments) {
                if(enchantment.i("getValue").f("category").i("canEnchant", val).aB()) {
                    possEnch.add(enchReg.i("getId", enchantment.i("getValue")).aI());
                }
            }
            possEnch = possEnch.stream().sorted().toList();
            JSONArray possibleEnchantments = new JSONArray();
            for(Integer poss : possEnch) {
                possibleEnchantments.put(poss);
            }
            element.put("possibleEnchantments", possibleEnchantments);
            
            result.put("element", element);
            resultAll.put(result);
        }
        
        Main.writePart(OUTPUT_PATH, resultAll);
    }
}
