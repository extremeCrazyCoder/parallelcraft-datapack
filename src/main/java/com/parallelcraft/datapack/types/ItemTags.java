package com.parallelcraft.datapack.types;

import com.parallelcraft.datapack.Main;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author extremeCrazyCoder
 */
public class ItemTags {
    public static final String OUTPUT_PATH = "tags/item";
    
    public static final String ITEM_PATH = "net.minecraft.tags.ItemTags";
    public static final String ITEM_DATA_PATH = "net.minecraft.tags.TagCollection";
    
    public static final String REGISTRY_NAME = "ITEM";
    private static int tagID = 0;

    public static void generateDatapackPart(Class registryClass) throws Exception {
        System.out.println("Generating ITEM_TAGS part");
        Method m = Main.fetchClass(ITEM_PATH).getMethod("getAllTags");
        Object tagCollection = m.invoke(null);
        Object obj = Main.invokeUnknownReflective(tagCollection, "getAllTags");
        
        Class clsBeh = Main.fetchClass(ITEM_DATA_PATH);
        JSONArray resultAll = new JSONArray();

        Set<Map.Entry<?, ?>> entries = (Set<Map.Entry<?, ?>>) obj.getClass().getMethod("entrySet").invoke(obj);
        for(Map.Entry<?, ?> entry : entries) {
            String name = (String) (Main.invokeUnknownReflective(entry.getKey(), "getPath"));
            
            JSONObject result = new JSONObject();
            result.put("name", name);
            
            Object val = entry.getValue();
            result.put("id", tagID++);
            
            JSONObject element = new JSONObject();
            JSONArray contents = new JSONArray();
            for(Object item: (List<?>) Main.invokeUnknownReflective(entry.getValue(), "getValues")) {
                contents.put(Main.getRegistryID(item, Items.REGISTRY_NAME));
            }
            
            element.put("contents", contents);
            result.put("element", element);
            resultAll.put(result);
        }
        
        Main.writePart(OUTPUT_PATH, resultAll);
    }
}
