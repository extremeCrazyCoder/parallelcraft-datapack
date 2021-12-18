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
public class Effects {
    public static final String OUTPUT_PATH = "world/effects";
    
    public static final String MOB_EFFECT_PATH = "net.minecraft.world.effect.MobEffect";

    public static final String REGISTRY_NAME = "MOB_EFFECT";
    
    public static void generateDatapackPart(Class registryClass) throws Exception {
        System.out.println("Generating EFFECTS part");
        Field f = registryClass.getField(REGISTRY_NAME);
        Object obj = f.get(null);
        
        Class clsBeh = Main.fetchClass(MOB_EFFECT_PATH);
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
            element.put("instantenous", (boolean) Main.invokeReflective(clsBeh, val, "isInstantenous"));
            
            result.put("element", element);
            resultAll.put(result);
        }
        
        Main.writePart(OUTPUT_PATH, resultAll);
    }
}
