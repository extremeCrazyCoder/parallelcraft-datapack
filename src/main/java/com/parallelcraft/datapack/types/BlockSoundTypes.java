package com.parallelcraft.datapack.types;

import com.parallelcraft.datapack.Main;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author extremeCrazyCoder
 */
public class BlockSoundTypes {
    public static final String OUTPUT_PATH = "world/blockSoundTypes";
    
    public static final String MATERIAL_PATH = "net.minecraft.world.level.block.SoundType";
    
    public static void generateDatapackPart(Class registryClass) throws Exception {
        System.out.println("Generating BLOCK_SOUND_TYPES part");
        
        Class clsBeh = Main.fetchClass(MATERIAL_PATH);
        JSONArray resultAll = new JSONArray();

        Field[] entries = clsBeh.getFields();
        int id = 0;
        for(Field entry : entries) {
            //skip non static non final fields
            if((entry.getModifiers() & Modifier.FINAL) == 0) continue;
            if((entry.getModifiers() & Modifier.STATIC) == 0) continue;
            
            JSONObject result = new JSONObject();
            result.put("name", entry.getName());
            result.put("id", id++);
            
            Object val = entry.get(null);
            JSONObject element = new JSONObject();
            
            element.put("volume", (float) Main.readReflective(clsBeh, val, "volume"));
            element.put("pitch", (float) Main.readReflective(clsBeh, val, "pitch"));
            element.put("breakSound", Main.getRegistryID(Main.readReflective(clsBeh, val, "breakSound"), Sounds.REGISTRY_NAME));
            element.put("stepSound", Main.getRegistryID(Main.readReflective(clsBeh, val, "stepSound"), Sounds.REGISTRY_NAME));
            element.put("placeSound", Main.getRegistryID(Main.readReflective(clsBeh, val, "placeSound"), Sounds.REGISTRY_NAME));
            element.put("hitSound", Main.getRegistryID(Main.readReflective(clsBeh, val, "hitSound"), Sounds.REGISTRY_NAME));
            element.put("fallSound", Main.getRegistryID(Main.readReflective(clsBeh, val, "fallSound"), Sounds.REGISTRY_NAME));
            
            result.put("element", element);
            resultAll.put(result);
        }
        
        Main.writePart(OUTPUT_PATH, resultAll);
    }
}
