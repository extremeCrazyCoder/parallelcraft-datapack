package com.parallelcraft.datapack.types;

import com.parallelcraft.datapack.Main;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

/**
 * @author extremeCrazyCoder
 */
public class ParticleTypes {
    public static final String OUTPUT_PATH = "world/particles";
    
    public static final String REGISTRY_NAME = "PARTICLE_TYPE";

    public static void generateDatapackPart(Class registryClass) throws Exception {
        Field f = registryClass.getField(REGISTRY_NAME);
        Object obj = f.get(null);
        
        //TODO save all inner variables...

        Set<Map.Entry<?, ?>> entries = (Set<Map.Entry<?, ?>>) obj.getClass().getMethod("entrySet").invoke(obj);
        for(Map.Entry<?, ?> entry : entries) {
            Object loc = entry.getKey().getClass().getMethod("location").invoke(entry.getKey());
            String name = (String) (loc.getClass().getMethod("getPath").invoke(loc));
            
            JSONObject result = new JSONObject();
            result.put("name", name);
            
            Object val = entry.getValue();
            result.put("id", Main.getRegistryID(val, REGISTRY_NAME));
            
            
            JSONObject element = new JSONObject();
            result.put("element", element);
            
            Main.writePart(OUTPUT_PATH, name, result);
        }
    }
}
