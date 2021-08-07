package com.parallelcraft.datapack.types;

import com.parallelcraft.datapack.Main;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
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
        Field f = registryClass.getField(REGISTRY_NAME);
        Object obj = f.get(null);
        
        Class clsBeh = Main.fetchClass(ITEM_DATA_PATH);

        Set<Map.Entry<?, ?>> entries = (Set<Map.Entry<?, ?>>) obj.getClass().getMethod("entrySet").invoke(obj);
        for(Map.Entry<?, ?> entry : entries) {
            Object loc = entry.getKey().getClass().getMethod("location").invoke(entry.getKey());
            String name = (String) (loc.getClass().getMethod("getPath").invoke(loc));
            
            JSONObject result = new JSONObject();
            result.put("name", name);
            
            Object val = entry.getValue();
            result.put("id", Main.getRegistryID(val, REGISTRY_NAME));
            
            JSONObject element = new JSONObject();
            Main.readReflective(clsBeh, val, "category");
            element.put("category", Main.findName(Main.readReflective(clsBeh, val, "category"), CREATIVE_MODE_PATH));
            element.put("rarity", ((Enum<?>) Main.readReflective(clsBeh, val, "rarity")).toString());
            element.put("maxStackSize", (int) Main.readReflective(clsBeh, val, "maxStackSize"));
            element.put("maxDamage", (int) Main.readReflective(clsBeh, val, "maxDamage"));
            element.put("isFireResistant", (boolean) Main.readReflective(clsBeh, val, "isFireResistant"));
            element.put("craftingRemainingItem", Main.readAndGetID(clsBeh, val, "craftingRemainingItem", REGISTRY_NAME));
            
            result.put("element", element);
            Main.writePart(OUTPUT_PATH, name, result);
        }
    }
}
