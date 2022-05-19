package com.parallelcraft.datapack.types;

import com.parallelcraft.datapack.Main;
import com.parallelcraft.datapack.reflection.MRes;
import com.parallelcraft.datapack.reflection.ReflectionHelper;
import com.parallelcraft.datapack.reflection.WrappedField;
import java.lang.reflect.Field;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author extremeCrazyCoder
 */
public class BlockSoundTypes {
    private static final String OUTPUT_PATH = "world/blockSoundTypes";
    
    private static final String BLOCK_SOUND_PATH = "net.minecraft.world.level.block.SoundType";
    
    public static void generateDatapackPart() throws Exception {
        System.out.println("Generating BLOCK_SOUND_TYPES part");
        
        int id = 0;
        JSONArray resultAll = new JSONArray();
        List<WrappedField> entries = ReflectionHelper.c(BLOCK_SOUND_PATH).getPublicFinalStaticFields();
        for(WrappedField entry : entries) {
            JSONObject result = new JSONObject();
            result.put("name", entry.getName());
            result.put("id", id++);
            
            MRes val = entry.getValue();
            JSONObject element = new JSONObject();
            
            element.put("volume", val.f("volume").aF());
            element.put("pitch", val.f("pitch").aF());
            element.put("breakSound", ReflectionHelper.getRegistryID(val.f("breakSound"), Sounds.REGISTRY_NAME));
            element.put("stepSound", ReflectionHelper.getRegistryID(val.f("stepSound"), Sounds.REGISTRY_NAME));
            element.put("placeSound", ReflectionHelper.getRegistryID(val.f("placeSound"), Sounds.REGISTRY_NAME));
            element.put("hitSound", ReflectionHelper.getRegistryID(val.f("hitSound"), Sounds.REGISTRY_NAME));
            element.put("fallSound", ReflectionHelper.getRegistryID(val.f("fallSound"), Sounds.REGISTRY_NAME));
            
            result.put("element", element);
            resultAll.put(result);
        }
        
        resultAll = Main.generateIDs(resultAll);
        Main.writePart(OUTPUT_PATH, resultAll);
    }
}
