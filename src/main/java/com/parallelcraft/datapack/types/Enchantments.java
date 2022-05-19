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
public class Enchantments {
    public static final String OUTPUT_PATH = "world/enchantments";
    
    public static final String REGISTRY_NAME = "ENCHANTMENT_REGISTRY";
    
    public static void generateDatapackPart() throws Exception {
        System.out.println("Generating ENCHANTMENTS part");
        
        MRes registry = ReflectionHelper.getRegistry(REGISTRY_NAME);
        
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
            element.put("rarity", val.f("rarity").get().toString());
            int minLevel = val.i("getMinLevel").aI();
            int maxLevel = val.i("getMaxLevel").aI();
            element.put("minLevel", minLevel);
            element.put("maxLevel", maxLevel);
            element.put("treasureOnly", val.i("isTreasureOnly").aB());
            element.put("curse", val.i("isCurse").aB());
            element.put("tradeable", val.i("isTradeable").aB());
            element.put("discoverable", val.i("isDiscoverable").aB());
            
            List<Integer> incomps = new ArrayList<>();
            for(MRes ench : entries) {
                if(ench.i("getValue").get() == val.get()) continue;
                if(! val.i("isCompatibleWith", ench.i("getValue")).aB()) {
                    incomps.add(registry.i("getId", ench.i("getValue")).aI());
                }
            }
            incomps = incomps.stream().sorted().toList();
            JSONArray incompatibleWith = new JSONArray();
            for(Integer incop : incomps) {
                incompatibleWith.put(incop);
            }
            element.put("incompatibleWith", incompatibleWith);
            
            JSONArray minCost = new JSONArray();
            JSONArray maxCost = new JSONArray();
            for(int i = minLevel; i < maxLevel; i++) {
                minCost.put(val.i("getMinCost", i).aI());
                maxCost.put(val.i("getMaxCost", i).aI());
            }
            element.put("minCost", minCost);
            element.put("maxCost", maxCost);
            
            
            result.put("element", element);
            resultAll.put(result);
        }
        
        Main.writePart(OUTPUT_PATH, resultAll);
    }
}
