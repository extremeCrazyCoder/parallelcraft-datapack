package com.parallelcraft.datapack.types;

import com.parallelcraft.datapack.Main;
import com.parallelcraft.datapack.reflection.MRes;
import com.parallelcraft.datapack.reflection.ReflectionHelper;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author extremeCrazyCoder
 */
public class Blocks {
    private static final String OUTPUT_PATH = "world/blocks";
    
    public static final String REGISTRY_NAME = "BLOCK_REGISTRY";
    
    public static void generateDatapackPart() throws Exception {
        System.out.println("Generating BLOCK part");
        
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
            element.put("asItem", ReflectionHelper.getRegistryID(val.i("asItem"), Items.REGISTRY_NAME));
            
            element.put("jumpFactor", val.f("jumpFactor").aF());
            element.put("speedFactor", val.f("speedFactor").aF());
            element.put("friction", val.f("friction").aF());
            element.put("explosionResistance", val.f("explosionResistance").aF());
            element.put("destroyTime", val.i("defaultDestroyTime").aF());
            element.put("hasCollision", val.f("hasCollision").aB());
            MRes props = val.f("properties");
            element.put("isAir", props.f("isAir").aB());
            element.put("soundType", ReflectionHelper.c(Sounds.SOUND_TYPE_PATH).findName(val.f("soundType")));
            element.put("material", ReflectionHelper.c(BlockMaterials.MATERIAL_PATH).findName(val.f("material")));
            
            result.put("element", element);
            resultAll.put(result);
        }
        
        Main.writePart(OUTPUT_PATH, resultAll);
    }
}
