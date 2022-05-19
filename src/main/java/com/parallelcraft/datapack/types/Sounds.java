package com.parallelcraft.datapack.types;

import com.parallelcraft.datapack.Main;
import com.parallelcraft.datapack.reflection.MRes;
import com.parallelcraft.datapack.reflection.ReflectionHelper;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author extremeCrazyCoder
 */
public class Sounds {
    private static final String OUTPUT_PATH = "world/sounds";
    
    public static final String REGISTRY_NAME = "SOUND_EVENT_REGISTRY";
    public static final String SOUND_TYPE_PATH = "net.minecraft.world.level.block.SoundType";

    public static void generateDatapackPart() throws Exception {
        System.out.println("Generating SOUNDS part");
        
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
            result.put("element", element);
            resultAll.put(result);
        }
        
        Main.writePart(OUTPUT_PATH, resultAll);
    }
}
