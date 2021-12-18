package com.parallelcraft.datapack.types;

import com.parallelcraft.datapack.Main;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author extremeCrazyCoder
 */
public class Enchantments {
    public static final String OUTPUT_PATH = "world/enchantments";
    
    public static final String ENCHANTMENT_PATH = "net.minecraft.world.item.enchantment.Enchantment";

    public static final String REGISTRY_NAME = "ENCHANTMENT";
    
    public static void generateDatapackPart(Class registryClass) throws Exception {
        System.out.println("Generating ENCHANTMENTS part");
        Field f = registryClass.getField(REGISTRY_NAME);
        Object obj = f.get(null);
        
        Class clsBeh = Main.fetchClass(ENCHANTMENT_PATH);
        JSONArray resultAll = new JSONArray();

        Set<Map.Entry<?, ?>> entries = (Set<Map.Entry<?, ?>>) obj.getClass().getMethod("entrySet").invoke(obj);
        for(Map.Entry<?, ?> entry : entries) {
            Object loc = entry.getKey().getClass().getMethod("location").invoke(entry.getKey());
            String name = (String) (loc.getClass().getMethod("getPath").invoke(loc));
            
            JSONObject result = new JSONObject();
            result.put("name", name);
            
            Object val = entry.getValue();
            result.put("id", Main.getRegistryID(val, REGISTRY_NAME));
            
            JSONObject element = new JSONObject();
            element.put("rarity", ((Enum<?>) Main.readReflective(clsBeh, val, "rarity")).toString());
            int minLevel = (int) Main.invokeReflective(clsBeh, val, "getMinLevel");
            int maxLevel = (int) Main.invokeReflective(clsBeh, val, "getMaxLevel");
            element.put("minLevel", minLevel);
            element.put("maxLevel", maxLevel);
            element.put("treasureOnly", (boolean) Main.invokeReflective(clsBeh, val, "isTreasureOnly"));
            element.put("curse", (boolean) Main.invokeReflective(clsBeh, val, "isCurse"));
            element.put("tradeable", (boolean) Main.invokeReflective(clsBeh, val, "isTradeable"));
            element.put("discoverable", (boolean) Main.invokeReflective(clsBeh, val, "isDiscoverable"));
            
            JSONArray incompatibleWith = new JSONArray();
            for(Map.Entry<?, ?> ench : entries) {
                if(ench.getValue() == val) continue;
                if(! ((boolean) Main.invokeUnknownReflective(val, "isCompatibleWith", ench.getValue()))) {
                    incompatibleWith.put(Main.getRegistryID(ench.getValue(), REGISTRY_NAME));
                }
            }
            element.put("incompatibleWith", incompatibleWith);
            
            JSONArray minCost = new JSONArray();
            JSONArray maxCost = new JSONArray();
            for(int i = minLevel; i < maxLevel; i++) {
                minCost.put(Main.invokeUnknownReflective(val, "getMinCost", i));
                maxCost.put(Main.invokeUnknownReflective( val, "getMaxCost", i));
            }
            element.put("minCost", minCost);
            element.put("maxCost", maxCost);
            
            
            result.put("element", element);
            resultAll.put(result);
        }
        
        Main.writePart(OUTPUT_PATH, resultAll);
    }
}
