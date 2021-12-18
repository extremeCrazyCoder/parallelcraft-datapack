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
public class Items {
    public static final String OUTPUT_PATH = "world/items";
    
    public static final String CREATIVE_MODE_PATH = "net.minecraft.world.item.CreativeModeTab";
    public static final String ITEM_PATH = "net.minecraft.world.item.Items";
    public static final String ITEM_DATA_PATH = "net.minecraft.world.item.Item";
    
    public static final String REGISTRY_NAME = "ITEM";

    public static void generateDatapackPart(Class registryClass) throws Exception {
        System.out.println("Generating ITEMS part");
        Field f = registryClass.getField(REGISTRY_NAME);
        Object obj = f.get(null);
        
        Class clsBeh = Main.fetchClass(ITEM_DATA_PATH);
        JSONArray resultAll = new JSONArray();
        
        Class enchCls = Main.fetchClass(Enchantments.ENCHANTMENT_PATH);
        Object enchRegistry = registryClass.getField(Enchantments.REGISTRY_NAME).get(null);
        Set<Map.Entry<?, ?>> enchantments = (Set<Map.Entry<?, ?>>) enchRegistry.getClass().getMethod("entrySet").invoke(enchRegistry);
        
        Set<Map.Entry<?, ?>> entries = (Set<Map.Entry<?, ?>>) obj.getClass().getMethod("entrySet").invoke(obj);
        for(Map.Entry<?, ?> entry : entries) {
            Object loc = entry.getKey().getClass().getMethod("location").invoke(entry.getKey());
            String name = (String) (loc.getClass().getMethod("getPath").invoke(loc));
            
            JSONObject result = new JSONObject();
            result.put("name", name);
            
            Object val = entry.getValue();
            result.put("id", Main.getRegistryID(val, REGISTRY_NAME));
            
            JSONObject element = new JSONObject();
            String categoryName = Main.findName(Main.readReflective(clsBeh, val, "category"), CREATIVE_MODE_PATH);
            if(categoryName != null) {
                element.put("category", categoryName.substring(4));
            }
            element.put("rarity", ((Enum<?>) Main.readReflective(clsBeh, val, "rarity")).toString());
            element.put("maxStackSize", (int) Main.readReflective(clsBeh, val, "maxStackSize"));
            element.put("maxDamage", (int) Main.readReflective(clsBeh, val, "maxDamage"));
            element.put("isFireResistant", (boolean) Main.readReflective(clsBeh, val, "isFireResistant"));
            element.put("craftingRemainingItem", Main.readAndGetID(clsBeh, val, "craftingRemainingItem", REGISTRY_NAME));
            element.put("enchantmentValue", Main.invokeReflective(clsBeh, val, "getEnchantmentValue"));
            
            JSONArray possibleEnchantments = new JSONArray();
            for(Map.Entry<?, ?> enchantment : enchantments) {
                if((boolean) Main.invokeUnknownReflective(Main.readReflective(enchCls, enchantment.getValue(), "category"), "canEnchant", val)) {
                    possibleEnchantments.put(Main.getRegistryID(enchantment.getValue(), Enchantments.REGISTRY_NAME));
                }
            }
            
            element.put("possibleEnchantments", possibleEnchantments);
            result.put("element", element);
            resultAll.put(result);
        }
        
        Main.writePart(OUTPUT_PATH, resultAll);
    }
}
