package com.parallelcraft.datapack.types;

import com.parallelcraft.datapack.Main;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

/**
 * @author extremeCrazyCoder
 */
public class Blocks {
    public static final String OUTPUT_PATH = "world/blocks";
    
    public static final String BLOCK_SETTINGS_PATH = "net.minecraft.world.level.block.state.BlockBehaviour";
    public static final String SOUND_TYPE_PATH = "net.minecraft.world.level.block.SoundType";
    public static final String MATERIAL_PATH = "net.minecraft.world.level.material.Material";

    public static final String REGISTRY_NAME = "BLOCK";
    
    public static void generateDatapackPart(Class registryClass) throws Exception {
        Field f = registryClass.getField(REGISTRY_NAME);
        Object obj = f.get(null);
        
        Class clsBeh = Main.fetchClass(BLOCK_SETTINGS_PATH);
        JSONObject resultAll = new JSONObject();

        Set<Map.Entry<?, ?>> entries = (Set<Map.Entry<?, ?>>) obj.getClass().getMethod("entrySet").invoke(obj);
        for(Map.Entry<?, ?> entry : entries) {
            Object loc = entry.getKey().getClass().getMethod("location").invoke(entry.getKey());
            String name = (String) (loc.getClass().getMethod("getPath").invoke(loc));
            
            JSONObject result = new JSONObject();
            result.put("name", name);
            
            Object val = entry.getValue();
            Method idMethod = obj.getClass().getMethod("getId", Object.class);
            result.put("id", idMethod.invoke(obj, val));
            
            JSONObject element = new JSONObject();
            element.put("asItem", Main.getRegistryID(Main.invokeReflective(clsBeh, val, "asItem"), Items.REGISTRY_NAME));
            
            element.put("jumpFactor", (float) Main.readReflective(clsBeh, val, "jumpFactor"));
            element.put("speedFactor", (float) Main.readReflective(clsBeh, val, "speedFactor"));
            element.put("friction", (float) Main.readReflective(clsBeh, val, "friction"));
            element.put("explosionResistance", (float) Main.readReflective(clsBeh, val, "explosionResistance"));
            element.put("destroyTime", (float) Main.invokeReflective(clsBeh, val, "defaultDestroyTime"));
            element.put("hasCollision", (boolean) Main.readReflective(clsBeh, val, "hasCollision"));
            Object props = Main.readReflective(clsBeh, val, "properties");
            element.put("isAir", (boolean) Main.readReflective(props.getClass(), props, "isAir"));
            element.put("soundType", Main.findName(Main.readReflective(clsBeh, val, "soundType"), SOUND_TYPE_PATH));
            element.put("material", Main.findName(Main.readReflective(clsBeh, val, "material"), MATERIAL_PATH));
            
            result.put("element", element);
            resultAll.put(name, result);
        }
        
        Main.writePart(OUTPUT_PATH, resultAll);
    }
}
